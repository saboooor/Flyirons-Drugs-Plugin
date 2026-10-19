package me.dexuby.UltimateDrugs.drugs.vanilla;

import me.dexuby.UltimateDrugs.drugs.actions.TimedConsumeAction;

public class ActiveTimedConsumeAction {
   private String drugId;
   private TimedConsumeAction action;
   private long expirationTimestamp;

   public ActiveTimedConsumeAction(String var1, TimedConsumeAction var2, long var3) {
      this.drugId = var1;
      this.action = var2;
      this.expirationTimestamp = var3;
   }

   public String getDrugId() {
      return this.drugId;
   }

   public TimedConsumeAction getAction() {
      return this.action;
   }

   public long getExpirationTimestamp() {
      return this.expirationTimestamp;
   }
}
