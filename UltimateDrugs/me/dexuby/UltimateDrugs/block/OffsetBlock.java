package me.dexuby.UltimateDrugs.block;

import org.bukkit.block.data.BlockData;

public abstract class OffsetBlock {
   private BlockData blockData;
   private BlockOffset blockOffset;

   public OffsetBlock(BlockData var1, BlockOffset var2) {
      this.blockData = var1;
      this.blockOffset = var2;
   }

   public BlockData getBlockData() {
      return this.blockData;
   }

   public BlockOffset getBlockOffset() {
      return this.blockOffset;
   }
}
