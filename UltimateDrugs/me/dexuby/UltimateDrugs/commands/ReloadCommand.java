package me.dexuby.UltimateDrugs.commands;

import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public ReloadCommand(Main var1) {
      super("dr", new String[]{"reload"}, "Used to reload the plugin files.", "/dr reload", "ultimatedrugs.reload", 0, 0);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      DrugManager var3 = this.main.getDrugManager();
      var3.getDrugs().clear();
      var3.clearDrugCache();
      this.main.getVanillaDropManager().getVanillaDrops().clear();
      this.configManager.getLangCache().clear();
      this.configManager.reloadConfig();
      this.configManager.reloadLang();
      var1.sendMessage(this.configManager.getLangString("files-reloaded"));
   }

   public List<String> getTabCompletion(String[] var1) {
      return null;
   }
}
