package com.drugs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.io.IOException;
import java.util.*;

/** Timed, persistent ingredient crops. All mutations run on the server thread. */
public final class DrugGrowing implements Listener {
    public record Crop(String id, String name, Material ingredient, Material starter, int seconds) {}
    private static final Map<String, Crop> crops = new LinkedHashMap<>();
    private static final Map<String, String> requirements = Map.ofEntries(
        Map.entry("heroin", "poppy"), Map.entry("fent", "poppy"),
        Map.entry("shrooms", "mushroom"), Map.entry("meth", "ephedra"),
        Map.entry("cocaine", "coca"), Map.entry("molly", "sassafras"),
        Map.entry("lsd", "ergot"), Map.entry("salvia", "salvia"),
        Map.entry("glue", "resin"), Map.entry("joint", "cannabis"),
        Map.entry("blunt", "cannabis"), Map.entry("cart", "cannabis"),
        Map.entry("edible", "cannabis"), Map.entry("cbd", "cannabis"));
    private record Plant(String crop, int elapsed) {}
    private final Map<String, Plant> plants = new HashMap<>();
    private final DrugsV2 plugin;
    private final File file;

    public DrugGrowing(DrugsV2 plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "ingredient_plants.yml");
        if (!new File(plugin.getDataFolder(), "growing.yml").exists()) plugin.saveResource("growing.yml", false);
        reloadConfiguration();
        var saved = YamlConfiguration.loadConfiguration(file);
        for (String key : saved.getKeys(false)) {
            String crop = saved.getString(key + ".crop");
            if (crops.containsKey(crop)) plants.put(key, new Plant(crop, Math.max(0, saved.getInt(key + ".elapsed"))));
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::save, 1200L, 1200L);
    }

    public void reloadConfiguration() {
        var config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "growing.yml"));
        crops.clear();
        add("poppy", "Poppy Pods", Material.POPPY, 600, config);
        add("mushroom", "Cultivated Mushrooms", Material.RED_MUSHROOM, 480, config);
        add("ephedra", "Ephedra Leaves", Material.QUARTZ, 900, config);
        add("coca", "Coca Leaves", Material.VINE, 900, config);
        add("sassafras", "Sassafras Leaves", Material.MAGENTA_DYE, 900, config);
        add("ergot", "Ergot Culture", Material.SPORE_BLOSSOM, 1200, config);
        add("salvia", "Salvia Leaves", Material.FERN, 600, config);
        add("resin", "Harvested Resin", Material.BONE_MEAL, 600, config);
        registerSeeds();
    }

    private static void add(String id, String name, Material ingredient, int seconds, YamlConfiguration config) {
        crops.put(id, new Crop(id, name, ingredient, ingredient, Math.max(8, config.getInt(id + ".growth-seconds", seconds))));
    }

    static Crop crop(String id) { return crops.get(id); }

    public static Set<String> cropIds() { return Collections.unmodifiableSet(crops.keySet()); }
    public static String requirement(String drug) { return requirements.get(drug.toLowerCase(Locale.ROOT)); }
    public static Material material(String crop) { return "cannabis".equals(crop) ? Material.FERN : crops.get(crop).ingredient(); }
    public static boolean isHarvest(ItemStack item, String crop) {
        return item != null && item.getType() == material(crop) && item.hasItemMeta()
            && ("harvest_" + crop).equals(DrugItemMetadata.getItemType(item.getItemMeta()));
    }
    public static ItemStack harvest(String crop, String strain) {
        ItemStack item = new ItemStack(material(crop));
        ItemMeta meta = item.getItemMeta();
        DrugItemMetadata.setItemType(meta, "harvest_" + crop);
        meta.setDisplayName("§a" + ("cannabis".equals(crop) ? "Cannabis Buds" : crops.get(crop).name()));
        meta.setLore(List.of("§7Harvested ingredient — use in drug recipes."));
        if (strain != null) {
            DrugItemMetadata.setStrainId(meta, strain);
            DrugItemMetadata.applyStrainLore(meta, StrainConfigLoader.getStrain(strain));
        }
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack seed(String crop) {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        DrugItemMetadata.setItemType(meta, "seed_" + crop);
        meta.setDisplayName("§e" + crops.get(crop).name() + " Seeds");
        meta.setLore(List.of("§7Right click hydrated farmland to plant.",
            "§7Growth: " + crops.get(crop).seconds() + " seconds in loaded chunks.",
            "§7Needs water and light; bone meal has no effect."));
        item.setItemMeta(meta);
        return item;
    }
    private void registerSeeds() {
        for (Crop crop : crops.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "seed_" + crop.id());
            Bukkit.removeRecipe(key);
            var recipe = new ShapelessRecipe(key, seed(crop.id()));
            recipe.addIngredient(Material.WHEAT_SEEDS);
            recipe.addIngredient(crop.starter());
            Bukkit.addRecipe(recipe);
        }
    }
    private static String key(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
    /** Harvest only tracked, mature ingredient crops; vanilla wheat is never touched. */
    public List<ItemStack> harvestMature(Block block) {
        Plant plant = plants.get(key(block));
        if (plant == null || block.getType() != Material.WHEAT
                || !crops.containsKey(plant.crop())
                || plant.elapsed() < crops.get(plant.crop()).seconds()) return List.of();
        plants.remove(key(block));
        block.setType(Material.AIR, false);
        save();
        return List.of(seed(plant.crop()), harvest(plant.crop(), null).asQuantity(2));
    }

    public boolean plantSeed(Block block, ItemStack seed) {
        String type = seed.hasItemMeta() ? DrugItemMetadata.getItemType(seed.getItemMeta()) : null;
        if (type == null || !type.startsWith("seed_") || !crops.containsKey(type.substring(5))
                || !block.isEmpty()
                || !(block.getRelative(0, -1, 0).getBlockData() instanceof org.bukkit.block.data.type.Farmland soil)
                || soil.getMoisture() == 0) return false;
        block.setType(Material.WHEAT, false);
        plants.put(key(block), new Plant(type.substring(5), 0));
        save();
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void plant(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK || event.getItem() == null) return;
        String type = DrugItemMetadata.getItemType(event.getItem().getItemMeta());
        if (type == null || !type.startsWith("seed_") || !crops.containsKey(type.substring(5))) return;
        event.setCancelled(true);
        Block soil = event.getClickedBlock();
        Block block = soil.getRelative(0, 1, 0);
        if (event.getBlockFace() != org.bukkit.block.BlockFace.UP || soil.getType() != Material.FARMLAND || !block.getType().isAir()) return;
        if (((org.bukkit.block.data.type.Farmland) soil.getBlockData()).getMoisture() == 0) {
            event.getPlayer().sendMessage("§eWater the farmland before planting.");
            return;
        }
        // Respect protection plugins that guard block placement rather than interaction.
        var replaced = block.getState();
        block.setType(Material.WHEAT, false);
        var place = new BlockPlaceEvent(block, replaced, soil, event.getItem(), event.getPlayer(), true, event.getHand());
        Bukkit.getPluginManager().callEvent(place);
        if (place.isCancelled() || !place.canBuild()) { replaced.update(true, false); return; }
        plants.put(key(block), new Plant(type.substring(5), 0));
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getItem().setAmount(event.getItem().getAmount() - 1);
        save();
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void harvest(BlockBreakEvent event) {
        Plant plant = plants.remove(key(event.getBlock()));
        if (plant == null) return;
        event.setDropItems(false);
        event.setExpToDrop(0);
        if (event.getBlock().getType() == Material.WHEAT && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Block block = event.getBlock();
            block.getWorld().dropItemNaturally(block.getLocation(), seed(plant.crop()));
            if (plant.elapsed() >= crops.get(plant.crop()).seconds())
                block.getWorld().dropItemNaturally(block.getLocation(), harvest(plant.crop(), null).asQuantity(2));
            else event.getPlayer().sendMessage("§eThis crop was not mature. Only its seed was recovered.");
        }
        save();
    }
    @EventHandler(ignoreCancelled = true)
    public void naturalGrowth(BlockGrowEvent event) {
        if (plants.containsKey(key(event.getBlock()))) event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    public void fertilize(BlockFertilizeEvent event) {
        if (plants.containsKey(key(event.getBlock()))) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void replaced(BlockPlaceEvent event) {
        plants.remove(key(event.getBlockPlaced()));
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void exploded(org.bukkit.event.entity.EntityExplodeEvent event) {
        for (Block block : event.blockList()) plants.remove(key(block));
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void exploded(BlockExplodeEvent event) {
        for (Block block : event.blockList()) plants.remove(key(block));
    }
    @EventHandler(ignoreCancelled = true)
    public void piston(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> plants.containsKey(key(block)))) event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    public void piston(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> plants.containsKey(key(block)))) event.setCancelled(true);
    }
    private void tick() {
        var iterator = plants.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            String[] parts = entry.getKey().split(":");
            World world = Bukkit.getWorld(UUID.fromString(parts[0]));
            int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]), z = Integer.parseInt(parts[3]);
            if (world == null || !world.isChunkLoaded(x >> 4, z >> 4)) continue;
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != Material.WHEAT) { iterator.remove(); continue; }
            Block soil = block.getRelative(0, -1, 0);
            if (!(soil.getBlockData() instanceof org.bukkit.block.data.type.Farmland farmland) || farmland.getMoisture() == 0 || block.getLightLevel() < 9) continue;
            Plant plant = entry.getValue();
            int seconds = crops.get(plant.crop()).seconds();
            int elapsed = Math.min(seconds, plant.elapsed() + 1);
            entry.setValue(new Plant(plant.crop(), elapsed));
            Ageable age = (Ageable) block.getBlockData();
            int stage = elapsed * age.getMaximumAge() / seconds;
            if (stage != age.getAge()) { age.setAge(stage); block.setBlockData(age, false); }
        }
    }
    public void save() {
        var yaml = new YamlConfiguration();
        plants.forEach((key, plant) -> { yaml.set(key + ".crop", plant.crop()); yaml.set(key + ".elapsed", plant.elapsed()); });
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("Cannot save ingredient crops: " + e.getMessage()); }
    }
}
