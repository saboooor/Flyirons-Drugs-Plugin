package me.dexuby.UltimateDrugs.guis;

import java.util.Arrays;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugBrewingRecipe;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class DrugBrewingPreviewGUI extends GUI {
   private Main main;

   public DrugBrewingPreviewGUI(Main var1) {
      super("drug-brewing-preview", true);
      this.main = var1;
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 45, this.main.getConfigManager().getLangString("drug-brewing-preview-gui-title"));

      for(int var2 = 0; var2 < 45; ++var2) {
         var1.setItem(var2, (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build());
      }

      Arrays.asList(13, 19, 30, 31, 32).forEach(var1::clear);
      return var1;
   }

   public void open(Player var1, Object... var2) {
      Inventory var3 = this.getInventory();
      if (var2.length > 0) {
         DrugBrewingRecipe var4 = (DrugBrewingRecipe)var2[0];
         if (var4 != null) {
            var3.setItem(13, var4.getInput());
            if (var4.getFuel() != null) {
               var3.setItem(19, var4.getFuel());
            }

            int[] var5 = new int[]{30};
            var4.getResults().forEach((var2x) -> {
               var3.setItem(var5[0], var2x);
               int var10002 = var5[0]++;
            });
         }
      }

      var1.openInventory(var3);
   }

   public void onClick(InventoryClickEvent var1) {
   }
}
