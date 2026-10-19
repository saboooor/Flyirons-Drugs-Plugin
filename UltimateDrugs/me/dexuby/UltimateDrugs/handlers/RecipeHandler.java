package me.dexuby.UltimateDrugs.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.DrugBrewingRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugCraftingRecipe;
import me.dexuby.UltimateDrugs.drugs.DrugRecipe;
import me.dexuby.UltimateDrugs.managers.RecipeManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RecipeHandler implements Listener {
   private Main main;
   private RecipeManager recipeManager;
   private Map<Block, BukkitTask> activeBrewingStands = new HashMap();
   private Set<UUID> blockedPlayers = new HashSet();

   public RecipeHandler(Main var1) {
      this.main = var1;
      this.recipeManager = var1.getRecipeManager();
   }

   @EventHandler
   public void onPrepareItemCraft(PrepareItemCraftEvent var1) {
      if (var1.getRecipe() instanceof ShapelessRecipe) {
         ShapelessRecipe var2 = (ShapelessRecipe)var1.getRecipe();
         DrugRecipe var3 = this.recipeManager.getDrugRecipeById(var2.getKey().getKey());
         if (var3 instanceof DrugCraftingRecipe) {
            List var4 = this.main.getRecipeManager().getAllShapelessCraftingRecipes();
            ArrayList var5 = new ArrayList();

            for(ItemStack var9 : var1.getInventory().getMatrix()) {
               if (var9 != null && var9.getType() != Material.AIR) {
                  var5.add(var9);
               }
            }

            for(DrugCraftingRecipe var15 : var4) {
               ArrayList var16 = new ArrayList(var15.getIngredients());
               ArrayList var17 = new ArrayList(var5);
               Iterator var10 = var16.iterator();

               while(var10.hasNext()) {
                  ItemStack var11 = (ItemStack)var10.next();
                  Iterator var12 = var17.iterator();

                  while(var12.hasNext()) {
                     ItemStack var13 = (ItemStack)var12.next();
                     if (var11.isSimilar(var13)) {
                        var12.remove();
                        var10.remove();
                        break;
                     }
                  }
               }

               if (var16.size() == 0 && var17.size() == 0) {
                  var1.getInventory().setResult(var15.getRecipe().getResult());
                  return;
               }
            }

            var1.getInventory().setResult((ItemStack)null);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent var1) {
      Inventory var2 = var1.getClickedInventory();
      if (var2 != null && var2.getType() == InventoryType.BREWING) {
         InventoryHolder var3 = var2.getHolder();
         InventoryView var4 = var1.getView();
         if (var3 instanceof BrewingStand) {
            if (this.main.getDrugManager().getDrugs().stream().noneMatch((var0) -> var0.getBrewingRecipes().size() > 0)) {
               return;
            }

            var1.setCancelled(true);
            BrewingStand var5 = (BrewingStand)var3;
            Player var6 = (Player)var1.getWhoClicked();
            if (this.blockedPlayers.contains(var6.getUniqueId())) {
               return;
            }

            int var7 = var1.getSlot();
            ItemStack var8 = var1.getCursor();
            ItemStack var9 = var2.getItem(var7);
            if (var8 != null && var8.getType() != Material.AIR) {
               this.blockedPlayers.add(var6.getUniqueId());
               var4.setCursor((ItemStack)null);
               if (var9 != null && var9.getType() != Material.AIR) {
                  this.main.getServer().getScheduler().runTaskLater(this.main, () -> {
                     ItemStack var7x = var8.clone();
                     var4.setCursor(var9);
                     var2.setItem(var7, var7x);
                     var6.updateInventory();
                     this.blockedPlayers.remove(var6.getUniqueId());
                  }, 1L);
               } else {
                  this.main.getServer().getScheduler().runTaskLater(this.main, () -> {
                     var2.setItem(var7, var8);
                     var4.setCursor((ItemStack)null);
                     var6.updateInventory();
                     this.blockedPlayers.remove(var6.getUniqueId());
                  }, 1L);
               }
            } else if (var9 != null && var9.getType() != Material.AIR) {
               this.blockedPlayers.add(var6.getUniqueId());
               ItemStack var10 = var9.clone();
               var2.setItem(var7, (ItemStack)null);
               this.main.getServer().getScheduler().runTaskLater(this.main, () -> {
                  var4.setCursor(var10);
                  var6.updateInventory();
                  this.blockedPlayers.remove(var6.getUniqueId());
               }, 1L);
            }

            if (var7 == 3) {
               Block var12 = var5.getBlock();
               if ((var8 != null && var8.getType() != Material.AIR || var9 != null && var9.getType() != Material.AIR) && var8 != null && var8.getType() != Material.AIR && this.activeBrewingStands.containsKey(var12)) {
                  BukkitTask var11 = (BukkitTask)this.activeBrewingStands.get(var5.getBlock());
                  this.activeBrewingStands.remove(var12);
                  var11.cancel();
               }

               if (var8 != null && var8.getType() != Material.AIR) {
                  DrugBrewingRecipe var13 = this.getDrugBrewingRecipeByInput(var8);
                  if (var13 != null) {
                     this.checkAndStartBrewing(var13, var6, var2, var8, var5, var12, true);
                  }
               }
            }
         }

      }
   }

   private void checkAndStartBrewing(final DrugBrewingRecipe var1, final Player var2, final Inventory var3, ItemStack var4, final BrewingStand var5, final Block var6, boolean var7) {
      List var8 = var1.getResults();

      for(int var9 = 0; var9 < var8.size() && var9 < 3; ++var9) {
         ItemStack var10 = var3.getItem(var9);
         if (var10 != null && var10.getType() != Material.AIR && !var10.isSimilar((ItemStack)var8.get(var9))) {
            return;
         }
      }

      ItemStack var12 = var3.getItem(4);
      if (var12 != null || var1.getFuel() == null) {
         if (var1.getFuel() != null) {
            if (!var1.getFuel().isSimilar(var12)) {
               return;
            }

            if (var12.getAmount() > 1) {
               var12.setAmount(var12.getAmount() - 1);
            } else {
               var3.setItem(4, (ItemStack)null);
            }
         }

         if (var7) {
            if (var4.getAmount() > 1) {
               var4.setAmount(var4.getAmount() - 1);
            } else {
               this.main.getServer().getScheduler().runTaskLater(this.main, () -> {
                  var3.setItem(3, (ItemStack)null);
                  var2.updateInventory();
               }, 1L);
            }
         } else {
            ItemStack var13 = var3.getItem(3);
            if (var13 == null || var13.getType() == Material.AIR) {
               return;
            }

            if (var13.getAmount() > 1) {
               var13.setAmount(var13.getAmount() - 1);
            } else {
               var3.setItem(3, (ItemStack)null);
            }
         }

         final int[] var14 = new int[]{400};
         BukkitTask var11 = (new BukkitRunnable() {
            public void run() {
               if (var6.getState() instanceof BrewingStand) {
                  BrewingStand var1x = (BrewingStand)var6.getState();
                  var1x.setBrewingTime(var14[0]);
                  var1x.update();
                  if (var14[0] <= 0) {
                     List var2x = var1.getResults();

                     for(int var3x = 0; var3x < var2x.size() && var3x < 3; ++var3x) {
                        ItemStack var4 = var3.getItem(var3x);
                        if (var4 != null && var4.getType() != Material.AIR) {
                           if (var4.isSimilar((ItemStack)var2x.get(var3x))) {
                              if (var4.getAmount() < var4.getType().getMaxStackSize()) {
                                 var4.setAmount(var4.getAmount() + 1);
                              } else {
                                 this.cancel();
                              }
                           } else {
                              this.cancel();
                           }
                        } else {
                           var3.setItem(var3x, (ItemStack)var2x.get(var3x));
                        }
                     }

                     ItemStack var5x = var3.getItem(3);
                     if (var5x != null && var5x.getType() != Material.AIR) {
                        RecipeHandler.this.checkAndStartBrewing(var1, var2, var3, (ItemStack)null, var5, var6, false);
                     }

                     this.cancel();
                  }

                  int[] var10000 = var14;
                  var10000[0] -= var1.getSpeed();
               } else {
                  RecipeHandler.this.activeBrewingStands.remove(var6);
                  this.cancel();
               }

            }
         }).runTaskTimer(this.main, 1L, 1L);
         this.activeBrewingStands.put(var5.getBlock(), var11);
      }
   }

   private DrugBrewingRecipe getDrugBrewingRecipeByInput(ItemStack var1) {
      return (DrugBrewingRecipe)this.recipeManager.getRegisteredDrugRecipes().stream().filter((var1x) -> var1x instanceof DrugBrewingRecipe && ((DrugBrewingRecipe)var1x).getInput().isSimilar(var1)).map((var0) -> (DrugBrewingRecipe)var0).findFirst().orElse((Object)null);
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent var1) {
      Block var2 = var1.getBlock();
      if (this.activeBrewingStands.containsKey(var2)) {
         BukkitTask var3 = (BukkitTask)this.activeBrewingStands.get(var2);
         this.activeBrewingStands.remove(var2);
         var3.cancel();
      }

   }
}
