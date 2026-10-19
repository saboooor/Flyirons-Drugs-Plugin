package me.dexuby.UltimateDrugs.config;

import java.util.ArrayList;
import me.dexuby.UltimateDrugs.drugs.growing.processors.ProcessorType;

public enum SettingsHolder {
   VANILLA_AUTO_REPLANT(false),
   VANILLA_AUTO_IGNORE_DRUG_PLANTS(true),
   VANILLA_AUTO_REPLANT_BLACKLIST(new ArrayList()),
   VANILLA_STORE_PLAYER_BLOCKS(false),
   BLOCK_SHUTDOWN_SAVE(false),
   PLAYER_PLANT_LIMIT(-1),
   PLANT_PROCESSOR_TYPE(ProcessorType.SYNC),
   STRUCTURE_PREVIEW(true),
   STRUCTURE_PREVIEW_UPDATE_DELAY(5L),
   DELETE_PLANT_ON_FAILED_LOAD(false),
   PERFORMANCE_MODE(false);

   private Object value;

   private SettingsHolder(Object var3) {
      this.value = var3;
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object var1) {
      this.value = var1;
   }
}
