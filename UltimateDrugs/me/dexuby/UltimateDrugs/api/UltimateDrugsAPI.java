package me.dexuby.UltimateDrugs.api;

import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class UltimateDrugsAPI {
   private static UltimateDrugsAPI instance;
   private static Main main;

   public boolean isPlayerOnDrugs(Player var1) {
      return main.getDrugManager().isPlayerOnDrugs(var1);
   }

   public DrugManager getDrugManager() {
      return main.getDrugManager();
   }

   public void unregisterPlant(UUID var1) {
      Plant var2 = main.getPlantManager().getPlantByUUID(var1);
      main.getPlantManager().unregisterPlant(var1);
      main.getConfigManager().removePlant(var1);

      for(Block var4 : var2.getCurrentBlocks()) {
         var4.removeMetadata("ultimatedrugs-plant", main);
      }

      main.getServer().getScheduler().runTaskAsynchronously(main, () -> main.getConfigManager().saveStorage());
   }

   public static UltimateDrugsAPI getInstance() {
      if (instance == null) {
         instance = new UltimateDrugsAPI();
         main = Main.getInstance();
      }

      return instance;
   }
}
