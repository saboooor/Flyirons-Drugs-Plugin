package me.dexuby.UltimateDrugs.utils;

public class TimeUtils {
   public static String convertSecondsToFormattedTime(long var0) {
      int var2 = Math.round((float)(var0 / 3600L));
      var0 -= (long)(var2 * 3600);
      int var3 = Math.round((float)(var0 / 60L));
      var0 -= (long)(var3 * 60);
      StringBuilder var4 = new StringBuilder();
      if (var2 < 10) {
         var4.append("0");
      }

      var4.append(var2).append("h ");
      if (var3 < 10) {
         var4.append("0");
      }

      var4.append(var3).append("m ");
      if (var0 < 10L) {
         var4.append("0");
      }

      var4.append(var0).append("s");
      return var4.toString();
   }
}
