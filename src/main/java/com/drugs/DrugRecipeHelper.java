package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;


/**
 * Loads and registers shaped crafting recipes from recipes.yml
 */
public class DrugRecipeHelper {

    /**
     * Registers a shaped crafting recipe from config for a given drug.
     *
     * Expected format in recipes.yml:
     *   drug_id:
     *     shape:
     *       - " A "
     *       - "BCB"
     *       - " D "
     *     ingredients:
     *       A: SUGAR
     *       B: BONE
     *       C: GHAST_TEAR
     *       D: APPLE
     *
     * @param id     The internal drug ID (e.g. "heroin")
     * @param section The section for this drug in recipes.yml
     * @param plugin The plugin instance for key registration
     */
    public static void registerDrugRecipe(String id, ConfigurationSection section, Plugin plugin) {
        if (section == null) return;

        Bukkit.getLogger().info("[LumiDrugs] Attempting to register recipe for: " + id);

        // Parse the shape
        var shape = section.getStringList("shape");
        if (shape.size() != 3) return;

        // Build the result item
        DrugEffectProfile profile = DrugRegistry.getProfileById(id);
        if (profile == null) return;

        ItemStack result = profile.createItem(1);
        registerItemRecipe(id, section, result, plugin);
    }

    public static void registerItemRecipe(String id, ConfigurationSection section, ItemStack result, Plugin plugin) {
        if (section == null || result == null) return;

        var shape = section.getStringList("shape");
        if (shape.size() != 3) return;

        NamespacedKey key = new NamespacedKey(plugin, id.toLowerCase());

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.toArray(new String[0]));

        // Set each character as a material
        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) return;

        for (String symbol : ingredients.getKeys(false)) {
            String matName = ingredients.getString(symbol);
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                recipe.setIngredient(symbol.charAt(0), mat);
            }
        }

        String crop = DrugGrowing.requirement(id);
        if (crop != null) {
            // Replace the first configured ingredient with the harvested crop when an old recipe
            // has no matching material (for example custom recipes on an existing server).
            char required = 0;
            for (String symbol : ingredients.getKeys(false)) {
                if (shape.stream().noneMatch(row -> row.indexOf(symbol.charAt(0)) >= 0)) continue;
                if (required == 0) required = symbol.charAt(0);
                if (Material.matchMaterial(ingredients.getString(symbol)) == DrugGrowing.material(crop)) {
                    required = symbol.charAt(0);
                    break;
                }
            }
            if (required == 0) return;
            recipe.setIngredient(required, DrugGrowing.material(crop));
        }
        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }
}
