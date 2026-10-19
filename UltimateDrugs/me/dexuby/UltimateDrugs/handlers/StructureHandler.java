package me.dexuby.UltimateDrugs.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.structures.Structure;
import me.dexuby.UltimateDrugs.structures.StructurePreviewRunnable;
import me.dexuby.UltimateDrugs.structures.StructureType;
import me.dexuby.UltimateDrugs.utils.EntityUtils;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

public class StructureHandler implements Listener {
   private static final ItemStack TEST_ITEM;
   private Main main;
   private final Map<UUID, StructurePreviewRunnable> playersInPreview = new HashMap();

   public StructureHandler(Main var1) {
      this.main = var1;
   }

   @EventHandler
   public void onPlayerItemHeld(PlayerItemHeldEvent var1) {
      if (this.main.getServer().getPluginManager().isPluginEnabled("ProtocolLib") && (Boolean)SettingsHolder.STRUCTURE_PREVIEW.getValue()) {
         Player var2 = var1.getPlayer();
         if (!this.isInPreview(var2)) {
            ItemStack var3 = var2.getInventory().getItem(var1.getNewSlot());
            if (var3 != null && var3.getType() != Material.AIR) {
               StructureType var4 = this.main.getStructureManager().getStructureTypeByItemStack(var3);
               if (var4 != null) {
                  Block var5 = var2.getTargetBlock((Set)null, 5);
                  if (!MaterialUtils.isAir(var5.getType())) {
                     Direction var6 = EntityUtils.getDirection(var2);
                     StructurePreviewRunnable var7 = new StructurePreviewRunnable(var2, var6, var4);
                     var7.runTaskTimer(this.main, 0L, (Long)SettingsHolder.STRUCTURE_PREVIEW_UPDATE_DELAY.getValue());
                     this.playersInPreview.put(var2.getUniqueId(), var7);
                  }
               }
            }
         } else {
            ((StructurePreviewRunnable)this.playersInPreview.get(var2.getUniqueId())).cancelSafely();
            this.playersInPreview.remove(var2.getUniqueId());
         }

      }
   }

   private boolean isInPreview(Player var1) {
      if (this.playersInPreview.containsKey(var1.getUniqueId())) {
         StructurePreviewRunnable var2 = (StructurePreviewRunnable)this.playersInPreview.get(var1.getUniqueId());
         if (!var2.isCancelled()) {
            return true;
         }

         this.playersInPreview.remove(var1.getUniqueId());
      }

      return false;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent var1) {
      if (var1.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block var2 = var1.getClickedBlock();
         if (var2 == null || !var2.hasMetadata("ultimatedrugs.structure-block")) {
            return;
         }

         Structure var3 = this.main.getStructureManager().getStructureByBlock(var2);
         if (var3 != null && var3.getInventory() != null) {
            var1.getPlayer().openInventory(var3.getInventory());
         }
      }

   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent var1) {
      if (this.main.getStructureManager().getStructureByInventory(var1.getView().getTopInventory()) != null) {
         var1.setCancelled(true);
      }

   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent var1) {
      Block var2 = var1.getBlock();
      if (var2.hasMetadata("ultimatedrugs.structure-block")) {
         Structure var3 = this.main.getStructureManager().getStructureByBlock(var2);
         if (var3 != null) {
            this.main.getStructureManager().removeStructure(var3);
         }
      }

   }

   @EventHandler
   public void onBlockFromTo(BlockFromToEvent var1) {
      if (var1.getToBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockExplode(BlockExplodeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockPistonExtend(BlockPistonExtendEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

      for(Block var3 : var1.getBlocks()) {
         if (var3.hasMetadata("ultimatedrugs.structure-block")) {
            var1.setCancelled(true);
            return;
         }
      }

   }

   @EventHandler
   public void onBlockPistonRetract(BlockPistonRetractEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

      for(Block var3 : var1.getBlocks()) {
         if (var3.hasMetadata("ultimatedrugs.structure-block")) {
            var1.setCancelled(true);
            return;
         }
      }

   }

   @EventHandler
   public void onStructureGrow(StructureGrowEvent var1) {
      if (var1.getLocation().getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockBurn(BlockBurnEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockFade(BlockFadeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockFertilize(BlockFertilizeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockGrow(BlockGrowEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockSpread(BlockSpreadEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockIgnite(BlockIgniteEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onCauldronLevelChange(CauldronLevelChangeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onLeavesDecay(LeavesDecayEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onMoistureChange(MoistureChangeEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   @EventHandler
   public void onSpongeAbsorb(SpongeAbsorbEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs.structure-block")) {
         var1.setCancelled(true);
      }

   }

   static {
      TEST_ITEM = (new ItemBuilder(Material.STICK)).build();
   }
}
