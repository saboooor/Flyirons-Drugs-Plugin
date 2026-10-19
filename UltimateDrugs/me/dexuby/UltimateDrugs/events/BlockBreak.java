package me.dexuby.UltimateDrugs.events;

import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugPlantBreakEvent;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.handlers.PlantHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.MetadataValue;

public class BlockBreak implements Listener {
   private final Main main;
   private final PlantHandler plantHandler;

   public BlockBreak(Main var1) {
      this.main = var1;
      this.plantHandler = var1.getPlantHandler();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent var1) {
      if (var1.getBlock().hasMetadata("ultimatedrugs-plant")) {
         var1.setCancelled(true);
         UUID var2 = UUID.fromString(((MetadataValue)var1.getBlock().getMetadata("ultimatedrugs-plant").get(0)).asString());
         Plant var3 = this.main.getPlantManager().getPlantByUUID(var2);
         if (var3 != null) {
            DrugPlantBreakEvent var4 = new DrugPlantBreakEvent(var1.getPlayer(), var3, var1.getBlock());
            if (!(Boolean)SettingsHolder.PERFORMANCE_MODE.getValue()) {
               this.main.getServer().getPluginManager().callEvent(var4);
               if (var4.isCancelled()) {
                  return;
               }
            }

            this.plantHandler.onDrugPlantBreak(var4);
         } else {
            var1.getBlock().removeMetadata("ultimatedrugs-plant", this.main);
         }
      }

   }
}
