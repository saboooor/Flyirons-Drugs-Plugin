package me.dexuby.UltimateDrugs.drugs.growing.processors;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugPlantGrowEvent;
import me.dexuby.UltimateDrugs.block.BlockOption;
import me.dexuby.UltimateDrugs.block.SpaceReport;
import me.dexuby.UltimateDrugs.block.SpaceReportType;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.ChunkLocation;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.drugs.growing.PlantType;
import me.dexuby.UltimateDrugs.drugs.growing.Weather;
import me.dexuby.UltimateDrugs.drugs.growing.WorldTime;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.managers.PlantManager;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import me.dexuby.UltimateDrugs.utils.Constants;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.metadata.FixedMetadataValue;

public class DefaultPlantProcessor extends PlantProcessor {
   private final Main main;
   private final PlantManager plantManager;
   private final DrugManager drugManager;

   public DefaultPlantProcessor(Main var1) {
      super(var1);
      this.main = var1;
      this.plantManager = var1.getPlantManager();
      this.drugManager = var1.getDrugManager();
   }

   public void start() {
      HashMap var1 = new HashMap();
      Queue var2 = this.getPlants();
      SecureRandom var3 = new SecureRandom();
      var2.forEach((var2x) -> this.collectChunkSnapshots(var2x, var1));
      this.main.getServer().getScheduler().runTaskAsynchronously(this.main, () -> {
         for(Plant var5 : var2) {
            if (!this.plantManager.getToRemove().contains(var5.getUUID())) {
               boolean var6 = true;
               Block var7 = var5.getBaseBlock();
               Drug var8 = this.drugManager.getDrugById(var5.getParentDrugId());
               if (var8 != null) {
                  PlantType var9 = var8.getGrowing().getPlantType();
                  if (var9.isWaterRequired()) {
                     boolean var10 = false;

                     for(int var11 = var7.getX() - 2; var11 <= var7.getX() + 2; ++var11) {
                        for(int var12 = var7.getY() - 1; var12 <= var7.getY() + 1; ++var12) {
                           for(int var13 = var7.getZ() - 2; var13 <= var7.getZ() + 2; ++var13) {
                              ChunkSnapshot var14 = this.getChunkSnapshotByChunkLocation(new ChunkLocation(var7.getWorld(), var11 >> 4, var13 >> 4), var1);
                              Material var15 = var14.getBlockType(Math.floorMod(var11, 16), var12, Math.floorMod(var13, 16));
                              if (var15 == Material.WATER) {
                                 var10 = true;
                              }
                           }
                        }
                     }

                     if (!var10) {
                        var6 = false;
                     }
                  }

                  if (var6 && var9.isLavaRequired()) {
                     boolean var16 = false;

                     for(int var18 = var7.getX() - 2; var18 <= var7.getX() + 2; ++var18) {
                        for(int var20 = var7.getY() - 1; var20 <= var7.getY() + 1; ++var20) {
                           for(int var22 = var7.getZ() - 2; var22 <= var7.getZ() + 2; ++var22) {
                              ChunkSnapshot var24 = this.getChunkSnapshotByChunkLocation(new ChunkLocation(var7.getWorld(), var18 >> 4, var22 >> 4), var1);
                              Material var26 = var24.getBlockType(Math.floorMod(var18, 16), var20, Math.floorMod(var22, 16));
                              if (var26 == Material.LAVA) {
                                 var16 = true;
                              }
                           }
                        }
                     }

                     if (!var16) {
                        var6 = false;
                     }
                  }

                  if (var6) {
                     if (var5.getTimeGrown() >= var5.getCurrentGrowingStage().getGrowDelay()) {
                        GrowingStage var17 = this.plantManager.getNextGrowingStage(var5, var9);
                        HashMap var19 = new HashMap();
                        SpaceReport var21 = (new SpaceReport.Creator()).withGrowingStage(var17).withDirection(var5.getPlaceDirection()).withBaseBlock(var7).doIgnorePlantBlocks(true).create(SpaceReportType.GROWING);

                        for(GrowingBlock var25 : var17.getGrowingBlocks()) {
                           Block var27 = BlockUtils.getRelativeBlock(var5.getPlaceDirection(), var25.getBlockOffset(), var7);
                           if (var21.getFreeBlocks().contains(var27)) {
                              var19.put(var27.getLocation(), var25);
                           }
                        }

                        if (var21.hasSpace()) {
                           this.main.getServer().getScheduler().runTask(this.main, () -> {
                              if (this.fulfillsGrowConditions(var5, var9)) {
                                 DrugPlantGrowEvent var5x = new DrugPlantGrowEvent(var5, var17);
                                 this.main.getServer().getPluginManager().callEvent(var5x);
                                 if (var5x.isCancelled() || this.plantManager.getToRemove().contains(var5.getUUID())) {
                                    return;
                                 }

                                 for(Block var7 : var5.getCurrentBlocks()) {
                                    var7.removeMetadata("ultimatedrugs-plant", this.main);
                                    var7.setType(Material.AIR, false);
                                 }

                                 var5.getCurrentBlocks().clear();

                                 for(Map.Entry var16 : var19.entrySet()) {
                                    Location var8 = (Location)var16.getKey();
                                    Block var9x = var8.getBlock();
                                    var9x.setBlockData(((GrowingBlock)var16.getValue()).getBlockData(), ((GrowingBlock)var16.getValue()).doApplyPhysics());
                                    if (((GrowingBlock)var16.getValue()).getSkullTexture() != null) {
                                       ReflectionUtils.setSkullBlockTexture(var9x, ((GrowingBlock)var16.getValue()).getSkullTexture());
                                    }

                                    if (((GrowingBlock)var16.getValue()).useRandomRotation()) {
                                       if (var9x.getBlockData() instanceof Rotatable) {
                                          Rotatable var10 = (Rotatable)var9x.getBlockData();
                                          var10.setRotation(BlockUtils.getRandomRotatableBlockFace());
                                          var9x.setBlockData(var10);
                                       } else if (var9x.getBlockData() instanceof Directional) {
                                          Directional var17x = (Directional)var9x.getBlockData();
                                          var17x.setFacing(BlockUtils.getRandomDirectionalBlockFace());
                                          var9x.setBlockData(var17x);
                                       }
                                    }

                                    if (((GrowingBlock)var16.getValue()).doAutoConnect() && Constants.AUTO_CONNECT_MATERIALS.contains(var9x.getType()) && var9x.getBlockData() instanceof Directional) {
                                       Directional var18 = (Directional)var9x.getBlockData();
                                       ArrayList var11 = new ArrayList();

                                       for(int var12 = var9x.getX() - 1; var12 <= var9x.getX() + 1; ++var12) {
                                          if (var12 != var9x.getX()) {
                                             var11.add(var9x.getWorld().getBlockAt(var12, var9x.getY(), var9x.getZ()));
                                          }
                                       }

                                       for(int var19x = var9x.getZ() - 1; var19x <= var9x.getZ() + 1; ++var19x) {
                                          if (var19x != var9x.getZ()) {
                                             var11.add(var9x.getWorld().getBlockAt(var9x.getX(), var9x.getY(), var19x));
                                          }
                                       }

                                       for(Block var13 : var11) {
                                          if (var9x.getType() == Material.COCOA && var13.getType().name().endsWith("JUNGLE_LOG") || var9x.getType() == Material.ATTACHED_PUMPKIN_STEM && var13.getType() == Material.PUMPKIN || var9x.getType() == Material.ATTACHED_MELON_STEM && var13.getType() == Material.MELON) {
                                             BlockFace var14 = var9x.getFace(var13);
                                             if (var14 != null) {
                                                var18.setFacing(var14);
                                             }

                                             var9x.setBlockData(var18);
                                             var5.setFoundationBlock(var13);
                                             break;
                                          }
                                       }
                                    }

                                    var5.getCurrentBlocks().add(var9x);
                                 }

                                 var5.getCurrentBlocks().forEach((var2) -> var2.setMetadata("ultimatedrugs-plant", new FixedMetadataValue(this.main, var5.getUUID().toString())));
                                 if (var17.isGrown()) {
                                    this.plantManager.getToRemove().add(var5.getUUID());
                                 }

                                 var5.setCurrentGrowingStage(var17);
                                 var5.setTimeGrown(0);
                                 this.main.getConfigManager().savePlant(var5);
                              }

                           });
                        }
                     } else {
                        this.main.getServer().getScheduler().runTask(this.main, () -> {
                           if (!var5.getCurrentGrowingStage().isFertilizerRequired()) {
                              if (this.fulfillsGrowConditions(var5, var9)) {
                                 var5.addTimeGrown();
                                 if (var8.getGrowing().getRandomGrowChance() > (double)0.0F && var3.nextDouble() <= var8.getGrowing().getRandomGrowChance() / (double)100.0F) {
                                    var5.addTimeGrown();
                                 }
                              }

                           }
                        });
                     }
                  }
               }
            }
         }

      });
   }

   private ChunkSnapshot getChunkSnapshotByChunkLocation(ChunkLocation var1, Map<ChunkLocation, ChunkSnapshot> var2) {
      return (ChunkSnapshot)var2.entrySet().stream().filter((var1x) -> ((ChunkLocation)var1x.getKey()).toString().equals(var1.toString())).findFirst().map(Map.Entry::getValue).orElse((Object)null);
   }

   private void collectChunkSnapshots(Plant var1, Map<ChunkLocation, ChunkSnapshot> var2) {
      Chunk var3 = var1.getBaseBlock().getChunk();

      for(int var4 = var3.getX() - 1; var4 <= var3.getX() + 1; ++var4) {
         for(int var5 = var3.getZ() - 1; var5 <= var3.getZ() + 1; ++var5) {
            Chunk var6 = var3.getWorld().getChunkAt(var4, var5);
            boolean var7 = var6.isLoaded();
            if (!var7) {
               var6.load();
            }

            ChunkLocation var8 = new ChunkLocation(var1.getBaseBlock().getWorld(), var6.getX(), var6.getZ());
            if (var2.keySet().stream().noneMatch((var1x) -> var1x.toString().equals(var8.toString()))) {
               var2.put(var8, var6.getChunkSnapshot());
            }

            if (!var7) {
               var6.unload();
            }
         }
      }

   }

   private boolean fulfillsGrowConditions(Plant var1, PlantType var2) {
      Block var3 = var1.getBaseBlock();
      boolean var4 = true;
      if (!var2.getBiomeWhitelist().isEmpty() && !var2.getBiomeWhitelist().contains(var3.getBiome())) {
         var4 = false;
      }

      if (var2.getRequiredWorldTime() != null) {
         long var5 = var3.getWorld().getTime();
         WorldTime var7 = var5 >= 12300L && var5 <= 23850L ? WorldTime.NIGHT : WorldTime.DAY;
         if (!var7.equals(var2.getRequiredWorldTime())) {
            var4 = false;
         }
      }

      if (!var2.getWeatherWhitelist().isEmpty()) {
         Weather var9;
         if (var3.getWorld().isThundering()) {
            var9 = Weather.THUNDER;
         } else if (var3.getWorld().hasStorm()) {
            var9 = Weather.RAIN;
         } else {
            var9 = Weather.CLEAR;
         }

         if (!var2.getWeatherWhitelist().contains(var9)) {
            var4 = false;
         }
      }

      if (!var2.getGrowBlockWhitelist().isEmpty()) {
         boolean var10 = false;

         for(BlockOption var11 : var2.getGrowBlockWhitelist()) {
            if (!var10 || var11.isRequired()) {
               Block var8 = BlockUtils.getRelativeBlock(var1.getPlaceDirection(), var11.getBlockOffset(), var3);
               if (var8.getBlockData().matches(var11.getBlockData())) {
                  var10 = true;
               } else if (var11.isRequired()) {
                  var10 = false;
                  break;
               }
            }
         }

         if (!var10) {
            var4 = false;
         }
      }

      return var4;
   }
}
