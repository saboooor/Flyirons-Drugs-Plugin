package me.dexuby.UltimateDrugs.drugs;

import java.util.Collection;
import java.util.UUID;

public class DrugBooster {
   private String id;
   private UUID ownerUUID;
   private boolean ownerOnly;
   private boolean allDrugs;
   private Collection<String> affectedDrugs;
   private double multiplier;
   private boolean stackable;
   private boolean active;
   private long duration;
   private long expirationTimestamp;

   public DrugBooster(String var1, UUID var2, boolean var3, boolean var4, Collection<String> var5, double var6, boolean var8, boolean var9, long var10, long var12) {
      this.id = var1;
      this.ownerUUID = var2;
      this.ownerOnly = var3;
      this.allDrugs = var4;
      this.affectedDrugs = var5;
      this.multiplier = var6;
      this.stackable = var8;
      this.active = var9;
      this.duration = var10;
      this.expirationTimestamp = var12;
   }

   public String getId() {
      return this.id;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public boolean isOwnerOnly() {
      return this.ownerOnly;
   }

   public boolean isAllDrugs() {
      return this.allDrugs;
   }

   public Collection<String> getAffectedDrugs() {
      return this.affectedDrugs;
   }

   public double getMultiplier() {
      return this.multiplier;
   }

   public boolean isStackable() {
      return this.stackable;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean var1) {
      this.active = var1;
   }

   public long getDuration() {
      return this.duration;
   }

   public long getExpirationTimestamp() {
      return this.expirationTimestamp;
   }

   public void setExpirationTimestamp(long var1) {
      this.expirationTimestamp = var1;
   }
}
