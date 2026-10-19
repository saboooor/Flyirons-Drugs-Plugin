package me.dexuby.UltimateDrugs.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.NumberUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.command.CommandSender;

public class HelpCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public HelpCommand(Main var1) {
      super("dr", new String[]{"help"}, "Used to display available commands.", "/dr <help>", "ultimatedrugs.help", 0, 1);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      int var3 = 1;
      if (var2.size() > 0) {
         if (NumberUtils.isValidInteger((String)var2.get(0))) {
            var3 = Integer.parseInt((String)var2.get(0));
            if (var3 < 1) {
               var3 = 1;
            }
         } else {
            var1.sendMessage(this.configManager.getLangString("invalid-page-number"));
         }
      }

      List var4 = (List)this.main.getCommandManager().getRegisteredCommands().stream().filter((var1x) -> var1.hasPermission(var1x.getPermission())).collect(Collectors.toList());
      int var5 = (int)Math.ceil((double)var4.size() / (double)6.0F);
      if (var3 > var5) {
         var1.sendMessage(this.configManager.getLangString("help-page-not-found"));
      } else {
         StringBuilder var6 = new StringBuilder(this.configManager.getLangString("help-page-header"));
         var6.append("\n");

         for(int var7 = var3 == 1 ? var3 : (var3 - 1) * 6 + 1; var7 < 6 * var3 + 1; ++var7) {
            if (var4.size() >= var7) {
               CustomCommand var8 = (CustomCommand)var4.get(var7 - 1);
               HashMap var9 = new HashMap();
               var9.put("%command_name%", var8.getName());
               var9.put("%command_args%", String.join(" ", var8.getSubNames()));
               var9.put("%command_description%", var8.getDescription());
               var9.put("%command_usage%", var8.getUsage());
               var9.put("%command_permission%", var8.getPermission());
               var6.append(TextUtils.replaceVariables(this.configManager.getLangString("help-command-layout"), var9));
               if (var7 < 6 * var3) {
                  var6.append("\n");
               }
            }
         }

         HashMap var10 = new HashMap();
         var10.put("%next_page%", var3 + 1 <= var5 ? Integer.toString(var3 + 1) : "/");
         var6.append("\n");
         var6.append(TextUtils.replaceVariables(this.configManager.getLangString("help-page-footer"), var10));
         var1.sendMessage(var6.toString());
      }

   }

   public List<String> getTabCompletion(String[] var1) {
      return Collections.singletonList("1");
   }
}
