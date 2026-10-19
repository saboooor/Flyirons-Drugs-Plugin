package me.dexuby.UltimateDrugs.drugs;

import org.bukkit.scheduler.BukkitTask;

public class ActiveDrugEffect {
   private Drug drug;
   private BukkitTask task;
   private long expirationTimestamp;

   public ActiveDrugEffect(Drug var1, BukkitTask var2, long var3) {
      this.drug = var1;
      this.task = var2;
      this.expirationTimestamp = var3;
   }

   public Drug getDrug() {
      return this.drug;
   }

   public BukkitTask getTask() {
      return this.task;
   }

   public long getExpirationTimestamp() {
      return this.expirationTimestamp;
   }
}
