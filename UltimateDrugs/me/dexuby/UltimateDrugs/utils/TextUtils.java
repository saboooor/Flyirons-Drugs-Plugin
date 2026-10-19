package me.dexuby.UltimateDrugs.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;

public class TextUtils {
   public static String replaceVariables(String var0, Map<String, String> var1) {
      String var2 = var0;

      for(String var4 : var1.keySet()) {
         String var5 = (String)var1.get(var4);
         var2 = var2.replace(var4, var5);
      }

      return var2;
   }

   public static List<String> replaceVariables(List<String> var0, Map<String, String> var1) {
      ArrayList var2 = new ArrayList();

      for(String var4 : var0) {
         String var5 = var4;

         for(String var7 : var1.keySet()) {
            String var8 = (String)var1.get(var7);
            var5 = var5.replace(var7, var8);
         }

         var2.add(var5);
      }

      return var2;
   }

   public static List<String> translateAlternateColorCodesFromList(char var0, List<String> var1) {
      ArrayList var2 = new ArrayList();

      for(String var4 : var1) {
         var2.add(ChatColor.translateAlternateColorCodes(var0, var4));
      }

      return var2;
   }

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
