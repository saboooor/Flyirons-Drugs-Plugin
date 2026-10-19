package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.guis.DrugBoostersGUI;
import me.dexuby.UltimateDrugs.guis.DrugBrewingPreviewGUI;
import me.dexuby.UltimateDrugs.guis.DrugBrewingRecipeListGUI;
import me.dexuby.UltimateDrugs.guis.DrugBuyGUI;
import me.dexuby.UltimateDrugs.guis.DrugCraftingPreviewGUI;
import me.dexuby.UltimateDrugs.guis.DrugCraftingRecipeListGUI;
import me.dexuby.UltimateDrugs.guis.DrugFurnacePreviewGUI;
import me.dexuby.UltimateDrugs.guis.DrugFurnaceRecipeListGUI;
import me.dexuby.UltimateDrugs.guis.DrugGrowingInfoGUI;
import me.dexuby.UltimateDrugs.guis.DrugInfoGUI;
import me.dexuby.UltimateDrugs.guis.DrugListGUI;
import me.dexuby.UltimateDrugs.guis.DrugSellGUI;
import me.dexuby.UltimateDrugs.guis.GUI;
import org.bukkit.inventory.Inventory;

public class GUIManager {
   private Collection<GUI> registeredGUIs = new ArrayList();
   private Map<UUID, Inventory> previousGUI = new HashMap();

   public GUIManager(Main var1) {
      this.registerGUI(new DrugBoostersGUI(var1));
      this.registerGUI(new DrugBrewingPreviewGUI(var1));
      this.registerGUI(new DrugBrewingRecipeListGUI(var1));
      this.registerGUI(new DrugBuyGUI(var1));
      this.registerGUI(new DrugCraftingPreviewGUI(var1));
      this.registerGUI(new DrugCraftingRecipeListGUI(var1));
      this.registerGUI(new DrugFurnacePreviewGUI(var1));
      this.registerGUI(new DrugFurnaceRecipeListGUI(var1));
      this.registerGUI(new DrugGrowingInfoGUI(var1));
      this.registerGUI(new DrugInfoGUI(var1));
      this.registerGUI(new DrugListGUI(var1));
      this.registerGUI(new DrugSellGUI(var1));
   }

   public GUI getGUIById(String var1) {
      return (GUI)this.registeredGUIs.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null);
   }

   private void registerGUI(GUI var1) {
      this.registeredGUIs.add(var1);
   }

   public boolean hasPreviousGUI(UUID var1) {
      return this.previousGUI.containsKey(var1);
   }

   public void setPreviousGUI(UUID var1, Inventory var2) {
      this.previousGUI.put(var1, var2);
   }

   public void clearPreviousGUI(UUID var1) {
      this.previousGUI.remove(var1);
   }

   public Inventory pollPreviousGUI(UUID var1) {
      Map.Entry var2 = (Map.Entry)this.previousGUI.entrySet().stream().filter((var1x) -> ((UUID)var1x.getKey()).equals(var1)).findFirst().orElse((Object)null);
      if (var2 != null) {
         this.clearPreviousGUI((UUID)var2.getKey());
         return (Inventory)var2.getValue();
      } else {
         return null;
      }
   }
}
