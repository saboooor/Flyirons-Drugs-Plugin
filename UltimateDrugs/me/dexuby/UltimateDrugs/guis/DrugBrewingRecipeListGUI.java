package me.dexuby.UltimateDrugs.guis;

import java.util.Objects;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugBrewingRecipe;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugBrewingRecipeListGUI extends GUI {
   private Main main;
   private ConfigManager configManager;

   public DrugBrewingRecipeListGUI(Main var1) {
      super("drug-brewing-recipe-list", true);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public Inventory getInventory() {
      return this.main.getServer().createInventory(new GUIHolder(this), 0, this.main.getConfigManager().getLangString("drug-brewing-recipe-list-gui-title"));
   }

   public void open(Player var1, Object... var2) {
      if (var2.length > 0) {
         Drug var3 = (Drug)var2[0];
         Inventory var4 = this.main.getServer().createInventory(new GUIHolder(this), (int)Math.round(Math.ceil(((double)var3.getBrewingRecipes().size() + (double)1.0F) / (double)9.0F) * (double)9.0F), this.main.getConfigManager().getLangString("drug-brewing-recipe-list-gui-title"));

         for(DrugBrewingRecipe var6 : var3.getBrewingRecipes()) {
            var4.addItem(new ItemStack[]{ReflectionUtils.setItemStackStringTag((new ItemBuilder((ItemStack)var6.getResults().get(0))).appendLore(this.configManager.getLangStringList("brewing-recipe-list-lore")).build(), "recipe-id", var6.getId())});
         }

         var4.setItem(var4.getSize() - 1, ReflectionUtils.setItemStackStringTag((new ItemBuilder(Material.BARRIER)).displayName(this.configManager.getLangString("back-item-display-name")).lore(this.configManager.getLangStringList("back-item-lore")).build(), "drug-id", var3.getId()));
         var1.openInventory(var4);
      }

   }

   public void onClick(InventoryClickEvent var1) {
      Player var2 = (Player)var1.getWhoClicked();
      if (var1.getClickedInventory() != null && var1.getCurrentItem() != null && var1.getCurrentItem().getType() != Material.AIR) {
         String var3 = ReflectionUtils.getItemStackStringTag((ItemStack)Objects.requireNonNull(var1.getClickedInventory().getItem(var1.getClickedInventory().getSize() - 1)), "drug-id");
         Drug var4 = this.main.getDrugManager().getDrugById(var3);
         if (var1.getSlot() == var1.getClickedInventory().getSize() - 1) {
            GUI var5 = this.main.getGUIManager().getGUIById("drug-info");
            var5.open(var2, var4);
         } else {
            String var8 = ReflectionUtils.getItemStackStringTag((ItemStack)Objects.requireNonNull(var1.getCurrentItem()), "recipe-id");
            DrugBrewingRecipe var6 = (DrugBrewingRecipe)var4.getBrewingRecipes().stream().filter((var1x) -> var1x.getId().equals(var8)).findFirst().orElse((Object)null);
            GUI var7 = this.main.getGUIManager().getGUIById("drug-brewing-preview");
            var7.open(var2, var6);
            this.main.getGUIManager().setPreviousGUI(var2.getUniqueId(), var1.getClickedInventory());
         }
      }

   }
}
