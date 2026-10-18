package com.drugs;

import com.drugs.DialogView.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/** Read-only dialogue and recipe pages: dialogs for dialogue/growing, inventory GUI for recipe preview. */
public final class EnforcerGuide implements Listener {
    public enum Page { DIALOGUE, RECIPE, GROWING }

    static final class RecipeMenu implements InventoryHolder {
        final String drug;
        final Inventory inventory;

        RecipeMenu(String drug) {
            this.drug = drug;
            this.inventory = Bukkit.createInventory(this, 54, "Crafting: " + drug);
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final DrugsV2 plugin;
    public EnforcerGuide(DrugsV2 plugin) { this.plugin = plugin; }

    public static void open(DrugsV2 plugin, Player player, String drug) {
        openPage(plugin, player, drug, Page.DIALOGUE);
    }

    public static void openPage(DrugsV2 plugin, Player player, String drug, Page page) {
        if (page == Page.RECIPE) {
            openRecipeInventory(plugin, player, drug);
        } else {
            MultiActionDialog dialog = createDialog(drug, page);
            plugin.showDialog(player, dialog);
        }
    }

    public static MultiActionDialog createDialog(String drug, Page page) {
        return switch (page) {
            case DIALOGUE -> dialogueDialog(drug);
            case GROWING -> growingDialog(drug);
            case RECIPE -> throw new IllegalArgumentException("Recipe page uses an inventory GUI");
        };
    }

    private static MultiActionDialog dialogueDialog(String drug) {
        String message = "Enforcer: I need " + drug + ".\n"
                + "Throw the goods near me to deliver them.\n"
                + "Need help? Choose a guide below.\n"
                + "Your delivery deadline keeps running.";
        DialogBase base = Dialogs.base("Cartel enforcer", message);
        List<ActionButton> actions = List.of(
                Dialogs.button("Crafting recipe", "/drugs guide " + drug + " recipe", false, Icon.CRAFTING_TABLE, "View the crafting recipe for " + drug + "."),
                Dialogs.button("How to grow", "/drugs guide " + drug + " growing", false, Icon.SEEDS, "Learn how to grow the required ingredient.")
        );
        return Dialogs.menu(base, actions, Dialogs.button("Close", "/drugs guide close", false, Icon.CLOSE, "Close this guide."));
    }

    private static MultiActionDialog growingDialog(String drug) {
        String crop = DrugGrowing.requirement(drug);
        List<String> instructions = growingSteps(crop);
        StringBuilder sb = new StringBuilder();
        if (crop != null) {
            sb.append("Growing guide for ").append(crop).append(":\n\n");
        }
        for (int i = 0; i < instructions.size(); i++) {
            sb.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
        }

        DialogBase base = Dialogs.base("Growing ingredients", sb.toString());
        List<ActionButton> actions = List.of(
                Dialogs.button("Crafting recipe", "/drugs guide " + drug + " recipe", false, Icon.CRAFTING_TABLE, "View the crafting recipe for " + drug + "."),
                Dialogs.button("Back to enforcer", "/drugs guide " + drug + " dialogue", false, Icon.ARROW, "Return to cartel enforcer instructions.")
        );
        return Dialogs.menu(base, actions, Dialogs.button("Close", "/drugs guide close", false, Icon.CLOSE, "Close this guide."));
    }

    private static void openRecipeInventory(DrugsV2 plugin, Player player, String drug) {
        RecipeMenu menu = new RecipeMenu(drug);
        Inventory inventory = menu.inventory;
        Recipe recipe = Bukkit.getRecipe(new NamespacedKey(plugin, drug));
        if (recipe instanceof ShapedRecipe shaped) {
            String crop = DrugGrowing.requirement(drug);
            String[] shape = shaped.getShape();
            Map<Character, ItemStack> ingredients = shaped.getIngredientMap();
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
        inventory.setItem(45, icon(Material.ARROW, "§eBack to enforcer"));
        inventory.setItem(49, icon(Material.WHEAT_SEEDS, "§aHow to grow the ingredient"));
        inventory.setItem(53, icon(Material.BARRIER, "§cClose"));
        player.openInventory(inventory);
    }

    public static List<String> growingSteps(String crop) {
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
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RecipeMenu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0
                || event.getRawSlot() >= menu.inventory.getSize()) return;
        int slot = event.getRawSlot();
        if (slot == 53) {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
            return;
        }
        if (slot == 45) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                openPage(plugin, player, menu.drug, Page.DIALOGUE);
            });
            return;
        }
        if (slot == 49) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                openPage(plugin, player, menu.drug, Page.GROWING);
            });
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RecipeMenu) {
            event.setCancelled(true);
        }
    }
}
