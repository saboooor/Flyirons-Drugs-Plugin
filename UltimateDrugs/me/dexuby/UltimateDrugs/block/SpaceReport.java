package me.dexuby.UltimateDrugs.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import me.dexuby.UltimateDrugs.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class SpaceReport {
   private final boolean space;
   private final Set<Block> freeBlocks;

   public SpaceReport(boolean var1, Set<Block> var2) {
      this.space = var1;
      this.freeBlocks = var2;
   }

   public boolean hasSpace() {
      return this.space;
   }

   public Set<Block> getFreeBlocks() {
      return this.freeBlocks;
   }

   public static class Creator {
      private GrowingStage growingStage;
      private List<OffsetBlock> blockList;
      private Direction direction;
      private Block baseBlock;
      private boolean ignorePlantBlocks;

      public Creator withGrowingStage(GrowingStage var1) {
         this.growingStage = var1;
         return this;
      }

      public Creator withBlockList(List<OffsetBlock> var1) {
         this.blockList = var1;
         return this;
      }

      public Creator withDirection(Direction var1) {
         this.direction = var1;
         return this;
      }

      public Creator withBaseBlock(Block var1) {
         this.baseBlock = var1;
         return this;
      }

      public Creator doIgnorePlantBlocks(boolean var1) {
         this.ignorePlantBlocks = var1;
         return this;
      }

      public SpaceReport create(SpaceReportType var1) {
         return var1 != SpaceReportType.GROWING ? this.generateSpaceReport() : this.generateGrowingSpaceReport();
      }

      private SpaceReport generateSpaceReport() {
         boolean var1 = true;
         HashSet var2 = new HashSet();

         for(OffsetBlock var4 : this.blockList) {
            Block var5 = BlockUtils.getRelativeBlock(this.direction, var4.getBlockOffset(), this.baseBlock);
            if (!MaterialUtils.isAir(var5.getType())) {
               var1 = false;
            } else {
               var2.add(var5);
            }
         }

         return new SpaceReport(var1, var2);
      }

      private SpaceReport generateGrowingSpaceReport() {
         boolean var1 = true;
         int var2 = 0;
         HashSet var3 = new HashSet();

         for(GrowingBlock var5 : this.growingStage.getGrowingBlocks()) {
            if (this.growingStage.getRequiredBlockCount() > 0 && var2 >= this.growingStage.getRequiredBlockCount()) {
               break;
            }

            Block var6 = BlockUtils.getRelativeBlock(this.direction, var5.getBlockOffset(), this.baseBlock);
            Material var7 = var6.getType();
            if (var7 != Material.AIR && var7 != Material.CAVE_AIR && var7 != Material.VOID_AIR && !var5.doIgnoreSpace() && (!this.ignorePlantBlocks || !var6.hasMetadata("ultimatedrugs-plant"))) {
               if (this.growingStage.getRequiredBlockCount() == 0) {
                  var1 = false;
                  break;
               }
            } else {
               var3.add(var6);
               ++var2;
            }
         }

         if (this.growingStage.getRequiredBlockCount() > 0 && var2 < this.growingStage.getRequiredBlockCount()) {
            var1 = false;
         }

         return new SpaceReport(var1, var3);
      }
   }
}
