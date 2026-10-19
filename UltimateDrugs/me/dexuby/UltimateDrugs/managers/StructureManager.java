package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.structures.Structure;
import me.dexuby.UltimateDrugs.structures.StructureType;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StructureManager {
   private final Main main;
   private final Collection<StructureType> structureTypes = new ArrayList();
   private final Collection<Structure> structures = new ArrayList();

   public StructureManager(Main var1) {
      this.main = var1;
   }

   public StructureType getStructureTypeByItemStack(ItemStack var1) {
      return (StructureType)this.structureTypes.stream().filter((var1x) -> var1x.getSpawnItem().isSimilar(var1)).findFirst().orElse((Object)null);
   }

   public Structure getStructureByInventory(Inventory var1) {
      return (Structure)this.structures.stream().filter((var1x) -> var1x.getInventory() != null && var1x.getInventory().equals(var1)).findFirst().orElse((Object)null);
   }

   public Structure getStructureByBlock(Block var1) {
      return (Structure)this.structures.stream().filter((var1x) -> var1x.getBlockList().contains(var1)).findFirst().orElse((Object)null);
   }

   public void addStructureType(StructureType var1) {
      this.structureTypes.add(var1);
   }

   public void removeStructureType(StructureType var1) {
      this.structureTypes.remove(var1);
   }

   public void addStructure(Structure var1) {
      this.structures.add(var1);
   }

   public void removeStructure(Structure var1) {
      var1.removeBlocks(this.main);
      this.structures.remove(var1);
   }
}
