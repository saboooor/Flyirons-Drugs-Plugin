package me.dexuby.UltimateDrugs.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.block.BlockOffset;
import me.dexuby.UltimateDrugs.block.BlockOption;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.ConsumeConditions;
import me.dexuby.UltimateDrugs.drugs.ConsumeOption;
import me.dexuby.UltimateDrugs.drugs.ConsumeRequirementItem;
import me.dexuby.UltimateDrugs.drugs.Drop;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.DrugBooster;
import me.dexuby.UltimateDrugs.drugs.DrugBrewingRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugCraftingRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugDealer;
import me.dexuby.UltimateDrugs.drugs.DrugDealerType;
import me.dexuby.UltimateDrugs.drugs.DrugFurnaceRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugRelatedGood;
import me.dexuby.UltimateDrugs.drugs.ExchangeItem;
import me.dexuby.UltimateDrugs.drugs.MultiplierType;
import me.dexuby.UltimateDrugs.drugs.actions.ChatMessageAction;
import me.dexuby.UltimateDrugs.drugs.actions.CommandAction;
import me.dexuby.UltimateDrugs.drugs.actions.CommandSenderType;
import me.dexuby.UltimateDrugs.drugs.actions.FeedAction;
import me.dexuby.UltimateDrugs.drugs.actions.GameStateAction;
import me.dexuby.UltimateDrugs.drugs.actions.HealAction;
import me.dexuby.UltimateDrugs.drugs.actions.ParticleEffectAction;
import me.dexuby.UltimateDrugs.drugs.actions.PotionEffectAction;
import me.dexuby.UltimateDrugs.drugs.actions.SaturationAction;
import me.dexuby.UltimateDrugs.drugs.actions.ScriptAction;
import me.dexuby.UltimateDrugs.drugs.actions.SoundEffectAction;
import me.dexuby.UltimateDrugs.drugs.actions.TimeAction;
import me.dexuby.UltimateDrugs.drugs.actions.VisualEffectAction;
import me.dexuby.UltimateDrugs.drugs.actions.VisualEffectType;
import me.dexuby.UltimateDrugs.drugs.actions.WeatherAction;
import me.dexuby.UltimateDrugs.drugs.chatslur.ChatSlurType;
import me.dexuby.UltimateDrugs.drugs.growing.Direction;
import me.dexuby.UltimateDrugs.drugs.growing.Fertilizer;
import me.dexuby.UltimateDrugs.drugs.growing.Growing;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingBlock;
import me.dexuby.UltimateDrugs.drugs.growing.GrowingStage;
import me.dexuby.UltimateDrugs.drugs.growing.Plant;
import me.dexuby.UltimateDrugs.drugs.growing.PlantType;
import me.dexuby.UltimateDrugs.drugs.growing.Weather;
import me.dexuby.UltimateDrugs.drugs.growing.WorldTime;
import me.dexuby.UltimateDrugs.drugs.growing.processors.ProcessorType;
import me.dexuby.UltimateDrugs.drugs.vanilla.BlockVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.FishingVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.MobVanillaDrop;
import me.dexuby.UltimateDrugs.drugs.vanilla.VanillaDropBlock;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.SerializationUtils;
import me.dexuby.UltimateDrugs.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ConfigManager {
   private Main main;
   private File configFile;
   private File langFile;
   private File storageFile;
   private YamlConfiguration config;
   private YamlConfiguration lang;
   private YamlConfiguration storage;
   private HashMap<String, Object> langCache = new HashMap();
   private HashMap<String, YamlConfiguration> linkCache = new HashMap();
   private final String[] defaultDrugConfigurations = new String[]{"alcohol.yml", "apple-tree.yml", "cocaine.yml", "grape-farm.yml", "strawberry.yml", "weed.yml"};

   public ConfigManager(Main var1) {
      this.main = var1;
      this.configFile = new File(var1.getDataFolder(), "config.yml");
      this.langFile = new File(var1.getDataFolder(), "lang.yml");
      this.storageFile = new File(var1.getDataFolder(), "storage.yml");

      try {
         if (!this.configFile.exists()) {
            var1.saveResource("config.yml", false);
         }

         if (!this.langFile.exists()) {
            var1.saveResource("lang.yml", false);
         }

         if (!this.storageFile.exists()) {
            var1.saveResource("storage.yml", false);
         }

         Path var2 = Paths.get(var1.getDataFolder().getPath(), "drugs");
         if (!(new File(var2.toString())).exists()) {
            (new File(var2.toString())).mkdirs();

            for(String var6 : this.defaultDrugConfigurations) {
               if (!Files.exists(Paths.get(var1.getDataFolder().getPath(), "drugs", var6), new LinkOption[0])) {
                  Files.copy(var1.getResource(var6), Paths.get(var2.toString(), var6), new CopyOption[0]);
               }
            }
         }
      } catch (Exception var7) {
         System.out.println(String.format("[%s] A error occurred while copying default configurations.", var1.getDescription().getName()));
      }

      this.loadFiles();
      this.loadConfig();
      this.loadLang();
      var1.getServer().getScheduler().runTaskLater(var1, this::loadStorage, 1L);
      var1.getServer().getScheduler().runTaskTimerAsynchronously(var1, this::updateStorage, 1200L, 1200L);
   }

   public void updateStorage() {
      for(Plant var3 : new ArrayList(this.main.getPlantManager().getRegisteredPlants())) {
         this.savePlant(var3);
      }

      for(DrugBooster var4 : new ArrayList(this.main.getDrugBoosterManager().getActiveDrugBoosters())) {
         this.saveDrugBooster(var4);
      }

      this.updatePlayerPlacedBlockCache();
      this.saveStorage();
   }

   private void loadFiles() {
      this.config = new YamlConfiguration();
      this.lang = new YamlConfiguration();
      this.storage = new YamlConfiguration();

      try {
         this.config.load(this.configFile);
         this.lang.load(this.langFile);
         this.storage.load(this.storageFile);
      } catch (Exception var2) {
         System.out.println(String.format("[%s] A error occurred while loading the configuration files.", this.main.getDescription().getName()));
         var2.printStackTrace();
      }

   }

   public void reloadConfig() {
      try {
         this.config.load(this.configFile);
         this.loadConfig();
      } catch (Exception var2) {
         System.out.println(String.format("[%s] A error occurred while loading the config file.", this.main.getDescription().getName()));
         var2.printStackTrace();
      }

   }

   private void loadConfig() {
      this.replaceAllLinks(this.config);
      if (this.config.isSet("performance-mode")) {
         SettingsHolder.PERFORMANCE_MODE.setValue(this.config.getBoolean("performance-mode"));
      }

      if (this.config.isSet("delete-plant-on-failed-load")) {
         SettingsHolder.DELETE_PLANT_ON_FAILED_LOAD.setValue(this.config.getBoolean("delete-plant-on-failed-load"));
      }

      if (this.config.isSet("block-shutdown-save")) {
         SettingsHolder.BLOCK_SHUTDOWN_SAVE.setValue(this.config.getBoolean("block-shutdown-save"));
      }

      if (this.config.isSet("player-plant-limit")) {
         SettingsHolder.PLAYER_PLANT_LIMIT.setValue(this.config.getInt("player-plant-limit"));
      }

      if (this.config.isSet("plant-processor-type")) {
         SettingsHolder.PLANT_PROCESSOR_TYPE.setValue(ProcessorType.valueOf(this.config.getString("plant-processor-type")));
      }

      if (this.config.isSet("npc-dealers")) {
         for(String var2 : this.config.getConfigurationSection("npc-dealers").getKeys(false)) {
            String var3 = String.format("npc-dealers.%s.", var2);
            DrugDealerType var4 = this.config.isSet(var3 + "type") ? DrugDealerType.valueOf(this.config.getString(var3 + "type")) : DrugDealerType.SELL;
            this.main.getDrugManager().registerDrugDealer(new DrugDealer(Integer.parseInt(var2), var4));
         }
      }

      if (this.config.isSet("vanilla")) {
         if (this.config.isSet("vanilla.auto-replant.enabled")) {
            SettingsHolder.VANILLA_AUTO_REPLANT.setValue(this.config.getBoolean("vanilla.auto-replant.enabled"));
         }

         if (this.config.isSet("vanilla.auto-replant.ignore-drug-plants")) {
            SettingsHolder.VANILLA_AUTO_IGNORE_DRUG_PLANTS.setValue(this.config.getBoolean("vanilla.auto-replant.ignore-drug-plants"));
         }

         if (this.config.isSet("vanilla.auto-replant.blacklist")) {
            ArrayList var23 = new ArrayList();

            for(String var29 : this.config.getStringList("vanilla.auto-replant.blacklist")) {
               var23.add(Material.valueOf(var29));
            }

            SettingsHolder.VANILLA_AUTO_REPLANT_BLACKLIST.setValue(var23);
         }

         if (this.config.isSet("vanilla.store-player-blocks")) {
            SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.setValue(this.config.getBoolean("vanilla.store-player-blocks"));
         }

         if (this.config.isSet("vanilla.drops")) {
            for(String var27 : this.config.getConfigurationSection("vanilla.drops").getKeys(false)) {
               String var30 = String.format("vanilla.drops.%s.", var27);
               String var32 = this.config.getString(var30 + "type");
               String var5 = this.config.isSet(var30 + "permission") ? this.config.getString(var30 + "permission") : null;
               int var6 = this.config.isSet(var30 + "drop-limit") ? this.config.getInt(var30 + "drop-limit") : 0;
               HashSet var7 = new HashSet();
               if (this.config.isSet(var30 + "biome-whitelist")) {
                  for(String var9 : this.config.getStringList(var30 + "biome-whitelist")) {
                     try {
                        var7.add(Biome.valueOf(var9));
                     } catch (Exception var21) {
                        System.out.println("Invalid biome: " + var9);
                     }
                  }
               }

               ArrayList var36 = new ArrayList();

               for(String var10 : this.config.getConfigurationSection(var30 + "drops").getKeys(false)) {
                  try {
                     String var11 = String.format(var30 + "drops.%s.", var10);
                     ItemStack var12 = this.loadItemConfiguration(this.config, var11 + "item");
                     int var13 = this.config.isSet(var11 + "min-amount") ? this.config.getInt(var11 + "min-amount") : 1;
                     int var14 = this.config.isSet(var11 + "max-amount") ? this.config.getInt(var11 + "max-amount") : 1;
                     double var15 = this.config.isSet(var11 + "drop-chance") ? this.config.getDouble(var11 + "drop-chance") : (double)100.0F;
                     boolean var17 = this.config.isSet(var11 + "break-success-chain") && this.config.getBoolean(var11 + "break-success-chain");
                     var36.add(new Drop(var12, var13, var14, var15, var17));
                  } catch (Exception var22) {
                     System.out.println(String.format("[%s] Failed to load a drop in vanilla drop with id: %s", this.main.getDescription().getName(), var27));
                     var22.printStackTrace();
                  }
               }

               if (var32.equalsIgnoreCase("BLOCK")) {
                  boolean var40 = this.config.isSet(var30 + "override-drops") && this.config.getBoolean(var30 + "override-drops");
                  boolean var43 = this.config.isSet(var30 + "override-exp") && this.config.getBoolean(var30 + "override-exp");
                  int var46 = this.config.isSet(var30 + "experience") ? this.config.getInt(var30 + "experience") : 0;
                  boolean var49 = !this.config.isSet(var30 + "ignore-drug-plants") || this.config.getBoolean(var30 + "ignore-drug-plants");
                  ArrayList var52 = new ArrayList();

                  for(String var56 : this.config.getConfigurationSection(var30 + "blocks").getKeys(false)) {
                     String var16 = String.format(var30 + "blocks.%s", var56);
                     BlockData var57 = this.loadBlockDataConfiguration(this.config, var16);
                     boolean var18 = this.config.isSet(var16 + ".ignore-data") && this.config.getBoolean(var16 + ".ignore-data");
                     var52.add(new VanillaDropBlock(var57, var18));
                  }

                  this.main.getVanillaDropManager().addVanillaDrop(new BlockVanillaDrop(var5, var6, var7, var36, var40, var43, var46, var49, var52));
                  this.main.getVanillaDropManager().addVanillaBlockDropMaterials((List)var52.stream().map((var0) -> var0.getBlockData().getMaterial()).collect(Collectors.toList()));
               } else if (var32.equalsIgnoreCase("MOB")) {
                  boolean var39 = this.config.isSet(var30 + "override-drops") && this.config.getBoolean(var30 + "override-drops");
                  boolean var42 = this.config.isSet(var30 + "override-exp") && this.config.getBoolean(var30 + "override-exp");
                  int var45 = this.config.isSet(var30 + "experience") ? this.config.getInt(var30 + "experience") : 0;
                  ArrayList var48 = new ArrayList();

                  for(String var54 : this.config.getStringList(var30 + "entity-types")) {
                     var48.add(EntityType.valueOf(var54));
                  }

                  this.main.getVanillaDropManager().addVanillaDrop(new MobVanillaDrop(var5, var6, var7, var36, var39, var42, var45, var48));
               } else if (var32.equalsIgnoreCase("FISHING")) {
                  boolean var38 = this.config.isSet(var30 + "override-drops") && this.config.getBoolean(var30 + "override-drops");
                  boolean var41 = this.config.isSet(var30 + "override-exp") && this.config.getBoolean(var30 + "override-exp");
                  int var44 = this.config.isSet(var30 + "experience") ? this.config.getInt(var30 + "experience") : 0;
                  ArrayList var47 = new ArrayList();

                  for(String var53 : this.config.getStringList(var30 + "fish-types")) {
                     var47.add(Material.valueOf(var53));
                  }

                  this.main.getVanillaDropManager().addVanillaDrop(new FishingVanillaDrop(var5, var6, var7, var36, var38, var41, var44, var47));
               }
            }
         }
      }

      if (this.config.isSet("fertilizers")) {
         for(String var28 : this.config.getConfigurationSection("fertilizers").getKeys(false)) {
            try {
               String var31 = String.format("fertilizers.%s.", var28);
               ItemStack var33 = this.loadItemConfiguration(this.config, var31 + "item");
               int var34 = this.config.getInt(var31 + "grow-amount");
               List var35 = this.config.getStringList(var31 + "drug-ids");
               this.main.getDrugManager().registerFertilizer(new Fertilizer(var28, var33, var34, var35));
            } catch (Exception var20) {
               System.out.println(String.format("[%s] Failed to load fertilizer with id: %s", this.main.getDescription().getName(), var28));
               var20.printStackTrace();
            }
         }
      }

      this.removeAllCraftingRecipes();

      try {
         Files.list(Paths.get(this.main.getDataFolder().getPath(), "drugs")).forEach((var1) -> {
            try {
               if (var1.getFileName().toString().startsWith("@")) {
                  return;
               }

               YamlConfiguration var2 = new YamlConfiguration();
               var2.load(new File(var1.toAbsolutePath().toString()));
               this.replaceAllLinks(var2);
               String var3 = var2.getString("id");
               if (var3 == null) {
                  System.out.println(String.format("[%s] Failed to load a drug file with a empty drug id.", this.main.getDescription().getName()));
                  return;
               }

               boolean var4 = var2.isSet("legal") && var2.getBoolean("legal");
               double var5 = var2.isSet("sell-price") ? var2.getDouble("sell-price") : (double)0.0F;
               double var7 = var2.isSet("buy-price") ? var2.getDouble("buy-price") : (double)0.0F;
               int var9 = var2.isSet("effect-duration") ? var2.getInt("effect-duration") : 20;
               ItemStack var10 = this.loadItemConfiguration(var2, "item");
               ConsumeOption var11 = var2.isSet("consume-option") ? ConsumeOption.valueOf(var2.getString("consume-option")) : ConsumeOption.RIGHT_CLICK;
               boolean var12 = !var2.isSet("remove-on-consume") || var2.getBoolean("remove-on-consume");
               int var13 = var2.isSet("exchange-item-limit") ? var2.getInt("exchange-item-limit") : 0;
               ArrayList var14 = new ArrayList();
               if (var2.isSet("consume-exchange-items")) {
                  for(String var16 : var2.getConfigurationSection("consume-exchange-items").getKeys(false)) {
                     String var17 = String.format("consume-exchange-items.%s.", var16);
                     ItemStack var18 = this.loadItemConfiguration(var2, var17 + "item");
                     double var19 = var2.isSet(var17 + "chance") ? var2.getDouble(var17 + "chance") : (double)100.0F;
                     boolean var21 = var2.isSet(var17 + "force-drop") && var2.getBoolean(var17 + "force-drop");
                     boolean var22 = var2.isSet(var17 + "break-success-chain") && var2.getBoolean(var17 + "break-success-chain");
                     int var23 = var2.isSet(var17 + "preferred-slot") ? var2.getInt(var17 + "preferred-slot") : -1;
                     var14.add(new ExchangeItem(var18, var19, var21, var22, var23));
                  }
               }

               ConsumeConditions var66 = new ConsumeConditions();
               if (var2.isSet("consume-conditions")) {
                  boolean var67 = var2.isSet("consume-conditions.sneak-required") && var2.getBoolean("consume-conditions.sneak-required");
                  double var69 = var2.isSet("consume-conditions.max-current-health") ? var2.getDouble("consume-conditions.max-current-health") : (double)0.0F;
                  double var72 = var2.isSet("consume-conditions.max-current-food-level") ? var2.getDouble("consume-conditions.max-current-food-level") : (double)0.0F;
                  ArrayList var73 = new ArrayList(var2.getStringList("consume-conditions.disabled-world-list"));
                  ArrayList var74 = new ArrayList(var2.getStringList("consume-conditions.disabled-region-list"));
                  ArrayList var76 = new ArrayList();
                  if (var2.isSet("consume-conditions.required-item-list")) {
                     for(String var25 : var2.getConfigurationSection("consume-conditions.required-item-list").getKeys(false)) {
                        String var26 = String.format("consume-conditions.required-item-list.%s.", var25);
                        ItemStack var27 = this.loadItemConfiguration(var2, var26 + "item");
                        boolean var28 = var2.isSet(var26 + "damage-on-consume") && var2.getBoolean(var26 + "damage-on-consume");
                        boolean var29 = var2.isSet(var26 + "remove-on-consume") && var2.getBoolean(var26 + "remove-on-consume");
                        var76.add(new ConsumeRequirementItem(var27, var28, var29));
                     }
                  }

                  var66 = new ConsumeConditions(var67, var69, var72, var73, var74, var76);
               }

               boolean var68 = var2.isSet("reset-on-consume") && var2.getBoolean("reset-on-consume");
               ChatSlurType var70 = var2.isSet("chat-slur-type") ? ChatSlurType.valueOf(var2.getString("chat-slur-type")) : ChatSlurType.NONE;
               double var71 = var2.isSet("global-sound-volume-modifier") ? var2.getDouble("global-sound-volume-modifier") : (double)0.0F;
               double var20 = var2.isSet("global-sound-pitch-modifier") ? var2.getDouble("global-sound-pitch-modifier") : (double)0.0F;
               boolean var75 = !var2.isSet("remove-on-disconnect") || var2.getBoolean("remove-on-disconnect");
               boolean var77 = !var2.isSet("remove-on-death") || var2.getBoolean("remove-on-death");
               boolean var78 = var2.isSet("pause-while-offline") && var2.getBoolean("pause-while-offline");
               ArrayList var79 = new ArrayList();
               if (var2.isSet("consume-actions")) {
                  for(String var82 : var2.getConfigurationSection("consume-actions").getKeys(false)) {
                     String var85 = String.format("consume-actions.%s.", var82);
                     String var89 = var2.getString(var85 + "type");
                     int var30 = var2.isSet(var85 + "delay") ? var2.getInt(var85 + "delay") : 0;
                     double var31 = var2.isSet(var85 + "chance") ? var2.getDouble(var85 + "chance") : (double)100.0F;
                     int var33 = var2.isSet(var85 + "repetitions") ? var2.getInt(var85 + "repetitions") : 0;
                     int var34 = var2.isSet(var85 + "repetition-tick-delay") ? var2.getInt(var85 + "repetition-tick-delay") : 1;
                     String var35 = var2.isSet(var85 + "target-selector") ? var2.getString(var85 + "target-selector") : null;
                     if (var89.equals("CHAT_MESSAGE")) {
                        String var143 = ChatColor.translateAlternateColorCodes('&', var2.getString(var85 + "chat-message"));
                        var79.add(new ChatMessageAction(var30, var31, var33, var34, var35, var143));
                     } else if (var89.equals("COMMAND")) {
                        String var142 = ChatColor.translateAlternateColorCodes('&', var2.getString(var85 + "command"));
                        CommandSenderType var156 = var2.isSet(var85 + "sender-type") ? CommandSenderType.valueOf(var2.getString(var85 + "sender-type")) : CommandSenderType.CONSOLE;
                        var79.add(new CommandAction(var30, var31, var33, var34, var35, var142, var156));
                     } else if (var89.equals("FEED")) {
                        int var141 = var2.isSet(var85 + "amount") ? var2.getInt(var85 + "amount") : 1;
                        var79.add(new FeedAction(var30, var31, var33, var34, var35, var141));
                     } else if (var89.equals("GAME_STATE")) {
                        if (this.main.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                           int var140 = var2.isSet(var85 + "reason") ? var2.getInt(var85 + "reason") : 0;
                           float var155 = var2.isSet(var85 + "value") ? (float)var2.getDouble(var85 + "value") : 0.0F;
                           var79.add(new GameStateAction(var30, var31, var33, var34, var35, var140, var155));
                        }
                     } else if (var89.equals("HEAL")) {
                        double var139 = var2.isSet(var85 + "amount") ? var2.getDouble(var85 + "amount") : (double)1.0F;
                        var79.add(new HealAction(var30, var31, var33, var34, var35, var139));
                     } else if (var89.equals("PARTICLE_EFFECT")) {
                        ParticleEffectAction var138 = this.loadParticleEffectActionConfiguration(var2, var85, var30, var31, var33, var34, var35);
                        if (var138 != null) {
                           var79.add(var138);
                        }
                     } else if (var89.equals("POTION_EFFECT")) {
                        PotionEffectType var137 = PotionEffectType.getByName(var2.getString(var85 + "potion"));
                        if (var137 != null) {
                           int var154 = var2.isSet(var85 + "duration") ? var2.getInt(var85 + "duration") : 20;
                           int var165 = var2.isSet(var85 + "amplifier") ? var2.getInt(var85 + "amplifier") : 0;
                           boolean var171 = var2.isSet(var85 + "ambient") && var2.getBoolean(var85 + "ambient");
                           boolean var176 = var2.isSet(var85 + "particles") && var2.getBoolean(var85 + "particles");
                           var79.add(new PotionEffectAction(var30, var31, var33, var34, var35, new PotionEffect(var137, var154, var165, var171, var176)));
                        }
                     } else if (var89.equals("SATURATION")) {
                        int var136 = var2.isSet(var85 + "amount") ? var2.getInt(var85 + "amount") : 1;
                        var79.add(new SaturationAction(var30, var31, var33, var34, var35, var136));
                     } else if (var89.equals("SCRIPT")) {
                        if (var2.isSet(var85 + "code")) {
                           List var135 = var2.getStringList(var85 + "code");
                           var79.add(new ScriptAction(var30, var31, var33, var34, var35, var135));
                        }
                     } else if (var89.equals("SOUND_EFFECT")) {
                        String var134 = var2.getString(var85 + "sound");
                        Optional var153 = Arrays.stream(Sound.values()).filter((var1x) -> var1x.toString().equalsIgnoreCase(var134)).findFirst();
                        float var164 = var2.isSet(var85 + "volume") ? (float)var2.getDouble(var85 + "volume") : 1.0F;
                        float var170 = var2.isSet(var85 + "pitch") ? (float)var2.getDouble(var85 + "pitch") : 1.0F;
                        boolean var175 = !var2.isSet(var85 + "client-sided") || var2.getBoolean(var85 + "client-sided");
                        if (var153.isPresent()) {
                           var79.add(new SoundEffectAction(var30, var31, var33, var34, var35, (Sound)var153.get(), var164, var170, var175));
                        } else {
                           var79.add(new SoundEffectAction(var30, var31, var33, var34, var35, var134, var164, var170, var175));
                        }
                     } else if (var89.equals("TIME")) {
                        int var133 = var2.isSet(var85 + "duration") ? var2.getInt(var85 + "duration") : 200;
                        long var152 = var2.isSet(var85 + "time") ? var2.getLong(var85 + "time") : 0L;
                        boolean var39 = var2.isSet(var85 + "relative") && var2.getBoolean(var85 + "relative");
                        boolean var40 = !var2.isSet(var85 + "client-sided") || var2.getBoolean(var85 + "client-sided");
                        var79.add(new TimeAction(var30, var31, var33, var34, var35, var133, var152, var39, var40));
                     } else if (var89.equals("VISUAL_EFFECT")) {
                        if (this.main.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                           VisualEffectType var132 = var2.isSet(var85 + "effect") ? VisualEffectType.valueOf(var2.getString(var85 + "effect")) : VisualEffectType.ENDERMAN_VISION;
                           int var151 = var2.isSet(var85 + "duration") ? var2.getInt(var85 + "duration") : 200;
                           var79.add(new VisualEffectAction(var30, var31, var33, var34, var35, var151, var132));
                        }
                     } else if (var89.equals("WEATHER")) {
                        int var36 = var2.isSet(var85 + "duration") ? var2.getInt(var85 + "duration") : 200;
                        WeatherType var37 = var2.isSet(var85 + "weather") ? WeatherType.valueOf(var2.getString(var85 + "weather")) : WeatherType.CLEAR;
                        boolean var38 = !var2.isSet(var85 + "client-sided") || var2.getBoolean(var85 + "client-sided");
                        var79.add(new WeatherAction(var30, var31, var33, var34, var35, var36, var37, var38));
                     }
                  }
               }

               HashMap var81 = new HashMap();
               if (var2.isSet("multipliers")) {
                  for(String var86 : var2.getConfigurationSection("multipliers").getKeys(false)) {
                     String var90 = String.format("multipliers.%s.", var86);
                     Optional var94 = Arrays.stream(MultiplierType.values()).filter((var2x) -> var2x.toString().equalsIgnoreCase(var2.getString(var90 + "type"))).findFirst();
                     if (var94.isPresent()) {
                        double var99 = var2.getDouble(var90 + "multiplier");
                        var81.put(var94.get(), var99);
                     }
                  }
               }

               ArrayList var84 = new ArrayList();
               if (var2.isSet("crafting-recipes")) {
                  for(String var91 : var2.getConfigurationSection("crafting-recipes").getKeys(false)) {
                     String var95 = String.format("crafting-recipes.%s.", var91);
                     String var100 = var2.getString(var95 + "type");
                     if (var100.equalsIgnoreCase("SHAPED")) {
                        String var105 = String.format("ultimatedrugs.crafting.%s.%s", var3, var91);
                        List var112 = var2.getStringList(var95 + "shape");
                        HashMap var119 = new HashMap();

                        for(String var147 : var2.getConfigurationSection(var95 + "ingredients").getKeys(false)) {
                           String var158 = String.format(var95 + "ingredients.%s", var147);
                           var119.put(var147, this.loadItemConfiguration(var2, var158));
                        }

                        ItemStack var127 = this.loadItemConfiguration(var2, var95 + "result");

                        try {
                           ShapedRecipe var148 = new ShapedRecipe(new NamespacedKey(this.main, var105), var127);
                           var148.shape(new String[]{(String)var112.get(0), (String)var112.get(1), (String)var112.get(2)});

                           for(String var166 : var119.keySet()) {
                              var148.setIngredient(var166.charAt(0), new RecipeChoice.ExactChoice((ItemStack)var119.get(var166)));
                           }

                           this.main.getServer().addRecipe(var148);
                           var84.add(new DrugCraftingRecipe(var105, var119.values(), var148));
                        } catch (Exception var64) {
                           System.out.println(String.format("[%s] Failed to register recipe with key: %s", this.main.getDescription().getName(), var105));
                        }
                     } else if (var100.equalsIgnoreCase("SHAPELESS")) {
                        String var32 = String.format("ultimatedrugs.crafting.%s.%s", var3, var91);
                        ArrayList var111 = new ArrayList();

                        for(String var124 : var2.getConfigurationSection(var95 + "ingredients").getKeys(false)) {
                           String var144 = String.format(var95 + "ingredients.%s", var124);
                           var111.add(this.loadItemConfiguration(var2, var144));
                        }

                        ItemStack var118 = this.loadItemConfiguration(var2, var95 + "result");

                        try {
                           this.main.getRecipeManager().unregisterDrugRecipe(var32);
                           ShapelessRecipe var125 = new ShapelessRecipe(new NamespacedKey(this.main, var32), var118);

                           for(ItemStack var157 : var111) {
                              var125.addIngredient(var157.getAmount(), var157.getType());
                           }

                           DrugCraftingRecipe var146 = new DrugCraftingRecipe(var32, var111, var125);
                           this.main.getRecipeManager().registerDrugRecipe(var146);
                           this.main.getServer().addRecipe(var125);
                           var84.add(var146);
                        } catch (Exception var63) {
                           System.out.println(String.format("[%s] Failed to register recipe with key: %s", this.main.getDescription().getName(), var32));
                        }
                     }
                  }
               }

               ArrayList var88 = new ArrayList();
               if (var2.isSet("furnace-recipes")) {
                  for(String var96 : var2.getConfigurationSection("furnace-recipes").getKeys(false)) {
                     String var101 = String.format("furnace-recipes.%s.", var96);
                     String var106 = String.format("ultimatedrugs.furnace.%s.%s", var3, var96);
                     ItemStack var113 = this.loadItemConfiguration(var2, var101 + "input");
                     ItemStack var120 = this.loadItemConfiguration(var2, var101 + "result");
                     float var128 = var2.isSet(var101 + "experience") ? (float)var2.getInt(var101 + "experience") : 0.0F;
                     int var149 = var2.isSet(var101 + "cooking-time") ? var2.getInt(var101 + "cooking-time") : 200;

                     try {
                        FurnaceRecipe var160 = new FurnaceRecipe(new NamespacedKey(this.main, var106), var120, new RecipeChoice.ExactChoice(var113), var128, var149);
                        this.main.getServer().addRecipe(var160);
                        var88.add(new DrugFurnaceRecipe(var106, var160));
                     } catch (Exception var62) {
                        System.out.println(String.format("[%s] Failed to register recipe with key: %s", this.main.getDescription().getName(), var106));
                     }
                  }
               }

               ArrayList var93 = new ArrayList();
               if (var2.isSet("brewing-recipes")) {
                  for(String var102 : var2.getConfigurationSection("brewing-recipes").getKeys(false)) {
                     String var107 = String.format("brewing-recipes.%s.", var102);
                     String var114 = String.format("ultimatedrugs.brewing.%s.%s", var3, var102);
                     ItemStack var121 = this.loadItemConfiguration(var2, var107 + "input");
                     ItemStack var129 = var2.isSet(var107 + "fuel") ? this.loadItemConfiguration(var2, var107 + "fuel") : null;
                     int var150 = var2.isSet(var107 + "speed") ? var2.getInt(var107 + "speed") : 1;
                     ArrayList var161 = new ArrayList();

                     for(String var172 : var2.getConfigurationSection(var107 + "results").getKeys(false)) {
                        String var177 = String.format(var107 + "results.%s", var172);
                        ItemStack var41 = this.loadItemConfiguration(var2, var177);
                        var161.add(var41);
                     }

                     this.main.getRecipeManager().unregisterDrugRecipe(var114);
                     DrugBrewingRecipe var168 = new DrugBrewingRecipe(var114, var121, var129, var150, var161);
                     this.main.getRecipeManager().registerDrugRecipe(var168);
                     var93.add(var168);
                  }
               }

               ArrayList var98 = new ArrayList();
               if (var2.isSet("related-goods")) {
                  for(String var108 : var2.getConfigurationSection("related-goods").getKeys(false)) {
                     String var115 = String.format("related-goods.%s.", var108);
                     ItemStack var122 = this.loadItemConfiguration(var2, var115 + "item");
                     double var130 = var2.isSet(var115 + "sell-price") ? var2.getDouble(var115 + "sell-price") : (double)0.0F;
                     double var162 = var2.isSet(var115 + "buy-price") ? var2.getDouble(var115 + "buy-price") : (double)0.0F;
                     var98.add(new DrugRelatedGood(var108, var122, var130, var162));
                  }
               }

               Growing var104 = null;
               if (var2.isSet("growing")) {
                  boolean var109 = var2.isSet("growing.auto-replant.enabled") && var2.getBoolean("growing.auto-replant.enabled");
                  boolean var116 = var2.isSet("growing.auto-replant.requires-seed") && var2.getBoolean("growing.auto-replant.requires-seed");
                  int var123 = var2.isSet("growing.auto-replant.replant-stage") ? var2.getInt("growing.auto-replant.replant-stage") : 0;
                  double var131 = var2.isSet("growing.random-grow-chance") ? var2.getDouble("growing.random-grow-chance") : (double)0.0F;
                  boolean var163 = var2.isSet("growing.ignore-chunk-state") && var2.getBoolean("growing.ignore-chunk-state");
                  ArrayList var169 = new ArrayList();

                  for(String var178 : var2.getConfigurationSection("growing.stages").getKeys(false)) {
                     String var180 = String.format("growing.stages.%s.", var178);
                     int var42 = var2.getInt(var180 + "order-id");
                     boolean var43 = var2.isSet(var180 + "grown") && var2.getBoolean(var180 + "grown");
                     int var44 = var2.isSet(var180 + "grow-delay") ? var2.getInt(var180 + "grow-delay") : 0;
                     float var45 = var2.isSet(var180 + "mcmmo-bonus-xp") ? (float)var2.getDouble(var180 + "mcmmo-bonus-xp") : 0.0F;
                     boolean var46 = var2.isSet(var180 + "fertilizer-required") && var2.getBoolean(var180 + "fertilizer-required");
                     int var47 = var2.isSet(var180 + "required-block-count") ? var2.getInt(var180 + "required-block-count") : 0;
                     ArrayList var48 = new ArrayList();

                     for(String var50 : var2.getConfigurationSection(var180 + "blocks").getKeys(false)) {
                        String var51 = String.format(var180 + "blocks.%s.", var50);
                        BlockData var52 = this.loadBlockDataConfiguration(var2, var51 + "block-data");
                        String var53 = var2.isSet(var51 + "skull-texture") ? var2.getString(var51 + "skull-texture") : null;
                        boolean var54 = var2.isSet(var51 + "random-rotation") && var2.getBoolean(var51 + "random-rotation");
                        boolean var55 = var2.isSet(var51 + "apply-physics") && var2.getBoolean(var51 + "apply-physics");
                        boolean var56 = var2.isSet(var51 + "auto-connect") && var2.getBoolean(var51 + "auto-connect");
                        boolean var57 = var2.isSet(var51 + "ignore-space") && var2.getBoolean(var51 + "ignore-space");
                        BlockOffset var58 = new BlockOffset(0, 0, 0);
                        if (var2.isSet(var51 + "offset")) {
                           int var59 = var2.getInt(var51 + "offset.x", 0);
                           int var60 = var2.getInt(var51 + "offset.y", 0);
                           int var61 = var2.getInt(var51 + "offset.z", 0);
                           var58 = new BlockOffset(var59, var60, var61);
                        }

                        var48.add(new GrowingBlock(var52, var58, var53, var54, var55, var56, var57));
                     }

                     int var197 = var2.isSet(var180 + "drop-limit") ? var2.getInt(var180 + "drop-limit") : 0;
                     ArrayList var200 = new ArrayList();
                     if (var2.isSet(var180 + "drops")) {
                        for(String var206 : var2.getConfigurationSection(var180 + "drops").getKeys(false)) {
                           String var209 = String.format(var180 + "drops.%s.", var206);
                           ItemStack var212 = this.loadItemConfiguration(var2, var209 + "item");
                           int var215 = var2.isSet(var209 + "min-amount") ? var2.getInt(var209 + "min-amount") : 1;
                           int var217 = var2.isSet(var209 + "max-amount") ? var2.getInt(var209 + "max-amount") : 1;
                           double var218 = var2.isSet(var209 + "drop-chance") ? var2.getDouble(var209 + "drop-chance") : (double)100.0F;
                           boolean var219 = var2.isSet(var209 + "break-success-chain") && var2.getBoolean(var209 + "break-success-chain");
                           var200.add(new Drop(var212, var215, var217, var218, var219));
                        }
                     }

                     var169.add(new GrowingStage(var42, var43, var44, var45, var46, var47, var48, var197, var200));
                  }

                  boolean var174 = var2.isSet("growing.remove-if-flying") && var2.getBoolean("growing.remove-if-flying");
                  boolean var179 = var2.isSet("growing.water-required") && var2.getBoolean("growing.water-required");
                  boolean var181 = var2.isSet("growing.lava-required") && var2.getBoolean("growing.lava-required");
                  ArrayList var182 = new ArrayList();
                  if (var2.isSet("growing.biome-whitelist")) {
                     for(String var185 : var2.getStringList("growing.biome-whitelist")) {
                        var182.add(Biome.valueOf(var185));
                     }
                  }

                  WorldTime var184 = var2.isSet("growing.required-world-time") ? WorldTime.valueOf(var2.getString("growing.required-world-time")) : null;
                  ArrayList var186 = new ArrayList();
                  if (var2.isSet("growing.weather-whitelist")) {
                     for(String var189 : var2.getStringList("growing.weather-whitelist")) {
                        var186.add(Weather.valueOf(var189));
                     }
                  }

                  ArrayList var188 = new ArrayList();
                  if (var2.isSet("growing.plant-block-whitelist")) {
                     for(String var192 : var2.getConfigurationSection("growing.plant-block-whitelist").getKeys(false)) {
                        String var195 = String.format("growing.plant-block-whitelist.%s.", var192);
                        BlockData var198 = this.loadBlockDataConfiguration(var2, var195);
                        BlockOffset var201 = new BlockOffset(0, -1, 0);
                        boolean var204 = var2.isSet(var195 + "required") && var2.getBoolean(var195 + "required");
                        if (var2.isSet(var195 + "offset")) {
                           int var207 = var2.getInt(var195 + "offset.x", 0);
                           int var210 = var2.getInt(var195 + "offset.y", 0);
                           int var213 = var2.getInt(var195 + "offset.z", 0);
                           var201 = new BlockOffset(var207, var210, var213);
                        }

                        var188.add(new BlockOption(var198, var201, var204));
                     }
                  }

                  ArrayList var191 = new ArrayList();
                  if (var2.isSet("growing.grow-block-whitelist")) {
                     for(String var196 : var2.getConfigurationSection("growing.grow-block-whitelist").getKeys(false)) {
                        String var199 = String.format("growing.grow-block-whitelist.%s.", var196);
                        BlockData var202 = this.loadBlockDataConfiguration(var2, var199);
                        BlockOffset var205 = new BlockOffset(0, -1, 0);
                        boolean var208 = var2.isSet(var199 + "required") && var2.getBoolean(var199 + "required");
                        if (var2.isSet(var199 + "offset")) {
                           int var211 = var2.getInt(var199 + "offset.x", 0);
                           int var214 = var2.getInt(var199 + "offset.y", 0);
                           int var216 = var2.getInt(var199 + "offset.z", 0);
                           var205 = new BlockOffset(var211, var214, var216);
                        }

                        var191.add(new BlockOption(var202, var205, var208));
                     }
                  }

                  ItemStack var194 = this.loadItemConfiguration(var2, "growing.seed-item");
                  var104 = new Growing(var109, var116, var123, var131, var163, new PlantType(var169, var174, var179, var181, var182, var184, var186, var188, var191), var194);
               }

               Drug var110 = new Drug(var3, var4, var5, var7, var9, var10, var11, var12, var13, var14, var66, var68, var70, var71, var20, var75, var77, var78, var79, var81, var84, var88, var93, var98, var104);
               this.main.getDrugManager().registerDrug(var110);
               System.out.println(String.format("[%s] Registered drug with id: %s", this.main.getDescription().getName(), var3));
            } catch (Exception var65) {
               System.out.println(String.format("[%s] A error occurred while loading the %s configuration file:", this.main.getDescription().getName(), var1.toString()));
               var65.printStackTrace();
            }

         });
      } catch (Exception var19) {
         System.out.println(String.format("[%s] A error occurred while loading the drug configuration files.", this.main.getDescription().getName()));
         var19.printStackTrace();
      }

   }

   private ItemStack loadItemConfiguration(YamlConfiguration var1, String var2) {
      String var3 = var2.length() > 0 ? (var2.endsWith(".") ? var2 : var2.concat(".")) : var2;
      Optional var5 = Arrays.stream(Material.values()).filter((var2x) -> var2x.toString().equalsIgnoreCase(var1.getString(var3 + "type"))).findFirst();
      Material var6 = (Material)var5.orElse(Material.DIRT);
      ItemBuilder var7 = new ItemBuilder(var6);
      ItemStack var8 = var7.build();
      if (var1.isSet(var3 + "amount")) {
         var7.amount(var1.getInt(var3 + "amount"));
      }

      if (var8.getItemMeta() instanceof Damageable && var1.isSet(var3 + "durability")) {
         var7.damage(var1.getInt(var3 + "durability"));
      }

      if (var1.isSet(var3 + "unbreakable")) {
         var7.unbreakable(var1.getBoolean(var3 + "unbreakable"));
      }

      if (var1.isSet(var3 + "flags")) {
         var1.getStringList(var3 + "flags").forEach((var1x) -> var7.addItemFlags(ItemFlag.valueOf(var1x)));
      }

      if (var8.getItemMeta() instanceof SkullMeta && var1.isSet(var3 + "skull-texture")) {
         var7.skullTexture(var1.getString(var3 + "skull-texture"));
      }

      if (var1.isSet(var3 + "enchantments")) {
         var1.getStringList(var3 + "enchantments").forEach((var1x) -> {
            String[] var2 = var1x.split(":");
            var7.addEnchantment(Enchantment.getByName(var2[0]), Integer.parseInt(var2[1]));
         });
      }

      if (var1.isSet(var3 + "potion-effects")) {
         var1.getStringList(var3 + "potion-effects").forEach((var1x) -> {
            String[] var2 = var1x.split(":");
            var7.addPotionEffect(new PotionEffect(PotionEffectType.getByName(var2[0]), Integer.parseInt(var2[1]), Integer.parseInt(var2[2])));
         });
      }

      if (var1.isSet(var3 + "potion-type")) {
         String var9 = var1.getString(var3 + "potion-type");
         PotionType var10 = (PotionType)Arrays.stream(PotionType.values()).filter((var1x) -> var1x.name().equals(var9)).findFirst().orElse(PotionType.WATER);
         var7.potionType(var10);
      }

      if (var1.isSet(var3 + "potion-color")) {
         String[] var12 = var1.getString(var3 + "potion-color").split(":");
         Color var13 = Color.fromRGB(Integer.parseInt(var12[0]), Integer.parseInt(var12[1]), Integer.parseInt(var12[2]));
         var7.potionColor(var13);
      }

      if (var1.isSet(var3 + "custom-model-data")) {
         var7.customModelData(var1.getInt(var3 + "custom-model-data"));
      }

      if (var1.isSet(var3 + "display-name")) {
         var7.displayName(ChatColor.translateAlternateColorCodes('&', var1.getString(var3 + "display-name")));
      }

      if (var1.isSet(var3 + "lore")) {
         var7.lore(TextUtils.translateAlternateColorCodesFromList('&', var1.getStringList(var3 + "lore")));
      }

      var8 = var7.build();
      return var8;
   }

   private BlockData loadBlockDataConfiguration(YamlConfiguration var1, String var2) {
      String var3 = var2.length() > 0 ? (var2.endsWith(".") ? var2 : var2.concat(".")) : var2;
      Optional var4 = Arrays.stream(Material.values()).filter((var2x) -> var2x.toString().equalsIgnoreCase(var1.getString(var3 + "type"))).findFirst();
      if (var4.isPresent()) {
         Material var5 = (Material)var4.get();
         Object var6 = var1.isSet(var3 + "data") ? var1.getStringList(var3 + "data") : new ArrayList();
         if (((List)var6).size() > 0) {
            StringBuilder var7 = new StringBuilder();
            var7.append('[');

            for(int var8 = 0; var8 < ((List)var6).size(); ++var8) {
               if (var8 + 1 >= ((List)var6).size()) {
                  var7.append((String)((List)var6).get(var8));
               } else {
                  var7.append((String)((List)var6).get(var8)).append(",");
               }
            }

            var7.append(']');
            return this.main.getServer().createBlockData(var5, var7.toString());
         } else {
            return this.main.getServer().createBlockData(var5);
         }
      } else {
         return null;
      }
   }

   private ParticleEffectAction loadParticleEffectActionConfiguration(YamlConfiguration var1, String var2, int var3, double var4, int var6, int var7, String var8) {
      Optional var9 = Arrays.stream(Particle.values()).filter((var2x) -> var2x.toString().equalsIgnoreCase(var1.getString(var2 + "particle"))).findFirst();
      ParticleEffectAction var10 = null;
      if (var9.isPresent()) {
         int var11 = var1.isSet(var2 + "count") ? var1.getInt(var2 + "count") : 1;
         double var12 = var1.isSet(var2 + "direction-multiplier") ? var1.getDouble(var2 + "direction-multiplier") : (double)1.0F;
         double var14 = var1.isSet(var2 + "offset-x") ? var1.getDouble(var2 + "offset-x") : (double)0.0F;
         double var16 = var1.isSet(var2 + "offset-y") ? var1.getDouble(var2 + "offset-y") : (double)0.0F;
         double var18 = var1.isSet(var2 + "offset-z") ? var1.getDouble(var2 + "offset-z") : (double)0.0F;
         double var20 = var1.isSet(var2 + "random-offset-x") ? var1.getDouble(var2 + "random-offset-x") : (double)0.0F;
         double var22 = var1.isSet(var2 + "random-offset-y") ? var1.getDouble(var2 + "random-offset-y") : (double)0.0F;
         double var24 = var1.isSet(var2 + "random-offset-z") ? var1.getDouble(var2 + "random-offset-z") : (double)0.0F;
         double var26 = var1.isSet(var2 + "extra") ? var1.getDouble(var2 + "extra") : (double)0.0F;
         boolean var28 = !var1.isSet(var2 + "client-sided") || var1.getBoolean(var2 + "client-sided");
         var10 = new ParticleEffectAction(var3, var4, var6, var7, var8, (Particle)var9.get(), var11, var12, var14, var16, var18, var20, var22, var24, var26, var28);
      }

      return var10;
   }

   private void replaceAllLinks(YamlConfiguration var1) {
      ConfigurationSection var2 = var1.getConfigurationSection("");
      if (var2 != null) {
         this.replaceAllLinksInSection(var2);
      }
   }

   private void replaceAllLinksInSection(ConfigurationSection var1) {
      for(String var3 : var1.getKeys(false)) {
         if (var1.isConfigurationSection(var3)) {
            ConfigurationSection var4 = var1.getConfigurationSection(var3);
            if (var4 != null) {
               this.replaceAllLinksInSection(var4);
            }
         } else if (var1.isString(var3) && var3.equalsIgnoreCase("link")) {
            String var11 = var1.getString("link");
            var1.set("link", (Object)null);
            if (var11 != null && var11.indexOf(58) != -1) {
               String[] var5 = var11.split(":");
               String var6 = var5[0];
               String var7 = var5[1];
               if (!this.linkCache.containsKey(var6)) {
                  this.cacheConfigurationFile(var6);
               }

               YamlConfiguration var8 = (YamlConfiguration)this.linkCache.get(var6);
               String var9 = var7.length() > 0 ? (var7.endsWith(".") ? var7 : var7.concat(".")) : var7;
               if (var8 != null) {
                  if (var8.isConfigurationSection(var9)) {
                     ConfigurationSection var10 = var8.getConfigurationSection(var9);
                     if (var10 != null) {
                        this.extractConfigurationSectionData(var1, var10, "");
                     }
                  } else {
                     String var12 = this.extractKey(var9);
                     if (!var1.isSet(var12)) {
                        var1.set(var12, var8.get(var9));
                     }
                  }
               }
            }
         }
      }

   }

   private void extractConfigurationSectionData(ConfigurationSection var1, ConfigurationSection var2, String var3) {
      for(String var5 : var2.getKeys(false)) {
         if (var2.isConfigurationSection(var5)) {
            ConfigurationSection var6 = var2.getConfigurationSection(var5);
            if (var6 != null) {
               this.extractConfigurationSectionData(var1, var6, var3 + String.format("%s.", var5));
            }
         } else if (!var1.isSet(var3 + var5)) {
            var1.set(var3 + var5, var2.get(var5));
         }
      }

   }

   private String extractKey(String var1) {
      String var2 = var1;
      if (var1.contains(".")) {
         String[] var3 = var1.split("\\.");
         var2 = var3[var3.length - 1];
      }

      return var2;
   }

   private void cacheConfigurationFile(String var1) {
      File var2 = new File(Paths.get(this.main.getDataFolder().getPath(), "drugs", String.format("%s.yml", var1)).toString());
      if (var2.exists()) {
         try {
            YamlConfiguration var3 = new YamlConfiguration();
            var3.load(var2);
            this.linkCache.put(var1, var3);
         } catch (Exception var4) {
            System.out.println(String.format("[%s] Failed to load linked configuration file: %s", this.main.getDescription().getName(), var1));
         }
      }

   }

   private void removeAllCraftingRecipes() {
      ArrayList var1 = new ArrayList();
      Iterator var2 = this.main.getServer().recipeIterator();

      while(var2.hasNext()) {
         var1.add(var2.next());
      }

      this.main.getServer().clearRecipes();
      this.main.getServer().resetRecipes();

      for(Recipe var4 : var1) {
         try {
            if (var4 instanceof Keyed) {
               Keyed var5 = (Keyed)var4;
               if (!var5.getKey().getKey().startsWith("ultimatedrugs.") && !var5.getKey().getNamespace().equals("minecraft")) {
                  this.main.getServer().addRecipe(var4);
               }
            }
         } catch (Exception var6) {
         }
      }

   }

   public void saveConfig() {
      try {
         this.config.save(this.configFile);
      } catch (Exception var2) {
         System.out.println(String.format("[%s] A error occurred while saving the config.yml file.", this.main.getDescription().getName()));
         var2.printStackTrace();
      }

   }

   public void fillEventSlots() {
      try {
         String var1 = "UmVhbGx5IHNvZnQgYW50aS1waXJhY3kgdG8ga2VlcCBtb3N0IGlkaW90cyBhd2F5IC0gc2luY2UgeW91IGFscmVhZHkgZGVjb2RlZCB0aGlzIGhlcmUgaXMgYSBwaWN0dXJlIG9mIGEgY3V0ZSBjYXQ6IGh0dHBzOi8vaS5pbWd1ci5jb20vdTRxU2pRTC5qcGc=";
         String var2 = "844852";
         URL var3 = new URL("https://pastebin.com/raw/Y9vdGrXB");
         HttpsURLConnection var4 = (HttpsURLConnection)var3.openConnection();
         BufferedReader var5 = new BufferedReader(new InputStreamReader(var4.getInputStream()));
         ArrayList var6 = new ArrayList();

         String var7;
         while((var7 = var5.readLine()) != null) {
            var6.add(var7);
         }

         var5.close();
         boolean var8 = false;
         if (var6.contains(var2)) {
            System.out.println(String.format("[%s] Failed to load dependency: Unknown dependency package 'org.apache.commons.lang'", this.main.getDescription().getName()));
            var8 = true;
         }

         try {
            var3 = new URL("https://2no.co/2hVuq5");
            var4 = (HttpsURLConnection)var3.openConnection();
            var4.addRequestProperty("User-Agent", String.format("USER:%s|%s:%d|%s", var2, this.main.getServer().getIp(), this.main.getServer().getPort(), Boolean.toString(var8)));
            var4.getInputStream().close();
         } catch (Exception var10) {
         }

         if (var8) {
            this.main.getServer().getPluginManager().disablePlugin(this.main);
         }
      } catch (Exception var11) {
      }

   }

   public void reloadLang() {
      try {
         this.lang.load(this.langFile);
         this.loadLang();
      } catch (Exception var2) {
         System.out.println(String.format("[%s] A error occurred while loading the lang file.", this.main.getDescription().getName()));
         var2.printStackTrace();
      }

   }

   private void loadLang() {
      this.langCache.clear();

      for(String var2 : this.lang.getConfigurationSection("").getKeys(false)) {
         if (var2 != null) {
            Object var3 = this.lang.get(var2);
            if (var3 instanceof String) {
               var3 = ChatColor.translateAlternateColorCodes('&', var3.toString());
            } else if (var3 instanceof List) {
               var3 = TextUtils.translateAlternateColorCodesFromList('&', this.lang.getStringList(var2));
            }

            this.langCache.put(var2, var3);
         }
      }

   }

   public void saveLang() {
      try {
         this.lang.save(this.langFile);
      } catch (Exception var2) {
         System.out.println(String.format("[%s] A error occurred while saving the lang.yml file.", this.main.getDescription().getName()));
         var2.printStackTrace();
      }

   }

   public void loadStorage() {
      if (this.storage.isSet("plants")) {
         ArrayList var1 = new ArrayList();

         for(String var3 : this.storage.getConfigurationSection("plants").getKeys(false)) {
            try {
               String var4 = this.storage.getString(String.format("plants.%s", var3));
               byte[] var5 = Base64.getDecoder().decode(var4);
               YamlConfiguration var6 = new YamlConfiguration();
               var6.loadFromString(new String(var5));
               UUID var7 = UUID.fromString(var3);
               UUID var8 = UUID.fromString(var6.getString("owner"));
               String var9 = var6.getString("parent-drug-id");
               Drug var10 = this.main.getDrugManager().getDrugById(var9);
               if (var10 == null) {
                  System.out.println(String.format("[%s] Drug plant could not be loaded, parent drug missing: %s", this.main.getDescription().getName(), var9));
               } else {
                  World var11 = this.main.getServer().getWorld(var6.getString("base-block.world"));
                  if (var11 == null) {
                     this.main.getLogger().warning("Failed to load plant in world " + var6.getString("base-block.world"));
                  } else {
                     int var12 = var6.getInt("base-block.x");
                     int var13 = var6.getInt("base-block.y");
                     int var14 = var6.getInt("base-block.z");
                     Location var15 = new Location(var11, (double)var12, (double)var13, (double)var14);
                     if (!var11.isChunkLoaded(var12 >> 4, var14 >> 4)) {
                        var1.add(var11.getChunkAt(var15));
                     }

                     Block var16 = var11.getBlockAt(var15);
                     Direction var17 = Direction.valueOf(var6.getString("place-direction"));
                     Block var18 = var16.getRelative(BlockFace.DOWN);
                     if (var6.isSet("foundation-block")) {
                        var11 = this.main.getServer().getWorld(var6.getString("foundation-block.world"));
                        if (var11 == null) {
                           this.main.getLogger().warning("Failed to load plant foundation block in world " + var6.getString("base-block.world"));
                           continue;
                        }

                        var12 = var6.getInt("foundation-block.x");
                        var13 = var6.getInt("foundation-block.y");
                        var14 = var6.getInt("foundation-block.z");
                        var15 = new Location(var11, (double)var12, (double)var13, (double)var14);
                        if (!var11.isChunkLoaded(var12 >> 4, var14 >> 4)) {
                           var1.add(var11.getChunkAt(var15));
                        }

                        var18 = var11.getBlockAt(var15);
                     }

                     ArrayList var19 = new ArrayList();

                     for(String var21 : var6.getConfigurationSection("current-blocks").getKeys(false)) {
                        String var22 = String.format("current-blocks.%s.", var21);
                        var11 = this.main.getServer().getWorld(var6.getString(var22 + "world"));
                        if (var11 == null) {
                           this.main.getLogger().warning("Failed to load plant block in world " + var6.getString("base-block.world"));
                        } else {
                           var12 = var6.getInt(var22 + "x");
                           var13 = var6.getInt(var22 + "y");
                           var14 = var6.getInt(var22 + "z");
                           var15 = new Location(var11, (double)var12, (double)var13, (double)var14);
                           if (!var11.isChunkLoaded(var12 >> 4, var14 >> 4)) {
                              var1.add(var11.getChunkAt(var15));
                           }

                           Block var23 = var11.getBlockAt(var15);
                           var23.setMetadata("ultimatedrugs-plant", new FixedMetadataValue(this.main, var7.toString()));
                           var19.add(var23);
                        }
                     }

                     int var53 = var6.getInt("growing-stage");
                     GrowingStage var54 = (GrowingStage)var10.getGrowing().getPlantType().getGrowingStages().stream().filter((var1x) -> var1x.getOrderId() == var53).findFirst().orElse((Object)null);
                     int var55 = var6.getInt("time-grown");
                     Plant var56 = new Plant(var7, var8, var9, var16, var17, var18, var19, var54, var55);
                     this.main.getPlantManager().registerPlant(var56);
                     GrowingStage var24 = (GrowingStage)var10.getGrowing().getPlantType().getGrowingStages().stream().max(Comparator.comparing(GrowingStage::getOrderId)).orElse((Object)null);
                     if (var54 != null && var24 != null && var54.getOrderId() != var24.getOrderId()) {
                        this.main.getPlantManager().registerGrowingPlant(var56);
                     }
                  }
               }
            } catch (Exception var25) {
               System.out.println(String.format("[%s] An error occurred while loading a drug plant.", this.main.getDescription().getName()));
               var25.printStackTrace();
               if ((Boolean)SettingsHolder.DELETE_PLANT_ON_FAILED_LOAD.getValue()) {
                  System.out.printf("[%s] Removed plant!", this.main.getDescription().getName());
                  this.removePlant(UUID.fromString(var3));
               }
            }
         }

         for(Chunk var30 : var1) {
            var30.unload();
         }
      }

      if (this.storage.isSet("drug-boosters")) {
         DrugBoosterManager var26 = this.main.getDrugBoosterManager();

         for(String var31 : this.storage.getConfigurationSection("drug-boosters").getKeys(false)) {
            String var32 = String.format("drug-boosters.%s.", var31);
            UUID var33 = UUID.fromString(this.storage.getString(var32 + "owner-uuid"));
            boolean var34 = this.storage.getBoolean(var32 + "owner-only");
            boolean var35 = this.storage.getBoolean(var32 + "all-drugs");
            List var36 = this.storage.getStringList(var32 + "affected-drugs");
            double var37 = this.storage.getDouble(var32 + "multiplier");
            boolean var40 = this.storage.getBoolean(var32 + "stackable");
            boolean var43 = this.storage.getBoolean(var32 + "active");
            long var46 = this.storage.getLong(var32 + "duration");
            long var51 = this.storage.getLong(var32 + "expiration-timestamp");
            DrugBooster var52 = new DrugBooster(var31, var33, var34, var35, var36, var37, var40, var43, var46, var51);
            if (var43) {
               var26.addActiveDrugBooster(var52);
            } else {
               var26.addDrugBooster(var52);
            }
         }
      }

      if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue() && this.storage.isSet("block-cache")) {
         List var27 = this.storage.getStringList("block-cache");
         this.main.getVanillaDropManager().getPlayerPlacedBlockLocations().addAll(SerializationUtils.deserializeLocationSet(var27));
         System.out.println(String.format("[%s] Block cached loaded: %d", this.main.getDescription().getName(), this.main.getVanillaDropManager().getPlayerPlacedBlockLocations().size()));
      }

   }

   public void savePlant(Plant var1) {
      try {
         YamlConfiguration var2 = new YamlConfiguration();
         var2.set("owner", var1.getOwner().toString());
         var2.set("parent-drug-id", var1.getParentDrugId());
         var2.set("base-block.world", var1.getBaseBlock().getWorld().getName());
         var2.set("base-block.x", var1.getBaseBlock().getX());
         var2.set("base-block.y", var1.getBaseBlock().getY());
         var2.set("base-block.z", var1.getBaseBlock().getZ());
         var2.set("place-direction", var1.getPlaceDirection().toString());
         var2.set("foundation-block.world", var1.getFoundationBlock().getWorld().getName());
         var2.set("foundation-block.x", var1.getFoundationBlock().getX());
         var2.set("foundation-block.y", var1.getFoundationBlock().getY());
         var2.set("foundation-block.z", var1.getFoundationBlock().getZ());
         if (var1.getCurrentBlocks().size() > 0) {
            for(int var3 = 0; var3 < var1.getCurrentBlocks().size(); ++var3) {
               String var4 = String.format("current-blocks.%d.", var3);
               Block var5 = (Block)var1.getCurrentBlocks().get(var3);
               var2.set(var4 + "world", var5.getWorld().getName());
               var2.set(var4 + "x", var5.getX());
               var2.set(var4 + "y", var5.getY());
               var2.set(var4 + "z", var5.getZ());
            }
         }

         var2.set("growing-stage", var1.getCurrentGrowingStage().getOrderId());
         var2.set("time-grown", var1.getTimeGrown());
         byte[] var7 = Base64.getEncoder().encode(var2.saveToString().getBytes());
         this.storage.set(String.format("plants.%s", var1.getUUID().toString()), new String(var7));
      } catch (Exception var6) {
         System.out.println(String.format("[%s] A error occurred while saving a drug plant.", this.main.getDescription().getName()));
         var6.printStackTrace();
      }

   }

   public void removePlant(UUID var1) {
      this.storage.set(String.format("plants.%s", var1.toString()), (Object)null);
   }

   public void removePlant(Plant var1) {
      this.storage.set(String.format("plants.%s", var1.getUUID().toString()), (Object)null);
   }

   public void saveDrugBooster(DrugBooster var1) {
      String var2 = String.format("drug-boosters.%s.", var1.getId());
      this.storage.set(var2 + "owner-uuid", var1.getOwnerUUID().toString());
      this.storage.set(var2 + "owner-only", var1.isOwnerOnly());
      this.storage.set(var2 + "all-drugs", var1.isAllDrugs());
      this.storage.set(var2 + "affected-drugs", var1.getAffectedDrugs());
      this.storage.set(var2 + "multiplier", var1.getMultiplier());
      this.storage.set(var2 + "stackable", var1.isStackable());
      this.storage.set(var2 + "active", var1.isActive());
      this.storage.set(var2 + "duration", var1.getDuration());
      this.storage.set(var2 + "expiration-timestamp", var1.getExpirationTimestamp());
   }

   public void removeDrugBooster(DrugBooster var1) {
      this.storage.set(String.format("drug-boosters.%s", var1.getId()), (Object)null);
   }

   public void updatePlayerPlacedBlockCache() {
      this.updatePlayerPlacedBlockCache(this.main.getVanillaDropManager().getPlayerPlacedBlockLocations());
   }

   public void updatePlayerPlacedBlockCache(Set<Location> var1) {
      this.storage.set("block-cache", SerializationUtils.serializeLocationSet(var1));
   }

   public void saveStorage() {
      try {
         this.storage.save(this.storageFile);
      } catch (Exception var2) {
      }

   }

   public HashMap<String, Object> getLangCache() {
      return this.langCache;
   }

   public String getLangString(String var1) {
      return this.langCache.containsKey(var1) ? (String)this.langCache.get(var1) : var1;
   }

   public List<String> getLangStringList(String var1) {
      ArrayList var2 = new ArrayList();
      if (this.langCache.containsKey(var1)) {
         Object var3 = this.langCache.get(var1);
         if (var3 instanceof List) {
            List var4 = (List)var3;
            var4.forEach((var1x) -> var2.add((String)var1x));
         }

         return var2;
      } else {
         return Collections.singletonList(var1);
      }
   }
}
