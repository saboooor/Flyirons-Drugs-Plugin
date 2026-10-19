package me.dexuby.UltimateDrugs.drugs.chatslur.strategies;

import java.util.Random;

public class StutterChatSlurStrategy implements ChatSlurStrategy {
   private final Random random = new Random();

   public String convert(String var1) {
      if (!var1.contains(" ")) {
         return var1;
      } else {
         StringBuilder var2 = new StringBuilder();
         String[] var3 = var1.split(" ");

         for(int var4 = 0; var4 < var3.length; ++var4) {
            String var5 = var3[var4];
            if (this.random.nextDouble() <= 0.3) {
               char var6 = var5.charAt(0);
               var2.append(String.format("%c-%s", var6, var5));
            } else {
               var2.append(var5);
            }

            if (var4 < var3.length - 1) {
               var2.append(" ");
            }
         }

         return var2.toString();
      }
   }
}
