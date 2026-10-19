package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WeatherAction extends TimedConsumeAction {
   private WeatherType weatherType;
   private boolean clientSided;

   public WeatherAction(int var1, double var2, int var4, int var5, String var6, int var7, WeatherType var8, boolean var9) {
      super(var1, var2, var4, var5, var6, var7);
      this.weatherType = var8;
      this.clientSided = var9;
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
         var1.setPlayerWeather(this.weatherType);
      } else {
         World var2 = var1.getWorld();
         if (this.weatherType == WeatherType.CLEAR) {
            var2.setThundering(false);
            var2.setStorm(false);
         } else {
            var2.setStorm(true);
         }
      }

   }

   public void end(Player var1) {
      if (this.clientSided) {
         var1.resetPlayerWeather();
      }

   }

   public WeatherType weatherType() {
      return this.weatherType;
   }

   public boolean isClientSided() {
      return this.clientSided;
   }

   public WeatherAction clone() {
      return new WeatherAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.getDuration(), this.weatherType, this.clientSided);
   }
}
