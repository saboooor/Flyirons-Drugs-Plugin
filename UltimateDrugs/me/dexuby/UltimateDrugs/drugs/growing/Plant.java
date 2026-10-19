package me.dexuby.UltimateDrugs.drugs.growing;

import java.util.List;
import java.util.UUID;
import org.bukkit.block.Block;

public class Plant {
   private UUID uuid;
   private UUID owner;
   private String parentDrugId;
   private Block baseBlock;
   private Direction placeDirection;
   private Block foundationBlock;
   private List<Block> currentBlocks;
   private GrowingStage currentGrowingStage;
   private int timeGrown;

   public Plant(UUID var1, UUID var2, String var3, Block var4, Direction var5, Block var6, List<Block> var7, GrowingStage var8, int var9) {
      this.uuid = var1;
      this.owner = var2;
      this.parentDrugId = var3;
      this.baseBlock = var4;
      this.placeDirection = var5;
      this.foundationBlock = var6;
      this.currentBlocks = var7;
      this.currentGrowingStage = var8;
      this.timeGrown = var9;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public UUID getOwner() {
      return this.owner;
   }

   public String getParentDrugId() {
      return this.parentDrugId;
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public Direction getPlaceDirection() {
      return this.placeDirection;
   }

   public Block getFoundationBlock() {
      return this.foundationBlock;
   }

   public void setFoundationBlock(Block var1) {
      this.foundationBlock = var1;
   }

   public List<Block> getCurrentBlocks() {
      return this.currentBlocks;
   }

   public GrowingStage getCurrentGrowingStage() {
      return this.currentGrowingStage;
   }

   public void setCurrentGrowingStage(GrowingStage var1) {
      this.currentGrowingStage = var1;
   }

   public int getTimeGrown() {
      return this.timeGrown;
   }

   public void addTimeGrown() {
      ++this.timeGrown;
   }

   public void setTimeGrown(int var1) {
      this.timeGrown = var1;
   }
}
