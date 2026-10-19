package me.dexuby.UltimateDrugs.utils;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.dexuby.UltimateDrugs.drugs.Drug;
import org.bukkit.entity.Player;

public class WGUtils {
   public static boolean isInDisabledRegion(Player var0, Drug var1) {
      RegionContainer var2 = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionQuery var3 = var2.createQuery();
      ApplicableRegionSet var4 = var3.getApplicableRegions(adapt(var0.getLocation()));
      return var4.getRegions().stream().anyMatch((var1x) -> var1.getConsumeConditions().getDisabledRegionList().contains(var1x.getId()));
   }

   private static Location adapt(org.bukkit.Location var0) {
      Preconditions.checkNotNull(var0);
      Vector3 var1 = Vector3.at(var0.getX(), var0.getY(), var0.getZ());
      return new Location(new BukkitWorld(var0.getWorld()), var1, var0.getYaw(), var0.getPitch());
   }
}
