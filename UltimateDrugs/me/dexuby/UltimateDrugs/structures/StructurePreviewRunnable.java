package me.dexuby.UltimateDrugs.structures;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.dexuby.UltimateDrugs.block.SpaceReport;
import me.dexuby.UltimateDrugs.block.SpaceReportType;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerBlockChange;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import me.dexuby.UltimateDrugs.utils.EntityUtils;
import me.dexuby.UltimateDrugs.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StructurePreviewRunnable extends BukkitRunnable {
   private final Player player;
   private final Direction direction;
   private final StructureType structureType;
   private final List<Block> blocksToUpdate = new ArrayList();

   public StructurePreviewRunnable(Player var1, Direction var2, StructureType var3) {
      this.player = var1;
      this.direction = var2;
      this.structureType = var3;
   }

   public void run() {
      if (!this.player.isValid() || !this.player.getInventory().getItemInMainHand().isSimilar(this.structureType.getSpawnItem())) {
         this.cancelSafely();
      }

      Block var1 = this.player.getTargetBlock((Set)null, 5);
      if (!MaterialUtils.isAir(var1.getType())) {
         BlockFace var2 = BlockUtils.getTargetBlockFace(this.player, true);
         if (var2 != null) {
            Block var3 = var1.getRelative(var2);
            SpaceReport var4 = (new SpaceReport.Creator()).withBlockList(this.structureType.getBlockList()).withDirection(this.direction).withBaseBlock(var3).create(SpaceReportType.DEFAULT);
            this.updateBlocks(var4.getFreeBlocks());
            boolean var5 = var4.hasSpace();
            if (this.structureType.getFoundationBlockOffset() != null) {
               Block var6 = BlockUtils.getRelativeBlock(EntityUtils.getDirection(this.player), this.structureType.getFoundationBlockOffset(), var3);
               if (!var6.getType().isSolid()) {
                  var5 = false;
               }
            }

            Material var9 = var5 ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS;

            for(Block var8 : var4.getFreeBlocks()) {
               (new WrapperPlayServerBlockChange()).setLocation(new BlockPosition(var8.getX(), var8.getY(), var8.getZ())).setBlockData(WrappedBlockData.createData(var9)).sendPacket(this.player);
               this.blocksToUpdate.add(var8);
            }

            return;
         }
      }

      this.updateBlocks((Set)null);
   }

   public void cancelSafely() {
      this.updateBlocks((Set)null);
      this.cancel();
   }

   private void updateBlocks(Set<Block> var1) {
      this.blocksToUpdate.forEach((var1x) -> {
         if (var1 == null || !var1.contains(var1x)) {
            var1x.getState().update();
         }

      });
      this.blocksToUpdate.clear();
   }
}
