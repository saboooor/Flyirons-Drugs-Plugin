package me.dexuby.UltimateDrugs.utils;

import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import org.bukkit.entity.Entity;

public class EntityUtils {
   public static Direction getDirection(Entity var0) {
      float var1 = var0.getLocation().getYaw();
      if (var1 < 0.0F) {
         var1 += 360.0F;
      }

      if (!(var1 >= 315.0F) && !(var1 < 45.0F)) {
         if (var1 < 135.0F) {
            return Direction.WEST;
         } else {
            return var1 < 225.0F ? Direction.NORTH : Direction.EAST;
         }
      } else {
         return Direction.SOUTH;
      }
   }
}
