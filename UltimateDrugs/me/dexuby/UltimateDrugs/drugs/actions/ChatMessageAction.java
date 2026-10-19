package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ChatMessageAction extends ConsumeAction {
   private String chatMessage;

   public ChatMessageAction(int var1, double var2, int var4, int var5, String var6, String var7) {
      super(var1, var2, var4, var5, var6);
      this.chatMessage = var7;
   }

   public void execute(UUID var1, Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null && var3.isValid() && var3.isOnline()) {
         HashMap var4 = new HashMap();
         var4.put("%player%", var3.getName());
         var4.put("%drug_id%", var2.getId());
         if (this.getTargetSelector() != null) {
            for(Entity var7 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
               if (var7 instanceof Player) {
                  var4.put("%target%", var7.getName());
               }

               this.apply(var7, var4);
            }
         } else {
            this.apply(var3, var4);
         }
      }

   }

   private void apply(CommandSender var1, Map<String, String> var2) {
      var1.sendMessage(TextUtils.replaceVariables(this.chatMessage, var2));
   }

   public String getChatMessage() {
      return this.chatMessage;
   }

   public ChatMessageAction clone() {
      return new ChatMessageAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.chatMessage);
   }
}
