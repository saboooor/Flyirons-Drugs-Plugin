package me.dexuby.UltimateDrugs.drugs.vanilla;

import java.util.Collection;
import java.util.Set;
import me.dexuby.UltimateDrugs.drugs.Drop;
import org.bukkit.block.Biome;

public class BlockVanillaDrop extends VanillaDrop {
   private boolean overrideDrops;
   private boolean overrideExp;
   private int experience;
   private boolean ignoreDrugPlants;
   private Collection<VanillaDropBlock> blocks;

   public BlockVanillaDrop(String var1, int var2, Set<Biome> var3, Collection<Drop> var4, boolean var5, boolean var6, int var7, boolean var8, Collection<VanillaDropBlock> var9) {
      super(var1, var2, var3, var4);
      this.overrideDrops = var5;
      this.overrideExp = var6;
      this.experience = var7;
      this.ignoreDrugPlants = var8;
      this.blocks = var9;
   }

   public boolean doOverrideDrops() {
      return this.overrideDrops;
   }

   public boolean doOverrideExp() {
      return this.overrideExp;
   }

   public int getExperience() {
      return this.experience;
   }

   public boolean doIgnoreDrugPlants() {
      return this.ignoreDrugPlants;
   }

   public Collection<VanillaDropBlock> getBlocks() {
      return this.blocks;
   }
}
