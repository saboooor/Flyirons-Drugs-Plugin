package com.drugs;

import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;
import java.util.random.RandomGenerator;

/** Temporary, player-specific encounters. Navigation and sales always run on the main thread. */
public final class CartelEnforcer implements Listener {
    private final DrugsV2 plugin;
    private final CartelDealer dealer;
    private final NamespacedKey key;
    private final LongSupplier clock;
    private final Map<UUID, Encounter> encounters = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private BukkitTask task;
    private long nextCheck;

    public CartelEnforcer(DrugsV2 plugin, CartelDealer dealer) {
        this(plugin, dealer, System::currentTimeMillis);
    }

    CartelEnforcer(DrugsV2 plugin, CartelDealer dealer, LongSupplier clock) {
        this.plugin = plugin;
        this.dealer = dealer;
        this.clock = clock;
        key = new NamespacedKey(plugin, "cartel_enforcer");
    }

    public void start() {
        // Recover from an interrupted previous run without keeping abandoned NPCs.
        for (World world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                if (isEnforcer(villager)) villager.remove();
            }
        }
        nextCheck = clock.getAsLong() + seconds("check-interval-seconds", 300, 10, 86400) * 1000L;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        for (UUID player : new ArrayList<>(encounters.keySet())) finish(player);
        cooldowns.clear();
    }

    void reload() {
        // Cancel existing demands rather than changing their price or rules halfway through.
        for (UUID player : new ArrayList<>(encounters.keySet())) {
            Player online = Bukkit.getPlayer(player);
            if (online != null) online.sendMessage(ChatColor.GRAY + "The cartel has called off its demand.");
            finish(player);
        }
        nextCheck = clock.getAsLong() + seconds("check-interval-seconds", 300, 10, 86400) * 1000L;
    }

    private FileConfiguration config() { return dealer.settings(); }

    private int seconds(String setting, int fallback, int min, int max) {
        return Math.max(min, Math.min(max, config().getInt("enforcer." + setting, fallback)));
    }

    private boolean eligible(Player player) {
        return player.isOnline() && !player.isDead()
                && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
                && player.hasPermission("drugs.cartel.sell")
                && !player.hasPermission("drugs.cartel.enforcer.bypass");
    }

    private boolean isEnforcer(Entity entity) {
        return entity instanceof Villager && entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 4) {
            sender.sendMessage("Usage: /cartel enforcer [player drug amount]");
            return true;
        }
        Player target = args.length == 1 ? (sender instanceof Player player ? player : null)
                : Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Choose an online player: /cartel enforcer <player> <drug> <amount>");
            return true;
        }
        Demand demand;
        try {
            demand = args.length == 1 ? randomDemand(ThreadLocalRandom.current())
                    : new Demand(args[2].toLowerCase(Locale.ROOT), Integer.parseInt(args[3]));
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Amount must be a whole number between 1 and 2304.");
            return true;
        }
        if (demand == null || !spawn(target, demand.drug(), demand.amount(), true)) {
            sender.sendMessage(ChatColor.RED + "Could not start encounter. Check the drug price, amount (1–2304), "
                    + "Vault economy, player's game mode/permissions, existing encounter, and nearby walkable ground.");
        } else sender.sendMessage(ChatColor.GREEN + "Cartel enforcer sent to " + target.getName() + ".");
        return true;
    }

    record Demand(String drug, int amount) {}

    Demand randomDemand(RandomGenerator random) {
        List<String> drugs = DrugRegistry.getRegisteredDrugNames().stream().filter(drug -> {
            double price = config().getDouble("prices." + drug, 0);
            return Double.isFinite(price) && price > 0;
        }).sorted().toList();
        if (drugs.isEmpty()) return null;
        int min = seconds("min-amount", 8, 1, 2304);
        int max = Math.max(min, seconds("max-amount", 32, 1, 2304));
        return new Demand(drugs.get(random.nextInt(drugs.size())), random.nextInt(min, max + 1));
    }

    boolean spawn(Player player, String drug, int amount, boolean manual) {
        long now = clock.getAsLong();
        if (!eligible(player) || encounters.containsKey(player.getUniqueId()) || drug == null
                || !DrugRegistry.isDrugRegistered(drug) || amount < 1 || amount > 2304
                || encounters.size() >= seconds("max-active", 8, 1, 100)
                || (!manual && cooldowns.getOrDefault(player.getUniqueId(), 0L) > now)) return false;
        double price = config().getDouble("prices." + drug, 0);
        if (!Double.isFinite(price) || price <= 0 || dealer.payments() == null) return false;
        Location location = findSpawn(player);
        if (location == null) return false;
        Villager npc = player.getWorld().spawn(location, Villager.class, villager -> {
            villager.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            villager.setCustomName(ChatColor.DARK_RED + "Cartel Enforcer");
            villager.setCustomNameVisible(true);
            villager.setAdult();
            villager.setAI(true);
            villager.setInvulnerable(true);
            villager.setSilent(true);
            villager.setCollidable(false);
            villager.setPersistent(false);
            villager.setRemoveWhenFarAway(false);
        });
        if (!npc.isValid()) { npc.remove(); return false; }
        Pathfinder.PathResult path = npc.getPathfinder().findPath(player);
        if (path == null || path.getFinalPoint() == null
                || path.getFinalPoint().distanceSquared(player.getLocation()) > 16
                || !npc.getPathfinder().moveTo(path, 1.15)) {
            npc.remove();
            return false;
        }
        Encounter encounter = new Encounter(npc, drug, amount, now);
        encounters.put(player.getUniqueId(), encounter);
        say(player, encounter, "arrival", "&8Someone from the cartel is heading your way.", 0);
        return true;
    }

    private Location findSpawn(Player player) {
        Location origin = player.getLocation();
        World world = player.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int minRadius = seconds("spawn-min-distance", 8, 4, 24);
        int maxRadius = Math.max(minRadius, seconds("spawn-max-distance", 12, 4, 32));
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = random.nextDouble(Math.PI * 2);
            double radius = random.nextDouble(minRadius, maxRadius + 1.0);
            int x = (int) Math.floor(origin.getX() + Math.cos(angle) * radius);
            int z = (int) Math.floor(origin.getZ() + Math.sin(angle) * radius);
            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
            // Search near the player's elevation so caves and buildings work too.
            for (int offset : new int[]{0, 1, -1, 2, -2, 3, -3, 4, -4}) {
                int y = origin.getBlockY() + offset;
                if (y <= world.getMinHeight() || y + 1 >= world.getMaxHeight()) continue;
                Block floor = world.getBlockAt(x, y - 1, z);
                Block feet = world.getBlockAt(x, y, z);
                Block head = world.getBlockAt(x, y + 1, z);
                if (!floor.isSolid() || !safeBlock(floor) || !feet.isPassable()
                        || !head.isPassable() || !safeBlock(feet) || !safeBlock(head)) continue;
                Location result = new Location(world, x + 0.5, y, z + 0.5);
                if (world.getWorldBorder().isInside(result)) return result;
            }
        }
        return null;
    }

    private boolean safeBlock(Block block) {
        return !block.isLiquid() && switch (block.getType()) {
            case LAVA, FIRE, SOUL_FIRE, MAGMA_BLOCK, CAMPFIRE, SOUL_CAMPFIRE,
                    CACTUS, SWEET_BERRY_BUSH, POWDER_SNOW, WITHER_ROSE,
                    NETHER_PORTAL, END_PORTAL, COBWEB -> false;
            default -> true;
        };
    }

    void tick() {
        long now = clock.getAsLong();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
        boolean economyAvailable = encounters.isEmpty() || dealer.payments() != null;
        for (UUID id : new ArrayList<>(encounters.keySet())) {
            Encounter encounter = encounters.get(id);
            Player player = Bukkit.getPlayer(id);
            if (player == null || !eligible(player) || !encounter.npc.isValid()
                    || player.getWorld() != encounter.npc.getWorld()
                    || player.getLocation().distanceSquared(encounter.npc.getLocation()) > 4096) {
                finish(id);
                continue;
            }
            if (!economyAvailable) {
                player.sendMessage(ChatColor.GRAY + "The cartel can't pay right now. The demand has been called off.");
                finish(id);
                continue;
            }
            double distance = player.getLocation().distanceSquared(encounter.npc.getLocation());
            if (encounter.deadline == 0) {
                if (distance <= 12.25 && encounter.npc.hasLineOfSight(player)) beginDemand(player, encounter, now);
                else if (now - encounter.spawnedAt >= seconds("approach-timeout-seconds", 60, 10, 300) * 1000L) {
                    finish(id);
                    continue;
                }
            }
            if (encounter.deadline > 0) {
                if (now >= encounter.deadline) {
                    threaten(player, encounter);
                    finish(id);
                    continue;
                }
                if (now >= encounter.nextWarning) {
                    say(player, encounter, "warning",
                            "&cThe cartel doesn't like waiting. Sell me {amount} {drug}. You have {seconds}s left.",
                            remainingSeconds(encounter, now));
                    encounter.nextWarning = now + seconds("warning-interval-seconds", 15, 5, 300) * 1000L;
                }
            }
            if (distance > 6.25) encounter.npc.getPathfinder().moveTo(player, 1.15);
            else encounter.npc.getPathfinder().stopPathfinding();
        }
        if (now >= nextCheck) {
            nextCheck = now + seconds("check-interval-seconds", 300, 10, 86400) * 1000L;
            if (!config().getBoolean("enforcer.enabled", true) || dealer.payments() == null) return;
            double chance = config().getDouble("enforcer.spawn-chance", 0.25);
            if (!Double.isFinite(chance)) return;
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(players);
            for (Player player : players) {
                if (encounters.size() >= seconds("max-active", 8, 1, 100)) break;
                if (ThreadLocalRandom.current().nextDouble() < Math.max(0, Math.min(1, chance))) {
                    Demand demand = randomDemand(ThreadLocalRandom.current());
                    if (demand != null) spawn(player, demand.drug(), demand.amount(), false);
                }
            }
        }
    }

    private void beginDemand(Player player, Encounter encounter, long now) {
        int duration = seconds("deadline-seconds", 90, 10, 3600);
        encounter.deadline = now + duration * 1000L;
        encounter.nextWarning = now + seconds("warning-interval-seconds", 15, 5, 300) * 1000L;
        say(player, encounter, "demand",
                "&4Cartel: &cSell me {amount} {drug} within {seconds}s, or you'll answer to the cartel. "
                        + "Right-click me with the full amount in your inventory. You'll be paid.", duration);
    }

    private int remainingSeconds(Encounter encounter, long now) {
        return (int) Math.max(0, (encounter.deadline - now + 999) / 1000);
    }

    private void say(Player player, Encounter encounter, String message, String fallback, int seconds) {
        String text = config().getString("enforcer.messages." + message, fallback)
                .replace("{amount}", Integer.toString(encounter.amount))
                .replace("{drug}", encounter.drug).replace("{seconds}", Integer.toString(seconds));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    private void threaten(Player player, Encounter encounter) {
        say(player, encounter, "failure", "&4Cartel: &cYou refused our deal. We'll remember this. Watch your back.", 0);
        // Dialogue only by default. Optional damage is capped and cannot deliberately kill the player.
        double damage = config().getDouble("enforcer.failure-damage", 0);
        if (Double.isFinite(damage) && damage > 0 && player.getWorld() == encounter.npc.getWorld()
                && player.getLocation().distanceSquared(encounter.npc.getLocation()) <= 36) {
            double nonlethal = Math.min(Math.min(20, damage), Math.max(0, player.getHealth() - 1));
            if (nonlethal > 0) player.damage(nonlethal, encounter.npc);
        }
    }

    private void finish(UUID id) {
        Encounter removed = encounters.remove(id);
        if (removed == null) return;
        removed.npc.remove();
        cooldowns.put(id, clock.getAsLong() + seconds("cooldown-seconds", 1800, 10, 86400) * 1000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!isEnforcer(event.getRightClicked())) return;
        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Encounter encounter = encounters.get(player.getUniqueId());
        if (encounter == null || !encounter.npc.getUniqueId().equals(event.getRightClicked().getUniqueId())) {
            player.sendMessage(ChatColor.GRAY + "The enforcer is here for someone else.");
            return;
        }
        if (!eligible(player) || player.getWorld() != encounter.npc.getWorld()
                || player.getLocation().distanceSquared(encounter.npc.getLocation()) > 36
                || !encounter.npc.hasLineOfSight(player)) return;
        long now = clock.getAsLong();
        if (encounter.deadline == 0) {
            beginDemand(player, encounter, now);
            return; // First interaction explains the deal before any items are sold.
        }
        if (now >= encounter.deadline) {
            threaten(player, encounter);
            finish(player.getUniqueId());
            return;
        }
        CartelDealer.Payments payments = dealer.payments();
        if (payments == null) {
            player.sendMessage(ChatColor.GRAY + "The cartel can't pay right now. The demand has been called off.");
            finish(player.getUniqueId());
        } else if (dealer.sellDemand(player, encounter.drug, encounter.amount, payments)) {
            say(player, encounter, "success", "&2Cartel: &aGood. We can do business. You're clear.", 0);
            finish(player.getUniqueId());
        }
    }

    @EventHandler public void onDamage(EntityDamageEvent event) {
        if (isEnforcer(event.getEntity())) event.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { finish(event.getPlayer().getUniqueId()); }
    @EventHandler public void onDeath(PlayerDeathEvent event) { finish(event.getEntity().getUniqueId()); }
    @EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { finish(event.getPlayer().getUniqueId()); }
    @EventHandler(ignoreCancelled = true) public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null && (event.getFrom().getWorld() != event.getTo().getWorld()
                || event.getFrom().distanceSquared(event.getTo()) > 4096)) finish(event.getPlayer().getUniqueId());
    }
    @EventHandler public void onChunkUnload(ChunkUnloadEvent event) {
        for (UUID id : new ArrayList<>(encounters.keySet())) {
            Villager npc = encounters.get(id).npc;
            Location location = npc.getLocation();
            if (location.getWorld() == event.getWorld() && (location.getBlockX() >> 4) == event.getChunk().getX()
                    && (location.getBlockZ() >> 4) == event.getChunk().getZ()) finish(id);
        }
    }
    @EventHandler public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (isEnforcer(entity) && encounters.values().stream()
                    .noneMatch(encounter -> encounter.npc.getUniqueId().equals(entity.getUniqueId()))) entity.remove();
        }
    }

    private static final class Encounter {
        final Villager npc;
        final String drug;
        final int amount;
        final long spawnedAt;
        long deadline;
        long nextWarning;
        Encounter(Villager npc, String drug, int amount, long spawnedAt) {
            this.npc = npc;
            this.drug = drug;
            this.amount = amount;
            this.spawnedAt = spawnedAt;
        }
    }
}
