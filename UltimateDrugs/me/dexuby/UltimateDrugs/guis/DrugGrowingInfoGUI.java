package me.dexuby.UltimateDrugs.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drop;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugGrowingInfoGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugGrowingInfoGUI(Main var1) {
      super("drug-growing-info", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      return this.main.getServer().createInventory(new GUIHolder(this), 0, this.main.getConfigManager().getLangString("drug-growing-info-gui-title"));
   }

   public void open(Player var1, Object... var2) {
      if (var2.length > 0) {
         Drug var3 = (Drug)var2[0];
         Inventory var4 = this.main.getServer().createInventory(new GUIHolder(this), (int)Math.round(Math.ceil((double)var3.getGrowing().getPlantType().getGrowingStages().size() / (double)9.0F) * (double)9.0F), this.main.getConfigManager().getLangString("drug-growing-info-gui-title"));
         int var5 = 0;

         for(GrowingStage var7 : var3.getGrowing().getPlantType().getGrowingStages()) {
            ArrayList var8 = new ArrayList();
            HashMap var9 = new HashMap();

            for(Drop var11 : var7.getDrops()) {
               var9.put("%drop_item_type%", var11.getItemStack().getType().name());
               var9.put("%drop_item_display_name%", var11.getItemStack().getItemMeta() != null ? (var11.getItemStack().getItemMeta().hasDisplayName() ? var11.getItemStack().getItemMeta().getDisplayName() : var11.getItemStack().getType().name()) : var11.getItemStack().getType().name());
               var9.put("%drop_min_amount%", Integer.toString(var11.getMinAmount()));
               var9.put("%drop_max_amount%", Integer.toString(var11.getMaxAmount()));
               var9.put("%drop_drop_chance%", Double.toString(var11.getDropChance()));
               var8.add(TextUtils.replaceVariables(this.configManager.getLangString("drug-growing-info-drop-layout"), var9));
            }

            var9.clear();
            var9.put("%growing_stage_order_id%", Integer.toString(var7.getOrderId()));
            var9.put("%growing_stage_grown%", var7.isGrown() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
            var9.put("%growing_stage_delay%", Integer.toString(var7.getGrowDelay()));
            var9.put("%growing_stage_drops%", var8.toString());
            ItemStack var15;
            if (((GrowingBlock)var7.getGrowingBlocks().get(0)).getSkullTexture() != null) {
               var15 = ReflectionUtils.getTexturedSkull(((GrowingBlock)var7.getGrowingBlocks().get(0)).getSkullTexture());
            } else {
               Material var16 = ((GrowingBlock)var7.getGrowingBlocks().get(0)).getBlockData().getMaterial();
               if (var16.isItem()) {
                  var15 = new ItemStack(var16);
               } else {
                  var15 = new ItemStack(Material.BARRIER);
               }
            }

            ArrayList var17 = new ArrayList();

            for(String var14 : this.configManager.getLangStringList("drug-growing-info-item-lore")) {
               if (var14.contains("%growing_stage_drops%")) {
                  var14 = var14.replace("%growing_stage_drops%", "");
                  if (var14.length() > 0) {
                     var17.add(var14);
                  }

                  var17.addAll(var8);
               } else {
                  var17.add(var14);
               }
            }

            var4.setItem(var5, (new ItemBuilder(var15)).displayName(TextUtils.replaceVariables(this.configManager.getLangString("drug-growing-info-item-display-name"), var9)).lore(TextUtils.replaceVariables((List)var17, var9)).build());
            ++var5;
         }

         var1.openInventory(var4);
      }

   }

   public void onClick(InventoryClickEvent var1) {
   }
}
