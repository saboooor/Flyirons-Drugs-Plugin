package me.dexuby.UltimateDrugs.drugs.chatslur.strategies;

import java.util.Random;

public class SmileysChatSlurStrategy implements ChatSlurStrategy {
   private final Random random = new Random();
   private final String[] smileys = new String[]{":)", "(:", ":D", ";)", "(;", ";D"};

   public String convert(String var1) {
      StringBuilder var2 = new StringBuilder();
      if (var1.contains(" ")) {
         String[] var3 = var1.split(" ");

         for(int var4 = 0; var4 < var3.length; ++var4) {
            String var5 = var3[var4];
            if (this.random.nextDouble() <= 0.4) {
               var2.append(this.smileys[this.random.nextInt(this.smileys.length - 1)]).append(" ").append(var5);
            } else {
               var2.append(var5);
            }

            if (var4 < var3.length - 1) {
               var2.append(" ");
            }
         }
      } else {
         var2.append(var1);
      }

      var2.append(" ").append(this.smileys[this.random.nextInt(this.smileys.length - 1)]);
      return var2.toString();
   }
}
