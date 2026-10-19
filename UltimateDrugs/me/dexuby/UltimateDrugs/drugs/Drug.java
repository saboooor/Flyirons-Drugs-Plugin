package me.dexuby.UltimateDrugs.drugs;

import java.util.Collection;
import java.util.Map;
import me.dexuby.UltimateDrugs.drugs.actions.ConsumeAction;
import me.dexuby.UltimateDrugs.drugs.chatslur.ChatSlurType;
import me.dexuby.UltimateDrugs.drugs.growing.Growing;
import org.bukkit.inventory.ItemStack;

public class Drug {
   private String id;
   private boolean legal;
   private double sellPrice;
   private double buyPrice;
   private int effectDuration;
   private ItemStack drugItemStack;
   private ConsumeOption consumeOption;
   private boolean removeOnConsume;
   private int exchangeItemLimit;
   private Collection<ExchangeItem> consumeExchangeItems;
   private ConsumeConditions consumeConditions;
   private boolean resetOnConsume;
   private ChatSlurType chatSlurType;
   private double globalSoundVolumeModifier;
   private double globalSoundPitchModifier;
   private boolean removeOnDisconnect;
   private boolean removeOnDeath;
   private boolean pauseWhileOffline;
   private Collection<ConsumeAction> consumeActions;
   private Map<MultiplierType, Double> multipliers;
   private Collection<DrugCraftingRecipe> craftingRecipes;
   private Collection<DrugFurnaceRecipe> furnaceRecipes;
   private Collection<DrugBrewingRecipe> drugBrewingRecipes;
   private Collection<DrugRelatedGood> drugRelatedGoods;
   private Growing growing;

   public Drug(String var1, boolean var2, double var3, double var5, int var7, ItemStack var8, ConsumeOption var9, boolean var10, int var11, Collection<ExchangeItem> var12, ConsumeConditions var13, boolean var14, ChatSlurType var15, double var16, double var18, boolean var20, boolean var21, boolean var22, Collection<ConsumeAction> var23, Map<MultiplierType, Double> var24, Collection<DrugCraftingRecipe> var25, Collection<DrugFurnaceRecipe> var26, Collection<DrugBrewingRecipe> var27, Collection<DrugRelatedGood> var28, Growing var29) {
      this.id = var1;
      this.legal = var2;
      this.sellPrice = var3;
      this.buyPrice = var5;
      this.effectDuration = var7;
      this.drugItemStack = var8;
      this.consumeOption = var9;
      this.removeOnConsume = var10;
      this.exchangeItemLimit = var11;
      this.consumeExchangeItems = var12;
      this.consumeConditions = var13;
      this.resetOnConsume = var14;
      this.chatSlurType = var15;
      this.globalSoundVolumeModifier = var16;
      this.globalSoundPitchModifier = var18;
      this.removeOnDisconnect = var20;
      this.removeOnDeath = var21;
      this.pauseWhileOffline = var22;
      this.consumeActions = var23;
      this.multipliers = var24;
      this.craftingRecipes = var25;
      this.furnaceRecipes = var26;
      this.drugBrewingRecipes = var27;
      this.drugRelatedGoods = var28;
      this.growing = var29;
   }

   public String getId() {
      return this.id;
   }

   public boolean isLegal() {
      return this.legal;
   }

   public double getSellPrice() {
      return this.sellPrice;
   }

   public double getBuyPrice() {
      return this.buyPrice;
   }

   public int getEffectDuration() {
      return this.effectDuration;
   }

   public ItemStack getDrugItemStack() {
      return this.drugItemStack;
   }

   public ConsumeOption getConsumeOption() {
      return this.consumeOption;
   }

   public boolean doRemoveOnConsume() {
      return this.removeOnConsume;
   }

   public int getExchangeItemLimit() {
      return this.exchangeItemLimit;
   }

   public Collection<ExchangeItem> getConsumeExchangeItems() {
      return this.consumeExchangeItems;
   }

   public ConsumeConditions getConsumeConditions() {
      return this.consumeConditions;
   }

   public boolean doResetOnConsume() {
      return this.resetOnConsume;
   }

   public ChatSlurType getChatSlurType() {
      return this.chatSlurType;
   }

   public double getGlobalSoundVolumeModifier() {
      return this.globalSoundVolumeModifier;
   }

   public double getGlobalSoundPitchModifier() {
      return this.globalSoundPitchModifier;
   }

   public boolean doRemoveOnDisconnect() {
      return this.removeOnDisconnect;
   }

   public boolean doRemoveOnDeath() {
      return this.removeOnDeath;
   }

   public boolean doPauseWhileOffline() {
      return this.pauseWhileOffline;
   }

   public Collection<ConsumeAction> getConsumeActions() {
      return this.consumeActions;
   }

   public Map<MultiplierType, Double> getMultipliers() {
      return this.multipliers;
   }

   public Collection<DrugCraftingRecipe> getCraftingRecipes() {
      return this.craftingRecipes;
   }

   public Collection<DrugFurnaceRecipe> getFurnaceRecipes() {
      return this.furnaceRecipes;
   }

   public Collection<DrugBrewingRecipe> getBrewingRecipes() {
      return this.drugBrewingRecipes;
   }

   public Collection<DrugRelatedGood> getDrugRelatedGoods() {
      return this.drugRelatedGoods;
   }

   public Growing getGrowing() {
      return this.growing;
   }
}
