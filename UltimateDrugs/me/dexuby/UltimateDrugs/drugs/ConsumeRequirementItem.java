package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.inventory.ItemStack;

public class ConsumeRequirementItem {
   private ItemStack itemStack;
   private boolean damageOnConsume;
   private boolean removeOnConsume;

   public ConsumeRequirementItem(ItemStack var1, boolean var2, boolean var3) {
      this.itemStack = var1;
      this.damageOnConsume = var2;
      this.removeOnConsume = var3;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public boolean doDamageOnConsume() {
      return this.damageOnConsume;
   }

   public boolean doRemoveOnConsume() {
      return this.removeOnConsume;
   }
}
