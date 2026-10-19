package me.dexuby.UltimateDrugs.utils;

public class NumberUtils {
   public static boolean isValidInteger(String var0) {
      try {
         Integer.parseInt(var0);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
