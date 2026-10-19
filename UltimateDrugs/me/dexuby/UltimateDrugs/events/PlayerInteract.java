package me.dexuby.UltimateDrugs.events;

import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugConsumeEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantFertilizerUseEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantInteractEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantPrePlantEvent;
import me.dexuby.UltimateDrugs.api.DrugPlantSendStatusEvent;
import me.dexuby.UltimateDrugs.drugs.ConsumeOption;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.Fertilizer;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.utils.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

public class PlayerInteract implements Listener {
   private Main main;
   private DrugManager drugManager;

   public PlayerInteract(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent var1) {
      boolean var2 = var1.isCancelled();
      Player var3 = var1.getPlayer();
      if (var1.getHand() == EquipmentSlot.OFF_HAND) {
         ItemStack var14 = var3.getInventory().getItemInOffHand();
         if (this.drugManager.isUseableDrugItem(var14)) {
            var1.setCancelled(true);
         }

      } else {
         Block var4 = var1.getClickedBlock();
         ItemStack var5 = var3.getInventory().getItemInMainHand();
         if (var1.getAction() == Action.RIGHT_CLICK_BLOCK && var4 != null && var4.hasMetadata("ultimatedrugs-plant")) {
            var1.setCancelled(true);
            UUID var6 = UUID.fromString(((MetadataValue)var4.getMetadata("ultimatedrugs-plant").get(0)).asString());
            Plant var7 = this.main.getPlantManager().getPlantByUUID(var6);
            if (var7 != null) {
               this.main.getServer().getPluginManager().callEvent(new DrugPlantInteractEvent(var3, var7));
               if (var3.isSneaking()) {
                  this.main.getServer().getPluginManager().callEvent(new DrugPlantSendStatusEvent(var3, var7));
               }

               Fertilizer var8;
               if ((var8 = this.drugManager.getFertilizerByItemStack(var5)) != null) {
                  boolean var9 = false;

                  for(String var11 : var8.getDrugIds()) {
                     if (var11.contains(":")) {
                        String[] var12 = var11.split(":");
                        if (var12.length == 2) {
                           var11 = var12[0];
                           int var13 = Integer.parseInt(var12[1]);
                           if (var7.getParentDrugId().equalsIgnoreCase(var11) && var7.getCurrentGrowingStage().getOrderId() == var13) {
                              var9 = true;
                              break;
                           }
                        }
                     } else if (var7.getParentDrugId().equalsIgnoreCase(var11)) {
                        var9 = true;
                        break;
                     }
                  }

                  if (var9) {
                     this.main.getServer().getPluginManager().callEvent(new DrugPlantFertilizerUseEvent(var3, var7, var8));
                  }
               }
            }
         }

         Drug var15;
         if ((var15 = this.drugManager.getDrugBySeed(var5)) != null) {
            if (var1.getAction() == Action.RIGHT_CLICK_BLOCK && var4 != null) {
               var1.setCancelled(true);
               BlockFace var17 = BlockUtils.getTargetBlockFace(var1.getPlayer(), false);
               if (var17 != null) {
                  this.main.getServer().getPluginManager().callEvent(new DrugPlantPrePlantEvent(var3, var15, var4.getRelative(var17)));
               }
            }
         } else if ((var15 = this.drugManager.getDrugByItemStack(var5)) != null) {
            if (var15.getConsumeOption() == ConsumeOption.RIGHT_CLICK) {
               if (var1.getAction() == Action.RIGHT_CLICK_BLOCK || var1.getAction() == Action.RIGHT_CLICK_AIR) {
                  var1.setCancelled(true);
                  this.main.getServer().getPluginManager().callEvent(new DrugConsumeEvent(var3, var15));
               }
            } else if (!var2) {
               var1.setCancelled(false);
            }
         }

      }
   }
}
