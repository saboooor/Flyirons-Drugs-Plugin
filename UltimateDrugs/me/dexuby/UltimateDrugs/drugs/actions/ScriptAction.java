package me.dexuby.UltimateDrugs.drugs.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import me.dexuby.UltimateDrugs.api.UltimateDrugsAPI;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ScriptAction extends ConsumeAction {
   private List<String> codeLines;

   public ScriptAction(int var1, double var2, int var4, int var5, String var6, List<String> var7) {
      super(var1, var2, var4, var5, var6);
      this.codeLines = var7;
      var7.add(0, "");
   }

   public void execute(UUID var1, final Drug var2) {
      Player var3 = Bukkit.getPlayer(var1);
      final OfflinePlayer var4 = Bukkit.getOfflinePlayer(var1);
      HashMap var5 = new HashMap<String, String>() {
         {
            this.put("%player_name%", var4.getName() != null ? var4.getName() : "Unknown");
            this.put("%player_uuid%", var4.getUniqueId().toString());
            this.put("%drug_id%", var2.getId());
         }
      };
      ScriptEngineManager var6 = new ScriptEngineManager();
      ScriptEngine var7 = var6.getEngineByName("JavaScript");
      var7.put("UltimateDrugs", UltimateDrugsAPI.getInstance());
      var7.put("Server", Bukkit.getServer());
      var7.put("Player", var3 != null ? var3 : var4);
      if (this.getTargetSelector() != null) {
         for(Entity var10 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
            var7.put("Target", var10);
            var5.put("%target_name%", var10.getName());
            var5.put("%target_uuid%", var10.getUniqueId().toString());
            this.apply(var7, var5);
         }
      } else {
         this.apply(var7, var5);
      }

   }

   private void apply(ScriptEngine var1, Map<String, String> var2) {
      try {
         this.codeLines.add(0, "load(\"nashorn:mozilla_compat.js\");");
         String var3 = String.join("\n", this.codeLines);
         var1.eval(TextUtils.replaceVariables(var3, var2));
      } catch (Exception var4) {
         System.out.println(String.format("[%s] Failed to execute script:", "UltimateDrugs"));
         var4.printStackTrace();
      }

   }

   public List<String> getCodeLines() {
      return this.codeLines;
   }

   public ScriptAction clone() {
      return new ScriptAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.codeLines);
   }
}
