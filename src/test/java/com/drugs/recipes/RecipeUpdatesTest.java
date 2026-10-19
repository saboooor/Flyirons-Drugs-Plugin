package com.drugs.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class RecipeUpdatesTest {
    @AfterEach void reset() { RecipeUpdates.reset(); }

    @Test void startupBatchPublishesOnceAfterAllRecipeChanges() {
        var recipe = mock(Recipe.class);
        var key = new NamespacedKey("lumidrugs", "test");
        try (var bukkit = mockStatic(Bukkit.class)) {
            RecipeUpdates.begin();
            for (int i = 0; i < 25; i++) RecipeUpdates.replace(key, recipe);
            bukkit.verify(() -> Bukkit.removeRecipe(key, false), times(25));
            bukkit.verify(() -> Bukkit.addRecipe(recipe, false), times(25));
            bukkit.verify(Bukkit::updateRecipes, never());
            RecipeUpdates.end();
            bukkit.verify(Bukkit::updateRecipes, times(1));
            RecipeUpdates.end();
            bukkit.verify(Bukkit::updateRecipes, times(1));
        }
    }

    @Test void unbatchedReplacementStillUpdatesClientsAndEmptyBatchDoesNot() {
        try (var bukkit = mockStatic(Bukkit.class)) {
            RecipeUpdates.begin();
            RecipeUpdates.end();
            bukkit.verify(Bukkit::updateRecipes, never());
            RecipeUpdates.replace(new NamespacedKey("lumidrugs", "test"), mock(Recipe.class));
            bukkit.verify(Bukkit::updateRecipes);
        }
    }
}
