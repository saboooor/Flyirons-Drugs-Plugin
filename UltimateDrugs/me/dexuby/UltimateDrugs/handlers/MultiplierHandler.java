package me.dexuby.UltimateDrugs.handlers;

import java.util.Map;
import java.util.Objects;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MultiplierHandler implements Listener {
   private Main main;
   private DrugManager drugManager;

   public MultiplierHandler(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent var1) {
      if (var1.getDamager() instanceof Player) {
         Player var2 = (Player)var1.getDamager();
         if (this.drugManager.isPlayerOnDrugs(var2)) {
            Map var3 = this.drugManager.getPlayerMultipliers(var2);
            if (var3.containsKey(MultiplierType.DAMAGE)) {
               var1.setDamage(var1.getDamage() * (Double)var3.get(MultiplierType.DAMAGE));
            }

            if (var1.getEntity() instanceof Player && var3.containsKey(MultiplierType.PVP_DAMAGE)) {
               var1.setDamage(var1.getDamage() * (Double)var3.get(MultiplierType.PVP_DAMAGE));
            }

            if (var2.getInventory().getItem(var2.getInventory().getHeldItemSlot()) == null && var3.containsKey(MultiplierType.HAND_DAMAGE)) {
               var1.setDamage(var1.getDamage() * (Double)var3.get(MultiplierType.HAND_DAMAGE));
            }

            if (var3.containsKey(MultiplierType.LIFE_STEAL)) {
               double var4 = var1.getFinalDamage() * (Double)var3.get(MultiplierType.LIFE_STEAL);
               double var6 = ((AttributeInstance)Objects.requireNonNull(var2.getAttribute(Attribute.GENERIC_MAX_HEALTH))).getValue();
               if (var2.getHealth() < var6) {
                  if (var2.getHealth() + var4 > var6) {
                     var2.setHealth(var6);
                  } else {
                     var2.setHealth(var2.getHealth() + var4);
                  }
               }
            }
         }
      } else if (var1.getDamager() instanceof Projectile) {
         Projectile var8 = (Projectile)var1.getDamager();
         if (var8.getShooter() instanceof Player) {
            Player var10 = (Player)var8.getShooter();
            if (var10 != null && var10.isValid() && var10.isOnline() && this.drugManager.isPlayerOnDrugs(var10)) {
               Map var12 = this.drugManager.getPlayerMultipliers(var10);
               if (var12.containsKey(MultiplierType.PROJECTILE_DAMAGE)) {
                  var1.setDamage(var1.getDamage() * (Double)var12.get(MultiplierType.PROJECTILE_DAMAGE));
               }
            }
         }
      }

      if (var1.getEntity() instanceof Player) {
         Player var9 = (Player)var1.getEntity();
         if (this.drugManager.isPlayerOnDrugs(var9)) {
            Map var11 = this.drugManager.getPlayerMultipliers(var9);
            if (var11.containsKey(MultiplierType.INCOMING_DAMAGE)) {
               var1.setDamage(var1.getDamage() * (Double)var11.get(MultiplierType.INCOMING_DAMAGE));
            }
         }
      }

   }
}
