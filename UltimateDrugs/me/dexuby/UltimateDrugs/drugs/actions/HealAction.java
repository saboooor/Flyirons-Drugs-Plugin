package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HealAction extends ConsumeAction {
   private double amount;

   public HealAction(int var1, double var2, int var4, int var5, String var6, double var7) {
      super(var1, var2, var4, var5, var6);
      this.amount = var7;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline() && !var3.isDead()) {
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
      AttributeInstance var2 = var1.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      double var3 = var2 != null ? var2.getValue() : (double)20.0F;
      if (!(this.amount > (double)0.0F) || !(var1.getHealth() >= var3)) {
         if (this.amount > (double)0.0F) {
            var1.setHealth(Math.min(var1.getHealth() + this.amount, var3));
         } else {
            var1.setHealth(Math.max(var1.getHealth() + this.amount, (double)0.0F));
         }

      }
   }

   public double getAmount() {
      return this.amount;
   }

   public HealAction clone() {
      return new HealAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.amount);
   }
}
