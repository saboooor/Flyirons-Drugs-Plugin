package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PotionEffectAction extends ConsumeAction {
   private PotionEffect potionEffect;

   public PotionEffectAction(int var1, double var2, int var4, int var5, String var6, PotionEffect var7) {
      super(var1, var2, var4, var5, var6);
      this.potionEffect = var7;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline()) {
         if (this.getTargetSelector() != null) {
            for(Entity var6 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
               if (var6 instanceof LivingEntity) {
                  this.apply((LivingEntity)var6);
               }
            }
         } else {
            this.apply(var3);
         }
      }

   }

   private void apply(LivingEntity var1) {
      var1.addPotionEffect(this.potionEffect);
   }

   public PotionEffect getPotionEffect() {
      return this.potionEffect;
   }

   public PotionEffectAction clone() {
      return new PotionEffectAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.potionEffect);
   }
}
