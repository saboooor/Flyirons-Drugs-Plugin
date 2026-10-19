package me.dexuby.UltimateDrugs.events;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import java.util.Map;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponDamageEntity implements Listener {
   private Main main;
   private DrugManager drugManager;

   public WeaponDamageEntity(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onWeaponDamageEntity(WeaponDamageEntityEvent var1) {
      if (var1.getDamager() instanceof Player) {
         Player var2 = (Player)var1.getDamager();
         if (this.drugManager.isPlayerOnDrugs(var2)) {
            Map var3 = this.drugManager.getPlayerMultipliers(var2);
            if (var3.containsKey(MultiplierType.WEAPON_DAMAGE)) {
               var1.setDamage(var1.getDamage() * (Double)var3.get(MultiplierType.WEAPON_DAMAGE));
            }
         }
      }

   }
}
