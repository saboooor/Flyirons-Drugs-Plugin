package me.dexuby.UltimateDrugs.drugs.vanilla;

import java.util.Collection;
import java.util.Set;
import me.dexuby.UltimateDrugs.drugs.Drop;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

public class MobVanillaDrop extends VanillaDrop {
   private boolean overrideDrops;
   private boolean overrideExp;
   private int experience;
   private Collection<EntityType> entityTypes;

   public MobVanillaDrop(String var1, int var2, Set<Biome> var3, Collection<Drop> var4, boolean var5, boolean var6, int var7, Collection<EntityType> var8) {
      super(var1, var2, var3, var4);
      this.overrideDrops = var5;
      this.overrideExp = var6;
      this.experience = var7;
      this.entityTypes = var8;
   }

   public boolean doOverrideDrops() {
      return this.overrideDrops;
   }

   public boolean doOverrideExp() {
      return this.overrideExp;
   }

   public int getExperience() {
      return this.experience;
   }

   public Collection<EntityType> getEntityTypes() {
      return this.entityTypes;
   }
}
