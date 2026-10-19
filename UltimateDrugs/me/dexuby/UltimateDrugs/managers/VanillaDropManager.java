package me.dexuby.UltimateDrugs.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.config.SettingsHolder;
import me.dexuby.UltimateDrugs.drugs.vanilla.VanillaDrop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class VanillaDropManager {
   private Main main;
   private Collection<VanillaDrop> vanillaDrops = new ArrayList();
   private Set<Material> vanillaBlockDropMaterials = new HashSet();
   private Set<Location> playerPlacedBlockLocations = new HashSet();

   public VanillaDropManager(Main var1) {
      this.main = var1;
      var1.getServer().getScheduler().runTaskTimer(var1, () -> {
         if ((Boolean)SettingsHolder.VANILLA_STORE_PLAYER_BLOCKS.getValue()) {
            HashSet var2 = new HashSet(this.playerPlacedBlockLocations);
            var1.getServer().getScheduler().runTaskAsynchronously(var1, () -> {
               var1.getConfigManager().updatePlayerPlacedBlockCache(var2);
               var1.getConfigManager().saveStorage();
            });
         }

      }, 1200L, 1200L);
   }

   public Collection<VanillaDrop> getVanillaDrops() {
      return this.vanillaDrops;
   }

   public void addVanillaDrop(VanillaDrop var1) {
      this.vanillaDrops.add(var1);
   }

   public void addVanillaDropMaterial(Material var1) {
      this.vanillaBlockDropMaterials.add(var1);
   }

   public void addVanillaBlockDropMaterials(List<Material> var1) {
      this.vanillaBlockDropMaterials.addAll(var1);
   }

   public boolean isVanillaBlockDropMaterial(Material var1) {
      return this.vanillaBlockDropMaterials.contains(var1);
   }

   public void addPlayerPlacedBlockLocation(Location var1) {
      this.playerPlacedBlockLocations.add(var1);
   }

   public void removePlayerPlacedBlockLocation(Location var1) {
      this.playerPlacedBlockLocations.remove(var1);
   }

   public boolean isPlayerPlacedBlock(Block var1) {
      return this.playerPlacedBlockLocations.contains(var1.getLocation());
   }

   public Set<Location> getPlayerPlacedBlockLocations() {
      return this.playerPlacedBlockLocations;
   }
}
