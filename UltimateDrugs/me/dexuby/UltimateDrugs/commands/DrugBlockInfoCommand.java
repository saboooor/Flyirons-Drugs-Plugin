package me.dexuby.UltimateDrugs.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrugBlockInfoCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public DrugBlockInfoCommand(Main var1) {
      super("dr", new String[]{"blockinfo"}, "Used to display the block data of the block you are looking at.", "/dr blockinfo", "ultimatedrugs.blockinfo", 0, 0);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      if (var1 instanceof Player) {
         Player var3 = (Player)var1;
         Block var4 = var3.getTargetBlock((Set)null, 6);
         HashMap var5 = new HashMap();
         var5.put("%block_data%", var4.getBlockData().getAsString());
         var3.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("block-info"), var5));
      } else {
         var1.sendMessage(this.configManager.getLangString("no-console-command"));
      }

   }

   public List<String> getTabCompletion(String[] var1) {
      return null;
   }
}
