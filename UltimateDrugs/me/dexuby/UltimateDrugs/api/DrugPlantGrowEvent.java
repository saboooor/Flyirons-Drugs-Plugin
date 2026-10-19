package me.dexuby.UltimateDrugs.api;

import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugPlantGrowEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private Plant plant;
   private GrowingStage nextGrowingStage;

   public DrugPlantGrowEvent(Plant var1, GrowingStage var2) {
      this.plant = var1;
      this.nextGrowingStage = var2;
   }

   public Plant getPlant() {
      return this.plant;
   }

   public GrowingStage getNextGrowingStage() {
      return this.nextGrowingStage;
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
