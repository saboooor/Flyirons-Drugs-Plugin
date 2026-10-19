package me.dexuby.UltimateDrugs.drugs.growing.processors;

import java.security.SecureRandom;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.managers.PlantManager;

public class SimpleAsyncPlantProcessor extends PlantProcessor {
   private final Main mainInstance;
   private final PlantManager plantManager;
   private final SecureRandom random = new SecureRandom();

   public SimpleAsyncPlantProcessor(Main var1) {
      super(var1);
      this.mainInstance = var1;
      this.plantManager = var1.getPlantManager();
   }

   public void start() {
   }
}
