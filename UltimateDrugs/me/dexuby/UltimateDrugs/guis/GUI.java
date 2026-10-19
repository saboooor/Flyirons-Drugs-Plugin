package me.dexuby.UltimateDrugs.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public abstract class GUI {
   private String id;
   private boolean cancelInteractions;

   GUI(String var1, boolean var2) {
      this.id = var1;
      this.cancelInteractions = var2;
   }

   public String getId() {
      return this.id;
   }

   public boolean doCancelInteractions() {
      return this.cancelInteractions;
   }

   public abstract Inventory getInventory();

   public abstract void open(Player var1, Object... var2);

   public abstract void onClick(InventoryClickEvent var1);

   public void onInventoryClick(InventoryClickEvent var1) {
   }

   public void onInventoryClose(InventoryCloseEvent var1) {
   }
}
