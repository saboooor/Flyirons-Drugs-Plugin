package me.dexuby.UltimateDrugs.drugs.growing;

import org.bukkit.inventory.ItemStack;

public class Growing {
   private boolean autoReplant;
   private boolean replantSeedRequired;
   private int replantStage;
   private double randomGrowChance;
   private boolean ignoreChunkState;
   private PlantType plantType;
   private ItemStack seedItemStack;

   public Growing(boolean var1, boolean var2, int var3, double var4, boolean var6, PlantType var7, ItemStack var8) {
      this.autoReplant = var1;
      this.replantSeedRequired = var2;
      this.replantStage = var3;
      this.randomGrowChance = var4;
      this.ignoreChunkState = var6;
      this.plantType = var7;
      this.seedItemStack = var8;
   }

   public boolean doAutoReplant() {
      return this.autoReplant;
   }

   public boolean isReplantSeedRequired() {
      return this.replantSeedRequired;
   }

   public int getReplantStage() {
      return this.replantStage;
   }

   public double getRandomGrowChance() {
      return this.randomGrowChance;
   }

   public boolean doIgnoreChunkState() {
      return this.ignoreChunkState;
   }

   public PlantType getPlantType() {
      return this.plantType;
   }

   public ItemStack getSeedItemStack() {
      return this.seedItemStack;
   }
}
