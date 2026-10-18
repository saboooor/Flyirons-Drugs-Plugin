package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;

import java.util.List;

/** Read-only dialogue and recipe pages, identified by holder rather than inventory title. */
final class EnforcerGuide implements Listener {
    enum Page { DIALOGUE, RECIPE, GROWING }
    private static final class Menu implements InventoryHolder {
        final String drug;
        final Page page;
        final Inventory inventory;
        Menu(String drug, Page page) {
            this.drug = drug;
            this.page = page;
            inventory = Bukkit.createInventory(this, 54, switch (page) {
                case DIALOGUE -> "Cartel enforcer";
                case RECIPE -> "Crafting: " + drug;
                case GROWING -> "Growing ingredients";
            });
        }
        @Override public Inventory getInventory() { return inventory; }
    }
    private final DrugsV2 plugin;
    EnforcerGuide(DrugsV2 plugin) { this.plugin = plugin; }

    static void open(DrugsV2 plugin, Player player, String drug) {
        openPage(plugin, player, drug, Page.DIALOGUE);
    }

    private static void openPage(DrugsV2 plugin, Player player, String drug, Page page) {
        Menu menu = new Menu(drug, page);
        Inventory inventory = menu.inventory;
        if (page == Page.DIALOGUE) {
            inventory.setItem(13, icon(Material.PAPER, "§6Enforcer: I need " + drug,
                    "§7Throw the goods near me to deliver them.",
                    "§7Need help? Choose a guide below.",
                    "§eYour delivery deadline keeps running."));
            inventory.setItem(29, icon(Material.CRAFTING_TABLE, "§aCrafting recipe", "§7View the crafting grid for " + drug + "."));
            inventory.setItem(33, icon(Material.WHEAT_SEEDS, "§aHow to grow", "§7Learn how to grow the required ingredient."));
        } else if (page == Page.RECIPE) {
            Recipe recipe = Bukkit.getRecipe(new NamespacedKey(plugin, drug));
            if (recipe instanceof ShapedRecipe shaped) {
                String crop = DrugGrowing.requirement(drug);
                String[] shape = shaped.getShape();
                var ingredients = shaped.getIngredientMap();
                for (int row = 0; row < shape.length; row++) {
                    for (int column = 0; column < shape[row].length(); column++) {
                        ItemStack ingredient = ingredients.get(shape[row].charAt(column));
                        if (ingredient != null && crop != null && DrugGrowing.material(crop) == ingredient.getType()) {
                            ingredient = DrugGrowing.harvest(crop, null);
                        }
                        inventory.setItem(10 + row * 9 + column, ingredient);
                    }
                }
                inventory.setItem(24, shaped.getResult().clone());
                inventory.setItem(16, icon(Material.CRAFTING_TABLE, "§eRecipe preview", "§7Copy the grid into a crafting table."));
            } else {
                inventory.setItem(22, icon(Material.BARRIER, "§cNo crafting recipe available"));
            }
            inventory.setItem(49, icon(Material.WHEAT_SEEDS, "§aHow to grow the ingredient"));
        } else {
            List<String> instructions = growingSteps(DrugGrowing.requirement(drug));
            for (int i = 0; i < instructions.size(); i++) {
                inventory.setItem(10 + i, icon(Material.PAPER, "§eStep " + (i + 1), "§7" + instructions.get(i)));
            }
            inventory.setItem(49, icon(Material.CRAFTING_TABLE, "§aCrafting recipe"));
        }
        if (page != Page.DIALOGUE) inventory.setItem(45, icon(Material.ARROW, "§eBack to enforcer"));
        inventory.setItem(53, icon(Material.BARRIER, "§cClose"));
        player.openInventory(inventory);
    }

    static List<String> growingSteps(String crop) {
        if (crop == null) return List.of("This recipe does not require a grown ingredient.");
        if (crop.equals("cannabis")) return List.of(
                "Place a fern or cannabis plant on suitable soil.",
                "Leave air above it and keep the chunk loaded.",
                "Wait for it to become a large fern (average " + MechanicsConfig.getCannabisGrowthSeconds() + "s).",
                "Bone meal cannot speed up cannabis growth.",
                "Break the mature large fern using shears.",
                "Collect the Cannabis Buds and use them in the recipe.");
        DrugGrowing.Crop definition = DrugGrowing.crop(crop);
        if (definition == null) return List.of("Growing information is currently unavailable.");
        String starter = definition.starter().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return List.of(
                "Craft seeds: wheat seeds + " + starter + " (shapeless).",
                "Right-click the top of hydrated farmland with those seeds.",
                "Keep water nearby, light at least 9, and the chunk loaded.",
                "Wait " + definition.seconds() + " seconds for fully mature wheat.",
                "Bone meal cannot speed up this crop.",
                "Break the mature crop in Survival to get 2 " + definition.name() + " and a seed.",
                "Use the harvested " + definition.name() + " in the recipe.");
    }

    private static ItemStack icon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0
                || event.getRawSlot() >= menu.inventory.getSize()) return;
        int slot = event.getRawSlot();
        Page next = null;
        if (slot == 53) {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            return;
        }
        if (slot == 45 && menu.page != Page.DIALOGUE) next = Page.DIALOGUE;
        if (menu.page == Page.DIALOGUE && slot == 29 || menu.page == Page.GROWING && slot == 49) next = Page.RECIPE;
        if (menu.page == Page.DIALOGUE && slot == 33 || menu.page == Page.RECIPE && slot == 49) next = Page.GROWING;
        if (next != null) {
            Page destination = next;
            plugin.getServer().getScheduler().runTask(plugin, () -> openPage(plugin, player, menu.drug, destination));
        }
    }

    @EventHandler public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }
}
