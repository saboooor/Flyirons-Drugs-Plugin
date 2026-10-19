package me.dexuby.UltimateDrugs.api;

import me.dexuby.UltimateDrugs.drugs.Drug;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugPlantPrePlantEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Player player;
   private Drug drug;
   private Block baseBlock;

   public DrugPlantPrePlantEvent(Player var1, Drug var2, Block var3) {
      this.player = var1;
      this.drug = var2;
      this.baseBlock = var3;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Drug getDrug() {
      return this.drug;
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
