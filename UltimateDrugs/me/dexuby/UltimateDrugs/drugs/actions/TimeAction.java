package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TimeAction extends TimedConsumeAction {
   private long time;
   private boolean relative;
   private boolean clientSided;

   public TimeAction(int var1, double var2, int var4, int var5, String var6, int var7, long var8, boolean var10, boolean var11) {
      super(var1, var2, var4, var5, var6, var7);
      this.time = var8;
      this.relative = var10;
      this.clientSided = var11;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline()) {
         if (this.getTargetSelector() != null) {
            for(Entity var6 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
               if (var6 instanceof Player) {
                  this.apply((Player)var6);
               }
            }
         } else {
            this.apply(var3);
         }

      }
   }

   private void apply(Player var1) {
      if (this.clientSided) {
         var1.setPlayerTime(this.time, this.relative);
      } else {
         var1.getWorld().setTime(this.time);
      }

   }

   public void end(Player var1) {
      if (this.clientSided) {
         var1.resetPlayerTime();
      }

   }

   public long getTime() {
      return this.time;
   }

   public boolean isRelative() {
      return this.relative;
   }

   public boolean isClientSided() {
      return this.clientSided;
   }

   public TimeAction clone() {
      return new TimeAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.getDuration(), this.time, this.relative, this.clientSided);
   }
}
