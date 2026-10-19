package me.dexuby.UltimateDrugs.handlers;

import com.gmail.nossr50.api.ExperienceAPI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugPlantBreakEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantFertilizerUseEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantPlantEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantPrePlantEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantReplantEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantSendStatusEvent;
import me.dexuby.UltimateDrugs.block.BlockOption;
import me.dexuby.UltimateDrugs.block.SpaceReport;
import me.dexuby.UltimateDrugs.block.SpaceReportType;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.Drop;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.drugs.growing.Growing;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.drugs.growing.processors.DefaultPlantProcessor;
import me.dexuby.UltimateDrugs.drugs.growing.processors.ProcessorType;
import me.dexuby.UltimateDrugs.drugs.growing.processors.SyncPlantProcessor;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.managers.PlantManager;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import me.dexuby.UltimateDrugs.utils.Constants;
import me.dexuby.UltimateDrugs.utils.EntityUtils;
import me.dexuby.UltimateDrugs.utils.MaterialUtils;
import me.dexuby.UltimateDrugs.utils.NumberUtils;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

public class PlantHandler implements Listener {
   private Main main;
   private PlantManager plantManager;
   private DrugManager drugManager;

   public PlantHandler(Main var1) {
      this.main = var1;
      this.plantManager = var1.getPlantManager();
      this.drugManager = var1.getDrugManager();
      ProcessorType var2 = (ProcessorType)SettingsHolder.PLANT_PROCESSOR_TYPE.getValue();
      Object var3 = var2 == ProcessorType.DEFAULT ? new DefaultPlantProcessor(var1) : new SyncPlantProcessor(var1);
      var1.getServer().getScheduler().runTaskTimerAsynchronously(var1, () -> {
         this.plantManager.updateGrowingPlants();
         final Queue var3x = this.getLoadedPlants();
         if (!var3x.isEmpty()) {
            final int var4 = (int)Math.ceil((double)var3x.size() / (double)20.0F);
            final int[] var5 = new int[]{0};
            (new BukkitRunnable() {
               public void run() {
                  int var1x = 0;

                  LinkedList var2;
                  for(var2 = new LinkedList(); var1x < var4 && !var3.isEmpty(); ++var1x) {
                     var2.add(var3.poll());
                  }

                  var1.setPlants(var2).start();
                  int var10002 = var5[0]++;
                  if (var5[0] >= 20 || var3.isEmpty()) {
                     this.cancel();
                  }

               }
            }).runTaskTimer(var1, 0L, 1L);
         }
      }, 20L, 20L);
   }

   private Queue<Plant> getLoadedPlants() {
      LinkedList var1 = new LinkedList();
      HashMap var2 = new HashMap();

      for(Plant var4 : new ArrayList(this.plantManager.getGrowingPlants())) {
         Growing var5;
         if (var2.containsKey(var4.getParentDrugId())) {
            var5 = (Growing)var2.get(var4.getParentDrugId());
         } else {
            var2.put(var4.getParentDrugId(), this.main.getDrugManager().getDrugById(var4.getParentDrugId()).getGrowing());
            var5 = (Growing)var2.get(var4.getParentDrugId());
         }

         if (var4.getBaseBlock().getWorld().isChunkLoaded(var4.getBaseBlock().getX() >> 4, var4.getBaseBlock().getZ() >> 4) || var5.doIgnoreChunkState()) {
            var1.add(var4);
         }
      }

      return var1;
   }

   public void onDrugPlantBreak(DrugPlantBreakEvent var1) {
      if (!var1.isCancelled()) {
         Plant var2 = var1.getPlant();
         this.destroyPlant(var1.getPlant(), var1.getBlock());
         Drug var3 = this.drugManager.getDrugById(var2.getParentDrugId());
         if (this.main.getPlantManager().getHighestGrowingStage(var3.getGrowing().getPlantType()).getOrderId() == var2.getCurrentGrowingStage().getOrderId() && var3.getGrowing().doAutoReplant() && var1.getPlayer().hasPermission("ultimatedrugs.auto-replant")) {
            this.main.getServer().getPluginManager().callEvent(new DrugPlantReplantEvent(var2, var1.getPlayer()));
         }

         if (var2.getCurrentGrowingStage().getMcMmoBonusXp() != 0.0F && this.main.getServer().getPluginManager().isPluginEnabled("mcMMO")) {
            ExperienceAPI.addRawXP(var1.getPlayer(), "herbalism", var2.getCurrentGrowingStage().getMcMmoBonusXp(), "UNKNOWN");
         }

      }
   }

   @EventHandler
   public void onDrugPlantPrePlant(DrugPlantPrePlantEvent var1) {
      Drug var2 = var1.getDrug();
      Player var3 = var1.getPlayer();
      Block var4 = var1.getBaseBlock();
      ItemStack var5 = var1.getPlayer().getInventory().getItemInMainHand();
      BlockPlaceEvent var6 = new BlockPlaceEvent(var4, var4.getState(), var4.getRelative(BlockFace.DOWN), var5, var3, true, EquipmentSlot.HAND);
      this.main.getServer().getPluginManager().callEvent(var6);
      if (!var6.isCancelled()) {
         boolean var7 = true;
         Direction var8 = EntityUtils.getDirection(var3);
         if (!var2.getGrowing().getPlantType().getPlantBlockWhitelist().isEmpty()) {
            boolean var9 = false;

            for(BlockOption var11 : var2.getGrowing().getPlantType().getPlantBlockWhitelist()) {
               if (!var9 || var11.isRequired()) {
                  Block var12 = BlockUtils.getRelativeBlock(var8, var11.getBlockOffset(), var4);
                  if (var12.getBlockData().matches(var11.getBlockData())) {
                     var9 = true;
                  } else if (var11.isRequired()) {
                     var9 = false;
                     break;
                  }
               }
            }

            if (!var9) {
               var7 = false;
            }
         }

         if (!var2.getGrowing().getPlantType().getBiomeWhitelist().isEmpty() && !var2.getGrowing().getPlantType().getBiomeWhitelist().contains(var4.getBiome())) {
            var7 = false;
         }

         int var17 = (Integer)SettingsHolder.PLAYER_PLANT_LIMIT.getValue();
         if (var17 >= 0 && !var3.hasPermission("ultimatedrugs.plant-limit.bypass") && this.main.getPlantManager().getPlantsByOwner(var3.getUniqueId()).size() >= var17) {
            var7 = false;
         }

         String var18 = null;
         List var19 = (List)var3.getEffectivePermissions().stream().filter((var1x) -> var1x.getPermission().startsWith(String.format("ultimatedrugs.plant-limit.%s.", var2.getId()))).map(PermissionAttachmentInfo::getPermission).collect(Collectors.toList());
         var19.remove(String.format("ultimatedrugs.plant-limit.%s.bypass", var2.getId()));
         int var20 = 0;

         for(String var14 : var19) {
            String var15 = var14.split("\\.")[3];
            if (NumberUtils.isValidInteger(var15)) {
               int var16 = Integer.parseInt(var15);
               if (var16 > var20) {
                  var20 = var16;
                  var18 = var14;
               }
            }
         }

         if (var18 != null && !var3.hasPermission(String.format("ultimatedrugs.plant-limit.%s.bypass", var2.getId()))) {
            long var21 = this.main.getPlantManager().getPlantsByOwner(var3.getUniqueId()).stream().filter((var1x) -> var1x.getParentDrugId().equals(var2.getId())).count();
            if (var21 >= (long)var20) {
               var7 = false;
            }
         }

         if (var7) {
            GrowingStage var22 = (GrowingStage)var1.getDrug().getGrowing().getPlantType().getGrowingStages().stream().filter((var0) -> var0.getOrderId() == 0).findFirst().orElse((Object)null);
            if (var22 != null) {
               SpaceReport var23 = (new SpaceReport.Creator()).withGrowingStage(var22).withDirection(var8).withBaseBlock(var4).doIgnorePlantBlocks(false).create(SpaceReportType.GROWING);
               if (var23.hasSpace()) {
                  this.main.getServer().getPluginManager().callEvent(new DrugPlantPlantEvent(var1.getPlayer(), var1.getDrug(), var1.getBaseBlock(), var22, var8, UUID.randomUUID(), var23));
               }
            }
         }

      }
   }

   @EventHandler
   public void onDrugPlantPlant(DrugPlantPlantEvent var1) {
      if (!var1.isCancelled()) {
         GrowingStage var2 = var1.getGrowingStage();
         Direction var3 = var1.getDirection();
         UUID var4 = var1.getPlantUUID();
         int var5 = var1.getPlayer().getInventory().getHeldItemSlot();
         ItemStack var6 = var1.getPlayer().getInventory().getItem(var5);
         if (var6 != null) {
            if (var1.getPlayer().getGameMode() != GameMode.CREATIVE) {
               if (var6.getAmount() > 1) {
                  var6.setAmount(var6.getAmount() - 1);
               } else {
                  var1.getPlayer().getInventory().clear(var5);
               }
            }

            this.createPlant(var4, var1.getPlayer().getUniqueId(), var1.getDrug().getId(), var2, var3, var1.getBaseBlock(), var1.getBaseBlock().getRelative(BlockFace.DOWN), var1.getSpaceReport());
         }
      }
   }

   @EventHandler
   public void onDrugPlantReplant(DrugPlantReplantEvent var1) {
      if (!var1.isCancelled()) {
         Plant var2 = var1.getPlant();
         Drug var3 = this.main.getDrugManager().getDrugById(var2.getParentDrugId());
         if (var3.getGrowing().isReplantSeedRequired() && var1.getPlayer() != null) {
            boolean var4 = false;

            for(int var5 = 0; var5 <= 40; ++var5) {
               ItemStack var6 = var1.getPlayer().getInventory().getItem(var5);
               if (var6 != null && var6.isSimilar(var3.getGrowing().getSeedItemStack())) {
                  if (var6.getAmount() > 1) {
                     var6.setAmount(var6.getAmount() - 1);
                  } else {
                     var1.getPlayer().getInventory().clear(var5);
                  }

                  var4 = true;
                  break;
               }
            }

            if (!var4) {
               var1.setCancelled(true);
               return;
            }
         }

         GrowingStage var7 = (GrowingStage)var3.getGrowing().getPlantType().getGrowingStages().stream().filter((var1x) -> var1x.getOrderId() == var3.getGrowing().getReplantStage()).findFirst().orElse((Object)null);
         if (var7 != null) {
            SpaceReport var8 = (new SpaceReport.Creator()).withGrowingStage(var7).withDirection(var2.getPlaceDirection()).withBaseBlock(var2.getBaseBlock()).doIgnorePlantBlocks(false).create(SpaceReportType.GROWING);
            this.createPlant(UUID.randomUUID(), var2.getOwner(), var2.getParentDrugId(), var7, var2.getPlaceDirection(), var2.getBaseBlock(), var2.getFoundationBlock(), var8);
         }

      }
   }

   private void createPlant(UUID var1, UUID var2, String var3, GrowingStage var4, Direction var5, Block var6, Block var7, SpaceReport var8) {
      if (var8.hasSpace()) {
         ArrayList var9 = new ArrayList();

         for(GrowingBlock var11 : var4.getGrowingBlocks()) {
            Block var12 = BlockUtils.getRelativeBlock(var5, var11.getBlockOffset(), var6);
            if (var8.getFreeBlocks().contains(var12)) {
               var12.setBlockData(var11.getBlockData(), var11.doApplyPhysics());
               if (var11.getSkullTexture() != null) {
                  ReflectionUtils.setSkullBlockTexture(var12, var11.getSkullTexture());
               }

               if (var11.useRandomRotation()) {
                  if (var12.getBlockData() instanceof Rotatable) {
                     Rotatable var13 = (Rotatable)var12.getBlockData();
                     var13.setRotation(BlockUtils.getRandomRotatableBlockFace());
                     var12.setBlockData(var13);
                  } else if (var12.getBlockData() instanceof Directional) {
                     Directional var19 = (Directional)var12.getBlockData();
                     var19.setFacing(BlockUtils.getRandomDirectionalBlockFace());
                     var12.setBlockData(var19);
                  }
               }

               if (var11.doAutoConnect() && Constants.AUTO_CONNECT_MATERIALS.contains(var12.getType())) {
                  ArrayList var20 = new ArrayList();

                  for(int var14 = var12.getX() - 1; var14 <= var12.getX() + 1; ++var14) {
                     if (var14 != var12.getX()) {
                        var20.add(var12.getWorld().getBlockAt(var14, var12.getY(), var12.getZ()));
                     }
                  }

                  for(int var21 = var12.getZ() - 1; var21 <= var12.getZ() + 1; ++var21) {
                     if (var21 != var12.getZ()) {
                        var20.add(var12.getWorld().getBlockAt(var12.getX(), var12.getY(), var21));
                     }
                  }

                  for(Block var15 : var20) {
                     if (var12.getType() == Material.COCOA && var15.getType().name().endsWith("JUNGLE_LOG") || var12.getType() == Material.ATTACHED_PUMPKIN_STEM && var15.getType() == Material.PUMPKIN || var12.getType() == Material.ATTACHED_MELON_STEM && var15.getType() == Material.MELON) {
                        Directional var16 = (Directional)var12.getBlockData();
                        BlockFace var17 = var12.getFace(var15);
                        if (var17 != null) {
                           var16.setFacing(var17);
                        }

                        var12.setBlockData(var16);
                        var7 = var15;
                        break;
                     }
                  }
               }

               var9.add(var12);
            }
         }

         var9.forEach((var2x) -> var2x.setMetadata("ultimatedrugs-plant", new FixedMetadataValue(this.main, var1.toString())));
         Plant var18 = new Plant(var1, var2, var3, var6, var5, var7, var9, var4, 0);
         this.main.getPlantManager().registerPlant(var18);
         this.main.getPlantManager().registerGrowingPlant(var18);
      }
   }

   @EventHandler
   public void onDrugPlantSendStatus(DrugPlantSendStatusEvent var1) {
      if (!var1.isCancelled()) {
         Plant var2 = var1.getPlant();
         Drug var3 = this.drugManager.getDrugById(var2.getParentDrugId());
         if (!var2.getCurrentGrowingStage().isGrown()) {
            int var4 = var2.getCurrentGrowingStage().getGrowDelay() > 0 ? var2.getCurrentGrowingStage().getGrowDelay() : var2.getTimeGrown();
            int var5 = (int)Math.round((double)var2.getTimeGrown() / (double)var4 * (double)10.0F);
            StringBuilder var6 = new StringBuilder();

            for(int var7 = 1; var7 <= 10; ++var7) {
               var6.append(var7 <= var5 ? this.main.getConfigManager().getLangString("filled-bar-part") : this.main.getConfigManager().getLangString("empty-bar-part"));
            }

            HashMap var8 = new HashMap();
            var8.put("%plant_stage%", Integer.toString(var2.getCurrentGrowingStage().getOrderId() + 1));
            if (var3 != null) {
               var8.put("%max_plant_stage%", Integer.toString(var3.getGrowing().getPlantType().getGrowingStages().size()));
            }

            var8.put("%progress_percentage%", Integer.toString((int)Math.round((double)var2.getTimeGrown() / (double)var4 * (double)100.0F)));
            var8.put("%progress_bar%", var6.toString());
            var1.getPlayer().sendMessage(TextUtils.replaceVariables(this.main.getConfigManager().getLangString("plant-progress-status"), var8));
         } else {
            var1.getPlayer().sendMessage(this.main.getConfigManager().getLangString("plant-progress-status-grown"));
         }

      }
   }

   @EventHandler
   public void onDrugPlantFertilizerUse(DrugPlantFertilizerUseEvent var1) {
      if (!var1.isCancelled()) {
         Plant var2 = var1.getPlant();
         if (!var2.getCurrentGrowingStage().isGrown() && var2.getTimeGrown() < var2.getCurrentGrowingStage().getGrowDelay()) {
            int var3 = var1.getPlayer().getInventory().getHeldItemSlot();
            ItemStack var4 = var1.getPlayer().getInventory().getItem(var3);
            if (var4 == null) {
               return;
            }

            if (var1.getPlayer().getGameMode() != GameMode.CREATIVE) {
               if (var4.getAmount() > 1) {
                  var4.setAmount(var4.getAmount() - 1);
               } else {
                  var1.getPlayer().getInventory().clear(var3);
               }
            }

            var2.setTimeGrown(var2.getTimeGrown() + var1.getFertilizer().getGrowAmount());
         }

      }
   }

   @EventHandler
   public void onBlockPhysics(BlockPhysicsEvent var1) {
      Block var2 = var1.getBlock();
      if (var2.hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
         Block var3 = var1.getSourceBlock();
         Plant var4 = this.main.getPlantManager().getPlantByUUID(UUID.fromString(((MetadataValue)var2.getMetadata("ultimatedrugs-plant").get(0)).asString()));
         if (var4 != null && MaterialUtils.isAir(var3.getType()) && var3.getLocation().equals(var4.getFoundationBlock().getLocation())) {
            boolean var5 = false;
            Drug var6 = this.drugManager.getDrugById(var4.getParentDrugId());
            if (var6 != null && var6.getGrowing() != null) {
               var5 = var6.getGrowing().getPlantType().doRemoveIfFlying();
            }

            if (!var2.getType().isOccluding() || !var2.getType().isSolid() || var5) {
               this.destroyPlant(var4, var2);
            }
         }
      }

   }

   @EventHandler
   public void onBlockFromTo(BlockFromToEvent var1) {
      if (var1.getToBlock().hasMetadata("ultimatedrugs-plant") && !var1.getToBlock().getType().isSolid()) {
         var1.setCancelled(true);
         Block var2 = var1.getToBlock();
         Plant var3 = this.main.getPlantManager().getPlantByUUID(UUID.fromString(((MetadataValue)var2.getMetadata("ultimatedrugs-plant").get(0)).asString()));
         this.destroyPlant(var3, var2);
      }

   }

   @EventHandler
   public void onBlockExplode(BlockExplodeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      } else {
         for(Block var3 : var1.blockList()) {
            if (var3.hasMetadata("ultimatedrugs-plant")) {
               Plant var4 = this.main.getPlantManager().getPlantByUUID(UUID.fromString(((MetadataValue)var3.getMetadata("ultimatedrugs-plant").get(0)).asString()));
               this.destroyPlant(var4, var3);
            }
         }
      }

   }

   @EventHandler
   public void onBlockPistonExtend(BlockPistonExtendEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      } else if (!var1.isCancelled()) {
         for(Block var3 : var1.getBlocks()) {
            if (var3.hasMetadata("ultimatedrugs-plant")) {
               Plant var4 = this.main.getPlantManager().getPlantByUUID(UUID.fromString(((MetadataValue)var3.getMetadata("ultimatedrugs-plant").get(0)).asString()));
               this.destroyPlant(var4, var3);
               var1.setCancelled(true);
               Drug var5 = this.drugManager.getDrugById(var4.getParentDrugId());
               if (this.main.getPlantManager().getHighestGrowingStage(var5.getGrowing().getPlantType()).getOrderId() == var4.getCurrentGrowingStage().getOrderId() && var5.getGrowing().doAutoReplant()) {
                  this.main.getServer().getScheduler().runTaskLater(this.main, () -> this.main.getServer().getPluginManager().callEvent(new DrugPlantReplantEvent(var4, (Player)null)), 5L);
                  return;
               }
            }
         }
      }

   }

   @EventHandler
   public void onBlockPistonRetract(BlockPistonRetractEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      } else if (!var1.isCancelled() && var1.isSticky()) {
         for(Block var3 : var1.getBlocks()) {
            if (var3.hasMetadata("ultimatedrugs-plant") && var3.getType().isSolid()) {
               Plant var4 = this.main.getPlantManager().getPlantByUUID(UUID.fromString(((MetadataValue)var3.getMetadata("ultimatedrugs-plant").get(0)).asString()));
               this.destroyPlant(var4, var3);
               var1.setCancelled(true);
               Drug var5 = this.drugManager.getDrugById(var4.getParentDrugId());
               if (this.main.getPlantManager().getHighestGrowingStage(var5.getGrowing().getPlantType()).getOrderId() == var4.getCurrentGrowingStage().getOrderId() && var5.getGrowing().doAutoReplant()) {
                  this.main.getServer().getScheduler().runTaskLater(this.main, () -> this.main.getServer().getPluginManager().callEvent(new DrugPlantReplantEvent(var4, (Player)null)), 5L);
                  return;
               }
            }
         }
      }

   }

   @EventHandler
   public void onStructureGrow(StructureGrowEvent var1) {
      if (var1.getLocation().getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockBurn(BlockBurnEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockFade(BlockFadeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockFertilizeEvent(BlockFertilizeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockGrow(BlockGrowEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockSpread(BlockSpreadEvent var1) {
      if (var1.getSource().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockIgnite(BlockIgniteEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onCauldronLevelChange(CauldronLevelChangeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onLeavesDecay(LeavesDecayEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onMoistureChange(MoistureChangeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onSpongeAbsorb(SpongeAbsorbEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
      }

   }

   private void destroyPlant(Plant var1, Block var2) {
      this.plantManager.unregisterPlant(var1.getUUID());

      for(Block var4 : var1.getCurrentBlocks()) {
         var4.removeMetadata("ultimatedrugs-plant", this.main);
         var4.setType(Material.AIR, false);
      }

      this.main.getConfigManager().removePlant(var1);
      if (var1.getCurrentGrowingStage().getDrops().size() > 0) {
         SecureRandom var12 = new SecureRandom();
         int var13 = 0;
         int var5 = var1.getCurrentGrowingStage().getDropLimit();
         boolean var6 = false;

         for(Drop var9 : var1.getCurrentGrowingStage().getDrops()) {
            if (var5 > 0 && var13 >= var5) {
               break;
            }

            ItemStack var10 = var9.getItemStack().clone();
            int var11 = var9.getMinAmount() != var9.getMaxAmount() ? var12.nextInt(var9.getMaxAmount() - var9.getMinAmount() + 1) + var9.getMinAmount() : var9.getMaxAmount();
            var10.setAmount(var11);
            if (var6 && var9.doBreakSuccessChain()) {
               var6 = false;
            } else if (var12.nextDouble() <= var9.getDropChance() / (double)100.0F) {
               if (!MaterialUtils.isAir(var10.getType())) {
                  var2.getWorld().dropItemNaturally(var2.getLocation(), var10);
               }

               ++var13;
               var6 = true;
            } else {
               var6 = false;
            }
         }
      }

   }
}
