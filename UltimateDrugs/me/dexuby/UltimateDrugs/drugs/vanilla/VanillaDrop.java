package me.dexuby.UltimateDrugs.drugs.vanilla;

import java.util.Collection;
import java.util.Set;
import me.dexuby.UltimateDrugs.drugs.Drop;
import org.bukkit.block.Biome;

public abstract class VanillaDrop {
   private String permission;
   private int dropLimit;
   private Set<Biome> biomeWhitelist;
   private Collection<Drop> drops;

   VanillaDrop(String var1, int var2, Set<Biome> var3, Collection<Drop> var4) {
      this.permission = var1;
      this.dropLimit = var2;
      this.biomeWhitelist = var3;
      this.drops = var4;
   }

   public String getPermission() {
      return this.permission;
   }

   public int getDropLimit() {
      return this.dropLimit;
   }

   public Set<Biome> getBiomeWhitelist() {
      return this.biomeWhitelist;
   }

   public Collection<Drop> getDrops() {
      return this.drops;
   }
}
