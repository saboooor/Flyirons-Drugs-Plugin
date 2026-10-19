package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ParticleEffectAction extends ConsumeAction {
   private Particle particle;
   private int count;
   private double directionMultiplier;
   private double offsetX;
   private double offsetY;
   private double offsetZ;
   private double randomOffsetX;
   private double randomOffsetY;
   private double randomOffsetZ;
   private double extra;
   private boolean clientSided;

   public ParticleEffectAction(int var1, double var2, int var4, int var5, String var6, Particle var7, int var8, double var9, double var11, double var13, double var15, double var17, double var19, double var21, double var23, boolean var25) {
      super(var1, var2, var4, var5, var6);
      this.particle = var7;
      this.count = var8;
      this.directionMultiplier = var9;
      this.offsetX = var11;
      this.offsetY = var13;
      this.offsetZ = var15;
      this.randomOffsetX = var17;
      this.randomOffsetY = var19;
      this.randomOffsetZ = var21;
      this.extra = var23;
      this.clientSided = var25;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline()) {
         if (this.getTargetSelector() != null) {
            for(Entity var6 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
               this.apply(var6);
            }
         } else {
            this.apply(var3);
         }

      }
   }

   private void apply(Entity var1) {
      Location var2 = var1.getLocation();
      var2.add(this.offsetX, this.offsetY, this.offsetZ);
      var2.add(var1.getLocation().getDirection().setY(0).normalize().multiply(this.directionMultiplier));
      if (this.clientSided) {
         if (var1 instanceof Player) {
            ((Player)var1).spawnParticle(this.particle, var2, this.count, this.randomOffsetX, this.randomOffsetY, this.randomOffsetZ, this.extra);
         }
      } else {
         var1.getWorld().spawnParticle(this.particle, var2, this.count, this.randomOffsetX, this.randomOffsetY, this.randomOffsetZ, this.extra);
      }

   }

   public void execute(Location var1) {
      if (var1.getWorld() != null) {
         var1.add(this.offsetX, this.offsetY, this.offsetZ);
         var1.getWorld().spawnParticle(this.particle, var1, this.count, this.randomOffsetX, this.randomOffsetY, this.randomOffsetZ, this.extra);
      }

   }

   public Particle getParticle() {
      return this.particle;
   }

   public int getCount() {
      return this.count;
   }

   public double getDirectionMultiplier() {
      return this.directionMultiplier;
   }

   public double getOffsetX() {
      return this.offsetX;
   }

   public double getOffsetY() {
      return this.offsetY;
   }

   public double getOffsetZ() {
      return this.offsetZ;
   }

   public double getRandomOffsetX() {
      return this.randomOffsetX;
   }

   public double getRandomOffsetY() {
      return this.randomOffsetY;
   }

   public double getRandomOffsetZ() {
      return this.randomOffsetZ;
   }

   public double getExtra() {
      return this.extra;
   }

   public boolean isClientSided() {
      return this.clientSided;
   }

   public ParticleEffectAction clone() {
      return new ParticleEffectAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.particle, this.count, this.directionMultiplier, this.offsetX, this.offsetY, this.offsetZ, this.randomOffsetX, this.randomOffsetY, this.randomOffsetZ, this.extra, this.clientSided);
   }
}
