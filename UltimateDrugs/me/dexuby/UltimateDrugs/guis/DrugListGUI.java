package me.dexuby.UltimateDrugs.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DrugListGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugListGUI(Main var1) {
      super("drug-list", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), (int)Math.min(Math.round(Math.ceil((double)this.main.getDrugManager().getDrugs().size() / (double)9.0F) * (double)9.0F), 54L), this.main.getConfigManager().getLangString("drug-list-gui-title"));
      return var1;
   }

   public void open(Player var1, Object... var2) {
      int var3 = var2.length > 0 ? (Integer)var2[0] : 0;
      Inventory var4 = this.getInventory();
      Object var5 = new ArrayList();

      for(Drug var7 : this.main.getDrugManager().getDrugs()) {
         HashMap var8 = new HashMap();
         var8.put("%drug_unboosted_sell_price%", var7.getSellPrice() > (double)0.0F ? Double.toString(var7.getSellPrice()) : this.configManager.getLangString("not-sellable"));
         var8.put("%drug_sell_price%", var7.getSellPrice() > (double)0.0F ? Double.toString(var7.getSellPrice() * this.main.getDrugBoosterManager().getBoosterMultiplier(var7, var1)) : this.configManager.getLangString("not-sellable"));
         var8.put("%drug_buy_price%", var7.getBuyPrice() > (double)0.0F ? Double.toString(var7.getBuyPrice()) : this.configManager.getLangString("not-buyable"));
         var8.put("%drug_booster_multiplier%", Double.toString(this.main.getDrugBoosterManager().getBoosterMultiplier(var7, var1)));
         ItemStack var9 = var7.getDrugItemStack().clone();
         var9 = ReflectionUtils.setItemStackStringTag(var9, "drug-id", var7.getId());
         if (var9 != null) {
            List var10 = this.main.getDrugBoosterManager().hasActiveBooster(var7) ? TextUtils.replaceVariables((List)this.configManager.getLangStringList("drug-list-boosted-item-lore"), var8) : TextUtils.replaceVariables((List)this.configManager.getLangStringList("drug-list-unboosted-item-lore"), var8);
            ItemMeta var11 = var9.getItemMeta();
            if (var11.hasLore()) {
               List var12 = var11.getLore();
               var12.addAll(var10);
               var11.setLore(var12);
            } else {
               var11.setLore(var10);
            }

            var9.setItemMeta(var11);
            ((List)var5).add(var9);
         }
      }

      if (var3 != 0) {
         var5 = ((List)var5).subList(45 * var3, ((List)var5).size());
      }

      boolean var13 = ((List)var5).size() > 45;
      if (var13) {
         var5 = ((List)var5).subList(0, 45);
      }

      int var14 = 0;

      for(ItemStack var18 : var5) {
         var4.setItem(var14, var18);
         ++var14;
      }

      if (var13 || var3 > 0) {
         ItemStack var16 = (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build();

         for(int var19 = 45; var19 < 54; ++var19) {
            var4.setItem(var19, var16);
         }

         if (var13) {
            var4.setItem(var4.getSize() - 1, (new ItemBuilder(Material.ARROW)).displayName(this.configManager.getLangString("next-page-item-display-name")).lore(this.configManager.getLangStringList("next-page-item-lore")).setStringTag("next-page", Integer.toString(var3 + 1)).build());
         }

         if (var3 > 0) {
            var4.setItem(var4.getSize() - 9, (new ItemBuilder(Material.ARROW)).displayName(this.configManager.getLangString("previous-page-item-display-name")).lore(this.configManager.getLangStringList("previous-page-item-lore")).setStringTag("previous-page", Integer.toString(var3 - 1)).build());
         }
      }

      var1.openInventory(var4);
   }

   public void onClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getCurrentItem() != null) {
         String var3 = ReflectionUtils.getItemStackStringTag(var1.getCurrentItem(), "drug-id");
         if (var3 != null && var3.length() > 0) {
            GUI var8 = this.main.getGUIManager().getGUIById("drug-info");
            Drug var10 = this.main.getDrugManager().getDrugById(var3);
            var8.open(var2, var10);
            return;
         }

         if (var1.getSlot() == var1.getInventory().getSize() - 1) {
            ItemStack var4 = var1.getCurrentItem();
            if (var4.getType() == Material.ARROW) {
               String var5 = ReflectionUtils.getItemStackStringTag(var4, "next-page");
               if (var5 != null) {
                  int var6 = Integer.parseInt(var5);
                  var2.closeInventory();
                  this.open(var2, var6);
               }
            }
         } else if (var1.getSlot() == var1.getInventory().getSize() - 9) {
            ItemStack var7 = var1.getCurrentItem();
            if (var7.getType() == Material.ARROW) {
               String var9 = ReflectionUtils.getItemStackStringTag(var7, "previous-page");
               if (var9 != null) {
                  int var11 = Integer.parseInt(var9);
                  var2.closeInventory();
                  this.open(var2, var11);
               }
            }
         }
      }

   }
}
