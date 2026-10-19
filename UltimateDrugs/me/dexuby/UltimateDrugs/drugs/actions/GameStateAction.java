package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerGameStateChange;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GameStateAction extends ConsumeAction {
   private int reason;
   private float value;

   public GameStateAction(int var1, double var2, int var4, int var5, String var6, int var7, float var8) {
      super(var1, var2, var4, var5, var6);
      this.reason = var7;
      this.value = var8;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline()) {
         if (Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            if (this.getTargetSelector() != null) {
               for(Entity var6 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
                  if (var6 instanceof Player) {
                     this.apply((Player)var6);
                  }
               }
            } else {
               this.apply(var3);
            }
         }

      }
   }

   private void apply(Player var1) {
      (new WrapperPlayServerGameStateChange()).setReason(this.reason).setValue(this.value).sendPacket(var1);
   }

   public int getReason() {
      return this.reason;
   }

   public float getValue() {
      return this.value;
   }

   public GameStateAction clone() {
      return new GameStateAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.reason, this.value);
   }
}
