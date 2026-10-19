package me.dexuby.UltimateDrugs.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.utils.NumberUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DrugSeedGiveCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public DrugSeedGiveCommand(Main var1) {
      super("dr", new String[]{"seed", "give"}, "Used to give a player a configured drug seed.", "/dr seed give <player> <drug> <amount>", "ultimatedrugs.seed.give", 2, 3);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      String var3 = (String)var2.get(0);
      Player var4 = this.main.getServer().getPlayer(var3);
      if (var4 != null) {
         String var5 = (String)var2.get(1);
         Drug var6 = this.main.getDrugManager().getDrugById(var5);
         if (var6 != null) {
            if (var6.getGrowing() != null) {
               ItemStack var7 = var6.getGrowing().getSeedItemStack().clone();
               if (var2.size() >= 3) {
                  if (!NumberUtils.isValidInteger((String)var2.get(2))) {
                     var1.sendMessage(this.configManager.getLangString("invalid-number"));
                     return;
                  }

                  var7.setAmount(Integer.parseInt((String)var2.get(2)));
               }

               if (var4.getInventory().firstEmpty() != -1) {
                  var4.getInventory().addItem(new ItemStack[]{var7});
                  HashMap var8 = new HashMap();
                  var8.put("%target%", var4.getName());
                  var8.put("%drug_id%", var6.getId());
                  var8.put("%amount%", var2.size() >= 3 ? (String)var2.get(2) : "1");
                  var1.sendMessage(TextUtils.replaceVariables(this.configManager.getLangString("seed-give-successful"), var8));
               } else {
                  var1.sendMessage(this.configManager.getLangString("target-inventory-is-full"));
               }
            } else {
               var1.sendMessage(this.configManager.getLangString("drug-has-no-seed"));
            }
         } else {
            var1.sendMessage(this.configManager.getLangString("drug-not-found"));
         }
      } else {
         var1.sendMessage(this.configManager.getLangString("player-not-found"));
      }

   }

   public List<String> getTabCompletion(String[] var1) {
      switch (var1.length) {
         case 1:
         default:
            return null;
         case 2:
            return (List)this.main.getDrugManager().getDrugs().stream().map(Drug::getId).filter((var1x) -> var1x.startsWith(var1[1])).collect(Collectors.toList());
         case 3:
            return Collections.singletonList("1");
      }
   }
}
