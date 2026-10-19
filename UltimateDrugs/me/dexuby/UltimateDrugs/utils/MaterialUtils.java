package me.dexuby.UltimateDrugs.utils;

import org.bukkit.Material;

public class MaterialUtils {
   public static boolean isAir(Material var0) {
      return var0 == Material.AIR || var0 == Material.CAVE_AIR || var0 == Material.VOID_AIR;
   }
}
