package me.dexuby.UltimateDrugs.events;

import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugDealer;
import me.dexuby.UltimateDrugs.guis.GUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCRightClick implements Listener {
   private Main main;

   public NPCRightClick(Main var1) {
      this.main = var1;
   }

   @EventHandler
   public void onNPCRightClick(NPCRightClickEvent var1) {
      DrugDealer var2 = this.main.getDrugManager().getDrugDealerByNPCId(var1.getNPC().getId());
      if (var2 != null) {
         String var3 = String.format("drug-%s", var2.getType().toString().toLowerCase());
         GUI var4 = this.main.getGUIManager().getGUIById(var3);
         if (var4 != null) {
            var4.open(var1.getClicker());
         }
      }

   }
}
