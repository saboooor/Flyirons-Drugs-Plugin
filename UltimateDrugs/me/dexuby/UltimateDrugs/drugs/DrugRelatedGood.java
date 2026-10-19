package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.inventory.ItemStack;

public class DrugRelatedGood {
   private String id;
   private ItemStack itemStack;
   private double sellPrice;
   private double buyPrice;

   public DrugRelatedGood(String var1, ItemStack var2, double var3, double var5) {
      this.id = var1;
      this.itemStack = var2;
      this.sellPrice = var3;
      this.buyPrice = var5;
   }

   public String getId() {
      return this.id;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public double getSellPrice() {
      return this.sellPrice;
   }

   public double getBuyPrice() {
      return this.buyPrice;
   }
}
