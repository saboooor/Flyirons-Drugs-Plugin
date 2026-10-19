package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;

public abstract class ConsumeAction {
   private int delay;
   private double chance;
   private int repetitions;
   private int repetitionTickDelay;
   private String targetSelector;

   ConsumeAction(int var1, double var2, int var4, int var5, String var6) {
      this.delay = var1;
      this.chance = var2;
      this.repetitions = var4;
      this.repetitionTickDelay = var5;
      this.targetSelector = var6;
   }

   public int getDelay() {
      return this.delay;
   }

   public void setDelay(int var1) {
      this.delay = var1;
   }

   public double getChance() {
      return this.chance;
   }

   public int getRepetitions() {
      return this.repetitions;
   }

   public int getRepetitionTickDelay() {
      return this.repetitionTickDelay;
   }

   public String getTargetSelector() {
      return this.targetSelector;
   }

   public abstract void execute(UUID var1, Drug var2);

   public abstract ConsumeAction clone();
}
