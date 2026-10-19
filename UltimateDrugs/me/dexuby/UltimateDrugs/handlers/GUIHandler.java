package me.dexuby.UltimateDrugs.handlers;

import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.guis.GUI;
import me.dexuby.UltimateDrugs.guis.GUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIHandler implements Listener {
   private Main main;

   public GUIHandler(Main var1) {
      this.main = var1;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent var1) {
      if (var1.getClickedInventory() != null && var1.getView().getTopInventory().getHolder() instanceof GUIHolder) {
         GUIHolder var2 = (GUIHolder)var1.getView().getTopInventory().getHolder();
         GUI var3 = var2.getGUI();
         if (var3.doCancelInteractions()) {
            var1.setCancelled(true);
         }

         if (var1.getClickedInventory().getHolder() != null && var1.getClickedInventory().getHolder().equals(var2)) {
            var3.onClick(var1);
         } else {
            var3.onInventoryClick(var1);
         }
      }

   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent var1) {
      Player var2 = (Player)var1.getPlayer();
      if (this.main.getGUIManager().hasPreviousGUI(var2.getUniqueId())) {
         this.main.getServer().getScheduler().runTaskLater(this.main, () -> var2.openInventory(this.main.getGUIManager().pollPreviousGUI(var2.getUniqueId())), 1L);
      }

      if (var1.getInventory().getHolder() instanceof GUIHolder) {
         GUIHolder var3 = (GUIHolder)var1.getInventory().getHolder();
         var3.getGUI().onInventoryClose(var1);
      }

   }
}
