package me.dexuby.UltimateDrugs.guis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugRelatedGood;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.managers.DrugBoosterManager;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.Pair;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugSellGUI extends GUI {
   private Main main;
   private ConfigManager configManager;
   private DrugManager drugManager;

   public DrugSellGUI(Main var1) {
      super("drug-sell", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
      this.drugManager = var1.getDrugManager();
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 54, this.main.getConfigManager().getLangString("drug-sell-gui-title"));
      ItemStack var2 = (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build();

      for(int var3 = 45; var3 < 54; ++var3) {
         var1.setItem(var3, var2);
      }

      return var1;
   }

   public void open(Player var1, Object... var2) {
      Inventory var3 = this.getInventory();
      this.updateConfirmButton(var3, var1);
      var1.openInventory(var3);
   }

   public void onClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getClickedInventory() != null) {
         if (var1.getSlot() < 45) {
            if (var1.getCurrentItem() != null) {
               if (var1.getClick() != ClickType.SHIFT_LEFT && var1.getClick() != ClickType.SHIFT_RIGHT) {
                  if (var2.getInventory().firstEmpty() == -1) {
                     return;
                  }

                  var2.getInventory().addItem(new ItemStack[]{var1.getCurrentItem()});
                  var1.getClickedInventory().clear(var1.getSlot());
               } else {
                  ItemStack var3 = var1.getCurrentItem().clone();

                  for(int var4 = 0; var4 < 45 && var2.getInventory().firstEmpty() != -1; ++var4) {
                     ItemStack var5 = var1.getClickedInventory().getItem(var4);
                     if (var5 != null && var3.isSimilar(var5)) {
                        var2.getInventory().addItem(new ItemStack[]{var5});
                        var1.getClickedInventory().clear(var4);
                     }
                  }
               }

               this.updateConfirmButton(var1.getClickedInventory(), var2);
            }
         } else if (var1.getSlot() == 49) {
            SellStatus var6 = this.getInventorySellStatus(var1.getClickedInventory(), var2);
            if (var6.totalAmount > (double)0.0F) {
               for(Pair var9 : var6.items) {
                  var1.getClickedInventory().clear((Integer)var9.getKey());
               }

               HashMap var8 = new HashMap();
               var8.put("%total_sell_amount%", Double.toString(var6.totalAmount));
               this.main.getEconomy().depositPlayer(var2, var6.totalAmount);
               var2.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("drug-sell-confirm"), var8));
            }
         }
      }

   }

   public void onInventoryClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getCurrentItem() != null) {
         if (var1.getClick() != ClickType.SHIFT_LEFT && var1.getClick() != ClickType.SHIFT_RIGHT) {
            if (var1.getView().getTopInventory().firstEmpty() == -1) {
               return;
            }

            var1.getView().getTopInventory().addItem(new ItemStack[]{var1.getCurrentItem()});
            var2.getInventory().clear(var1.getSlot());
         } else {
            ItemStack var3 = var1.getCurrentItem().clone();

            for(int var4 = 0; var4 <= 40 && var1.getView().getTopInventory().firstEmpty() != -1; ++var4) {
               ItemStack var5 = var2.getInventory().getItem(var4);
               if (var5 != null && var3.isSimilar(var5)) {
                  var1.getView().getTopInventory().addItem(new ItemStack[]{var5});
                  var2.getInventory().clear(var4);
               }
            }
         }

         this.updateConfirmButton(var1.getView().getTopInventory(), var2);
      }

   }

   public void onInventoryClose(InventoryCloseEvent var1) {
      Inventory var2 = var1.getInventory();
      Player var3 = (Player)var1.getPlayer();

      for(int var4 = 0; var4 <= 44; ++var4) {
         ItemStack var5 = var2.getItem(var4);
         if (var5 != null) {
            if (var3.getInventory().firstEmpty() != -1) {
               var3.getInventory().addItem(new ItemStack[]{var5});
            } else {
               var3.getWorld().dropItemNaturally(var3.getLocation(), var5);
            }
         }

         var2.clear(var4);
      }

   }

   private void updateConfirmButton(Inventory var1, Player var2) {
      SellStatus var3 = this.getInventorySellStatus(var1, var2);
      HashMap var4 = new HashMap();
      var4.put("%total_sell_amount%", Double.toString(var3.totalAmount));
      var1.setItem(49, (new ItemBuilder(Material.LIME_DYE)).displayName(TextUtils.replaceVariables(this.configManager.getLangString("drug-sell-confirm-display-name"), var4)).lore(TextUtils.replaceVariables((List)this.configManager.getLangStringList("drug-sell-confirm-lore"), var4)).build());
   }

   private SellStatus getInventorySellStatus(Inventory var1, Player var2) {
      SellStatus var3 = new SellStatus();
      DrugBoosterManager var4 = this.main.getDrugBoosterManager();

      for(int var5 = 0; var5 < 45; ++var5) {
         ItemStack var6 = var1.getItem(var5);
         if (var6 != null) {
            for(Drug var8 : this.drugManager.getDrugs()) {
               if (var8.getDrugItemStack().isSimilar(var6)) {
                  if (var8.getSellPrice() > (double)0.0F) {
                     double var13 = var8.getSellPrice() * (double)var6.getAmount() * var4.getBoosterMultiplier(var8, var2);
                     var13 = (double)Math.round(var13 * (double)100.0F) / (double)100.0F;
                     var3.addToTotalAmount(var13);
                     var3.addItemStack(var5, var6);
                     break;
                  }
               } else {
                  for(DrugRelatedGood var10 : var8.getDrugRelatedGoods()) {
                     if (!(var10.getSellPrice() <= (double)0.0F) && var10.getItemStack().isSimilar(var6)) {
                        double var11 = var10.getSellPrice() * (double)var6.getAmount();
                        var11 = (double)Math.round(var11 * (double)100.0F) / (double)100.0F;
                        var3.addToTotalAmount(var11);
                        var3.addItemStack(var5, var6);
                        break;
                     }
                  }
               }
            }
         }
      }

      return var3;
   }

   private class SellStatus {
      private double totalAmount;
      private Collection<Pair<Integer, ItemStack>> items;

      private SellStatus() {
         this.items = new ArrayList();
      }

      private void addToTotalAmount(double var1) {
         this.totalAmount += var1;
      }

      private void addItemStack(int var1, ItemStack var2) {
         this.items.add(new Pair(var1, var2));
      }
   }
}
