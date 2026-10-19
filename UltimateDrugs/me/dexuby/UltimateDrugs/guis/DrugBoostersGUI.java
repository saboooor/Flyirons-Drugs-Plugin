package me.dexuby.UltimateDrugs.guis;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugBooster;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.managers.DrugBoosterManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import me.dexuby.UltimateDrugs.utils.TimeUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugBoostersGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugBoostersGUI(Main var1) {
      super("drug-boosters", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 27, this.main.getConfigManager().getLangString("drug-boosters-gui-title"));
      return var1;
   }

   public void open(Player var1, Object... var2) {
      Collection var3 = this.main.getDrugBoosterManager().getAvailablePlayerDrugBoosters(var1);
      Inventory var4 = this.main.getServer().createInventory(new GUIHolder(this), Math.max((int)Math.round(Math.ceil((double)var3.size() / (double)9.0F) * (double)9.0F), 9), this.main.getConfigManager().getLangString("drug-boosters-gui-title"));

      for(DrugBooster var6 : var3) {
         HashMap var7 = new HashMap();
         var7.put("%drug_booster_id%", var6.getId());
         var7.put("%drug_booster_all_drugs%", var6.isActive() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
         var7.put("%drug_booster_affected_drugs%", var6.getAffectedDrugs().size() > 0 ? String.join("\n", var6.getAffectedDrugs()) : this.configManager.getLangString("none"));
         var7.put("%drug_booster_multiplier%", Double.toString(var6.getMultiplier()));
         var7.put("%drug_booster_stackable%", var6.isStackable() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
         var7.put("%drug_booster_duration%", TimeUtils.convertSecondsToFormattedTime(var6.getDuration()));
         ItemStack var8 = (new ItemBuilder(Material.NETHER_STAR)).displayName(TextUtils.replaceVariables(this.configManager.getLangString("drug-booster-item-display-name"), var7)).lore(TextUtils.replaceVariables((List)this.configManager.getLangStringList("drug-booster-item-lore"), var7)).build();
         var8 = ReflectionUtils.setItemStackStringTag(var8, "booster-id", var6.getId());
         var4.addItem(new ItemStack[]{var8});
      }

      var1.openInventory(var4);
   }

   public void onClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getCurrentItem() != null) {
         DrugBoosterManager var3 = this.main.getDrugBoosterManager();
         String var4 = ReflectionUtils.getItemStackStringTag(var1.getCurrentItem(), "booster-id");
         if (var4 != null && !var4.equals("")) {
            DrugBooster var5 = var3.getDrugBoosterById(var4);
            HashMap var6 = new HashMap();
            var6.put("%drug_booster_owner%", var2.getName());
            var6.put("%drug_booster_id%", var5.getId());
            var6.put("%drug_booster_all_drugs%", var5.isActive() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
            var6.put("%drug_booster_affected_drugs%", var5.getAffectedDrugs().size() > 0 ? String.join("\n", var5.getAffectedDrugs()) : this.configManager.getLangString("none"));
            var6.put("%drug_booster_multiplier%", Double.toString(var5.getMultiplier()));
            var6.put("%drug_booster_stackable%", var5.isStackable() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
            var6.put("%drug_booster_duration%", TimeUtils.convertSecondsToFormattedTime(var5.getDuration()));
            var3.activateBooster(var5);
            var2.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("booster-activated"), var6));
            this.main.getServer().broadcastMessage(TextUtils.replaceVariables(this.configManager.getLangString("booster-activation-broadcast"), var6));
            var2.closeInventory();
         }
      }

   }
}
