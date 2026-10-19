package me.dexuby.UltimateDrugs.commands;

import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.guis.GUI;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrugListCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public DrugListCommand(Main var1) {
      super("dr", new String[]{"list"}, "Used to list all configured drugs.", "/dr list", "ultimatedrugs.list", 0, 0);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      if (var1 instanceof Player && var1.hasPermission("ultimatedrugs.list.others")) {
         Player var3 = (Player)var1;
         GUI var4 = this.main.getGUIManager().getGUIById("drug-list");
         if (var4 != null) {
            var4.open(var3);
         } else {
            var3.sendMessage(this.configManager.getLangString("gui-not-found"));
         }
      } else {
         var1.sendMessage(this.configManager.getLangString("no-console-command"));
      }

   }

   public List<String> getTabCompletion(String[] var1) {
      return null;
   }
}
