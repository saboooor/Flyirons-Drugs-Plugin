package me.dexuby.UltimateDrugs.drugs.actions;

import org.bukkit.entity.Player;

public abstract class TimedConsumeAction extends ConsumeAction {
   private int duration;

   TimedConsumeAction(int var1, double var2, int var4, int var5, String var6, int var7) {
      super(var1, var2, var4, var5, var6);
      this.duration = var7;
   }

   public int getDuration() {
      return this.duration;
   }

   public abstract void end(Player var1);
}
