package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugCraftingRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeManager {
   private Main main;
   private Collection<DrugRecipe> registeredDrugRecipes = new ArrayList();

   public RecipeManager(Main var1) {
      this.main = var1;
   }

   public Collection<DrugRecipe> getRegisteredDrugRecipes() {
      return this.registeredDrugRecipes;
   }

   public DrugRecipe getDrugRecipeById(String var1) {
      return (DrugRecipe)this.registeredDrugRecipes.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null);
   }

   public List<DrugCraftingRecipe> getAllShapelessCraftingRecipes() {
      return (List)this.registeredDrugRecipes.stream().filter((var0) -> var0 instanceof DrugCraftingRecipe && ((DrugCraftingRecipe)var0).getRecipe() instanceof ShapelessRecipe).map((var0) -> (DrugCraftingRecipe)var0).collect(Collectors.toList());
   }

   void registerDrugRecipe(DrugRecipe var1) {
      this.registeredDrugRecipes.add(var1);
   }

   void unregisterDrugRecipe(String var1) {
      if (this.registeredDrugRecipes.stream().anyMatch((var1x) -> var1x.getId().equals(var1))) {
         this.registeredDrugRecipes.remove(this.registeredDrugRecipes.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null));
      }

   }
}
