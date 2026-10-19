package me.dexuby.UltimateDrugs.structures;

import java.util.List;
import me.dexuby.UltimateDrugs.block.BlockOffset;
import me.dexuby.UltimateDrugs.block.OffsetBlock;
import org.bukkit.inventory.ItemStack;

public class StructureType {
   private final ItemStack spawnItem;
   private final List<OffsetBlock> blockList;
   private final BlockOffset foundationBlockOffset;

   public StructureType(ItemStack var1, List<OffsetBlock> var2) {
      this.spawnItem = var1;
      this.blockList = var2;
      this.foundationBlockOffset = null;
   }

   public StructureType(ItemStack var1, List<OffsetBlock> var2, BlockOffset var3) {
      this.spawnItem = var1;
      this.blockList = var2;
      this.foundationBlockOffset = var3;
   }

   public ItemStack getSpawnItem() {
      return this.spawnItem;
   }

   public List<OffsetBlock> getBlockList() {
      return this.blockList;
   }

   public BlockOffset getFoundationBlockOffset() {
      return this.foundationBlockOffset;
   }
}
