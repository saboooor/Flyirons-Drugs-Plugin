package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.inventory.FurnaceRecipe;

public class DrugFurnaceRecipe extends DrugRecipe {
   private FurnaceRecipe furnaceRecipe;

   public DrugFurnaceRecipe(String var1, FurnaceRecipe var2) {
      super(var1);
      this.furnaceRecipe = var2;
   }

   public FurnaceRecipe getFurnaceRecipe() {
      return this.furnaceRecipe;
   }
}
