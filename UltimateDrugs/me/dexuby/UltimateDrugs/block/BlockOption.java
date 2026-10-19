package me.dexuby.UltimateDrugs.block;

import org.bukkit.block.data.BlockData;

public class BlockOption {
   private BlockData blockData;
   private BlockOffset blockOffset;
   private boolean required;

   public BlockOption(BlockData var1, BlockOffset var2, boolean var3) {
      this.blockData = var1;
      this.blockOffset = var2;
      this.required = var3;
   }

   public BlockData getBlockData() {
      return this.blockData;
   }

   public BlockOffset getBlockOffset() {
      return this.blockOffset;
   }

   public boolean isRequired() {
      return this.required;
   }
}
