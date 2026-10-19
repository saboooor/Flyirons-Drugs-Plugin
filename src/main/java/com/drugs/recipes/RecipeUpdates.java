package com.drugs.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

/** Main-thread recipe batches avoid rebuilding and resending the recipe list for every change. */
public final class RecipeUpdates {
    private static int batches;
    private static boolean changed;

    private RecipeUpdates() {}
    public static void begin() { batches++; }
    public static void end() {
        if (batches == 0) return;
        if (--batches == 0 && changed) {
            changed = false;
            Bukkit.updateRecipes();
        }
    }
    public static void replace(NamespacedKey key, Recipe recipe) {
        Bukkit.removeRecipe(key, false);
        changed = true;
        Bukkit.addRecipe(recipe, false);
        if (batches == 0) {
            changed = false;
            Bukkit.updateRecipes();
        }
    }
    public static void reset() { batches = 0; changed = false; }
}
