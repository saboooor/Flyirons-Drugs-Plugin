package me.dexuby.UltimateDrugs.events;

import java.util.Map;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.utils.Constants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.potion.PotionEffect;

public class EntityPotionEffect implements Listener {
   private final Main main;
   private final DrugManager drugManager;

   public EntityPotionEffect(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onEntityPotionEffect(EntityPotionEffectEvent var1) {
      if (var1.getEntity() instanceof Player && (var1.getAction() == Action.ADDED || var1.getAction() == Action.CHANGED) && var1.getCause() != Cause.PLUGIN) {
         if (var1.getNewEffect() == null) {
            return;
         }

         PotionEffect var2 = var1.getNewEffect();
         Player var3 = (Player)var1.getEntity();
         if (this.drugManager.isPlayerOnDrugs(var3)) {
            double var4 = (double)1.0F;
            Map var6 = this.drugManager.getPlayerMultipliers(var3);
            if (Constants.POSITIVE_EFFECT_TYPES.contains(var2.getType().getName())) {
               if (var6.containsKey(MultiplierType.POSITIVE_POTION_EFFECT_DURATION)) {
                  var4 = (Double)var6.get(MultiplierType.POSITIVE_POTION_EFFECT_DURATION);
               }
            } else if (Constants.NEGATIVE_EFFECT_TYPES.contains(var2.getType().getName()) && var6.containsKey(MultiplierType.NEGATIVE_POTION_EFFECT_DURATION)) {
               var4 = (Double)var6.get(MultiplierType.NEGATIVE_POTION_EFFECT_DURATION);
            }

            if (var4 != (double)1.0F) {
               var1.setCancelled(true);
               var3.removePotionEffect(var2.getType());
               var3.addPotionEffect(new PotionEffect(var2.getType(), (int)Math.round((double)var2.getDuration() * var4), var2.getAmplifier(), var2.isAmbient(), var2.hasParticles(), var2.hasIcon()));
            }
         }
      }

   }
}
