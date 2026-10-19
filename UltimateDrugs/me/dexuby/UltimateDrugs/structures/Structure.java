package me.dexuby.UltimateDrugs.structures;

import java.util.Set;
import me.dexuby.UltimateDrugs.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

public class Structure {
   private final StructureType structureType;
   private final Set<Block> blockList;
   private final Inventory inventory;

   public Structure(StructureType var1, Set<Block> var2, Inventory var3) {
      this.structureType = var1;
      this.blockList = var2;
      this.inventory = var3;
   }

   public StructureType getStructureType() {
      return this.structureType;
   }

   public Set<Block> getBlockList() {
      return this.blockList;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public void removeBlocks(Main var1) {
      for(Block var3 : this.blockList) {
         var3.removeMetadata("ultimatedrugs.structure-block", var1);
         var3.setType(Material.AIR);
      }

      this.blockList.clear();
   }
}
