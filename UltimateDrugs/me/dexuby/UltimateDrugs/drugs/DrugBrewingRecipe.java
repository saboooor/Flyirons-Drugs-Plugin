package me.dexuby.UltimateDrugs.drugs;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public class DrugBrewingRecipe extends DrugRecipe {
   private ItemStack input;
   private ItemStack fuel;
   private int speed;
   private List<ItemStack> results;

   public DrugBrewingRecipe(String var1, ItemStack var2, ItemStack var3, int var4, List<ItemStack> var5) {
      super(var1);
      this.input = var2;
      this.fuel = var3;
      this.speed = var4;
      this.results = var5;
   }

   public ItemStack getInput() {
      return this.input;
   }

   public ItemStack getFuel() {
      return this.fuel;
   }

   public int getSpeed() {
      return this.speed;
   }

   public List<ItemStack> getResults() {
      return this.results;
   }
}
