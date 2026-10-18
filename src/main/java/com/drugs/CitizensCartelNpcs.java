package com.drugs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Citizens owns persistent buyers; a separate memory-only registry owns temporary enforcers. */
final class CitizensCartelNpcs implements CartelNpcs, Listener {
    static final String ROLE = "flyirons-cartel-role";
    private static final String TEMPORARY_REGISTRY = "flyirons-cartel-enforcers";
    private final DrugsV2 plugin;
    private final CartelDealer dealer;
    private final NPCRegistry temporary;
    private final YamlConfiguration bundledSkin;
    private boolean closed;

    CitizensCartelNpcs(DrugsV2 plugin, CartelDealer dealer) {
        this.plugin = plugin;
        this.dealer = dealer;
        temporary = CitizensAPI.createInMemoryNPCRegistry(TEMPORARY_REGISTRY);
        bundledSkin = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(
                CitizensCartelNpcs.class.getResourceAsStream("/cartel-skin.yml")), StandardCharsets.UTF_8));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Citizens loads saved NPCs during its own startup. Also handle its enable event below.
        plugin.getServer().getScheduler().runTask(plugin, this::reload);
    }

    @Override public String unavailableReason() {
        if (closed) return "Cartel NPC support has shut down. Restart the server.";
        return plugin.getServer().getPluginManager().isPluginEnabled("Citizens") ? null
                : "Citizens is not enabled. Install a compatible build and restart the server.";
    }

    @Override public Character create(Location location, String role) {
        if (unavailableReason() != null) return null;
        NPCRegistry registry = role.equals("buyer") ? CitizensAPI.getNPCRegistry() : temporary;
        NPC npc = null;
        try {
            npc = registry.createNPC(EntityType.PLAYER, role.equals("buyer") ? buyerName() : "Cartel Enforcer");
            npc.data().setPersistent(ROLE, role);
            if (role.equals("enforcer")) {
                npc.data().setPersistent("activation-range", 64);
                npc.data().setPersistent("pathfinder-open-doors",
                        dealer.settings().getBoolean("enforcer.open-doors", true));
            }
            npc.setProtected(true);
            npc.data().setPersistent("collidable", false);
            applySkin(npc);
            if (!npc.spawn(location)) {
                plugin.getLogger().warning("Citizens rejected cartel " + role + " spawn at " + location);
                npc.destroy();
                return null;
            }
            if (npc.getEntity() instanceof LivingEntity living) living.setCanPickupItems(false);
            if (role.equals("buyer")) registry.saveToStore();
            return new Citizen(npc);
        } catch (RuntimeException exception) {
            if (npc != null) npc.destroy();
            plugin.getLogger().warning("Could not create cartel player NPC: " + exception.getMessage());
            return null;
        }
    }

    @Override public Character find(Entity entity, String role) {
        if (closed || !(entity instanceof Player)) return null;
        NPC npc = (role.equals("buyer") ? CitizensAPI.getNPCRegistry() : temporary).getNPC(entity);
        return owns(npc, role) ? new Citizen(npc) : null;
    }

    private boolean owns(NPC npc, String role) {
        return npc != null && role.equals(npc.data().get(ROLE, ""));
    }

    private String buyerName() {
        String name = dealer.settings().getString("npc.name", "Pablo").trim();
        return name.isEmpty() ? "Pablo" : name;
    }

    private void applySkin(NPC npc) {
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.clearTexture();
        skin.setFetchDefaultSkin(false);
        skin.setShouldUpdateSkins(false);
        String username = dealer.settings().getString("npc.skin-name", "").trim();
        if (!username.isEmpty()) skin.setSkinName(username);
        else skin.setSkinPersistent(bundledSkin.getString("name"), bundledSkin.getString("signature"),
                bundledSkin.getString("texture"));
    }

    @Override public void reload() {
        if (closed) return;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (owns(npc, "buyer")) {
                npc.setName(buyerName());
                applySkin(npc);
                if (npc.getEntity() instanceof LivingEntity living) living.setCanPickupItems(false);
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) migrate(villager);
        }
    }

    private void migrate(Villager villager) {
        NamespacedKey oldKey = new NamespacedKey(plugin, "cartel_dealer");
        if (villager.getPersistentDataContainer().has(oldKey, PersistentDataType.BYTE)
                && create(villager.getLocation(), "buyer") != null) villager.remove();
    }

    @Override public void shutdown() {
        if (closed) return;
        closed = true;
        temporary.deregisterAll();
        CitizensAPI.removeNamedNPCRegistry(TEMPORARY_REGISTRY);
        CitizensAPI.getNPCRegistry().saveToStore();
    }

    @EventHandler(ignoreCancelled = true) public void onRightClick(NPCRightClickEvent event) {
        if (closed || !event.getNPC().isSpawned()) return;
        Entity entity = event.getNPC().getEntity();
        if (owns(event.getNPC(), "buyer")) {
            event.setCancelled(true);
            dealer.interact(event.getClicker(), entity);
        } else if (owns(event.getNPC(), "enforcer")) {
            event.setCancelled(true);
            dealer.interactEnforcer(event.getClicker(), entity);
        }
    }

    @EventHandler(ignoreCancelled = true) public void onLeftClick(NPCLeftClickEvent event) {
        if (closed || !event.getNPC().isSpawned() || !owns(event.getNPC(), "enforcer")) return;
        event.setCancelled(true);
        dealer.interactEnforcer(event.getClicker(), event.getNPC().getEntity());
    }

    @EventHandler public void onCitizensEnable(CitizensEnableEvent event) { reload(); }
    @EventHandler public void onChunkLoad(ChunkLoadEvent event) {
        if (closed) return;
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Villager villager) migrate(villager);
        }
    }

    static boolean runningAway(Location pursuer, Location previous, Location current, boolean sprinting) {
        if (!sprinting || previous == null || !current.getWorld().equals(previous.getWorld())
                || !current.getWorld().equals(pursuer.getWorld())) return false;
        double dx = current.getX() - previous.getX();
        double dz = current.getZ() - previous.getZ();
        return dx * dx + dz * dz > 0.0001
                && dx * (current.getX() - pursuer.getX()) + dz * (current.getZ() - pursuer.getZ()) > 0;
    }

    private final class Citizen implements Character {
        private org.bukkit.scheduler.BukkitTask climbingTask;
        private Player climbingTarget;
        private boolean climbing;
        private int retryTicks;
        private Location previousTargetLocation;
        private final EnforcerWallClimber climber = new EnforcerWallClimber();
        private final NPC npc;
        Citizen(NPC npc) { this.npc = npc; }
        public LivingEntity entity() { return (LivingEntity) npc.getEntity(); }
        public boolean valid() { return npc.isSpawned() && npc.getEntity() != null && npc.getEntity().isValid(); }
        public boolean approach(Player player) {
            if (!valid()) return false;
            if (owns(npc, "enforcer") && climbingTask == null) {
                climbingTarget = player;
                previousTargetLocation = player.getLocation();
                climbingTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                    if (closed || !valid() || !climbingTarget.isOnline() || climbingTarget.isDead()) {
                        stop();
                        return;
                    }
                    Location targetLocation = climbingTarget.getLocation();
                    boolean sprinting = !climbing && runningAway(entity().getLocation(), previousTargetLocation,
                            targetLocation, climbingTarget.isSprinting());
                    previousTargetLocation = targetLocation;
                    float speed = sprinting ? 1.5F : 1.15F;
                    Navigator chase = npc.getNavigator();
                    chase.getDefaultParameters().speedModifier(speed);
                    if (chase.isNavigating()) chase.getLocalParameters().speedModifier(speed);
                    if (entity() instanceof Player body) body.setSprinting(sprinting);
                    int height = Math.max(1, Math.min(16,
                            dealer.settings().getInt("enforcer.wall-climbing.max-height", 6)));
                    int timeout = Math.max(20, Math.min(400,
                            dealer.settings().getInt("enforcer.wall-climbing.timeout-ticks", 100)));
                    boolean wasClimbing = climbing;
                    climbing = dealer.settings().getBoolean("enforcer.wall-climbing.enabled", true)
                            && climber.tick(entity(), climbingTarget, height, timeout,
                                    () -> npc.getNavigator().cancelNavigation());
                    if (!climbing && !npc.getNavigator().isNavigating()) {
                        climber.approachWhenNoPath(entity(), climbingTarget, sprinting ? 0.235 : 0.18);
                    }
                    if (!climbing && (wasClimbing || ++retryTicks >= 10)) {
                        retryTicks = 0;
                        if (wasClimbing || !npc.getNavigator().isNavigating()) {
                            npc.getNavigator().setTarget(climbingTarget, false);
                        }
                    }
                }, 1L, 1L);
            }
            if (climbing) return true;
            Navigator navigator = npc.getNavigator();
            if (navigator.isNavigating() && navigator.getEntityTarget() != null
                    && navigator.getEntityTarget().getTarget().equals(player)) return true;
            navigator.getDefaultParameters().range(64).speedModifier(1.15F).distanceMargin(2)
                    .useNewPathfinder(true).updatePathRate(10).stationaryTicks(20)
                    .stuckAction((ignoredNpc, ignoredNavigator) -> false);
            // A preliminary canNavigateTo check can reject a freshly spawned NPC.
            // Let navigation run; the encounter's approach timeout handles unreachable players.
            navigator.setTarget(player, false);
            return true;
        }
        public void stop() {
            if (climbingTask != null) climbingTask.cancel();
            climbingTask = null;
            climber.reset();
            climbing = false;
            retryTicks = 0;
            if (valid() && entity() instanceof Player body) body.setSprinting(false);
            previousTargetLocation = null;
            npc.getNavigator().cancelNavigation();
        }
        public void destroy() {
            stop();
            NPCRegistry registry = npc.getOwningRegistry();
            npc.destroy();
            registry.saveToStore();
        }
    }
}
