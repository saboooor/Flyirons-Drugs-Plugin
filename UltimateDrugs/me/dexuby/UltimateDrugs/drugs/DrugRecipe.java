package me.dexuby.UltimateDrugs.drugs;

public abstract class DrugRecipe {
   private String id;

   DrugRecipe(String var1) {
      this.id = var1;
   }

   public String getId() {
      return this.id;
   }
}
