package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.List;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.commands.CustomCommand;
import me.dexuby.UltimateDrugs.handlers.CommandHandler;

public class CommandManager {
   private Main main;
   private CommandHandler commandHandler;
   private List<CustomCommand> registeredCommands = new ArrayList();

   public CommandManager(Main var1) {
      this.main = var1;
      this.commandHandler = new CommandHandler(var1);
   }

   public List<CustomCommand> getRegisteredCommands() {
      return this.registeredCommands;
   }

   public void registerCommand(CustomCommand var1) {
      this.main.getCommand(var1.getName()).setExecutor(this.commandHandler);
      this.registeredCommands.add(var1);
   }
}
