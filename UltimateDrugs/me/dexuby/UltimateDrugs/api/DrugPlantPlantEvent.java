package me.dexuby.UltimateDrugs.api;

import java.util.UUID;
import me.dexuby.UltimateDrugs.block.SpaceReport;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrugPlantPlantEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private Player player;
   private Drug drug;
   private Block baseBlock;
   private GrowingStage growingStage;
   private Direction direction;
   private UUID plantUUID;
   private SpaceReport spaceReport;

   public DrugPlantPlantEvent(Player var1, Drug var2, Block var3, GrowingStage var4, Direction var5, UUID var6, SpaceReport var7) {
      this.player = var1;
      this.drug = var2;
      this.baseBlock = var3;
      this.growingStage = var4;
      this.direction = var5;
      this.plantUUID = var6;
      this.spaceReport = var7;
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

   public GrowingStage getGrowingStage() {
      return this.growingStage;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public UUID getPlantUUID() {
      return this.plantUUID;
   }

   public SpaceReport getSpaceReport() {
      return this.spaceReport;
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
