package me.dexuby.UltimateDrugs.drugs.growing;

import java.util.Collection;
import org.bukkit.inventory.ItemStack;

public class Fertilizer {
   private String id;
   private ItemStack itemStack;
   private int growAmount;
   private Collection<String> drugIds;

   public Fertilizer(String var1, ItemStack var2, int var3, Collection<String> var4) {
      this.id = var1;
      this.itemStack = var2;
      this.growAmount = var3;
      this.drugIds = var4;
   }

   public String getId() {
      return this.id;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public int getGrowAmount() {
      return this.growAmount;
   }

   public Collection<String> getDrugIds() {
      return this.drugIds;
   }
}
