package me.dexuby.UltimateDrugs.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.commands.CustomCommand;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandHandler implements CommandExecutor, TabCompleter {
   private Main main;
   private ConfigManager configManager;

   public CommandHandler(Main var1) {
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      for(CustomCommand var7 : (Collection)this.main.getCommandManager().getRegisteredCommands().stream().filter((var3x) -> (var3x.getName().equalsIgnoreCase(var3) || var2.getAliases().contains(var3x.getName().toLowerCase())) && var3x.getSubNames().length <= var4.length).collect(Collectors.toList())) {
         boolean var8 = true;

         for(int var9 = 0; var9 < var7.getSubNames().length; ++var9) {
            if (!var4[var9].equalsIgnoreCase(var7.getSubNames()[var9])) {
               var8 = false;
               break;
            }
         }

         if (var8) {
            if (var1.hasPermission(var7.getPermission())) {
               if (var4.length >= var7.getSubNames().length + var7.getMinArgs() && var4.length <= var7.getSubNames().length + var7.getMaxArgs()) {
                  ArrayList var10 = new ArrayList(var7.getSubNames().length > 0 ? Arrays.asList(var4).subList(var7.getSubNames().length, var4.length) : Arrays.asList(var4));
                  var7.executeCommand(var1, var10);
                  return true;
               }

               var1.sendMessage(var7.getUsage());
               return true;
            }

            var1.sendMessage(this.configManager.getLangString("command-no-permission"));
            return true;
         }
      }

      var1.sendMessage(this.configManager.getLangString("command-not-found"));
      return true;
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      if (var4.length == 1) {
         return (List)this.main.getCommandManager().getRegisteredCommands().stream().filter((var1x) -> var1x.getSubNames().length > 0 && var1x.getSubNames()[0].startsWith(var4[0])).map((var0) -> var0.getSubNames()[0]).collect(Collectors.toList());
      } else {
         for(CustomCommand var7 : (Collection)this.main.getCommandManager().getRegisteredCommands().stream().filter((var3x) -> (var3x.getName().equalsIgnoreCase(var3) || var2.getAliases().contains(var3x.getName().toLowerCase())) && var3x.getSubNames().length <= var4.length).collect(Collectors.toList())) {
            if (var1.hasPermission(var7.getPermission()) && var7.getSubNames().length > 0 && var7.getSubNames()[0].equalsIgnoreCase(var4[0])) {
               boolean var8 = var7.getSubNames().length >= var4.length;
               if (var8) {
                  return Collections.singletonList(var7.getSubNames()[var4.length - 1]);
               }

               return var7.getTabCompletion((String[])Arrays.copyOfRange(var4, var7.getSubNames().length, var4.length));
            }
         }

         return null;
      }
   }
}
