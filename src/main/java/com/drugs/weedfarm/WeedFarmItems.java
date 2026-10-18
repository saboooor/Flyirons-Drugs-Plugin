package com.drugs.weedfarm;

import com.drugs.DrugItemMetadata;
import com.drugs.DrugsV2;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public final class WeedFarmItems {
    public static final String ITEM_TYPE_CONTROLLER = "farm_controller";
    public static final String ITEM_TYPE_AREA_WAND = "farm_area_wand";
    public static final String ITEM_TYPE_CONTRACT = "farm_work_contract";

    private WeedFarmItems() {}

    public static ItemStack createControllerItem() {
        ItemStack stack = new ItemStack(Material.BARREL);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Farm Controller");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Place this barrel to create", ChatColor.GRAY + "an automated drug crop farm."));
            DrugItemMetadata.setItemType(meta, ITEM_TYPE_CONTROLLER);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static NamespacedKey dataKey() {
        return new NamespacedKey(DrugsV2.getInstance(), "farm_data");
    }

    static String encodeFarm(WeedFarm farm) {
        var data = new YamlConfiguration();
        data.set("id", farm.getFarmId());
        data.set("enabled", farm.isEnabled());
        data.set("region", farm.hasRegion());
        Location origin = farm.getControllerLocation();
        data.set("min", Arrays.asList(farm.getMinX() - origin.getBlockX(), farm.getMinY() - origin.getBlockY(), farm.getMinZ() - origin.getBlockZ()));
        data.set("max", Arrays.asList(farm.getMaxX() - origin.getBlockX(), farm.getMaxY() - origin.getBlockY(), farm.getMaxZ() - origin.getBlockZ()));
        data.set("workers", farm.getAssignedVillagers().stream().map(UUID::toString).toList());
        return data.saveToString();
    }

    static WeedFarm decodeFarm(String encoded, Location location) {
        try {
            var data = new YamlConfiguration();
            data.loadFromString(encoded);
            String id = data.getString("id");
            if (id == null || !id.matches("[a-zA-Z0-9-]{1,36}")) throw new IllegalArgumentException();
            var min = data.getIntegerList("min");
            var max = data.getIntegerList("max");
            if (min.size() != 3 || max.size() != 3) throw new IllegalArgumentException();
            var farm = new WeedFarm(id, location);
            Location first = location.clone().add(min.get(0), min.get(1), min.get(2));
            Location second = location.clone().add(max.get(0), max.get(1), max.get(2));
            if (data.getBoolean("region", true) && (!farm.canApplyRegion(first, second) || first.getBlockY() > second.getBlockY()
                    || first.getBlockY() < location.getWorld().getMinHeight()
                    || second.getBlockY() >= location.getWorld().getMaxHeight())) throw new IllegalArgumentException();
            if (data.getBoolean("region", true)) farm.setRegion(location.getWorld(), first, second);
            farm.setEnabled(data.getBoolean("enabled", true));
            for (String worker : data.getStringList("workers")) {
                if (farm.getAssignedVillagers().size() >= WeedFarm.MAX_WORKERS) throw new IllegalArgumentException();
                farm.getAssignedVillagers().add(UUID.fromString(worker));
            }
            return farm;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid saved farm data", exception);
        }
    }

    public static ItemStack createControllerItem(WeedFarm farm) {
        ItemStack item = createControllerItem();
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(dataKey(), PersistentDataType.STRING, encodeFarm(farm));
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Place to restore this farm.",
                ChatColor.GRAY + "Workers: " + farm.getAssignedVillagers().size() + "/5",
                ChatColor.GRAY + (farm.isEnabled() ? "Started" : "Stopped"),
                ChatColor.DARK_GRAY + "Farm: " + farm.getFarmId()));
        item.setItemMeta(meta);
        return item;
    }

    public static WeedFarm savedFarm(ItemStack item, Location location) {
        String data = item.getItemMeta().getPersistentDataContainer().get(dataKey(), PersistentDataType.STRING);
        return data == null ? null : decodeFarm(data, location);
    }

    public static ItemStack createAreaWand(String farmId) {
        ItemStack stack = new ItemStack(Material.ARROW);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Farm Area Wand");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Left click: set pos1",
                    ChatColor.GRAY + "Right click: set pos2",
                    ChatColor.GRAY + "Max area: 16x16",
                    ChatColor.DARK_GRAY + "Farm: " + farmId));
            DrugItemMetadata.setItemType(meta, ITEM_TYPE_AREA_WAND + ":" + farmId);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static ItemStack createContract(String farmId) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Work Contract");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Hold this contract and",
                    ChatColor.GRAY + "right-click a villager to assign.",
                    ChatColor.GRAY + "Max villagers: 5",
                    ChatColor.DARK_GRAY + "Farm: " + farmId));
            DrugItemMetadata.setItemType(meta, ITEM_TYPE_CONTRACT + ":" + farmId);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static boolean isControllerItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        String type = DrugItemMetadata.getItemType(stack.getItemMeta());
        return ITEM_TYPE_CONTROLLER.equalsIgnoreCase(type);
    }

    public static String parseFarmId(ItemStack stack, String prefix) {
        if (stack == null || !stack.hasItemMeta()) return null;
        String itemType = DrugItemMetadata.getItemType(stack.getItemMeta());
        if (itemType == null || !itemType.startsWith(prefix + ":")) {
            return null;
        }
        return itemType.substring(prefix.length() + 1);
    }
}
