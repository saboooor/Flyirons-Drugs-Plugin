package me.dexuby.UltimateDrugs.handlers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.api.DrugConsumeEvent;
import me.dexuby.UltimateDrugs.drugs.ActiveDrugEffect;
import me.dexuby.UltimateDrugs.drugs.ConsumeConditions;
import me.dexuby.UltimateDrugs.drugs.ConsumeRequirementItem;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.drugs.ExchangeItem;
import me.dexuby.UltimateDrugs.drugs.actions.ConsumeAction;
import me.dexuby.UltimateDrugs.drugs.actions.TimedConsumeAction;
import me.dexuby.UltimateDrugs.drugs.chatslur.ChatSlurConverter;
import me.dexuby.UltimateDrugs.drugs.chatslur.ChatSlurType;
import me.dexuby.UltimateDrugs.managers.ConfigManager;
import me.dexuby.UltimateDrugs.managers.DrugManager;
import me.dexuby.UltimateDrugs.utils.ItemBuilder;
import me.dexuby.UltimateDrugs.utils.MaterialUtils;
import me.dexuby.UltimateDrugs.utils.WGUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DrugHandler implements Listener {
   private Main main;
   private DrugManager drugManager;
   private ConfigManager configManager;

   public DrugHandler(Main var1) {
      this.main = var1;
      this.drugManager = var1.getDrugManager();
      this.configManager = var1.getConfigManager();
   }

   @EventHandler
   public void onDrugConsume(DrugConsumeEvent var1) {
      if (!var1.isCancelled()) {
         final Player var2 = var1.getPlayer();
         final Drug var3 = var1.getDrug();
         ConsumeConditions var4 = var3.getConsumeConditions();
         boolean var5 = false;
         if (this.drugManager.isPlayerOnDrug(var2, var3)) {
            if (!var3.doResetOnConsume()) {
               var2.sendMessage(this.configManager.getLangString("already-on-drug"));
               var1.setCancelled(true);
               return;
            }

            var5 = true;
         }

         if ((!var4.isSneakRequired() || var2.isSneaking()) && !var4.getDisabledWorldList().contains(var2.getWorld().getName()) && (var4.getDisabledRegionList().size() <= 0 || !this.main.getServer().getPluginManager().isPluginEnabled("WorldGuard") || !this.main.getServer().getPluginManager().isPluginEnabled("WorldEdit") || !WGUtils.isInDisabledRegion(var2, var3))) {
            if (var4.getMaxCurrentHealth() > (double)0.0F && var2.getHealth() > var4.getMaxCurrentHealth()) {
               var1.setCancelled(true);
            } else if (var4.getMaxCurrentFoodLevel() > (double)0.0F && (double)var2.getFoodLevel() > var4.getMaxCurrentFoodLevel()) {
               var1.setCancelled(true);
            } else {
               boolean var6 = true;

               for(ConsumeRequirementItem var8 : var4.getRequiredItemList()) {
                  if (!var8.doDamageOnConsume()) {
                     if (!var2.getInventory().containsAtLeast(var8.getItemStack(), var8.getItemStack().getAmount())) {
                        var6 = false;
                        break;
                     }
                  } else {
                     boolean var9 = false;
                     ItemStack[] var10 = var2.getInventory().getContents();

                     for(int var11 = 0; var11 < var10.length - 1; ++var11) {
                        if (var10[var11] != null && (new ItemBuilder(var10[var11].clone())).damage(0).build().isSimilar((new ItemBuilder(var8.getItemStack().clone())).damage(0).build())) {
                           var9 = true;
                        }
                     }

                     if (!var9) {
                        var6 = false;
                        break;
                     }
                  }
               }

               if (!var6) {
                  var1.setCancelled(true);
               } else {
                  final SecureRandom var18 = new SecureRandom();
                  if (var3.doRemoveOnConsume() || var3.getConsumeExchangeItems().size() > 0) {
                     ItemStack var19 = var2.getInventory().getItemInMainHand();
                     if (var2.getGameMode() != GameMode.CREATIVE) {
                        if (var19.getAmount() > 1) {
                           var19.setAmount(var19.getAmount() - 1);
                        } else {
                           var2.getInventory().setItemInMainHand((ItemStack)null);
                        }
                     }

                     if (var3.getConsumeExchangeItems().size() > 0) {
                        int var23 = 0;
                        int var27 = var3.getExchangeItemLimit();
                        boolean var31 = false;

                        for(ExchangeItem var13 : var3.getConsumeExchangeItems()) {
                           if (var27 > 0 && var23 >= var27) {
                              break;
                           }

                           if (var31 && var13.doBreakSuccessChain()) {
                              var31 = false;
                           } else if (var18.nextDouble() <= var13.getChance() / (double)100.0F) {
                              ++var23;
                              var31 = true;
                              ItemStack var14 = var13.getItem().clone();
                              if (var13.doForceDrop()) {
                                 var2.getWorld().dropItemNaturally(var2.getLocation(), var14);
                              } else {
                                 int var15 = var13.getPreferredSlot();
                                 if (var15 >= 0) {
                                    ItemStack var16 = var2.getInventory().getItem(var15);
                                    if (var16 == null || var16.isSimilar(var14)) {
                                       if (var16 == null) {
                                          var2.getInventory().setItem(var15, var14);
                                          continue;
                                       }

                                       if (var16.getAmount() + var14.getAmount() <= var16.getMaxStackSize()) {
                                          var16.setAmount(var16.getAmount() + var14.getAmount());
                                          continue;
                                       }

                                       if (var16.getAmount() < var16.getMaxStackSize()) {
                                          int var17 = var16.getMaxStackSize() - var16.getAmount();
                                          var16.setAmount(var16.getMaxStackSize());
                                          var14.setAmount(var14.getAmount() - var17);
                                       }
                                    }
                                 }

                                 if (MaterialUtils.isAir(var2.getInventory().getItemInMainHand().getType())) {
                                    var2.getInventory().setItemInMainHand(var14);
                                 } else if (var2.getInventory().firstEmpty() != -1) {
                                    var2.getInventory().addItem(new ItemStack[]{var14});
                                 } else {
                                    var2.getWorld().dropItemNaturally(var2.getLocation(), var14);
                                 }
                              }
                           } else {
                              var31 = false;
                           }
                        }
                     }
                  }

                  for(ConsumeRequirementItem var24 : var4.getRequiredItemList()) {
                     HashMap var28 = new HashMap();
                     ItemStack[] var32 = var2.getInventory().getContents();

                     for(int var35 = 0; var35 < var32.length - 1; ++var35) {
                        if (var32[var35] != null) {
                           if (var24.doDamageOnConsume()) {
                              if ((new ItemBuilder(var32[var35].clone())).damage(0).build().isSimilar((new ItemBuilder(var24.getItemStack().clone())).damage(0).build())) {
                                 var28.put(var35, var32[var35]);
                              }
                           } else if (var32[var35].isSimilar(var24.getItemStack())) {
                              var28.put(var35, var32[var35]);
                           }
                        }
                     }

                     if (var24.doRemoveOnConsume()) {
                        int var36 = 0;

                        for(Map.Entry var43 : var28.entrySet()) {
                           if (((ItemStack)var43.getValue()).getAmount() + var36 >= var24.getItemStack().getAmount()) {
                              if (((ItemStack)var43.getValue()).getAmount() + var36 == var24.getItemStack().getAmount()) {
                                 var2.getInventory().setItem((Integer)var43.getKey(), (ItemStack)null);
                              } else {
                                 ItemStack var47 = var2.getInventory().getItem((Integer)var43.getKey());
                                 var47.setAmount(var47.getAmount() - (var24.getItemStack().getAmount() - var36));
                              }
                              break;
                           }

                           var36 += ((ItemStack)var43.getValue()).getAmount();
                           var2.getInventory().setItem((Integer)var43.getKey(), (ItemStack)null);
                        }
                     }

                     if (var24.doDamageOnConsume()) {
                        for(Map.Entry var40 : var28.entrySet()) {
                           ItemStack var44 = (ItemStack)var40.getValue();
                           if (var44.getItemMeta() instanceof Damageable) {
                              Damageable var48 = (Damageable)var44.getItemMeta();
                              if (var48.getDamage() >= var44.getType().getMaxDurability() - 1) {
                                 var2.getInventory().setItem((Integer)var40.getKey(), (ItemStack)null);
                              } else {
                                 var48.setDamage(var48.getDamage() + 1);
                                 var44.setItemMeta((ItemMeta)var48);
                              }
                              break;
                           }
                        }
                     }
                  }

                  if (var5) {
                     ActiveDrugEffect var21 = this.drugManager.getActiveDrugEffect(var2, var3);
                     if (var21 != null) {
                        var21.getTask().cancel();
                        this.drugManager.removeActiveDrugEffect(var2, var3);
                        this.drugManager.forceEndTimedConsumeActions(var2, var3.getId());
                     }
                  }

                  final ArrayList var22 = new ArrayList(var3.getConsumeActions());
                  if (var22.size() > 0) {
                     for(ConsumeAction var29 : var3.getConsumeActions()) {
                        int var33 = var29.getRepetitions();
                        int var38 = var29.getRepetitionTickDelay() > 0 ? var29.getRepetitionTickDelay() : 1;
                        if (var33 > 0) {
                           int var42 = var29.getDelay() + var38;

                           for(int var46 = 0; var46 < var33; ++var46) {
                              if (var42 <= var3.getEffectDuration()) {
                                 ConsumeAction var49 = var29.clone();
                                 var49.setDelay(var42);
                                 var22.add(var49);
                                 var42 += var38;
                              }
                           }
                        } else if (var33 == -1) {
                           for(int var41 = var29.getDelay() + var38; var41 <= var3.getEffectDuration(); var41 += var38) {
                              ConsumeAction var45 = var29.clone();
                              var45.setDelay(var41);
                              var22.add(var45);
                           }
                        }
                     }
                  }

                  final ConsumeAction var26 = (ConsumeAction)var22.stream().max(Comparator.comparing(ConsumeAction::getDelay)).orElse((Object)null);
                  final int[] var30 = new int[]{0};
                  BukkitTask var34 = (new BukkitRunnable() {
                     public void run() {
                        if (!var3.doPauseWhileOffline() || var3.doPauseWhileOffline() && var2.isOnline()) {
                           ArrayList var1 = new ArrayList();

                           for(ConsumeAction var3x : (List)var22.stream().filter((var1x) -> var1x.getDelay() == var30[0]).collect(Collectors.toList())) {
                              if (var18.nextDouble() <= var3x.getChance() / (double)100.0F) {
                                 var1.add(var3x);
                              }
                           }

                           if (var1.size() > 0) {
                              DrugHandler.this.main.getServer().getScheduler().runTask(DrugHandler.this.main, () -> var1.forEach((var3x) -> {
                                    var3x.execute(var2.getUniqueId(), var3);
                                    if (var3x instanceof TimedConsumeAction) {
                                       DrugHandler.this.drugManager.addTimedConsumeAction(var2.getUniqueId(), var3.getId(), (TimedConsumeAction)var3x);
                                    }

                                 }));
                           }

                           if (var26 == null || var30[0] >= var26.getDelay()) {
                              this.cancel();
                           }

                           int var10002 = var30[0]++;
                        }

                     }
                  }).runTaskTimerAsynchronously(this.main, 0L, 1L);
                  this.main.getDrugManager().addActiveDrugEffect(var2, var3, var34, System.currentTimeMillis() + (long)(var3.getEffectDuration() / 20 * 1000));
               }
            }
         } else {
            var1.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent var1) {
      Player var2 = var1.getPlayer();
      if (this.drugManager.isPlayerOnDrugs(var2)) {
         String var3 = var1.getMessage();

         for(ActiveDrugEffect var6 : (List)this.drugManager.getActiveDrugEffects(var2).stream().filter((var0) -> var0.getDrug().getChatSlurType() != ChatSlurType.NONE).collect(Collectors.toList())) {
            var3 = (new ChatSlurConverter(var3)).setSlurStrategy(var6.getDrug().getChatSlurType().getChatSlurStrategy()).convert();
         }

         var1.setMessage(var3);
      }

   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent var1) {
      Player var2 = var1.getPlayer();

      for(ActiveDrugEffect var5 : new ArrayList(this.drugManager.getActiveDrugEffects(var2))) {
         if (var5.getDrug().doRemoveOnDisconnect()) {
            var5.getTask().cancel();
            this.drugManager.removeActiveDrugEffect(var2, var5.getDrug());
         }
      }

      this.drugManager.forceEndTimedConsumeActions(var2.getUniqueId());
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent var1) {
      Player var2 = var1.getEntity();

      for(ActiveDrugEffect var5 : new ArrayList(this.drugManager.getActiveDrugEffects(var2))) {
         if (var5.getDrug().doRemoveOnDeath()) {
            var5.getTask().cancel();
            this.drugManager.removeActiveDrugEffect(var2, var5.getDrug());
         }
      }

      this.drugManager.forceEndTimedConsumeActions(var2.getUniqueId());
   }
}
