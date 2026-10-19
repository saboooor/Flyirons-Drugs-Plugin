package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SoundEffectAction extends ConsumeAction {
   private Sound sound;
   private String customSound;
   private float volume;
   private float pitch;
   private boolean clientSided;

   public SoundEffectAction(int var1, double var2, int var4, int var5, String var6, Sound var7, float var8, float var9, boolean var10) {
      super(var1, var2, var4, var5, var6);
      this.sound = var7;
      this.volume = var8;
      this.pitch = var9;
      this.clientSided = var10;
   }

   public SoundEffectAction(int var1, double var2, int var4, int var5, String var6, String var7, float var8, float var9, boolean var10) {
      super(var1, var2, var4, var5, var6);
      this.customSound = var7;
      this.volume = var8;
      this.pitch = var9;
      this.clientSided = var10;
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
      if (this.clientSided) {
         if (!(var1 instanceof Player)) {
            return;
         }

         Player var2 = (Player)var1;
         if (this.sound != null) {
            var2.playSound(var1.getLocation(), this.sound, this.volume, this.pitch);
         } else {
            var2.playSound(var1.getLocation(), this.customSound, this.volume, this.pitch);
         }
      } else if (this.sound != null) {
         var1.getWorld().playSound(var1.getLocation(), this.sound, this.volume, this.pitch);
      } else {
         var1.getWorld().playSound(var1.getLocation(), this.customSound, this.volume, this.pitch);
      }

   }

   public Sound getSound() {
      return this.sound;
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public boolean isClientSided() {
      return this.clientSided;
   }

   public SoundEffectAction clone() {
      return this.sound != null ? new SoundEffectAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.sound, this.volume, this.pitch, this.clientSided) : new SoundEffectAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.customSound, this.volume, this.pitch, this.clientSided);
   }
}
