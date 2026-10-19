package me.dexuby.UltimateDrugs.drugs.vanilla;

import org.bukkit.block.data.BlockData;

public class VanillaDropBlock {
   private BlockData blockData;
   private boolean ignoreData;

   public VanillaDropBlock(BlockData var1, boolean var2) {
      this.blockData = var1;
      this.ignoreData = var2;
   }

   public BlockData getBlockData() {
      return this.blockData;
   }

   public boolean doIgnoreData() {
      return this.ignoreData;
   }
}
