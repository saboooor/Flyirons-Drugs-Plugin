package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.ActiveDrugEffect;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugDealer;
import me.dexuby.UltimateDrugs.drugs.DrugRelatedGood;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.drugs.actions.TimedConsumeAction;
import me.dexuby.UltimateDrugs.drugs.growing.Fertilizer;
import me.dexuby.UltimateDrugs.drugs.vanilla.ActiveTimedConsumeAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class DrugManager {
   private Main main;
   private List<Drug> drugs = new ArrayList();
   private List<Fertilizer> fertilizers = new ArrayList();
   private List<DrugDealer> drugDealers = new ArrayList();
   private Map<UUID, List<ActiveDrugEffect>> activePlayerDrugs = new HashMap();
   private Map<String, Drug> drugCache = new HashMap();
   private Map<UUID, List<ActiveTimedConsumeAction>> timedActionsToRemove = new HashMap();
   private Map<UUID, List<ActiveTimedConsumeAction>> timedActionsToAdd = new HashMap();
   private Map<UUID, List<ActiveTimedConsumeAction>> activeTimedConsumeActions = new HashMap();

   public DrugManager(Main var1) {
      this.main = var1;
      var1.getServer().getScheduler().runTaskTimer(var1, () -> {
         for(Map.Entry var3 : this.timedActionsToAdd.entrySet()) {
            if (this.activeTimedConsumeActions.containsKey(var3.getKey())) {
               ((List)this.activeTimedConsumeActions.get(var3.getKey())).addAll((Collection)var3.getValue());
            } else {
               this.activeTimedConsumeActions.put(var3.getKey(), var3.getValue());
            }
         }

         this.timedActionsToAdd.clear();

         for(Map.Entry var9 : this.timedActionsToRemove.entrySet()) {
            if (this.activeTimedConsumeActions.containsKey(var9.getKey())) {
               for(ActiveTimedConsumeAction var5 : (List)var9.getValue()) {
                  ((List)this.activeTimedConsumeActions.get(var9.getKey())).removeIf((var1x) -> var1x.getDrugId().equals(var5.getDrugId()) && var1x.getAction().equals(var5.getAction()));
               }
            }
         }

         this.timedActionsToRemove.clear();
         if (this.activeTimedConsumeActions.entrySet().size() > 0) {
            for(Map.Entry var10 : this.activeTimedConsumeActions.entrySet()) {
               if (((List)var10.getValue()).size() != 0) {
                  Player var11 = var1.getServer().getPlayer((UUID)var10.getKey());

                  for(ActiveTimedConsumeAction var6 : new ArrayList((Collection)var10.getValue())) {
                     if (System.currentTimeMillis() >= var6.getExpirationTimestamp()) {
                        var6.getAction().end(var11);
                        ((List)this.activeTimedConsumeActions.get(var10.getKey())).remove(var6);
                     }
                  }
               }
            }
         }

      }, 1L, 1L);
   }

   void registerDrug(Drug var1) {
      this.drugs.add(var1);
   }

   public List<Drug> getDrugs() {
      return this.drugs;
   }

   public void clearDrugCache() {
      this.drugCache.clear();
   }

   public Drug getDrugById(String var1) {
      Drug var2;
      if (this.drugCache.containsKey(var1)) {
         var2 = (Drug)this.drugCache.get(var1);
      } else {
         var2 = (Drug)this.drugs.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null);
         if (var2 != null) {
            this.drugCache.put(var1, var2);
         }
      }

      return var2;
   }

   public List<Drug> getSellableDrugs() {
      return (List)this.drugs.stream().filter((var0) -> var0.getSellPrice() > (double)0.0F).collect(Collectors.toList());
   }

   public List<DrugRelatedGood> getDrugRelatedGoods() {
      ArrayList var1 = new ArrayList();
      this.drugs.stream().map(Drug::getDrugRelatedGoods).forEach(var1::addAll);
      return var1;
   }

   public List<Drug> getBuyableDrugs() {
      return (List)this.drugs.stream().filter((var0) -> var0.getBuyPrice() > (double)0.0F).collect(Collectors.toList());
   }

   public Drug getDrugByItemStack(ItemStack var1) {
      return (Drug)this.drugs.stream().filter((var1x) -> var1x.getDrugItemStack().isSimilar(var1)).findFirst().orElse((Object)null);
   }

   public Drug getDrugBySeed(ItemStack var1) {
      return (Drug)this.drugs.stream().filter((var1x) -> var1x.getGrowing() != null && var1x.getGrowing().getSeedItemStack().isSimilar(var1)).findFirst().orElse((Object)null);
   }

   public boolean isUseableDrugItem(ItemStack var1) {
      return this.drugs.stream().anyMatch((var2) -> var2.getDrugItemStack().isSimilar(var1) || var2.getGrowing() != null && var2.getGrowing().getSeedItemStack().isSimilar(var1) || this.fertilizers.stream().anyMatch((var1x) -> var1x.getItemStack().isSimilar(var1)));
   }

   void registerFertilizer(Fertilizer var1) {
      this.fertilizers.add(var1);
   }

   public List<Fertilizer> getFertilizers() {
      return this.fertilizers;
   }

   public Fertilizer getFertilizerById(String var1) {
      return (Fertilizer)this.fertilizers.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null);
   }

   public Fertilizer getFertilizerByItemStack(ItemStack var1) {
      return (Fertilizer)this.fertilizers.stream().filter((var1x) -> var1x.getItemStack().isSimilar(var1)).findFirst().orElse((Object)null);
   }

   void registerDrugDealer(DrugDealer var1) {
      this.drugDealers.add(var1);
   }

   public DrugDealer getDrugDealerByNPCId(int var1) {
      return (DrugDealer)this.drugDealers.stream().filter((var1x) -> var1x.getNpcId() == var1).findFirst().orElse((Object)null);
   }

   public List<ActiveDrugEffect> getActiveDrugEffects(Player var1) {
      List var2 = (List)this.activePlayerDrugs.getOrDefault(var1.getUniqueId(), new ArrayList());
      var2.removeIf((var0) -> System.currentTimeMillis() >= var0.getExpirationTimestamp());
      return var2;
   }

   public ActiveDrugEffect getActiveDrugEffect(Player var1, Drug var2) {
      List var3 = this.getActiveDrugEffects(var1);
      return (ActiveDrugEffect)var3.stream().filter((var1x) -> var1x.getDrug().getId().equals(var2.getId())).findFirst().orElse((Object)null);
   }

   public boolean isPlayerOnDrugs(Player var1) {
      return this.getActiveDrugEffects(var1).size() > 0;
   }

   public boolean isPlayerOnDrug(Player var1, Drug var2) {
      return this.getActiveDrugEffects(var1).stream().anyMatch((var1x) -> var1x.getDrug().getId().equals(var2.getId()));
   }

   public void addActiveDrugEffect(Player var1, Drug var2, BukkitTask var3, Long var4) {
      List var5 = this.getActiveDrugEffects(var1);
      var5.add(new ActiveDrugEffect(var2, var3, var4));
      this.activePlayerDrugs.put(var1.getUniqueId(), var5);
   }

   public void removeActiveDrugEffect(Player var1, String var2) {
      List var3 = this.getActiveDrugEffects(var1);
      var3.removeIf((var1x) -> var1x.getDrug().getId().equals(var2));
      this.activePlayerDrugs.put(var1.getUniqueId(), var3);
   }

   public void removeActiveDrugEffect(Player var1, Drug var2) {
      this.removeActiveDrugEffect(var1, var2.getId());
   }

   public Map<MultiplierType, Double> getPlayerMultipliers(Player var1) {
      HashMap var2 = new HashMap();

      for(ActiveDrugEffect var5 : this.getActiveDrugEffects(var1)) {
         Drug var6 = var5.getDrug();
         if (var6 != null) {
            for(MultiplierType var8 : var6.getMultipliers().keySet()) {
               if (var2.containsKey(var8)) {
                  double var9 = (Double)var2.get(var8);
                  var2.put(var8, var9 + ((Double)var6.getMultipliers().get(var8) - (double)1.0F));
               } else {
                  var2.put(var8, var6.getMultipliers().get(var8));
               }
            }
         }
      }

      return var2;
   }

   public void addTimedConsumeAction(UUID var1, String var2, TimedConsumeAction var3) {
      ActiveTimedConsumeAction var4 = new ActiveTimedConsumeAction(var2, var3, System.currentTimeMillis() + (long)(var3.getDuration() / 20 * 1000));
      if (this.timedActionsToAdd.containsKey(var1)) {
         ((List)this.timedActionsToAdd.get(var1)).add(var4);
      } else {
         this.timedActionsToAdd.put(var1, new ArrayList(Collections.singletonList(var4)));
      }

   }

   public void forceEndTimedConsumeActions(UUID var1) {
      Player var2 = this.main.getServer().getPlayer(var1);
      if (this.activeTimedConsumeActions.containsKey(var1)) {
         ((List)this.activeTimedConsumeActions.get(var1)).forEach((var3) -> {
            var3.getAction().end(var2);
            if (this.timedActionsToRemove.containsKey(var1)) {
               ((List)this.timedActionsToRemove.get(var1)).add(var3);
            } else {
               this.timedActionsToRemove.put(var1, new ArrayList(Collections.singletonList(var3)));
            }

         });
      }

   }

   public void forceEndTimedConsumeActions(Player var1, String var2) {
      if (this.activeTimedConsumeActions.containsKey(var1.getUniqueId())) {
         ((List)this.activeTimedConsumeActions.get(var1.getUniqueId())).stream().filter((var1x) -> var1x.getDrugId().equals(var2)).forEach((var2x) -> {
            var2x.getAction().end(var1);
            if (this.timedActionsToRemove.containsKey(var1.getUniqueId())) {
               ((List)this.timedActionsToRemove.get(var1.getUniqueId())).add(var2x);
            } else {
               this.timedActionsToRemove.put(var1.getUniqueId(), new ArrayList(Collections.singletonList(var2x)));
            }

         });
      }

   }

   public void forceEndAllTimedConsumeActions() {
      for(Map.Entry var2 : this.activeTimedConsumeActions.entrySet()) {
         ((List)var2.getValue()).forEach((var2x) -> var2x.getAction().end(this.main.getServer().getPlayer((UUID)var2.getKey())));
      }

   }
}
