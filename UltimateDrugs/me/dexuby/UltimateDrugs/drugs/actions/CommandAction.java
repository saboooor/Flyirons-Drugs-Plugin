package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandAction extends ConsumeAction {
   private String command;
   private CommandSenderType senderType;

   public CommandAction(int var1, double var2, int var4, int var5, String var6, String var7, CommandSenderType var8) {
      super(var1, var2, var4, var5, var6);
      this.command = var7;
      this.senderType = var8;
   }

   public void execute(UUID var1, Drug var2) {
      OfflinePlayer var3 = Bukkit.getOfflinePlayer(var1);
      HashMap var4 = new HashMap();
      var4.put("%player%", var3.getName() != null ? var3.getName() : "Unknown");
      var4.put("%drug_id%", var2.getId());
      if (this.getTargetSelector() != null) {
         Object var5 = this.senderType == CommandSenderType.PLAYER ? Bukkit.getPlayer(var1) : Bukkit.getServer().getConsoleSender();
         if (var5 == null) {
            return;
         }

         for(Entity var8 : ReflectionUtils.getEntitiesFromSelector((CommandSender)var5, this.getTargetSelector())) {
            if (var8 instanceof Player) {
               Player var9 = (Player)var8;
               if (this.senderType == CommandSenderType.TARGET) {
                  var5 = var9;
               }

               var4.put("%target%", var9.getName());
               this.apply((CommandSender)var5, var4);
            }
         }
      } else {
         Object var10;
         if (this.senderType == CommandSenderType.PLAYER) {
            Player var11 = Bukkit.getPlayer(var1);
            if (var11 == null || !var11.isValid() || !var11.isOnline()) {
               return;
            }

            var10 = var11;
         } else {
            var10 = Bukkit.getServer().getConsoleSender();
         }

         this.apply((CommandSender)var10, var4);
      }

   }

   private void apply(CommandSender var1, Map<String, String> var2) {
      Bukkit.getServer().dispatchCommand(var1, TextUtils.replaceVariables(this.command, var2));
   }

   public String getCommand() {
      return this.command;
   }

   public CommandAction clone() {
      return new CommandAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.command, this.senderType);
   }
}
