package me.dexuby.UltimateDrugs.drugs.growing;

import java.util.Collection;
import java.util.List;
import me.dexuby.UltimateDrugs.block.BlockOption;
import org.bukkit.block.Biome;

public class PlantType {
   private List<GrowingStage> growingStages;
   private boolean removeIfFlying;
   private boolean waterRequired;
   private boolean lavaRequired;
   private Collection<Biome> biomeWhitelist;
   private WorldTime requiredWorldTime;
   private Collection<Weather> weatherWhitelist;
   private Collection<BlockOption> plantBlockWhitelist;
   private Collection<BlockOption> growBlockWhitelist;

   public PlantType(List<GrowingStage> var1, boolean var2, boolean var3, boolean var4, Collection<Biome> var5, WorldTime var6, Collection<Weather> var7, Collection<BlockOption> var8, Collection<BlockOption> var9) {
      this.growingStages = var1;
      this.removeIfFlying = var2;
      this.waterRequired = var3;
      this.lavaRequired = var4;
      this.biomeWhitelist = var5;
      this.requiredWorldTime = var6;
      this.weatherWhitelist = var7;
      this.plantBlockWhitelist = var8;
      this.growBlockWhitelist = var9;
   }

   public List<GrowingStage> getGrowingStages() {
      return this.growingStages;
   }

   public boolean doRemoveIfFlying() {
      return this.removeIfFlying;
   }

   public boolean isWaterRequired() {
      return this.waterRequired;
   }

   public boolean isLavaRequired() {
      return this.lavaRequired;
   }

   public Collection<Biome> getBiomeWhitelist() {
      return this.biomeWhitelist;
   }

   public WorldTime getRequiredWorldTime() {
      return this.requiredWorldTime;
   }

   public Collection<Weather> getWeatherWhitelist() {
      return this.weatherWhitelist;
   }

   public Collection<BlockOption> getPlantBlockWhitelist() {
      return this.plantBlockWhitelist;
   }

   public Collection<BlockOption> getGrowBlockWhitelist() {
      return this.growBlockWhitelist;
   }
}
