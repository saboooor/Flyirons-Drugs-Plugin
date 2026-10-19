package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugBooster;
import org.bukkit.entity.Player;

public class DrugBoosterManager {
   private Main main;
   private Collection<DrugBooster> inactiveDrugBoosters = new ArrayList();
   private Collection<DrugBooster> activeDrugBoosters = new ArrayList();

   public DrugBoosterManager(Main var1) {
      this.main = var1;
      var1.getServer().getScheduler().runTaskTimer(var1, () -> {
         Iterator var2 = this.activeDrugBoosters.iterator();

         while(var2.hasNext()) {
            DrugBooster var3 = (DrugBooster)var2.next();
            if (System.currentTimeMillis() >= var3.getExpirationTimestamp()) {
               var1.getConfigManager().removeDrugBooster(var3);
               var2.remove();
               var1.getServer().broadcastMessage(var1.getConfigManager().getLangString("booster-expired-broadcast"));
            }
         }

      }, 20L, 20L);
   }

   public Collection<DrugBooster> getInactiveDrugBoosters() {
      return this.inactiveDrugBoosters;
   }

   public Collection<DrugBooster> getActiveDrugBoosters() {
      return this.activeDrugBoosters;
   }

   public DrugBooster getDrugBoosterById(String var1) {
      ArrayList var2 = new ArrayList();
      var2.addAll(this.inactiveDrugBoosters);
      var2.addAll(this.activeDrugBoosters);
      return (DrugBooster)var2.stream().filter((var1x) -> var1x.getId().equals(var1)).findFirst().orElse((Object)null);
   }

   public Collection<DrugBooster> getAvailablePlayerDrugBoosters(Player var1) {
      return (Collection)this.inactiveDrugBoosters.stream().filter((var1x) -> var1x.getOwnerUUID().equals(var1.getUniqueId())).collect(Collectors.toList());
   }

   public void addDrugBooster(DrugBooster var1) {
      this.inactiveDrugBoosters.add(var1);
   }

   public void removeDrugBooster(DrugBooster var1) {
      this.inactiveDrugBoosters.remove(var1);
   }

   public void addActiveDrugBooster(DrugBooster var1) {
      this.activeDrugBoosters.add(var1);
   }

   public void removeActiveDrugBooster(DrugBooster var1) {
      this.activeDrugBoosters.remove(var1);
   }

   public void activateBooster(DrugBooster var1) {
      if (!var1.isActive()) {
         this.removeDrugBooster(var1);
         var1.setActive(true);
         var1.setExpirationTimestamp(System.currentTimeMillis() + var1.getDuration() * 1000L);
         this.addActiveDrugBooster(var1);
         this.main.getConfigManager().saveDrugBooster(var1);
      }

   }

   public boolean hasActiveBooster(Drug var1) {
      return this.activeDrugBoosters.stream().anyMatch((var1x) -> var1x.isAllDrugs() || var1x.getAffectedDrugs().contains(var1));
   }

   public double getBoosterMultiplier(Drug var1, Player var2) {
      ArrayList var3 = new ArrayList(this.activeDrugBoosters);
      double var4 = this.getTotalMultiplier((Collection)var3.stream().filter((var0) -> !var0.isStackable()).collect(Collectors.toList()), var1, var2);
      double var6 = this.getTotalMultiplier((Collection)var3.stream().filter(DrugBooster::isStackable).collect(Collectors.toList()), var1, var2);
      return var4 > var6 ? var4 : var6;
   }

   private double getTotalMultiplier(Collection<DrugBooster> var1, Drug var2, Player var3) {
      double var4 = (double)1.0F;

      for(DrugBooster var7 : var1) {
         if ((!var7.isOwnerOnly() || var3.getUniqueId().equals(var7.getOwnerUUID())) && (var7.isAllDrugs() || var7.getAffectedDrugs().contains(var2.getId()))) {
            if (var7.isStackable()) {
               var4 += var7.getMultiplier() - (double)1.0F;
            } else if (var4 < var7.getMultiplier()) {
               var4 = var7.getMultiplier();
            }
         }
      }

      return var4;
   }
}
