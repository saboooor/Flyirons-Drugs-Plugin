package me.dexuby.UltimateDrugs.drugs.growing;

import java.util.Collection;
import java.util.List;
import me.dexuby.UltimateDrugs.drugs.Drop;

public class GrowingStage {
   private int orderId;
   private boolean grown;
   private int growDelay;
   private float mcMmoBonusXp;
   private boolean fertilizerRequired;
   private int requiredBlockCount;
   private List<GrowingBlock> growingBlocks;
   private int dropLimit;
   private Collection<Drop> drops;

   public GrowingStage(int var1, boolean var2, int var3, float var4, boolean var5, int var6, List<GrowingBlock> var7, int var8, Collection<Drop> var9) {
      this.orderId = var1;
      this.grown = var2;
      this.growDelay = var3;
      this.mcMmoBonusXp = var4;
      this.fertilizerRequired = var5;
      this.requiredBlockCount = var6;
      this.growingBlocks = var7;
      this.dropLimit = var8;
      this.drops = var9;
   }

   public int getOrderId() {
      return this.orderId;
   }

   public boolean isGrown() {
      return this.grown;
   }

   public int getGrowDelay() {
      return this.growDelay;
   }

   public float getMcMmoBonusXp() {
      return this.mcMmoBonusXp;
   }

   public boolean isFertilizerRequired() {
      return this.fertilizerRequired;
   }

   public int getRequiredBlockCount() {
      return this.requiredBlockCount;
   }

   public List<GrowingBlock> getGrowingBlocks() {
      return this.growingBlocks;
   }

   public int getDropLimit() {
      return this.dropLimit;
   }

   public Collection<Drop> getDrops() {
      return this.drops;
   }
}
