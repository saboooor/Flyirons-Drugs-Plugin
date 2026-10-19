package me.dexuby.UltimateDrugs.api;

import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugPlantSendStatusEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private Player player;
   private Plant plant;

   public DrugPlantSendStatusEvent(Player var1, Plant var2) {
      this.player = var1;
      this.plant = var2;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Plant getPlant() {
      return this.plant;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean var1) {
      this.cancel = var1;
   }
}
