package me.dexuby.UltimateDrugs.commands;

import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.guis.GUI;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrugBuyCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public DrugBuyCommand(Main var1) {
      super("dr", new String[]{"buy"}, "Used to buy drugs.", "/dr buy <player>", "ultimatedrugs.buy", 0, 1);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      Player var3;
      if (var2.size() > 0 && var1.hasPermission("ultimatedrugs.buy.others")) {
         var3 = this.main.getServer().getPlayer((String)var2.get(0));
         if (var3 == null) {
            var1.sendMessage(this.configManager.getLangString("player-not-found"));
            return;
         }
      } else {
         if (!(var1 instanceof Player)) {
            var1.sendMessage(this.configManager.getLangString("no-console-command"));
            return;
         }

         var3 = (Player)var1;
      }

      GUI var4 = this.main.getGUIManager().getGUIById("drug-buy");
      if (var4 != null) {
         var4.open(var3);
      } else {
         var1.sendMessage(this.configManager.getLangString("gui-not-found"));
      }

   }

   public List<String> getTabCompletion(String[] var1) {
      return null;
   }
}
