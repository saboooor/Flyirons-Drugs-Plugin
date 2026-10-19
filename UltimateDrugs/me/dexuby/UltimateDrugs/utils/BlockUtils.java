package me.dexuby.UltimateDrugs.utils;

import java.util.List;
import java.util.Random;
import java.util.Set;
import me.dexuby.UltimateDrugs.block.BlockOffset;
import me.dexuby.UltimateDrugs.drugs.growing.CustomBlockFace;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BlockUtils {
   public static BlockFace getRandomRotatableBlockFace() {
      Random var0 = new Random();
      BlockFace[] var1 = new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.EAST_NORTH_EAST, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.WEST_NORTH_WEST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH_NORTH_EAST};
      return var1[var0.nextInt(var1.length)];
   }

   public static BlockFace getRandomDirectionalBlockFace() {
      Random var0 = new Random();
      BlockFace[] var1 = new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
      return var1[var0.nextInt(var1.length)];
   }

   public static Block getRelativeBlock(Direction var0, BlockOffset var1, Block var2) {
      Block var3 = var2;
      if (var1.getX() != 0) {
         if (var1.getX() > 0) {
            var3 = var2.getRelative(getRelativeBlockFace(var0, CustomBlockFace.LEFT), var1.getX());
         } else {
            var3 = var2.getRelative(getRelativeBlockFace(var0, CustomBlockFace.RIGHT), Math.abs(var1.getX()));
         }
      }

      if (var1.getY() != 0) {
         var3 = var3.getRelative(0, var1.getY(), 0);
      }

      if (var1.getZ() != 0) {
         if (var1.getZ() > 0) {
            var3 = var3.getRelative(getRelativeBlockFace(var0, CustomBlockFace.FRONT), var1.getZ());
         } else {
            var3 = var3.getRelative(getRelativeBlockFace(var0, CustomBlockFace.BACK), Math.abs(var1.getZ()));
         }
      }

      return var3;
   }

   private static BlockFace getRelativeBlockFace(Direction var0, CustomBlockFace var1) {
      if (var1 != CustomBlockFace.LEFT && var1 != CustomBlockFace.RIGHT && var1 != CustomBlockFace.FRONT && var1 != CustomBlockFace.BACK) {
         return BlockFace.valueOf(var1.toString());
      } else {
         switch (var0) {
            case NORTH:
               if (var1 == CustomBlockFace.LEFT) {
                  return BlockFace.WEST;
               } else if (var1 == CustomBlockFace.RIGHT) {
                  return BlockFace.EAST;
               } else {
                  if (var1 == CustomBlockFace.FRONT) {
                     return BlockFace.SOUTH;
                  }

                  return BlockFace.NORTH;
               }
            case EAST:
               if (var1 == CustomBlockFace.LEFT) {
                  return BlockFace.NORTH;
               } else if (var1 == CustomBlockFace.RIGHT) {
                  return BlockFace.SOUTH;
               } else {
                  if (var1 == CustomBlockFace.FRONT) {
                     return BlockFace.WEST;
                  }

                  return BlockFace.EAST;
               }
            case SOUTH:
               if (var1 == CustomBlockFace.LEFT) {
                  return BlockFace.EAST;
               } else if (var1 == CustomBlockFace.RIGHT) {
                  return BlockFace.WEST;
               } else {
                  if (var1 == CustomBlockFace.FRONT) {
                     return BlockFace.NORTH;
                  }

                  return BlockFace.SOUTH;
               }
            case WEST:
               if (var1 == CustomBlockFace.LEFT) {
                  return BlockFace.SOUTH;
               } else if (var1 == CustomBlockFace.RIGHT) {
                  return BlockFace.NORTH;
               } else {
                  if (var1 == CustomBlockFace.FRONT) {
                     return BlockFace.EAST;
                  }

                  return BlockFace.WEST;
               }
            default:
               return null;
         }
      }
   }

   public static BlockFace getTargetBlockFaceOld(Player var0, boolean var1) {
      List var2 = var0.getLastTwoTargetBlocks((Set)null, 100);
      if (var2.size() == 2 && (!var1 || ((Block)var2.get(1)).getType().isSolid())) {
         Block var3 = (Block)var2.get(1);
         Block var4 = (Block)var2.get(0);
         BlockFace var5 = var3.getFace(var4);
         if (!var1 && !((Block)var2.get(1)).getType().isSolid()) {
            var5 = BlockFace.SELF;
         }

         return var5;
      } else {
         return null;
      }
   }

   public static BlockFace getTargetBlockFace(Player var0, boolean var1) {
      List var2 = var0.getLastTwoTargetBlocks((Set)null, 100);
      if (var2.size() != 2) {
         return null;
      } else {
         BlockFace var3 = ((Block)var2.get(1)).getFace((Block)var2.get(0));
         if (var1 && !((Block)var2.get(1)).getType().isSolid()) {
            var3 = BlockFace.SELF;
         }

         return var3;
      }
   }
}
