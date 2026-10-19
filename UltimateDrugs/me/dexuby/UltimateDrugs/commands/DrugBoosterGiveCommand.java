package me.dexuby.UltimateDrugs.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugBooster;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrugBoosterGiveCommand extends CustomCommand {
   private Main main;
   private ConfigManager configManager;

   public DrugBoosterGiveCommand(Main var1) {
      super("dr", new String[]{"booster", "give"}, "Used to give boosters to players", "/dr booster give <player> <owner only> <all drugs> <affected drugs> <multiplier> <stackable> <duration>", "ultimatedrugs.booster.give", 7, 7);
      this.main = var1;
      this.configManager = var1.getConfigManager();
   }

   public void executeCommand(CommandSender var1, List<String> var2) {
      String var3 = (String)var2.get(0);
      Player var4 = this.main.getServer().getPlayer(var3);
      if (var4 != null) {
         boolean var5 = Boolean.parseBoolean((String)var2.get(1));
         boolean var6 = Boolean.parseBoolean((String)var2.get(2));
         String var7 = (String)var2.get(3);
         ArrayList var8 = new ArrayList();
         if (var7.indexOf(44) != -1) {
            Collections.addAll(var8, var7.split(","));
         } else if (!var7.equals("null")) {
            var8.add(var7);
         }

         double var9 = Double.parseDouble((String)var2.get(4));
         boolean var11 = Boolean.parseBoolean((String)var2.get(5));
         long var12 = Long.parseLong((String)var2.get(6));
         DrugBooster var14 = new DrugBooster(UUID.randomUUID().toString(), var4.getUniqueId(), var5, var6, var8, var9, var11, false, var12, -1L);
         this.main.getDrugBoosterManager().addDrugBooster(var14);
         this.configManager.saveDrugBooster(var14);
         var1.sendMessage(this.configManager.getLangString("booster-give-successful"));
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
         case 3:
         case 6:
            return Arrays.asList("true", "false");
         case 4:
            return (List)this.main.getDrugManager().getDrugs().stream().map(Drug::getId).filter((var1x) -> var1x.startsWith(var1[3])).collect(Collectors.toList());
         case 5:
            return Collections.singletonList("1.5");
         case 7:
            return Collections.singletonList("60");
      }
   }
}
