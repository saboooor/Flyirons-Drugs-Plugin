package me.dexuby.UltimateDrugs.events;

import com.gamingmesh.jobs.api.JobsPaymentEvent;
import java.util.Map;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JobsPayment implements Listener {
   private Main main;
   private DrugManager drugManager;

   public JobsPayment(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onJobsPayment(JobsPaymentEvent var1) {
      if (this.main.getServer().getPlayer(var1.getPlayer().getUniqueId()) != null) {
         Player var2 = this.main.getServer().getPlayer(var1.getPlayer().getUniqueId());
         if (this.drugManager.isPlayerOnDrugs(var2)) {
            Map var3 = this.drugManager.getPlayerMultipliers(var2);
            if (var3.containsKey(MultiplierType.JOBS_MONEY_EARNING)) {
               var1.setAmount(var1.getAmount() * (Double)var3.get(MultiplierType.JOBS_MONEY_EARNING));
            }
         }
      }

   }
}
