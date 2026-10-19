package me.dexuby.UltimateDrugs;

import me.dexuby.UltimateDrugs.commands.DrugBlockInfoCommand;
import me.dexuby.UltimateDrugs.commands.DrugBoosterGiveCommand;
import me.dexuby.UltimateDrugs.commands.DrugBoostersCommand;
import me.dexuby.UltimateDrugs.commands.DrugBuyCommand;
import me.dexuby.UltimateDrugs.commands.DrugFertilizerGiveCommand;
import me.dexuby.UltimateDrugs.commands.DrugGiveCommand;
import me.dexuby.UltimateDrugs.commands.DrugListCommand;
import me.dexuby.UltimateDrugs.commands.DrugSeedGiveCommand;
import me.dexuby.UltimateDrugs.commands.DrugSellCommand;
import me.dexuby.UltimateDrugs.commands.HelpCommand;
import me.dexuby.UltimateDrugs.commands.ReloadCommand;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.events.BlockBreak;
import me.dexuby.UltimateDrugs.events.EntityPotionEffect;
import me.dexuby.UltimateDrugs.events.JobsExpGain;
import me.dexuby.UltimateDrugs.events.JobsPayment;
import me.dexuby.UltimateDrugs.events.McMMOPlayerXpGain;
import me.dexuby.UltimateDrugs.events.NPCRightClick;
import me.dexuby.UltimateDrugs.events.PlayerInteract;
import me.dexuby.UltimateDrugs.events.PlayerItemConsume;
import me.dexuby.UltimateDrugs.events.WeaponDamageEntity;
import me.dexuby.UltimateDrugs.handlers.DrugHandler;
import me.dexuby.UltimateDrugs.handlers.GUIHandler;
import me.dexuby.UltimateDrugs.handlers.MultiplierHandler;
import me.dexuby.UltimateDrugs.handlers.PlantHandler;
import me.dexuby.UltimateDrugs.handlers.ProtocolLibHandler;
import me.dexuby.UltimateDrugs.handlers.RecipeHandler;
import me.dexuby.UltimateDrugs.handlers.VanillaHandler;
import me.dexuby.UltimateDrugs.managers.CommandManager;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.managers.DrugBoosterManager;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.managers.GUIManager;
import me.dexuby.UltimateDrugs.managers.PlantManager;
import me.dexuby.UltimateDrugs.managers.RecipeManager;
import me.dexuby.UltimateDrugs.managers.StructureManager;
import me.dexuby.UltimateDrugs.managers.VanillaDropManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   private static Main instance;
   private Economy economy;
   private PluginManager pluginManager;
   private DrugManager drugManager;
   private DrugBoosterManager drugBoosterManager;
   private PlantManager plantManager;
   private RecipeManager recipeManager;
   private StructureManager structureManager;
   private VanillaDropManager vanillaDropManager;
   private ConfigManager configManager;
   private GUIManager guiManager;
   private CommandManager commandManager;
   private PlantHandler plantHandler;

   public void onEnable() {
      loadConfig0();
      instance = this;
      new Metrics(this);
      this.registerManagers();
      this.plantHandler = new PlantHandler(this);
      this.registerHandlers();
      this.registerEvents();
      this.registerCommands();
      if (this.pluginManager.isPluginEnabled("Vault")) {
         RegisteredServiceProvider var1 = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (var1 != null) {
            this.economy = (Economy)var1.getProvider();
         }
      }

   }

   public void onDisable() {
      if (!(Boolean)SettingsHolder.BLOCK_SHUTDOWN_SAVE.getValue()) {
         this.configManager.updateStorage();
      }

      this.drugManager.forceEndAllTimedConsumeActions();
   }

   private void registerManagers() {
      this.pluginManager = this.getServer().getPluginManager();
      this.drugManager = new DrugManager(this);
      this.drugBoosterManager = new DrugBoosterManager(this);
      this.plantManager = new PlantManager();
      this.recipeManager = new RecipeManager(this);
      this.structureManager = new StructureManager(this);
      this.vanillaDropManager = new VanillaDropManager(this);
      this.configManager = new ConfigManager(this);
      this.guiManager = new GUIManager(this);
      this.commandManager = new CommandManager(this);
   }

   private void registerHandlers() {
      this.pluginManager.registerEvents(new DrugHandler(this), this);
      this.pluginManager.registerEvents(new GUIHandler(this), this);
      this.pluginManager.registerEvents(new MultiplierHandler(this), this);
      this.pluginManager.registerEvents(this.plantHandler, this);
      if (this.pluginManager.isPluginEnabled("ProtocolLib")) {
         new ProtocolLibHandler(this);
      }

      this.pluginManager.registerEvents(new RecipeHandler(this), this);
      this.pluginManager.registerEvents(new VanillaHandler(this), this);
   }

   private void registerEvents() {
      this.pluginManager.registerEvents(new BlockBreak(this), this);
      this.pluginManager.registerEvents(new EntityPotionEffect(this), this);
      if (this.pluginManager.isPluginEnabled("Jobs")) {
         this.pluginManager.registerEvents(new JobsExpGain(this), this);
         this.pluginManager.registerEvents(new JobsPayment(this), this);
      }

      if (this.pluginManager.isPluginEnabled("mcMMO")) {
         this.pluginManager.registerEvents(new McMMOPlayerXpGain(this), this);
      }

      if (this.pluginManager.isPluginEnabled("Citizens")) {
         this.pluginManager.registerEvents(new NPCRightClick(this), this);
      }

      this.pluginManager.registerEvents(new PlayerInteract(this), this);
      this.pluginManager.registerEvents(new PlayerItemConsume(this), this);
      if (this.pluginManager.isPluginEnabled("CrackShot")) {
         this.pluginManager.registerEvents(new WeaponDamageEntity(this), this);
      }

      this.configManager.fillEventSlots();
   }

   private void registerCommands() {
      this.commandManager.registerCommand(new DrugBlockInfoCommand(this));
      this.commandManager.registerCommand(new DrugBoosterGiveCommand(this));
      this.commandManager.registerCommand(new DrugBoostersCommand(this));
      this.commandManager.registerCommand(new DrugBuyCommand(this));
      this.commandManager.registerCommand(new DrugFertilizerGiveCommand(this));
      this.commandManager.registerCommand(new DrugGiveCommand(this));
      this.commandManager.registerCommand(new DrugListCommand(this));
      this.commandManager.registerCommand(new DrugSeedGiveCommand(this));
      this.commandManager.registerCommand(new DrugSellCommand(this));
      this.commandManager.registerCommand(new HelpCommand(this));
      this.commandManager.registerCommand(new ReloadCommand(this));
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public DrugManager getDrugManager() {
      return this.drugManager;
   }

   public DrugBoosterManager getDrugBoosterManager() {
      return this.drugBoosterManager;
   }

   public PlantManager getPlantManager() {
      return this.plantManager;
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public StructureManager getStructureManager() {
      return this.structureManager;
   }

   public VanillaDropManager getVanillaDropManager() {
      return this.vanillaDropManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public GUIManager getGUIManager() {
      return this.guiManager;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public PlantHandler getPlantHandler() {
      return this.plantHandler;
   }

   public static Main getInstance() {
      return instance;
   }
}
