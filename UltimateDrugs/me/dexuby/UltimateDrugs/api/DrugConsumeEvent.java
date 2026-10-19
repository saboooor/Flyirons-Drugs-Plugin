package me.dexuby.UltimateDrugs.api;

import me.dexuby.UltimateDrugs.drugs.Drug;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugConsumeEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private Player player;
   private Drug drug;

   public DrugConsumeEvent(Player var1, Drug var2) {
      this.player = var1;
      this.drug = var2;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Drug getDrug() {
      return this.drug;
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
