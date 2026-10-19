package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.inventory.ItemStack;

public class Drop {
   private ItemStack itemStack;
   private int minAmount;
   private int maxAmount;
   private double dropChance;
   private boolean breakSuccessChain;

   public Drop(ItemStack var1, int var2, int var3, double var4, boolean var6) {
      this.itemStack = var1;
      this.minAmount = var2;
      this.maxAmount = var3;
      this.dropChance = var4;
      this.breakSuccessChain = var6;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public int getMinAmount() {
      return this.minAmount;
   }

   public int getMaxAmount() {
      return this.maxAmount;
   }

   public double getDropChance() {
      return this.dropChance;
   }

   public boolean doBreakSuccessChain() {
      return this.breakSuccessChain;
   }
}
