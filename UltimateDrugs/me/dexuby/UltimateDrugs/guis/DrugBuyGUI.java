package me.dexuby.UltimateDrugs.guis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugRelatedGood;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugBuyGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugBuyGUI(Main var1) {
      super("drug-buy", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      int var1 = (int)this.main.getDrugManager().getDrugRelatedGoods().stream().filter((var0) -> var0.getBuyPrice() > (double)0.0F).count();
      Inventory var2 = this.main.getServer().createInventory(new GUIHolder(this), (int)Math.min(Math.round(Math.ceil((double)(this.main.getDrugManager().getBuyableDrugs().size() + var1) / (double)9.0F) * (double)9.0F), 54L), this.main.getConfigManager().getLangString("drug-buy-gui-title"));
      return var2;
   }

   public void open(Player var1, Object... var2) {
      int var3 = var2.length > 0 ? (Integer)var2[0] : 0;
      Inventory var4 = this.getInventory();
      Object var5 = new ArrayList();

      for(Drug var7 : this.main.getDrugManager().getDrugs()) {
         if (var7.getBuyPrice() > (double)0.0F) {
            HashMap var8 = new HashMap();
            var8.put("%buy_price%", Double.toString(var7.getBuyPrice()));
            var8.put("%buy_price_16%", Double.toString(var7.getBuyPrice() * (double)16.0F));
            ((List)var5).add(ReflectionUtils.setItemStackStringTag((new ItemBuilder(var7.getDrugItemStack())).appendLore(TextUtils.replaceVariables((List)this.configManager.getLangStringList("buy-drug-lore"), var8)).build(), "drug-id", var7.getId()));
         }

         for(DrugRelatedGood var9 : var7.getDrugRelatedGoods()) {
            if (var9.getBuyPrice() > (double)0.0F) {
               HashMap var10 = new HashMap();
               var10.put("%buy_price%", Double.toString(var9.getBuyPrice()));
               var10.put("%buy_price_16%", Double.toString(var9.getBuyPrice() * (double)16.0F));
               ((List)var5).add(ReflectionUtils.setItemStackStringTags((new ItemBuilder(var9.getItemStack())).appendLore(TextUtils.replaceVariables((List)this.configManager.getLangStringList("buy-related-good-lore"), var10)).build(), new AbstractMap.SimpleEntry("drug-id", var7.getId()), new AbstractMap.SimpleEntry("related-good-id", var9.getId())));
            }
         }
      }

      if (var3 != 0) {
         var5 = ((List)var5).subList(45 * var3, ((List)var5).size());
      }

      boolean var11 = ((List)var5).size() > 45;
      if (var11) {
         var5 = ((List)var5).subList(0, 45);
      }

      ((List)var5).forEach((var1x) -> var4.addItem(new ItemStack[]{var1x}));
      if (var11 || var3 > 0) {
         ItemStack var12 = (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build();

         for(int var14 = 45; var14 < 54; ++var14) {
            var4.setItem(var14, var12);
         }

         if (var11) {
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
         if (var3 != null && !var3.equals("")) {
            Drug var4 = this.main.getDrugManager().getDrugById(var3);
            if (var4 != null) {
               String var5 = ReflectionUtils.getItemStackStringTag(var1.getCurrentItem(), "related-good-id");
               if (var5 != null && !var5.equals("")) {
                  var4.getDrugRelatedGoods().stream().filter((var1x) -> var1x.getId().equals(var5)).findFirst().ifPresent((var4x) -> {
                     if (var1.getClick() != ClickType.SHIFT_LEFT && var1.getClick() != ClickType.SHIFT_RIGHT) {
                        this.proceedPurchase(var2, var4, var4x.getItemStack().clone(), var4x.getBuyPrice(), false);
                     } else {
                        ItemStack var5 = var4x.getItemStack().clone();
                        var5.setAmount(16);
                        this.proceedPurchase(var2, var4, var5, var4x.getBuyPrice() * (double)16.0F, false);
                     }

                  });
               } else if (var1.getClick() != ClickType.SHIFT_LEFT && var1.getClick() != ClickType.SHIFT_RIGHT) {
                  this.proceedPurchase(var2, var4, var4.getDrugItemStack().clone(), var4.getBuyPrice(), true);
               } else {
                  ItemStack var6 = var4.getDrugItemStack().clone();
                  var6.setAmount(16);
                  this.proceedPurchase(var2, var4, var6, var4.getBuyPrice() * (double)16.0F, true);
               }
            }
         }

         if (var1.getSlot() == var1.getInventory().getSize() - 1) {
            ItemStack var7 = var1.getCurrentItem();
            if (var7.getType() == Material.ARROW) {
               String var9 = ReflectionUtils.getItemStackStringTag(var7, "next-page");
               if (var9 != null) {
                  int var11 = Integer.parseInt(var9);
                  var2.closeInventory();
                  this.open(var2, var11);
               }
            }
         } else if (var1.getSlot() == var1.getInventory().getSize() - 9) {
            ItemStack var8 = var1.getCurrentItem();
            if (var8.getType() == Material.ARROW) {
               String var10 = ReflectionUtils.getItemStackStringTag(var8, "previous-page");
               if (var10 != null) {
                  int var12 = Integer.parseInt(var10);
                  var2.closeInventory();
                  this.open(var2, var12);
               }
            }
         }
      }

   }

   private void proceedPurchase(Player var1, Drug var2, ItemStack var3, double var4, boolean var6) {
      var4 = (double)Math.round(var4 * (double)100.0F) / (double)100.0F;
      Economy var7 = this.main.getEconomy();
      if (var1.getInventory().firstEmpty() == -1) {
         var1.sendMessage(this.configManager.getLangString("inventory-full"));
      } else if (var7.has(var1, var4)) {
         var7.withdrawPlayer(var1, var4);
         var1.getInventory().addItem(new ItemStack[]{var3});
         HashMap var8 = new HashMap();
         var8.put("%drug_id%", var2.getId());
         var8.put("%buy_price%", Double.toString(var4));
         if (var3.getItemMeta() != null && var3.getItemMeta().hasDisplayName()) {
            var8.put("%item_display_name%", var3.getItemMeta().getDisplayName());
         }

         if (var6) {
            var1.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("drug-purchased"), var8));
         } else {
            var1.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("drug-good-purchased"), var8));
         }
      } else {
         var1.sendMessage(this.configManager.getLangString("not-enough-money"));
      }

   }
}
