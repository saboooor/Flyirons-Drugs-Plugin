package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FeedAction extends ConsumeAction {
   private int amount;

   public FeedAction(int var1, double var2, int var4, int var5, String var6, int var7) {
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
      if (this.amount <= 0 || var1.getFoodLevel() < 20) {
         if (this.amount > 0) {
            var1.setFoodLevel(Math.min(var1.getFoodLevel() + this.amount, 20));
         } else {
            var1.setFoodLevel(Math.max(var1.getFoodLevel() + this.amount, 0));
         }

      }
   }

   public double getAmount() {
      return (double)this.amount;
   }

   public FeedAction clone() {
      return new FeedAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.amount);
   }
}
