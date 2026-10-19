package me.dexuby.UltimateDrugs.drugs.growing.processors;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugPlantGrowEvent;
import me.dexuby.UltimateDrugs.block.BlockOption;
import me.dexuby.UltimateDrugs.block.SpaceReport;
import me.dexuby.UltimateDrugs.block.SpaceReportType;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.Growing;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.drugs.growing.PlantType;
import me.dexuby.UltimateDrugs.drugs.growing.Weather;
import me.dexuby.UltimateDrugs.drugs.growing.WorldTime;
import me.dexuby.UltimateDrugs.managers.PlantManager;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import me.dexuby.UltimateDrugs.utils.Constants;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.metadata.FixedMetadataValue;

public class SyncPlantProcessor extends PlantProcessor {
   private final Main mainInstance;
   private final PlantManager plantManager;
   private final SecureRandom random = new SecureRandom();

   public SyncPlantProcessor(Main var1) {
      super(var1);
      this.mainInstance = var1;
      this.plantManager = var1.getPlantManager();
   }

   public void start() {
      for(Plant var2 : this.getPlants()) {
         if (!this.plantManager.getToRemove().contains(var2.getUUID())) {
            Drug var3 = this.mainInstance.getDrugManager().getDrugById(var2.getParentDrugId());
            if (var3 != null) {
               Growing var4 = var3.getGrowing();
               PlantType var5 = var4.getPlantType();
               Block var6 = var2.getBaseBlock();
               boolean var7 = this.fulfillsGrowConditions(var2, var5, var6);
               if (var7) {
                  if (var2.getTimeGrown() >= var2.getCurrentGrowingStage().getGrowDelay()) {
                     GrowingStage var8 = this.plantManager.getNextGrowingStage(var2, var5);
                     SpaceReport var9 = (new SpaceReport.Creator()).withGrowingStage(var8).withDirection(var2.getPlaceDirection()).withBaseBlock(var6).doIgnorePlantBlocks(true).create(SpaceReportType.GROWING);
                     if (var9.hasSpace()) {
                        HashMap var10 = new HashMap();

                        for(GrowingBlock var12 : var8.getGrowingBlocks()) {
                           Block var13 = BlockUtils.getRelativeBlock(var2.getPlaceDirection(), var12.getBlockOffset(), var6);
                           if (var9.getFreeBlocks().contains(var13)) {
                              var10.put(var13.getLocation(), var12);
                           }
                        }

                        DrugPlantGrowEvent var14 = new DrugPlantGrowEvent(var2, var8);
                        this.mainInstance.getServer().getPluginManager().callEvent(var14);
                        if (var14.isCancelled()) {
                           return;
                        }

                        this.removeCurrentPlantBlocks(var2);
                        this.placeNewPlantBlocks(var2, var10);
                        if (var8.isGrown()) {
                           this.plantManager.getToRemove().add(var2.getUUID());
                        }

                        var2.setCurrentGrowingStage(var8);
                        var2.setTimeGrown(0);
                        this.mainInstance.getConfigManager().savePlant(var2);
                     }
                  } else if (!var2.getCurrentGrowingStage().isFertilizerRequired()) {
                     var2.addTimeGrown();
                     if (var4.getRandomGrowChance() > (double)0.0F && this.random.nextDouble() <= var4.getRandomGrowChance() / (double)100.0F) {
                        var2.addTimeGrown();
                     }
                  }
               }
            }
         }
      }

   }

   private boolean fulfillsGrowConditions(Plant var1, PlantType var2, Block var3) {
      boolean var4 = this.hasRequiredLiquids(var2, var3);
      if (!var4) {
         return false;
      } else {
         var4 = this.isValidBiome(var2, var3.getBiome());
         if (!var4) {
            return false;
         } else {
            var4 = this.isValidWorldTime(var2, var3.getWorld());
            if (!var4) {
               return false;
            } else {
               var4 = this.isValidWeather(var2, var3.getWorld());
               if (!var4) {
                  return false;
               } else {
                  var4 = this.hasProperGrowBlock(var1, var2, var3);
                  return var4;
               }
            }
         }
      }
   }

   private boolean hasRequiredLiquids(PlantType var1, Block var2) {
      if (var1.isWaterRequired() && !this.hasWater(var2)) {
         return false;
      } else {
         return !var1.isLavaRequired() || this.hasLava(var2);
      }
   }

   private boolean hasWater(Block var1) {
      for(int var2 = var1.getX() - 2; var2 <= var1.getX() + 2; ++var2) {
         for(int var3 = var1.getY() - 1; var3 <= var1.getY() + 1; ++var3) {
            for(int var4 = var1.getZ() - 2; var4 <= var1.getZ() + 2; ++var4) {
               Block var5 = var1.getWorld().getBlockAt(var2, var3, var4);
               if (var5.getType() == Material.WATER) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean hasLava(Block var1) {
      for(int var2 = var1.getX() - 2; var2 <= var1.getX() + 2; ++var2) {
         for(int var3 = var1.getY() - 1; var3 <= var1.getY() + 1; ++var3) {
            for(int var4 = var1.getZ() - 2; var4 <= var1.getZ() + 2; ++var4) {
               Block var5 = var1.getWorld().getBlockAt(var2, var3, var4);
               if (var5.getType() == Material.LAVA) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isValidBiome(PlantType var1, Biome var2) {
      return var1.getBiomeWhitelist().isEmpty() ? true : var1.getBiomeWhitelist().contains(var2);
   }

   private boolean isValidWorldTime(PlantType var1, World var2) {
      if (var1.getRequiredWorldTime() == null) {
         return true;
      } else {
         long var3 = var2.getTime();
         WorldTime var5 = var3 >= 12300L && var3 <= 23850L ? WorldTime.NIGHT : WorldTime.DAY;
         return var1.getRequiredWorldTime() == var5;
      }
   }

   private boolean isValidWeather(PlantType var1, World var2) {
      if (var1.getWeatherWhitelist().isEmpty()) {
         return true;
      } else {
         Weather var3;
         if (var2.isThundering()) {
            var3 = Weather.THUNDER;
         } else if (var2.hasStorm()) {
            var3 = Weather.RAIN;
         } else {
            var3 = Weather.CLEAR;
         }

         return var1.getWeatherWhitelist().contains(var3);
      }
   }

   private boolean hasProperGrowBlock(Plant var1, PlantType var2, Block var3) {
      if (var2.getGrowBlockWhitelist().isEmpty()) {
         return true;
      } else {
         boolean var4 = false;

         for(BlockOption var6 : var2.getGrowBlockWhitelist()) {
            if (!var4 || var6.isRequired()) {
               Block var7 = BlockUtils.getRelativeBlock(var1.getPlaceDirection(), var6.getBlockOffset(), var3);
               if (var7.getBlockData().matches(var6.getBlockData())) {
                  var4 = true;
               } else if (var6.isRequired()) {
                  return false;
               }
            }
         }

         return var4;
      }
   }

   private void removeCurrentPlantBlocks(Plant var1) {
      for(Block var3 : var1.getCurrentBlocks()) {
         var3.removeMetadata("ultimatedrugs-plant", this.mainInstance);
         var3.setType(Material.AIR, false);
      }

      var1.getCurrentBlocks().clear();
   }

   private void placeNewPlantBlocks(Plant var1, Map<Location, GrowingBlock> var2) {
      for(Map.Entry var4 : var2.entrySet()) {
         Location var5 = (Location)var4.getKey();
         GrowingBlock var6 = (GrowingBlock)var4.getValue();
         Block var7 = var5.getBlock();
         var7.setBlockData(var6.getBlockData(), var6.doApplyPhysics());
         if (var6.getSkullTexture() != null) {
            ReflectionUtils.setSkullBlockTexture(var7, var6.getSkullTexture());
         }

         if (var6.useRandomRotation()) {
            if (var7.getBlockData() instanceof Rotatable) {
               Rotatable var8 = (Rotatable)var7.getBlockData();
               var8.setRotation(BlockUtils.getRandomRotatableBlockFace());
               var7.setBlockData(var8);
            } else if (var7.getBlockData() instanceof Directional) {
               Directional var13 = (Directional)var7.getBlockData();
               var13.setFacing(BlockUtils.getRandomDirectionalBlockFace());
               var7.setBlockData(var13);
            }
         }

         if (var6.doAutoConnect() && Constants.AUTO_CONNECT_MATERIALS.contains(var7.getType()) && var7.getBlockData() instanceof Directional) {
            Directional var14 = (Directional)var7.getBlockData();
            ArrayList var9 = new ArrayList();

            for(int var10 = var7.getX() - 1; var10 <= var7.getX() + 1; ++var10) {
               if (var10 != var7.getX()) {
                  var9.add(var7.getWorld().getBlockAt(var10, var7.getY(), var7.getZ()));
               }
            }

            for(int var15 = var7.getZ() - 1; var15 <= var7.getZ() + 1; ++var15) {
               if (var15 != var7.getZ()) {
                  var9.add(var7.getWorld().getBlockAt(var7.getX(), var7.getY(), var15));
               }
            }

            for(Block var11 : var9) {
               if (var7.getType() == Material.COCOA && var11.getType().name().endsWith("JUNGLE_LOG") || var7.getType() == Material.ATTACHED_PUMPKIN_STEM && var11.getType() == Material.PUMPKIN || var7.getType() == Material.ATTACHED_MELON_STEM && var11.getType() == Material.MELON) {
                  BlockFace var12 = var7.getFace(var11);
                  if (var12 != null) {
                     var14.setFacing(var12);
                  }

                  var7.setBlockData(var14);
                  var1.setFoundationBlock(var11);
                  break;
               }
            }
         }

         var1.getCurrentBlocks().add(var7);
      }

      var1.getCurrentBlocks().forEach((var2x) -> var2x.setMetadata("ultimatedrugs-plant", new FixedMetadataValue(this.mainInstance, var1.getUUID().toString())));
   }
}
