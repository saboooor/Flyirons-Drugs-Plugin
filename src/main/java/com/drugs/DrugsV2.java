package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.drugs.weedfarm.WeedFarmControllerListener;
import com.drugs.weedfarm.WeedFarmItems;
import com.drugs.weedfarm.WeedFarmManager;
import com.drugs.weedfarm.WeedFarmWorkerService;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * Main plugin class for DrugsV2.
 */
public class DrugsV2 extends JavaPlugin {

    private static DrugsV2 instance;
    private FileConfiguration recipesConfig;
    private File recipesFile;
    private BongListener bongListener;
    private DrugGrowing drugGrowing;
    private WeedFarmManager weedFarmManager;
    private WeedFarmControllerListener weedFarmControllerListener;

    public WeedFarmControllerListener getWeedFarmControllerListener() {
        return weedFarmControllerListener;
    }
    private LsdEffectManager lsdEffectManager;
    private SalviaEffectManager salviaEffectManager;
    private PluginStartup startup;
    private DrugsPlaceholderExpansion placeholderExpansion;
    private boolean cannabisLoaded;
    private boolean bongsLoaded;
    private boolean farmsLoaded;
    private boolean addictionLoaded;
    private CartelEnforcer cartelEnforcer;
    private CartelNpcs cartelNpcs;
    private CartelDroppedSales cartelDroppedSales;
    private BedrockForms bedrockForms;

    public static DrugsV2 getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        // PlugMan calls this on the server thread. Defer heavy initialization so ticks
        // continue between phases, and keep commands safe until all services are ready.
        for (String name : List.of("drugs", "drugstatus", "tolerance", "strains", "cartel")) {
            getCommand(name).setExecutor((sender, command, label, args) -> {
                sender.sendMessage("§eLumiDrugs is initializing. Please try again shortly.");
                return true;
            });
            getCommand(name).setTabCompleter((sender, command, label, args) -> List.of());
        }
        startup = new PluginStartup(this, List.of(
                new PluginStartup.Step("default configuration", () -> {
                    saveDefaultConfig();
                    saveRecipesConfig();
                    saveToleranceConfig();
                    saveAchievementSettingsConfig();
                    saveAchievementsConfig();
                    saveOverdoseConfig();
                    saveAddictionConfig();
                    saveStrainsConfig();
                    saveBongConfig();
                    saveMechanicsConfig();
                }),
                new PluginStartup.Step("tolerance configuration", () -> ToleranceConfigLoader.load(getDataFolder())),
                new PluginStartup.Step("achievement settings", () -> AchievementSettingsLoader.load(getDataFolder())),
                new PluginStartup.Step("overdose configuration", () -> OverdoseEffectManager.load(getDataFolder())),
                new PluginStartup.Step("addiction configuration", () -> AddictionConfigLoader.load(this)),
                new PluginStartup.Step("strain configuration", () -> StrainConfigLoader.load(getDataFolder())),
                new PluginStartup.Step("bong and mechanics configuration", () -> {
                    BongConfigLoader.load(getDataFolder());
                    MechanicsConfig.load(getDataFolder());
                }),
                new PluginStartup.Step("saved cannabis plants", () -> {
                    CannabisPlantRegistry.init(getDataFolder());
                    cannabisLoaded = true;
                }),
                new PluginStartup.Step("saved bongs", () -> {
                    BongRegistry.init(getDataFolder());
                    bongsLoaded = true;
                }),
                new PluginStartup.Step("saved weed farms", () -> {
                    weedFarmManager = new WeedFarmManager(getDataFolder());
                    weedFarmManager.load();
                    farmsLoaded = true;
                }),
                new PluginStartup.Step("ingredient crops and recipes", () -> drugGrowing = new DrugGrowing(this)),
                new PluginStartup.Step("drug recipes", () -> DrugRegistry.init(this)),
                new PluginStartup.Step("achievement definitions", () -> CustomAchievementLoader.load(getDataFolder())),
                new PluginStartup.Step("cure recipes", () -> CureRegistry.init(this)),
                new PluginStartup.Step("bong and farm recipes", () -> {
                    registerBongRecipe();
                    registerFarmControllerRecipe();
                }),
                new PluginStartup.Step("saved addiction data", () -> {
                    AddictionManager.init(this);
                    addictionLoaded = true;
                }),
                new PluginStartup.Step("gameplay listeners and services", this::startGameplay)
        ), () -> getLogger().info("LumiDrugs is ready!"), exception -> {
            getLogger().log(Level.SEVERE, "Cannot initialize LumiDrugs", exception);
            getServer().getPluginManager().disablePlugin(this);
        });
        startup.start();
        getLogger().info("LumiDrugs initialization scheduled across server ticks.");
    }

    private void startGameplay() {
        PerformanceOptimizer.initialize();
        ToleranceTracker.startDecayTask();
        var manager = getServer().getPluginManager();
        manager.registerEvents(drugGrowing, this);
        manager.registerEvents(new DrugStatusTracker(), this);
        manager.registerEvents(new DrugMenuListener(), this);
        manager.registerEvents(new DrugUseListener(), this);
        manager.registerEvents(new AddictionListener(), this);
        manager.registerEvents(new CannabisPlantListener(), this);
        manager.registerEvents(new StrainCraftingListener(), this);
        manager.registerEvents(new HarvestCraftingListener(), this);
        manager.registerEvents(new StrainsMenuListener(), this);
        manager.registerEvents(new EnforcerGuide(this), this);
        bongListener = new BongListener();
        manager.registerEvents(bongListener, this);
        lsdEffectManager = new LsdEffectManager(this);
        manager.registerEvents(lsdEffectManager, this);
        salviaEffectManager = new SalviaEffectManager(this);
        manager.registerEvents(salviaEffectManager, this);
        manager.registerEvents(new NativeAdvancements(), this);
        weedFarmControllerListener = new WeedFarmControllerListener(weedFarmManager);
        manager.registerEvents(weedFarmControllerListener, this);
        getServer().getScheduler().runTaskTimer(this, new WeedFarmWorkerService(weedFarmManager, drugGrowing),
                MechanicsConfig.getWorkerTickInterval(), MechanicsConfig.getWorkerTickInterval());
        getServer().getScheduler().runTask(this, () -> BongRegistry.respawnMissing(bongListener));

        // Register commands
        CartelDealer cartelDealer = new CartelDealer(this);
        getServer().getPluginManager().registerEvents(cartelDealer, this);
        getCommand("cartel").setExecutor(cartelDealer);
        getCommand("cartel").setTabCompleter(cartelDealer);
        cartelNpcs = CartelNpcs.create(this, cartelDealer);
        cartelDealer.setNpcs(cartelNpcs);
        cartelEnforcer = new CartelEnforcer(this, cartelDealer);
        cartelDealer.setEnforcer(cartelEnforcer);
        getServer().getPluginManager().registerEvents(cartelEnforcer, this);
        cartelEnforcer.start();
        cartelDroppedSales = new CartelDroppedSales(this, cartelDealer, cartelEnforcer);
        getServer().getPluginManager().registerEvents(cartelDroppedSales, this);
        cartelDroppedSales.start();
        getCommand("drugs").setExecutor(new DrugsCommand());
        getCommand("drugs").setTabCompleter(new DrugsTabCompleter());
        getCommand("drugstatus").setExecutor(new DrugStatusCommand());
        getCommand("tolerance").setExecutor(new ToleranceCommand());
        StrainsCommand strainsCommand = new StrainsCommand();
        getCommand("strains").setExecutor(strainsCommand);
        getCommand("strains").setTabCompleter(strainsCommand);
        if (manager.getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new DrugsPlaceholderExpansion(this);
            placeholderExpansion.register();
        }
        bedrockForms = new BedrockForms(this);
        bedrockForms.refresh();
    }

    public void showDialog(org.bukkit.entity.Player player, DialogView.MultiActionDialog dialog) {
        if (bedrockForms != null && bedrockForms.isBedrock(player)) {
            bedrockForms.show(player, dialog);
        } else {
            player.showDialog(DialogView.paper(dialog));
        }
    }

    public void executeUiCommand(org.bukkit.entity.Player player, String command) {
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        player.performCommand(cmd);
    }

    @Override
    public String namespace() {
        String name = getName();
        return name != null ? name.toLowerCase(java.util.Locale.ROOT) : "LumiDrugs";
    }

    @Override
    public void onDisable() {
        if (startup != null) startup.cancel();
        if (cartelDroppedSales != null) cartelDroppedSales.shutdown();
        if (cartelEnforcer != null) cartelEnforcer.shutdown();
        if (cartelNpcs != null) cartelNpcs.shutdown();
        NativeAdvancements.shutdown();
        if (placeholderExpansion != null) placeholderExpansion.unregister();
        if (lsdEffectManager != null) lsdEffectManager.cleanupAll();
        if (salviaEffectManager != null) salviaEffectManager.cleanupAll();
        // Save only successfully loaded stores; unloading during initialization must
        // not replace saved player data with an empty, partly initialized registry.
        if (cannabisLoaded) savePhase("saving cannabis plants", CannabisPlantRegistry::save);
        if (drugGrowing != null) savePhase("saving ingredient crops", drugGrowing::save);
        if (bongsLoaded) savePhase("saving bongs", BongRegistry::save);
        if (farmsLoaded) savePhase("saving weed farms", weedFarmManager::save);
        if (addictionLoaded) savePhase("saving addiction data", AddictionManager::shutdown);
        getLogger().info("LumiDrugs disabled.");
    }

    private void savePhase(String name, Runnable action) {
        getLogger().info("Shutdown phase: " + name);
        long started = System.nanoTime();
        try {
            action.run();
        } finally {
            PluginStartup.reportDuration(this, name, started);
        }
    }

    public void saveRecipesConfig() {
        if (recipesFile == null) {
            recipesFile = new File(getDataFolder(), "recipes.yml");
        }
        if (!recipesFile.exists()) {
            saveResource("recipes.yml", false);
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
    }

    public void saveToleranceConfig() {
        File file = new File(getDataFolder(), "tolerance.yml");
        if (!file.exists()) {
            saveResource("tolerance.yml", false);
        }
    }
    
    public void saveAchievementSettingsConfig() {
        File file = new File(getDataFolder(), "achievement_settings.yml");
        if (!file.exists()) {
            saveResource("achievement_settings.yml", false);
        }
    }
    
    public void saveAchievementsConfig() {
        File file = new File(getDataFolder(), "achievements.yml");
        if (!file.exists()) {
            saveResource("achievements.yml", false);
        }
    }

    /**
     * Saves the overdose config file
     */
    public void saveOverdoseConfig() {
        File file = new File(getDataFolder(), "overdose.yml");
        if (!file.exists()) {
            saveResource("overdose.yml", false);
        }
    }

    public void saveAddictionConfig() {
        File file = new File(getDataFolder(), "addiction.yml");
        if (!file.exists()) {
            saveResource("addiction.yml", false);
        }
    }

    public void saveStrainsConfig() {
        File file = new File(getDataFolder(), "strains.yml");
        if (!file.exists()) {
            saveResource("strains.yml", false);
        }
    }

    public void saveBongConfig() {
        File file = new File(getDataFolder(), "bong.yml");
        if (!file.exists()) {
            saveResource("bong.yml", false);
        }
    }

    public void saveMechanicsConfig() {
        File file = new File(getDataFolder(), "mechanics.yml");
        if (!file.exists()) {
            saveResource("mechanics.yml", false);
        }
    }

    private void registerBongRecipe() {
        if (recipesConfig == null) return;

        var recipeSection = recipesConfig.getConfigurationSection("bong");
        if (recipeSection == null) return;

        DrugRecipeHelper.registerItemRecipe("bong", recipeSection, BongItemFactory.createBongItem(1), this);
    }



    private void registerFarmControllerRecipe() {
        if (recipesConfig == null) return;

        var recipeSection = recipesConfig.getConfigurationSection("farm-controller");
        if (recipeSection == null) return;

        DrugRecipeHelper.registerItemRecipe("farm_controller", recipeSection, WeedFarmItems.createControllerItem(), this);
    }
    public FileConfiguration getRecipesConfig() {
        return recipesConfig;
    }

    public void reloadGrowing() {
        if (drugGrowing != null) drugGrowing.reloadConfiguration();
    }

    public BongListener getBongListener() {
        return bongListener;
    }

    public LsdEffectManager getLsdEffectManager() {
        return lsdEffectManager;
    }

    public SalviaEffectManager getSalviaEffectManager() {
        return salviaEffectManager;
    }
}
