package me.dexuby.UltimateDrugs.guis;

import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugCraftingRecipe;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class DrugCraftingPreviewGUI extends GUI {
   private Main main;

   public DrugCraftingPreviewGUI(Main var1) {
      super("drug-crafting-preview", true);
      this.main = var1;
   }

   public Inventory getInventory() {
      Inventory var1 = this.main.getServer().createInventory(new GUIHolder(this), 45, this.main.getConfigManager().getLangString("drug-crafting-preview-gui-title"));

      for(int var2 = 0; var2 < 45; ++var2) {
         var1.setItem(var2, (new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)).displayName("§0").build());
      }

      int var5 = 10;
      int var3 = 0;

      for(int var4 = 0; var4 < 9; ++var4) {
         var1.clear(var5);
         ++var3;
         if (var3 < 3) {
            ++var5;
         } else {
            var5 += 7;
            var3 = 0;
         }
      }

      return var1;
   }

   public void open(Player var1, Object... var2) {
      Inventory var3 = this.getInventory();
      if (var2.length > 0) {
         DrugCraftingRecipe var4 = (DrugCraftingRecipe)var2[0];
         if (var4 != null) {
            if (var4.getRecipe() instanceof ShapedRecipe) {
               ShapedRecipe var16 = (ShapedRecipe)var4.getRecipe();
               int var17 = 10;
               int var18 = 0;

               for(String var11 : var16.getShape()) {
                  for(char var15 : var11.toCharArray()) {
                     if (var16.getIngredientMap().containsKey(var15)) {
                        var3.setItem(var17, (ItemStack)var16.getIngredientMap().get(var15));
                        ++var18;
                        if (var18 < 3) {
                           ++var17;
                        } else {
                           var17 += 7;
                           var18 = 0;
                        }
                     }
                  }
               }
            } else {
               int var5 = 10;
               int var6 = 0;

               for(ItemStack var8 : var4.getIngredients()) {
                  for(int var9 = 0; var9 < var8.getAmount(); ++var9) {
                     ItemStack var10 = var8.clone();
                     var10.setAmount(1);
                     var3.setItem(var5, var10);
                     ++var6;
                     if (var6 < 3) {
                        ++var5;
                     } else {
                        var5 += 7;
                        var6 = 0;
                     }
                  }
               }
            }

            var3.setItem(24, var4.getRecipe().getResult());
         }
      }

      var1.openInventory(var3);
   }

   public void onClick(InventoryClickEvent var1) {
   }
}
