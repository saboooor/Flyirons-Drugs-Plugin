package me.dexuby.UltimateDrugs.drugs.growing;

import org.bukkit.World;

public class ChunkLocation {
   private World world;
   private int x;
   private int z;

   public ChunkLocation(World var1, int var2, int var3) {
      this.world = var1;
      this.x = var2;
      this.z = var3;
   }

   public World getWorld() {
      return this.world;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public String toString() {
      return String.format("WORLD:%s|X:%d|Z:%d", this.world.getName(), this.x, this.z);
   }
}
