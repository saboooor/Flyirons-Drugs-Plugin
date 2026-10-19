package me.dexuby.UltimateDrugs.drugs;

import java.util.Collection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class DrugCraftingRecipe extends DrugRecipe {
   private Collection<ItemStack> ingredients;
   private Recipe recipe;

   public DrugCraftingRecipe(String var1, Collection<ItemStack> var2, Recipe var3) {
      super(var1);
      this.ingredients = var2;
      this.recipe = var3;
   }

   public Collection<ItemStack> getIngredients() {
      return this.ingredients;
   }

   public Recipe getRecipe() {
      return this.recipe;
   }
}
