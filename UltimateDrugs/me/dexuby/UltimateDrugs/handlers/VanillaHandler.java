package me.dexuby.UltimateDrugs.handlers;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.VanillaDropDropEvent;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.Drop;
import me.dexuby.UltimateDrugs.drugs.vanilla.BlockVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.FishingVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.MobVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.VanillaDrop;
import me.dexuby.UltimateDrugs.managers.VanillaDropManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class VanillaHandler implements Listener {
   private Main main;
   private VanillaDropManager vanillaDropManager;

   public VanillaHandler(Main var1) {
      this.main = var1;
      this.vanillaDropManager = var1.getVanillaDropManager();
   }

   @EventHandler
   public void onVanillaDropDrop(VanillaDropDropEvent var1) {
      if (!var1.isCancelled() && var1.getLocation().getWorld() != null) {
         int var2 = 0;
         int var3 = var1.getVanillaDrop().getDropLimit();
         boolean var4 = false;
         Random var5 = new Random();

         for(Drop var7 : var1.getDrops()) {
            if (var3 > 0 && var2 >= var3) {
               break;
            }

            ItemStack var8 = var7.getItemStack().clone();
            int var9 = var7.getMinAmount() != var7.getMaxAmount() ? var5.nextInt(var7.getMaxAmount() - var7.getMinAmount() + 1) + var7.getMinAmount() : var7.getMaxAmount();
            var8.setAmount(var9);
            if (var4 && var7.doBreakSuccessChain()) {
               var4 = false;
            } else if (var5.nextDouble() <= var7.getDropChance() / (double)100.0F) {
               if (var1.getVanillaDrop() instanceof FishingVanillaDrop) {
                  if (var1.getExtraData().length == 1) {
                     Vector var10 = (Vector)var1.getExtraData()[0];
                     Item var11 = var1.getLocation().getWorld().dropItem(var1.getLocation(), var8);
                     var11.setVelocity(var10);
                  }
               } else {
                  var1.getLocation().getWorld().dropItemNaturally(var1.getLocation(), var8);
               }

               ++var2;
               var4 = true;
            } else {
               var4 = false;
            }
         }

      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent var1) {
      if (!var1.isCancelled()) {
         if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.vanillaDropManager.isPlayerPlacedBlock(var1.getBlock())) {
            this.vanillaDropManager.removePlayerPlacedBlockLocation(var1.getBlock().getLocation());
         }

         if (var1.getPlayer().getGameMode() != GameMode.CREATIVE) {
            for(VanillaDrop var4 : (Collection)this.vanillaDropManager.getVanillaDrops().stream().filter((var1x) -> var1x instanceof BlockVanillaDrop && (!((BlockVanillaDrop)var1x).doIgnoreDrugPlants() || !var1.getBlock().hasMetadata("ultimatedrugs-plant")) && ((BlockVanillaDrop)var1x).getBlocks().stream().anyMatch((var1xx) -> var1xx.doIgnoreData() && var1.getBlock().getType() == var1xx.getBlockData().getMaterial() || var1xx.getBlockData().matches(var1.getBlock().getBlockData()))).collect(Collectors.toList())) {
               BlockVanillaDrop var5 = (BlockVanillaDrop)var4;
               if (var5.getPermission() == null || var1.getPlayer().hasPermission(var5.getPermission())) {
                  if (!var5.getBiomeWhitelist().isEmpty() && !var5.getBiomeWhitelist().contains(var1.getBlock().getBiome())) {
                     return;
                  }

                  if (var5.doOverrideDrops()) {
                     var1.setDropItems(false);
                  }

                  if (var5.doOverrideExp()) {
                     var1.setExpToDrop(0);
                  }

                  if (var5.getExperience() != 0) {
                     var1.setExpToDrop(var1.getExpToDrop() + var5.getExperience());
                  }

                  this.main.getServer().getPluginManager().callEvent(new VanillaDropDropEvent(var5, var1.getBlock().getLocation(), var5.getDrops(), new Object[0]));
               }
            }

            if ((Boolean)SettingsHolder.VANILLA_AUTO_REPLANT.getValue() && var1.getPlayer().hasPermission("ultimatedrugs.vanilla.replant") && var1.getBlock().getBlockData() instanceof Ageable) {
               if (var1.getBlock().hasMetadata("ultimatedrugs-plant") && (Boolean)SettingsHolder.VANILLA_AUTO_IGNORE_DRUG_PLANTS.getValue()) {
                  return;
               }

               Collection var6 = (Collection)SettingsHolder.VANILLA_AUTO_REPLANT_BLACKLIST.getValue();
               if (!var6.contains(var1.getBlock().getType())) {
                  Ageable var7 = (Ageable)var1.getBlock().getBlockData();
                  if (var7.getAge() == var7.getMaximumAge()) {
                     var7.setAge(0);
                     this.main.getServer().getScheduler().runTaskLater(this.main, () -> var1.getBlock().setBlockData(var7), 1L);
                  }
               }
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.vanillaDropManager.isVanillaBlockDropMaterial(var1.getBlock().getType())) {
         this.vanillaDropManager.addPlayerPlacedBlockLocation(var1.getBlock().getLocation());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue()) {
         BlockFace var2 = var1.getDirection();

         for(Block var4 : var1.getBlocks()) {
            if (this.vanillaDropManager.isPlayerPlacedBlock(var4)) {
               this.vanillaDropManager.removePlayerPlacedBlockLocation(var4.getLocation());
               this.vanillaDropManager.addPlayerPlacedBlockLocation(var4.getRelative(var2).getLocation());
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue()) {
         BlockFace var2 = var1.getDirection();

         for(Block var4 : var1.getBlocks()) {
            if (this.vanillaDropManager.isPlayerPlacedBlock(var4)) {
               this.vanillaDropManager.removePlayerPlacedBlockLocation(var4.getLocation());
               this.vanillaDropManager.addPlayerPlacedBlockLocation(var4.getRelative(var2).getLocation());
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.vanillaDropManager.isPlayerPlacedBlock(var1.getBlock())) {
         this.vanillaDropManager.removePlayerPlacedBlockLocation(var1.getBlock().getLocation());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockExplode(BlockExplodeEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue()) {
         for(Block var3 : var1.blockList()) {
            if (this.vanillaDropManager.isPlayerPlacedBlock(var3)) {
               this.vanillaDropManager.removePlayerPlacedBlockLocation(var3.getLocation());
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue()) {
         for(Block var3 : var1.blockList()) {
            if (this.vanillaDropManager.isPlayerPlacedBlock(var3)) {
               this.vanillaDropManager.removePlayerPlacedBlockLocation(var3.getLocation());
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockFade(BlockFadeEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.vanillaDropManager.isPlayerPlacedBlock(var1.getBlock())) {
         this.vanillaDropManager.removePlayerPlacedBlockLocation(var1.getBlock().getLocation());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onLeavesDecay(LeavesDecayEvent var1) {
      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.vanillaDropManager.isPlayerPlacedBlock(var1.getBlock())) {
         this.vanillaDropManager.removePlayerPlacedBlockLocation(var1.getBlock().getLocation());
      }

   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent var1) {
      Player var2 = var1.getEntity().getKiller();
      if (var2 != null) {
         for(VanillaDrop var5 : (Collection)this.vanillaDropManager.getVanillaDrops().stream().filter((var1x) -> var1x instanceof MobVanillaDrop && ((MobVanillaDrop)var1x).getEntityTypes().contains(var1.getEntityType())).collect(Collectors.toList())) {
            MobVanillaDrop var6 = (MobVanillaDrop)var5;
            if (var6.getPermission() == null || var2.hasPermission(var6.getPermission())) {
               if (!var6.getBiomeWhitelist().isEmpty() && !var6.getBiomeWhitelist().contains(var1.getEntity().getLocation().getBlock().getBiome())) {
                  return;
               }

               if (var6.doOverrideDrops()) {
                  var1.getDrops().clear();
               }

               if (var6.doOverrideExp()) {
                  var1.setDroppedExp(0);
               }

               if (var6.getExperience() != 0) {
                  var1.setDroppedExp(var1.getDroppedExp() + var6.getExperience());
               }

               this.main.getServer().getPluginManager().callEvent(new VanillaDropDropEvent(var6, var1.getEntity().getLocation(), var6.getDrops(), new Object[0]));
            }
         }
      }

   }

   @EventHandler
   public void onPlayerFish(PlayerFishEvent var1) {
      if ((var1.getState() == State.CAUGHT_FISH || var1.getState() == State.CAUGHT_ENTITY) && var1.getCaught() instanceof Item) {
         Item var2 = (Item)var1.getCaught();
         ItemStack var3 = var2.getItemStack();

         for(FishingVanillaDrop var6 : (Collection)this.vanillaDropManager.getVanillaDrops().stream().filter((var0) -> var0 instanceof FishingVanillaDrop).map((var0) -> (FishingVanillaDrop)var0).collect(Collectors.toList())) {
            if (var6.getFishTypes().contains(var3.getType()) && (var6.getPermission() == null || var1.getPlayer().hasPermission(var6.getPermission()))) {
               if (!var6.getBiomeWhitelist().isEmpty() && !var6.getBiomeWhitelist().contains(var1.getCaught().getLocation().getBlock().getBiome())) {
                  return;
               }

               if (var6.doOverrideDrops() && var2.isValid()) {
                  var2.remove();
               }

               if (var6.doOverrideExp()) {
                  var1.setExpToDrop(0);
               }

               if (var6.getExperience() != 0) {
                  var1.setExpToDrop(var1.getExpToDrop() + var6.getExperience());
               }

               Vector var7 = var1.getPlayer().getLocation().toVector().subtract(var2.getLocation().toVector()).normalize().multiply(new Vector((double)0.5F, (double)1.0F, (double)0.5F));
               this.main.getServer().getPluginManager().callEvent(new VanillaDropDropEvent(var6, var2.getLocation(), var6.getDrops(), new Object[]{var7}));
            }
         }
      }

   }
}
