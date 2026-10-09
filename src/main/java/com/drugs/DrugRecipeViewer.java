package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import java.util.Map;

/** Read-only display of the registered recipe, including harvested ingredients. */
public class DrugRecipeViewer {
    public static void openRecipe(Player player, String drugId) {
        String key = "farm-controller".equals(drugId) ? "farm_controller" : drugId;
        Recipe registered = Bukkit.getRecipe(new NamespacedKey(DrugsV2.getInstance(), key));
        if (!(registered instanceof ShapedRecipe recipe)) {
            player.sendMessage("§cRecipe not found for: " + drugId);
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 27, "Recipe: " + drugId);
        Map<Character, RecipeChoice> ingredients = recipe.getChoiceMap();
        String crop = DrugGrowing.requirement(drugId);
        String[] shape = recipe.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int column = 0; column < shape[row].length(); column++) {
                RecipeChoice choice = ingredients.get(shape[row].charAt(column));
                if (choice == null) continue;
                ItemStack ingredient = choice.getItemStack();
                if (crop != null && ingredient.getType() == DrugGrowing.material(crop))
                    ingredient = DrugGrowing.harvest(crop, "cannabis".equals(crop) ? DrugItemMetadata.DEFAULT_STRAIN_ID : null);
                gui.setItem(row * 9 + column + 1, ingredient);
            }
        }
        gui.setItem(15, recipe.getResult());
        player.openInventory(gui);
        if (crop != null) {
            if ("cannabis".equals(crop)) player.sendMessage("§aGrow ferns into large ferns, then harvest with shears for Cannabis Buds.");
            else player.sendMessage("§aCraft " + crop + " seeds using wheat seeds + " + DrugGrowing.material(crop).name().toLowerCase().replace('_', ' ')
                + ". Plant on hydrated farmland and wait for full growth.");
        }
    }
}
