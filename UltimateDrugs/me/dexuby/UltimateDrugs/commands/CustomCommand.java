package me.dexuby.UltimateDrugs.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class CustomCommand {
   private String name;
   private String[] subNames;
   private String description;
   private String usage;
   private String permission;
   private int minArgs;
   private int maxArgs;

   CustomCommand(String var1, String[] var2, String var3, String var4, String var5, int var6, int var7) {
      this.name = var1;
      this.subNames = var2;
      this.description = var3;
      this.usage = var4;
      this.permission = var5;
      this.minArgs = var6;
      this.maxArgs = var7;
   }

   public String getName() {
      return this.name;
   }

   public String[] getSubNames() {
      return this.subNames;
   }

   public String getDescription() {
      return this.description;
   }

   public String getUsage() {
      return this.usage;
   }

   public String getPermission() {
      return this.permission;
   }

   public int getMinArgs() {
      return this.minArgs;
   }

   public int getMaxArgs() {
      return this.maxArgs;
   }

   public abstract void executeCommand(CommandSender var1, List<String> var2);

   public abstract List<String> getTabCompletion(String[] var1);
}
