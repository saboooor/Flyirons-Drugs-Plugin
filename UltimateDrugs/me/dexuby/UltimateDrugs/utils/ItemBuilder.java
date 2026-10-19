package me.dexuby.UltimateDrugs.utils;

import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class ItemBuilder {
   private ItemStack itemStack;

   public ItemBuilder(Material var1) {
      this.itemStack = new ItemStack(var1);
   }

   public ItemBuilder(ItemStack var1) {
      this.itemStack = var1.clone();
   }

   public ItemBuilder itemMeta(ItemMeta var1) {
      this.itemStack.setItemMeta(var1);
      return this;
   }

   public ItemBuilder type(Material var1) {
      this.itemStack.setType(var1);
      return this;
   }

   public ItemBuilder amount(int var1) {
      this.itemStack.setAmount(var1);
      return this;
   }

   public ItemBuilder damage(int var1) {
      if (this.itemStack.getItemMeta() instanceof Damageable) {
         Damageable var2 = (Damageable)this.itemStack.getItemMeta();
         var2.setDamage(var1);
         this.itemStack.setItemMeta((ItemMeta)var2);
      }

      return this;
   }

   public ItemBuilder unbreakable(boolean var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null) {
         var2.setUnbreakable(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder displayName(String var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null) {
         var2.setDisplayName(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder lore(List<String> var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null) {
         var2.setLore(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder appendLore(List<String> var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null) {
         if (var2.hasLore()) {
            List var3 = var2.getLore();
            var3.addAll(var1);
            var2.setLore(var3);
            this.itemStack.setItemMeta(var2);
         } else {
            var2.setLore(var1);
            this.itemStack.setItemMeta(var2);
         }
      }

      return this;
   }

   public ItemBuilder addItemFlags(ItemFlag... var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null) {
         var2.addItemFlags(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder skullTexture(String var1) {
      if (this.itemStack.getItemMeta() instanceof SkullMeta) {
         ReflectionUtils.setSkullTexture(this.itemStack, var1);
      }

      return this;
   }

   public ItemBuilder skullOwner(OfflinePlayer var1) {
      if (this.itemStack.getItemMeta() instanceof SkullMeta) {
         SkullMeta var2 = (SkullMeta)this.itemStack.getItemMeta();
         var2.setOwningPlayer(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder addEnchantment(Enchantment var1, int var2) {
      this.itemStack.addUnsafeEnchantment(var1, var2);
      return this;
   }

   public ItemBuilder addPotionEffect(PotionEffect var1) {
      if (this.itemStack.getItemMeta() instanceof PotionMeta) {
         PotionMeta var2 = (PotionMeta)this.itemStack.getItemMeta();
         var2.addCustomEffect(var1, false);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder potionType(PotionType var1) {
      if (this.itemStack.getItemMeta() instanceof PotionMeta) {
         PotionMeta var2 = (PotionMeta)this.itemStack.getItemMeta();
         var2.setBasePotionData(new PotionData(var1));
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder potionColor(Color var1) {
      if (this.itemStack.getItemMeta() instanceof PotionMeta) {
         PotionMeta var2 = (PotionMeta)this.itemStack.getItemMeta();
         var2.setColor(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemBuilder setStringTag(String var1, String var2) {
      this.itemStack = ReflectionUtils.setItemStackStringTag(this.itemStack, var1, var2);
      return this;
   }

   public ItemBuilder customModelData(int var1) {
      ItemMeta var2 = this.itemStack.getItemMeta();
      if (var2 != null && ReflectionUtils.getVersionId() >= 240) {
         var2.setCustomModelData(var1);
         this.itemStack.setItemMeta(var2);
      }

      return this;
   }

   public ItemStack build() {
      return this.itemStack;
   }
}
