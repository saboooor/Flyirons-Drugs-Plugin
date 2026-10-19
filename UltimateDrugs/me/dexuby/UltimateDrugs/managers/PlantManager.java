package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.drugs.growing.PlantType;

public class PlantManager {
   private final Map<UUID, Plant> plants = new HashMap();
   private final Map<UUID, Plant> growingPlants = new HashMap();
   private final Collection<Plant> toAdd = new ArrayList();
   private final Collection<UUID> toRemove = new ArrayList();

   Collection<Plant> getRegisteredPlants() {
      return this.plants.values();
   }

   public Collection<Plant> getGrowingPlants() {
      return this.growingPlants.values();
   }

   public Collection<UUID> getToRemove() {
      return this.toRemove;
   }

   public void updateGrowingPlants() {
      for(Plant var2 : this.toAdd) {
         this.growingPlants.put(var2.getUUID(), var2);
      }

      this.toAdd.clear();

      for(UUID var4 : this.toRemove) {
         this.growingPlants.remove(var4);
      }

      this.toRemove.clear();
   }

   public GrowingStage getNextGrowingStage(Plant var1, PlantType var2) {
      return (GrowingStage)var2.getGrowingStages().stream().filter((var1x) -> var1x.getOrderId() > var1.getCurrentGrowingStage().getOrderId()).findFirst().orElse(var1.getCurrentGrowingStage());
   }

   public GrowingStage getHighestGrowingStage(PlantType var1) {
      return (GrowingStage)var1.getGrowingStages().stream().max(Comparator.comparing(GrowingStage::getOrderId)).orElse((Object)null);
   }

   public void registerPlant(Plant var1) {
      this.plants.put(var1.getUUID(), var1);
   }

   public void registerGrowingPlant(Plant var1) {
      this.toAdd.add(var1);
   }

   public void unregisterPlant(UUID var1) {
      this.plants.remove(var1);
      this.toAdd.removeIf((var1x) -> var1x.getUUID().equals(var1));
      if (this.growingPlants.containsKey(var1) && !this.toRemove.contains(var1)) {
         this.toRemove.add(var1);
      }

   }

   public Plant getPlantByUUID(UUID var1) {
      return (Plant)this.plants.get(var1);
   }

   public Collection<Plant> getPlantsByOwner(UUID var1) {
      return (Collection)this.plants.values().stream().filter((var1x) -> var1x.getOwner().equals(var1)).collect(Collectors.toList());
   }
}
