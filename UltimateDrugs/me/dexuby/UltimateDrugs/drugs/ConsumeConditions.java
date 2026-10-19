package me.dexuby.UltimateDrugs.drugs;

import java.util.ArrayList;
import java.util.List;

public class ConsumeConditions {
   private boolean sneakRequired;
   private double maxCurrentHealth;
   private double maxCurrentFoodLevel;
   private List<String> disabledWorldList;
   private List<String> disabledRegionList;
   private List<ConsumeRequirementItem> requiredItemList;

   public ConsumeConditions() {
      this.sneakRequired = false;
      this.maxCurrentHealth = (double)0.0F;
      this.maxCurrentFoodLevel = (double)0.0F;
      this.disabledWorldList = new ArrayList();
      this.disabledRegionList = new ArrayList();
      this.requiredItemList = new ArrayList();
   }

   public ConsumeConditions(boolean var1, double var2, double var4, List<String> var6, List<String> var7, List<ConsumeRequirementItem> var8) {
      this.sneakRequired = var1;
      this.maxCurrentHealth = var2;
      this.maxCurrentFoodLevel = var4;
      this.disabledWorldList = var6;
      this.disabledRegionList = var7;
      this.requiredItemList = var8;
   }

   public boolean isSneakRequired() {
      return this.sneakRequired;
   }

   public double getMaxCurrentHealth() {
      return this.maxCurrentHealth;
   }

   public double getMaxCurrentFoodLevel() {
      return this.maxCurrentFoodLevel;
   }

   public List<String> getDisabledWorldList() {
      return this.disabledWorldList;
   }

   public List<String> getDisabledRegionList() {
      return this.disabledRegionList;
   }

   public List<ConsumeRequirementItem> getRequiredItemList() {
      return this.requiredItemList;
   }
}
