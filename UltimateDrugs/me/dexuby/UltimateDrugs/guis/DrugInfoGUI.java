package me.dexuby.UltimateDrugs.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugInfoGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugInfoGUI(Main var1) {
      super("drug-info", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 9, this.configManager.getLangString("drug-info-gui-title"));
      var1.setItem(8, (new ItemBuilder(Material.BARRIER)).displayName(this.configManager.getLangString("back-item-display-name")).lore(this.configManager.getLangStringList("back-item-lore")).build());
      return var1;
   }

   public void open(Player var1, Object... var2) {
      Inventory var3 = this.getInventory();
      if (var2.length == 1) {
         Drug var4 = (Drug)var2[0];
         HashMap var5 = new HashMap();
         var5.put("%drug_id%", var4.getId());
         var5.put("%drug_legal%", var4.isLegal() ? this.configManager.getLangString("yes") : this.configManager.getLangString("no"));
         var5.put("%drug_effect_duration%", TextUtils.convertSecondsToFormattedTime((long)(var4.getEffectDuration() / 20)));
         var3.setItem(0, ReflectionUtils.setItemStackStringTag((new ItemBuilder(Material.NAME_TAG)).displayName(TextUtils.replaceVariables(this.configManager.getLangString("drug-info-item-display-name"), var5)).lore(TextUtils.replaceVariables((List)this.configManager.getLangStringList("drug-info-item-lore"), var5)).build(), "drug-id", var4.getId()));
         if (var4.getGrowing() != null) {
            var3.addItem(new ItemStack[]{(new ItemBuilder(Material.GRASS)).displayName(this.configManager.getLangString("drug-info-growing-item-display-name")).lore(this.configManager.getLangStringList("drug-info-growing-item-lore")).build()});
         }

         if (var4.getCraftingRecipes().size() > 0) {
            var3.addItem(new ItemStack[]{(new ItemBuilder(Material.CRAFTING_TABLE)).displayName(this.configManager.getLangString("drug-info-crafting-item-display-name")).lore(this.configManager.getLangStringList("drug-info-crafting-item-lore")).build()});
         }

         if (var4.getFurnaceRecipes().size() > 0) {
            var3.addItem(new ItemStack[]{(new ItemBuilder(Material.FURNACE)).displayName(this.configManager.getLangString("drug-info-furnace-item-display-name")).lore(this.configManager.getLangStringList("drug-info-furnace-item-lore")).build()});
         }

         if (var4.getBrewingRecipes().size() > 0) {
            var3.addItem(new ItemStack[]{(new ItemBuilder(Material.BREWING_STAND)).displayName(this.configManager.getLangString("drug-info-brewing-item-display-name")).lore(this.configManager.getLangStringList("drug-info-brewing-item-lore")).build()});
         }
      }

      var1.openInventory(var3);
   }

   public void onClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getCurrentItem() != null) {
         String var3 = ReflectionUtils.getItemStackStringTag(((Inventory)Objects.requireNonNull(var1.getClickedInventory())).getItem(0), "drug-id");
         Drug var4 = this.main.getDrugManager().getDrugById(var3);
         if (var1.getSlot() == 8) {
            GUI var5 = this.main.getGUIManager().getGUIById("drug-list");
            var5.open(var2);
         } else if (var1.getCurrentItem().getType() == Material.GRASS) {
            GUI var6 = this.main.getGUIManager().getGUIById("drug-growing-info");
            var6.open(var2, var4);
            this.main.getGUIManager().setPreviousGUI(var2.getUniqueId(), var1.getClickedInventory());
         } else if (var1.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
            GUI var7 = this.main.getGUIManager().getGUIById("drug-crafting-recipe-list");
            var7.open(var2, var4);
         } else if (var1.getCurrentItem().getType() == Material.FURNACE) {
            GUI var8 = this.main.getGUIManager().getGUIById("drug-furnace-recipe-list");
            var8.open(var2, var4);
         } else if (var1.getCurrentItem().getType() == Material.BREWING_STAND) {
            GUI var9 = this.main.getGUIManager().getGUIById("drug-brewing-recipe-list");
            var9.open(var2, var4);
         }
      }

   }
}
