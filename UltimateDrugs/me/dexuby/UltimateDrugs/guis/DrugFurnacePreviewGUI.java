package me.dexuby.UltimateDrugs.guis;

import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugFurnaceRecipe;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DrugFurnacePreviewGUI extends GUI {
   private Main main;

   public DrugFurnacePreviewGUI(Main var1) {
      super("drug-furnace-preview", true);
      this.main = var1;
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 27, this.main.getConfigManager().getLangString("drug-furnace-preview-gui-title"));

      for(int var2 = 0; var2 < 27; ++var2) {
         var1.setItem(var2, (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build());
      }

      int var4 = 11;

      for(int var3 = 0; var3 < 3; ++var3) {
         var1.clear(var4);
         var4 += 2;
      }

      return var1;
   }

   public void open(Player var1, Object... var2) {
      Inventory var3 = this.getInventory();
      if (var2.length > 0) {
         DrugFurnaceRecipe var4 = (DrugFurnaceRecipe)var2[0];
         if (var4 != null) {
            var3.setItem(11, var4.getFurnaceRecipe().getInput());
            var3.setItem(13, new ItemStack(Material.COAL));
            var3.setItem(15, var4.getFurnaceRecipe().getResult());
         }
      }

      var1.openInventory(var3);
   }

   public void onClick(InventoryClickEvent var1) {
   }
}
