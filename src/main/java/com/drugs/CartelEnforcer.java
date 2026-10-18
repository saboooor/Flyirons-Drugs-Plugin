package com.drugs;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
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
                if (villager.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) villager.remove();
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

    private boolean hasBypass(Player player) {
        return player.isPermissionSet("drugs.cartel.enforcer.bypass")
                && player.hasPermission("drugs.cartel.enforcer.bypass");
    }

    private boolean eligible(Player player) {
        return player.isOnline() && !player.isDead() && !player.hasMetadata("NPC")
                && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
                && player.hasPermission("drugs.cartel.sell")
                && !hasBypass(player);
    }

    private boolean isEnforcer(Entity entity) {
        return dealer.npcs() != null && dealer.npcs().find(entity, "enforcer") != null;
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
        String error = demand == null ? "No registered drugs have a positive, finite base price in cartel.yml."
                : startEncounter(target, demand.drug(), demand.amount(), true);
        if (error != null) sender.sendMessage(ChatColor.RED + "Could not start encounter: " + error);
        else sender.sendMessage(ChatColor.GREEN + "Cartel enforcer sent to " + target.getName() + ".");
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
        return startEncounter(player, drug, amount, manual) == null;
    }

    /** Returns a specific failure message, or null after starting the encounter. */
    private String startEncounter(Player player, String drug, int amount, boolean manual) {
        long now = clock.getAsLong();
        if (!player.isOnline() || player.isDead()) return "The target must be online and alive.";
        if (player.hasMetadata("NPC")) return "The target is an NPC, not a real player.";
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return "The target must be in Survival or Adventure mode.";
        if (!player.hasPermission("drugs.cartel.sell")) return "The target lacks drugs.cartel.sell permission.";
        if (hasBypass(player))
            return "The target has drugs.cartel.enforcer.bypass. Remove that permission to allow encounters.";
        if (encounters.containsKey(player.getUniqueId()))
            return "The target already has an active encounter. Finish it or use /cartel reload to call it off.";
        if (drug == null || !DrugRegistry.isDrugRegistered(drug)) return "Unknown drug ID: " + drug + ". Use /drugs list.";
        if (amount < 1 || amount > 2304) return "Amount must be between 1 and 2304.";
        if (encounters.size() >= seconds("max-active", 8, 1, 100)) return "The server's enforcer.max-active limit has been reached.";
        if (!manual && cooldowns.getOrDefault(player.getUniqueId(), 0L) > now) return "The target is on an encounter cooldown.";
        double price = config().getDouble("prices." + drug, 0);
        if (!Double.isFinite(price) || price <= 0)
            return "prices." + drug + " must be positive and finite in cartel.yml. Run /cartel reload after editing.";
        if (dealer.payments() == null) return "Vault has no enabled economy provider. Check Vault and EssentialsX startup logs.";
        CartelNpcs npcs = dealer.npcs();
        if (npcs == null) return "Cartel NPC support is not initialized. Check the startup log.";
        String unavailable = npcs.unavailableReason();
        if (unavailable != null) return unavailable;
        Location location = findSpawn(player);
        if (location == null) return "No safe nearby ground found in loaded chunks. Try an open, flat area.";
        CartelNpcs.Character npc = npcs.create(location, "enforcer");
        if (npc == null) return "Citizens could not spawn the player NPC. Check the server console for errors or spawn protection.";
        if (!npc.valid() || !npc.approach(player)) {
            npc.destroy();
            return "The player NPC did not become ready for navigation. Check Citizens compatibility and the server console.";
        }
        Encounter encounter = new Encounter(npc, drug, amount, now);
        encounters.put(player.getUniqueId(), encounter);
        say(player, encounter, "arrival", "&8Someone from the cartel is heading your way.", 0);
        return null;
    }

    private Location findSpawn(Player player) {
        int minRadius = seconds("spawn-min-distance", 8, 4, 24);
        int maxRadius = Math.max(minRadius, seconds("spawn-max-distance", 12, 4, 32));
        return findSafeLocation(player.getLocation(), minRadius, maxRadius);
    }

    static Location findSafeLocation(Location origin, double minRadius, double maxRadius) {
        World world = origin.getWorld();
        if (world == null) return null;
        ThreadLocalRandom random = ThreadLocalRandom.current();
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
                if (world.getWorldBorder().isInside(result)) {
                    org.bukkit.util.Vector lookDir = origin.toVector().subtract(result.toVector()).setY(0);
                    if (lookDir.lengthSquared() > 0.01) result.setDirection(lookDir);
                    return result;
                }
            }
        }
        return null;
    }

    static boolean safeBlock(Block block) {
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
            if (player == null || !eligible(player) || !encounter.npc.valid()) {
                finish(id);
                continue;
            }
            if (player.getWorld() != encounter.npc.entity().getWorld()
                    || player.getLocation().distanceSquared(encounter.npc.entity().getLocation()) > 4096) {
                encounter.npc.teleportCloser(player);
                continue;
            }
            if (!economyAvailable) {
                player.sendMessage(ChatColor.GRAY + "The cartel can't pay right now. The demand has been called off.");
                finish(id);
                continue;
            }
            double distance = player.getLocation().distanceSquared(encounter.npc.entity().getLocation());
            if (encounter.deadline == 0) {
                if (distance <= 12.25 && encounter.npc.entity().hasLineOfSight(player)) beginDemand(player, encounter, now);
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
                            "&cThe cartel doesn't like waiting. Throw {remaining} more {drug} near me. You have {seconds}s left.",
                            remainingSeconds(encounter, now));
                    encounter.nextWarning = now + seconds("warning-interval-seconds", 15, 5, 300) * 1000L;
                }
            }
            if (distance > 6.25) encounter.npc.approach(player);
            else encounter.npc.stop();
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
                        + "Throw the drugs near me. Smaller deliveries count, and you'll be paid.", duration);
    }

    private int remainingSeconds(Encounter encounter, long now) {
        return (int) Math.max(0, (encounter.deadline - now + 999) / 1000);
    }

    private void say(Player player, Encounter encounter, String message, String fallback, int seconds) {
        String text = config().getString("enforcer.messages." + message, fallback)
                .replace("{amount}", Integer.toString(encounter.amount))
                .replace("{drug}", encounter.drug).replace("{seconds}", Integer.toString(seconds))
                .replace("{remaining}", Integer.toString(encounter.amount - encounter.delivered));
        // Update the previous default text in existing configs as well.
        text = text.replace("Right-click me with the full amount in your inventory.", "Throw the drugs near me. Smaller deliveries count.");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    private void threaten(Player player, Encounter encounter) {
        say(player, encounter, "failure", "&4Cartel: &cYou refused our deal. We'll remember this. Watch your back.", 0);
        // Dialogue only by default. Optional damage is capped and cannot deliberately kill the player.
        double damage = config().getDouble("enforcer.failure-damage", 0);
        if (Double.isFinite(damage) && damage > 0 && player.getWorld() == encounter.npc.entity().getWorld()
                && player.getLocation().distanceSquared(encounter.npc.entity().getLocation()) <= 36) {
            double nonlethal = Math.min(Math.min(20, damage), Math.max(0, player.getHealth() - 1));
            if (nonlethal > 0) player.damage(nonlethal, encounter.npc.entity());
        }
    }

    private void finish(UUID id) {
        Encounter removed = encounters.remove(id);
        if (removed == null) return;
        removed.npc.destroy();
        cooldowns.put(id, clock.getAsLong() + seconds("cooldown-seconds", 1800, 10, 86400) * 1000L);
    }

    void interact(Player player, Entity entity) {
        if (!isEnforcer(entity)) return;
        Encounter encounter = encounters.get(player.getUniqueId());
        if (encounter == null || !encounter.npc.valid() || !encounter.npc.entity().getUniqueId().equals(entity.getUniqueId())) {
            player.sendMessage(ChatColor.GRAY + "The enforcer is here for someone else.");
            return;
        }
        if (!eligible(player) || player.getWorld() != encounter.npc.entity().getWorld()
                || player.getLocation().distanceSquared(encounter.npc.entity().getLocation()) > 36
                || !encounter.npc.entity().hasLineOfSight(player)) return;
        long now = clock.getAsLong();
        if (encounter.deadline == 0) {
            beginDemand(player, encounter, now);
            EnforcerGuide.open(plugin, player, encounter.drug);
            return; // First interaction explains the deal before any items are sold.
        }
        if (now >= encounter.deadline) {
            threaten(player, encounter);
            finish(player.getUniqueId());
            return;
        }
        EnforcerGuide.open(plugin, player, encounter.drug);
    }

    boolean acceptDrop(Player player, Item item) {
        Encounter encounter = encounters.get(player.getUniqueId());
        if (encounter == null || encounter.deadline == 0 || !eligible(player) || !item.isValid()
                || !encounter.npc.valid() || clock.getAsLong() >= encounter.deadline) return false;
        LivingEntity npc = encounter.npc.entity();
        if (item.getWorld() != npc.getWorld() || npc.getLocation().distanceSquared(item.getLocation()) > 4
                || !npc.hasLineOfSight(item)) return false;
        if (CartelDealer.price(item.getItemStack(), config()) == 0
                || !encounter.drug.equals(DrugItemMetadata.getDrugId(item.getItemStack().getItemMeta()))) return false;
        CartelDealer.Payments payments = dealer.payments();
        if (payments == null) {
            player.sendMessage(ChatColor.GRAY + "The cartel can't pay right now. The demand has been called off.");
            finish(player.getUniqueId());
            return true;
        }
        int sold = dealer.sellDropped(player, item, encounter.amount - encounter.delivered, payments);
        encounter.delivered += sold;
        if (encounter.delivered >= encounter.amount) {
            say(player, encounter, "success", "&2Cartel: &aGood. We can do business. You're clear.", 0);
            finish(player.getUniqueId());
        } else if (sold > 0) {
            player.sendMessage(ChatColor.GREEN + "Delivered " + encounter.delivered + "/" + encounter.amount + " "
                    + encounter.drug + ". Throw " + (encounter.amount - encounter.delivered) + " more near me.");
        }
        return true;
    }

    @EventHandler public void onDamage(EntityDamageEvent event) {
        if (isEnforcer(event.getEntity())) event.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { finish(event.getPlayer().getUniqueId()); }
    @EventHandler public void onDeath(PlayerDeathEvent event) { finish(event.getEntity().getUniqueId()); }
    @EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { followPlayer(event.getPlayer()); }
    @EventHandler(ignoreCancelled = true) public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null) followPlayer(event.getPlayer());
    }

    private void followPlayer(Player player) {
        if (player == null) return;
        Encounter encounter = encounters.get(player.getUniqueId());
        if (encounter == null || !encounter.npc.valid()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!encounters.containsKey(player.getUniqueId()) || !encounter.npc.valid() || !player.isOnline()) return;
            encounter.npc.teleportCloser(player);
        });
    }

    @EventHandler public void onChunkUnload(ChunkUnloadEvent event) {
        for (UUID id : new ArrayList<>(encounters.keySet())) {
            Encounter encounter = encounters.get(id);
            if (encounter == null) continue;
            CartelNpcs.Character npc = encounter.npc;
            if (!npc.valid()) { finish(id); continue; }
            Location location = npc.entity().getLocation();
            if (location.getWorld() == event.getWorld() && (location.getBlockX() >> 4) == event.getChunk().getX()
                    && (location.getBlockZ() >> 4) == event.getChunk().getZ()) {
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline()) {
                    if (!npc.teleportCloser(player)) finish(id);
                } else {
                    finish(id);
                }
            }
        }
    }
    @EventHandler public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (isEnforcer(entity) && encounters.values().stream()
                    .noneMatch(encounter -> encounter.npc.valid() && encounter.npc.entity().getUniqueId().equals(entity.getUniqueId()))) {
                dealer.npcs().find(entity, "enforcer").destroy();
            }
        }
    }

    private static final class Encounter {
        final CartelNpcs.Character npc;
        final String drug;
        final int amount;
        final long spawnedAt;
        int delivered;
        long deadline;
        long nextWarning;
        Encounter(CartelNpcs.Character npc, String drug, int amount, long spawnedAt) {
            this.npc = npc;
            this.drug = drug;
            this.amount = amount;
            this.spawnedAt = spawnedAt;
        }
    }
}
