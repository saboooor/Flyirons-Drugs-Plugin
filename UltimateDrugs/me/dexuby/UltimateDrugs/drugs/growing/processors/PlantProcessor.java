package me.dexuby.UltimateDrugs.drugs.growing.processors;

import java.util.Queue;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;

public abstract class PlantProcessor {
   private final Main mainInstance;
   private Queue<Plant> plants;

   PlantProcessor(Main var1) {
      this.mainInstance = var1;
   }

   public PlantProcessor setPlants(Queue<Plant> var1) {
      this.plants = var1;
      return this;
   }

   Main getMainInstance() {
      return this.mainInstance;
   }

   Queue<Plant> getPlants() {
      return this.plants;
   }

   public abstract void start();
}
