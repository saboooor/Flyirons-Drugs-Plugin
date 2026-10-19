package me.dexuby.UltimateDrugs.events;

import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugConsumeEvent;
import me.dexuby.UltimateDrugs.drugs.ConsumeOption;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerItemConsume implements Listener {
   private final Main main;
   private final DrugManager drugManager;

   public PlayerItemConsume(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onPlayerItemConsume(PlayerItemConsumeEvent var1) {
      Player var3 = var1.getPlayer();
      Drug var2;
      if ((var2 = this.drugManager.getDrugByItemStack(var1.getItem())) != null) {
         var1.setCancelled(true);
         if (var2.getConsumeOption() == ConsumeOption.CONSUME) {
            this.main.getServer().getPluginManager().callEvent(new DrugConsumeEvent(var3, var2));
         }
      }

   }
}
