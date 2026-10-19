package me.dexuby.UltimateDrugs.guis;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements InventoryHolder {
   private GUI gui;

   GUIHolder(GUI var1) {
      this.gui = var1;
   }

   public GUI getGUI() {
      return this.gui;
   }

   public Inventory getInventory() {
      return this.gui.getInventory();
   }
}
