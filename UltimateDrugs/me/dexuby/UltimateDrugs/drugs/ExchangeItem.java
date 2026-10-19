package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.inventory.ItemStack;

public class ExchangeItem {
   private ItemStack item;
   private double chance;
   private boolean forceDrop;
   private boolean breakSuccessChain;
   private int preferredSlot;

   public ExchangeItem(ItemStack var1, double var2, boolean var4, boolean var5, int var6) {
      this.item = var1;
      this.chance = var2;
      this.forceDrop = var4;
      this.breakSuccessChain = var5;
      this.preferredSlot = var6;
   }

   public ItemStack getItem() {
      return this.item;
   }

   public double getChance() {
      return this.chance;
   }

   public boolean doForceDrop() {
      return this.forceDrop;
   }

   public boolean doBreakSuccessChain() {
      return this.breakSuccessChain;
   }

   public int getPreferredSlot() {
      return this.preferredSlot;
   }
}
