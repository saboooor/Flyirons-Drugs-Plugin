package me.dexuby.UltimateDrugs.api;

import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugPlantReplantEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private Plant plant;
   private Player player;

   public DrugPlantReplantEvent(Plant var1, Player var2) {
      this.plant = var1;
      this.player = var2;
   }

   public Plant getPlant() {
      return this.plant;
   }

   public Player getPlayer() {
      return this.player;
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
