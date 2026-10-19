package me.dexuby.UltimateDrugs.events;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import java.util.Map;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOPlayerXpGain implements Listener {
   private Main main;
   private DrugManager drugManager;

   public McMMOPlayerXpGain(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onMcMMOPlayerXpGain(McMMOPlayerXpGainEvent var1) {
      Player var2 = var1.getPlayer();
      if (this.drugManager.isPlayerOnDrugs(var2)) {
         Map var3 = this.drugManager.getPlayerMultipliers(var2);
         if (var3.containsKey(MultiplierType.MCMMO_EXP_EARNING)) {
            var1.setRawXpGained((float)((double)var1.getRawXpGained() * (Double)var3.get(MultiplierType.MCMMO_EXP_EARNING)));
         }
      }

   }
}
