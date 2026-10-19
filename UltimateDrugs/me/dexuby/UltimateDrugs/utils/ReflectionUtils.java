package me.dexuby.UltimateDrugs.utils;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ReflectionUtils {
   private static int versionId;
   private static Map<String, Class<?>> classCache = new HashMap();
   private static Map<String, Method> methodCache = new HashMap();
   private static Map<String, Constructor<?>> constructorCache = new HashMap();
   private static Map<String, Field> fieldCache = new HashMap();

   private static Field getAndPrepareField(Class var0, String var1) {
      try {
         Field var2 = var0.getDeclaredField(var1);
         var2.setAccessible(true);
         return var2;
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static ItemStack getTexturedSkull(String var0) {
      ItemStack var1 = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta var2 = (SkullMeta)var1.getItemMeta();

      try {
         if (versionId >= 250) {
            Method var3 = (Method)methodCache.get("CraftMetaSkull-setProfile");
            var3.setAccessible(true);
            var3.invoke(var2, getProfile(var0));
         } else {
            Field var5 = ((Class)Objects.requireNonNull(classCache.get("CraftMetaSkull"))).getDeclaredField("profile");
            var5.setAccessible(true);
            var5.set(var2, getProfile(var0));
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      var1.setItemMeta(var2);
      return var1;
   }

   public static void setSkullTexture(ItemStack var0, String var1) {
      SkullMeta var2 = (SkullMeta)var0.getItemMeta();

      try {
         if (versionId >= 250) {
            Method var3 = (Method)methodCache.get("CraftMetaSkull-setProfile");
            var3.setAccessible(true);
            var3.invoke(var2, getProfile(var1));
         } else {
            Field var5 = ((Class)Objects.requireNonNull(classCache.get("CraftMetaSkull"))).getDeclaredField("profile");
            var5.setAccessible(true);
            var5.set(var2, getProfile(var1));
         }
      } catch (Exception var4) {
      }

      var0.setItemMeta(var2);
   }

   public static void setSkullBlockTexture(Block var0, String var1) {
      if (var0.getType() == Material.PLAYER_HEAD || var0.getType() == Material.PLAYER_WALL_HEAD) {
         try {
            GameProfile var2 = getProfile(var1);
            Object var3 = ((Method)methodCache.get("CraftWorld-getHandle")).invoke(var0.getWorld());
            Object var4 = ((Method)methodCache.get("WorldServer-getTileEntity")).invoke(var3, ((Constructor)constructorCache.get("BlockPosition")).newInstance(var0.getX(), var0.getY(), var0.getZ()));
            if (var4 == null) {
               return;
            }

            ((Method)methodCache.get("TileEntitySkull-setGameProfile")).invoke(var4, var2);
            var0.getWorld().refreshChunk(var0.getChunk().getX(), var0.getChunk().getZ());
         } catch (Exception var5) {
            var5.printStackTrace();
         }

      }
   }

   private static GameProfile getProfile(String var0) {
      GameProfile var1 = new GameProfile(UUID.nameUUIDFromBytes(var0.getBytes()), (String)null);
      Property var2 = new Property("textures", var0);
      var1.getProperties().put("textures", var2);
      return var1;
   }

   public static boolean removeRecipeByKey(String var0) {
      try {
         Object var1 = ((Method)methodCache.get("CraftServer-getServer")).invoke(Bukkit.getServer());
         Object var2 = ((Method)methodCache.get("MinecraftServer-getCraftingManager")).invoke(var1);
         Object var3 = null;
         Map var4 = (Map)((Field)fieldCache.get("CraftingManager-recipes")).get(var2);

         for(Map.Entry var6 : var4.entrySet()) {
            String var7 = (String)((Method)methodCache.get("MinecraftKey-getKey")).invoke(var6.getKey());
            if (var7.equals(var0)) {
               var3 = var6.getKey();
               break;
            }
         }

         if (var3 != null) {
            var4.remove(var3);
         }

         return true;
      } catch (Exception var8) {
         return false;
      }
   }

   public static ItemStack setItemStackStringTag(ItemStack var0, String var1, String var2) {
      try {
         Object var3 = ((Method)methodCache.get("CraftItemStack-asNMSCopy")).invoke((Object)null, var0);
         boolean var4 = (Boolean)((Method)methodCache.get("ItemStack-hasTag")).invoke(var3);
         if (!var4) {
            ((Method)methodCache.get("ItemStack-setTag")).invoke(var3, ((Class)classCache.get("NBTTagCompound")).newInstance());
         }

         Object var5 = ((Method)methodCache.get("ItemStack-getTag")).invoke(var3);
         ((Method)methodCache.get("NBTTagCompound-setString")).invoke(var5, var1, var2);
         ((Method)methodCache.get("ItemStack-setTag")).invoke(var3, var5);
         return (ItemStack)((Method)methodCache.get("CraftItemStack-asBukkitCopy")).invoke((Object)null, var3);
      } catch (Exception var6) {
         var6.printStackTrace();
         return null;
      }
   }

   @SafeVarargs
   public static ItemStack setItemStackStringTags(ItemStack var0, AbstractMap.SimpleEntry<String, String>... var1) {
      try {
         Object var2 = ((Method)methodCache.get("CraftItemStack-asNMSCopy")).invoke((Object)null, var0);
         boolean var3 = (Boolean)((Method)methodCache.get("ItemStack-hasTag")).invoke(var2);
         if (!var3) {
            ((Method)methodCache.get("ItemStack-setTag")).invoke(var2, ((Class)classCache.get("NBTTagCompound")).newInstance());
         }

         Object var4 = ((Method)methodCache.get("ItemStack-getTag")).invoke(var2);

         for(AbstractMap.SimpleEntry var8 : var1) {
            ((Method)methodCache.get("NBTTagCompound-setString")).invoke(var4, var8.getKey(), var8.getValue());
         }

         ((Method)methodCache.get("ItemStack-setTag")).invoke(var2, var4);
         return (ItemStack)((Method)methodCache.get("CraftItemStack-asBukkitCopy")).invoke((Object)null, var2);
      } catch (Exception var9) {
         return null;
      }
   }

   public static String getItemStackStringTag(ItemStack var0, String var1) {
      try {
         Object var2 = ((Method)methodCache.get("CraftItemStack-asNMSCopy")).invoke((Object)null, var0);
         boolean var3 = (Boolean)((Method)methodCache.get("ItemStack-hasTag")).invoke(var2);
         if (var3) {
            Object var4 = ((Method)methodCache.get("ItemStack-getTag")).invoke(var2);
            return (String)((Method)methodCache.get("NBTTagCompound-getString")).invoke(var4, var1);
         } else {
            return null;
         }
      } catch (Exception var5) {
         return null;
      }
   }

   public static List<Entity> getEntitiesFromSelector(CommandSender var0, String var1) {
      ArrayList var2 = new ArrayList();

      try {
         Object var3 = ((Method)methodCache.get("ArgumentEntity-multipleEntities")).invoke((Object)null);
         Object var4 = ((Method)methodCache.get("ArgumentEntity-parse")).invoke(var3, new StringReader(var1));
         Object var5 = ((Method)methodCache.get("VanillaCommandWrapper-getListener")).invoke(classCache.get("VanillaCommandWrapper"), var0);

         for(Object var8 : (List)((Method)methodCache.get("EntitySelector-getEntities")).invoke(var4, var5)) {
            try {
               var2.add((Entity)((Method)methodCache.get("Entity-getBukkitEntity")).invoke(var8));
            } catch (Exception var10) {
            }
         }
      } catch (Exception var11) {
      }

      return var2;
   }

   public static void prepareRespawnPacketContainer(PacketContainer var0, World var1) {
      try {
         Object var2 = BukkitConverters.getWorldConverter().getGeneric(var1);
         Object var3;
         if (versionId >= 270) {
            var3 = ((Method)methodCache.get("World-getDimensionKey")).invoke(var2);
         } else {
            var3 = ((Field)fieldCache.get("World-dimensionKey")).get(var2);
         }

         Object var4 = null;
         if (versionId >= 280) {
            ((Method)methodCache.get("World-getTypeKey")).invoke(var2);
         } else {
            ((Field)fieldCache.get("World-typeKey")).get(var2);
         }

         Class var5 = (Class)classCache.get("ResourceKey");
         Class var6 = (Class)classCache.get("DimensionManager");
         if (versionId >= 262) {
            Object var7 = ((Method)methodCache.get("CraftWorld-getHandle")).invoke(var1);
            Object var8 = ((Method)methodCache.get("World-getDimensionManager")).invoke(((Class)classCache.get("World")).cast(var7));
            var0.getSpecificModifier(var6).write(0, var8);
            var0.getSpecificModifier(var5).write(0, var3);
         } else {
            var0.getSpecificModifier(var5).write(0, var4);
            var0.getSpecificModifier(var5).write(1, var3);
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public static PacketContainer createRespawnPacket1_20(World var0, GameMode var1) {
      try {
         Class var2 = Class.forName("net.minecraft.world.level.EnumGamemode");
         Class var3 = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutRespawn");
         Class var4 = (Class)classCache.get("ResourceKey");
         Constructor var5 = var3.getDeclaredConstructor(var4, var4, Long.TYPE, var2, var2, Boolean.TYPE, Boolean.TYPE, Byte.TYPE, Optional.class, Integer.TYPE);
         Object var6 = BukkitConverters.getWorldConverter().getGeneric(var0);
         Object var7 = ((Method)methodCache.get("World-getDimensionKey")).invoke(var6);
         Object var8 = ((Method)methodCache.get("World-getTypeKey")).invoke(var6);
         Enum var9 = Enum.valueOf(var2, var1.name());
         return PacketContainer.fromPacket(var5.newInstance(var7, var8, var0.getSeed(), var9, var9, false, false, 3, Optional.empty(), 0));
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }

   public static PacketContainer createRespawnPacket1_19_3(World var0, GameMode var1) {
      try {
         Class var2 = Class.forName("net.minecraft.world.level.EnumGamemode");
         Class var3 = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutRespawn");
         Class var4 = (Class)classCache.get("ResourceKey");
         Constructor var5 = var3.getDeclaredConstructor(var4, var4, Long.TYPE, var2, var2, Boolean.TYPE, Boolean.TYPE, Byte.TYPE, Optional.class);
         Object var6 = BukkitConverters.getWorldConverter().getGeneric(var0);
         Object var7 = ((Method)methodCache.get("World-getDimensionKey")).invoke(var6);
         Object var8 = ((Method)methodCache.get("World-getTypeKey")).invoke(var6);
         Enum var9 = Enum.valueOf(var2, var1.name());
         return PacketContainer.fromPacket(var5.newInstance(var7, var8, var0.getSeed(), var9, var9, false, false, 3, Optional.empty()));
      } catch (Exception var10) {
         var10.printStackTrace();
         return null;
      }
   }

   public static void prepareRespawnPacketContainerNew(PacketContainer var0, World var1) {
      try {
         Object var2 = BukkitConverters.getWorldConverter().getGeneric(var1);
         Object var3 = ((Method)methodCache.get("World-getDimensionKey")).invoke(var2);
         Object var4 = ((Method)methodCache.get("World-getTypeKey")).invoke(var2);
         Class var5 = (Class)classCache.get("ResourceKey");
         var0.getSpecificModifier(var5).write(0, var4);
         var0.getSpecificModifier(var5).write(1, var3);
         var0.getSpecificModifier(Optional.class).write(0, Optional.empty());
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private static int calculateVersionId() {
      String var0 = Bukkit.getServer().getBukkitVersion().split("-")[0];
      String[] var1 = var0.split("\\.");
      int var2 = 0;
      int var3 = 100;

      for(String var7 : var1) {
         var2 += Integer.parseInt(var7) * var3;
         var3 = (int)((double)var3 * 0.1);
      }

      return var2;
   }

   public static int getVersionId() {
      return versionId;
   }

   public static int getVersionNumber() {
      return Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].replace(".", "").substring(0, 3));
   }

   private static String getVersion() {
      return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
   }

   static {
      String var0 = getVersion();
      versionId = calculateVersionId();

      try {
         classCache.put("CraftMetaSkull", Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftMetaSkull", var0)));
         classCache.put("CraftWorld", Class.forName(String.format("org.bukkit.craftbukkit.%s.CraftWorld", var0)));
         classCache.put("CraftServer", Class.forName(String.format("org.bukkit.craftbukkit.%s.CraftServer", var0)));
         classCache.put("CraftItemStack", Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", var0)));
         if (versionId >= 270) {
            classCache.put("ItemStack", Class.forName("net.minecraft.world.item.ItemStack"));
            classCache.put("NBTTagCompound", Class.forName("net.minecraft.nbt.NBTTagCompound"));
            classCache.put("MinecraftServer", Class.forName("net.minecraft.server.MinecraftServer"));
            classCache.put("MinecraftKey", Class.forName("net.minecraft.resources.MinecraftKey"));
            classCache.put("CraftingManager", Class.forName("net.minecraft.world.item.crafting.CraftingManager"));
            classCache.put("WorldServer", Class.forName("net.minecraft.server.level.WorldServer"));
            classCache.put("BlockPosition", Class.forName("net.minecraft.core.BlockPosition"));
            classCache.put("TileEntitySkull", Class.forName("net.minecraft.world.level.block.entity.TileEntitySkull"));
            classCache.put("Entity", Class.forName("net.minecraft.world.entity.Entity"));
            classCache.put("ArgumentEntity", Class.forName("net.minecraft.commands.arguments.ArgumentEntity"));
            classCache.put("EntitySelector", Class.forName("net.minecraft.commands.arguments.selector.EntitySelector"));
            classCache.put("CommandListenerWrapper", Class.forName("net.minecraft.commands.CommandListenerWrapper"));
         } else {
            classCache.put("ItemStack", Class.forName(String.format("net.minecraft.server.%s.ItemStack", var0)));
            classCache.put("NBTTagCompound", Class.forName(String.format("net.minecraft.server.%s.NBTTagCompound", var0)));
            classCache.put("MinecraftServer", Class.forName(String.format("net.minecraft.server.%s.MinecraftServer", var0)));
            classCache.put("MinecraftKey", Class.forName(String.format("net.minecraft.server.%s.MinecraftKey", var0)));
            classCache.put("CraftingManager", Class.forName(String.format("net.minecraft.server.%s.CraftingManager", var0)));
            classCache.put("WorldServer", Class.forName(String.format("net.minecraft.server.%s.WorldServer", var0)));
            classCache.put("BlockPosition", Class.forName(String.format("net.minecraft.server.%s.BlockPosition", var0)));
            classCache.put("TileEntitySkull", Class.forName(String.format("net.minecraft.server.%s.TileEntitySkull", var0)));
            classCache.put("Entity", Class.forName(String.format("net.minecraft.server.%s.Entity", var0)));
            classCache.put("ArgumentEntity", Class.forName(String.format("net.minecraft.server.%s.ArgumentEntity", var0)));
            classCache.put("EntitySelector", Class.forName(String.format("net.minecraft.server.%s.EntitySelector", var0)));
            classCache.put("CommandListenerWrapper", Class.forName(String.format("net.minecraft.server.%s.CommandListenerWrapper", var0)));
         }

         classCache.put("GameProfile", Class.forName("com.mojang.authlib.GameProfile"));
         classCache.put("VanillaCommandWrapper", Class.forName(String.format("org.bukkit.craftbukkit.%s.command.VanillaCommandWrapper", var0)));
         if (versionId >= 270) {
            classCache.put("ResourceKey", Class.forName("net.minecraft.resources.ResourceKey"));
            classCache.put("World", Class.forName("net.minecraft.world.level.World"));
            classCache.put("DimensionManager", Class.forName("net.minecraft.world.level.dimension.DimensionManager"));
         } else if (versionId >= 260) {
            classCache.put("ResourceKey", Class.forName(String.format("net.minecraft.server.%s.ResourceKey", var0)));
            classCache.put("World", Class.forName(String.format("net.minecraft.server.%s.World", var0)));
            classCache.put("DimensionManager", Class.forName(String.format("net.minecraft.server.%s.DimensionManager", var0)));
         }

         if (versionId >= 300) {
            methodCache.put("ItemStack-hasTag", ((Class)classCache.get("ItemStack")).getMethod("u"));
            methodCache.put("ItemStack-getTag", ((Class)classCache.get("ItemStack")).getMethod("v"));
            methodCache.put("ItemStack-setTag", ((Class)classCache.get("ItemStack")).getMethod("c", (Class)classCache.get("NBTTagCompound")));
            methodCache.put("MinecraftServer-getCraftingManager", ((Class)classCache.get("MinecraftServer")).getMethod("aE"));
            methodCache.put("NBTTagCompound-getString", ((Class)classCache.get("NBTTagCompound")).getMethod("l", String.class));
            methodCache.put("NBTTagCompound-setString", ((Class)classCache.get("NBTTagCompound")).getMethod("a", String.class, String.class));
            methodCache.put("MinecraftKey-getKey", ((Class)classCache.get("MinecraftKey")).getMethod("a"));
         } else if (versionId >= 292) {
            methodCache.put("ItemStack-hasTag", ((Class)classCache.get("ItemStack")).getMethod("t"));
            methodCache.put("ItemStack-getTag", ((Class)classCache.get("ItemStack")).getMethod("u"));
            methodCache.put("ItemStack-setTag", ((Class)classCache.get("ItemStack")).getMethod("c", (Class)classCache.get("NBTTagCompound")));
            methodCache.put("MinecraftServer-getCraftingManager", ((Class)classCache.get("MinecraftServer")).getMethod("aE"));
            methodCache.put("NBTTagCompound-getString", ((Class)classCache.get("NBTTagCompound")).getMethod("l", String.class));
            methodCache.put("NBTTagCompound-setString", ((Class)classCache.get("NBTTagCompound")).getMethod("a", String.class, String.class));
            methodCache.put("MinecraftKey-getKey", ((Class)classCache.get("MinecraftKey")).getMethod("a"));
         } else if (versionId >= 290) {
            methodCache.put("ItemStack-hasTag", ((Class)classCache.get("ItemStack")).getMethod("t"));
            methodCache.put("ItemStack-getTag", ((Class)classCache.get("ItemStack")).getMethod("u"));
            methodCache.put("ItemStack-setTag", ((Class)classCache.get("ItemStack")).getMethod("c", (Class)classCache.get("NBTTagCompound")));
            methodCache.put("MinecraftServer-getCraftingManager", ((Class)classCache.get("MinecraftServer")).getMethod("aC"));
            methodCache.put("NBTTagCompound-getString", ((Class)classCache.get("NBTTagCompound")).getMethod("l", String.class));
            methodCache.put("NBTTagCompound-setString", ((Class)classCache.get("NBTTagCompound")).getMethod("a", String.class, String.class));
            methodCache.put("MinecraftKey-getKey", ((Class)classCache.get("MinecraftKey")).getMethod("a"));
         } else if (versionId >= 280) {
            methodCache.put("ItemStack-hasTag", ((Class)classCache.get("ItemStack")).getMethod("s"));
            methodCache.put("ItemStack-getTag", ((Class)classCache.get("ItemStack")).getMethod("t"));
            methodCache.put("ItemStack-setTag", ((Class)classCache.get("ItemStack")).getMethod("c", (Class)classCache.get("NBTTagCompound")));
            methodCache.put("MinecraftServer-getCraftingManager", ((Class)classCache.get("MinecraftServer")).getMethod("aC"));
            methodCache.put("NBTTagCompound-getString", ((Class)classCache.get("NBTTagCompound")).getMethod("l", String.class));
            methodCache.put("NBTTagCompound-setString", ((Class)classCache.get("NBTTagCompound")).getMethod("a", String.class, String.class));
            methodCache.put("MinecraftKey-getKey", ((Class)classCache.get("MinecraftKey")).getMethod("a"));
         } else {
            methodCache.put("ItemStack-hasTag", ((Class)classCache.get("ItemStack")).getMethod("hasTag"));
            methodCache.put("ItemStack-getTag", ((Class)classCache.get("ItemStack")).getMethod("getTag"));
            methodCache.put("ItemStack-setTag", ((Class)classCache.get("ItemStack")).getMethod("setTag", (Class)classCache.get("NBTTagCompound")));
            methodCache.put("MinecraftServer-getCraftingManager", ((Class)classCache.get("MinecraftServer")).getMethod("getCraftingManager"));
            methodCache.put("NBTTagCompound-getString", ((Class)classCache.get("NBTTagCompound")).getMethod("getString", String.class));
            methodCache.put("NBTTagCompound-setString", ((Class)classCache.get("NBTTagCompound")).getMethod("setString", String.class, String.class));
            methodCache.put("MinecraftKey-getKey", ((Class)classCache.get("MinecraftKey")).getMethod("getKey"));
         }

         methodCache.put("CraftItemStack-asNMSCopy", ((Class)classCache.get("CraftItemStack")).getMethod("asNMSCopy", ItemStack.class));
         methodCache.put("CraftItemStack-asBukkitCopy", ((Class)classCache.get("CraftItemStack")).getMethod("asBukkitCopy", (Class)classCache.get("ItemStack")));
         methodCache.put("CraftWorld-getHandle", ((Class)classCache.get("CraftWorld")).getMethod("getHandle"));
         methodCache.put("CraftServer-getServer", ((Class)classCache.get("CraftServer")).getMethod("getServer"));
         if (versionId >= 280) {
            methodCache.put("WorldServer-getTileEntity", ((Class)classCache.get("World")).getMethod("c_", (Class)classCache.get("BlockPosition")));
         } else if (versionId >= 270) {
            methodCache.put("WorldServer-getTileEntity", ((Class)classCache.get("World")).getMethod("getTileEntity", (Class)classCache.get("BlockPosition")));
         } else {
            methodCache.put("WorldServer-getTileEntity", ((Class)classCache.get("WorldServer")).getMethod("getTileEntity", (Class)classCache.get("BlockPosition")));
         }

         if (versionId >= 280) {
            methodCache.put("TileEntitySkull-setGameProfile", ((Class)classCache.get("TileEntitySkull")).getMethod("a", (Class)classCache.get("GameProfile")));
         } else {
            methodCache.put("TileEntitySkull-setGameProfile", ((Class)classCache.get("TileEntitySkull")).getMethod("setGameProfile", (Class)classCache.get("GameProfile")));
         }

         if (versionId >= 250) {
            methodCache.put("CraftMetaSkull-setProfile", ((Class)classCache.get("CraftMetaSkull")).getDeclaredMethod("setProfile", (Class)classCache.get("GameProfile")));
         }

         if (versionId >= 280) {
            methodCache.put("ArgumentEntity-multipleEntities", ((Class)classCache.get("ArgumentEntity")).getDeclaredMethod("b"));
         } else if (versionId >= 240) {
            methodCache.put("ArgumentEntity-multipleEntities", ((Class)classCache.get("ArgumentEntity")).getDeclaredMethod("multipleEntities"));
         } else {
            methodCache.put("ArgumentEntity-multipleEntities", ((Class)classCache.get("ArgumentEntity")).getDeclaredMethod("b"));
         }

         if (versionId >= 280) {
            methodCache.put("ArgumentEntity-parse", ((Class)classCache.get("ArgumentEntity")).getDeclaredMethod("a", StringReader.class));
         } else {
            methodCache.put("ArgumentEntity-parse", ((Class)classCache.get("ArgumentEntity")).getDeclaredMethod("parse", StringReader.class));
         }

         if (versionId >= 280) {
            methodCache.put("EntitySelector-getEntities", ((Class)classCache.get("EntitySelector")).getDeclaredMethod("b", (Class)classCache.get("CommandListenerWrapper")));
         } else if (versionId >= 240) {
            methodCache.put("EntitySelector-getEntities", ((Class)classCache.get("EntitySelector")).getDeclaredMethod("getEntities", (Class)classCache.get("CommandListenerWrapper")));
         } else {
            methodCache.put("EntitySelector-getEntities", ((Class)classCache.get("EntitySelector")).getDeclaredMethod("b", (Class)classCache.get("CommandListenerWrapper")));
         }

         methodCache.put("VanillaCommandWrapper-getListener", ((Class)classCache.get("VanillaCommandWrapper")).getDeclaredMethod("getListener", CommandSender.class));
         methodCache.put("Entity-getBukkitEntity", ((Class)classCache.get("Entity")).getDeclaredMethod("getBukkitEntity"));
         if (versionId >= 293) {
            methodCache.put("World-getDimensionKey", ((Class)classCache.get("World")).getDeclaredMethod("ac"));
         } else if (versionId >= 290) {
            methodCache.put("World-getDimensionKey", ((Class)classCache.get("World")).getDeclaredMethod("ab"));
         } else if (versionId >= 280) {
            methodCache.put("World-getDimensionKey", ((Class)classCache.get("World")).getDeclaredMethod("aa"));
         } else if (versionId >= 270) {
            methodCache.put("World-getDimensionKey", ((Class)classCache.get("World")).getDeclaredMethod("getDimensionKey"));
         }

         if (versionId >= 300) {
            methodCache.put("World-getDimensionManager", ((Class)classCache.get("World")).getDeclaredMethod("x_"));
         } else if (versionId >= 280) {
            methodCache.put("World-getDimensionManager", ((Class)classCache.get("World")).getDeclaredMethod("q_"));
         } else if (versionId >= 262) {
            methodCache.put("World-getDimensionManager", ((Class)classCache.get("World")).getDeclaredMethod("getDimensionManager"));
         }

         constructorCache.put("BlockPosition", ((Class)classCache.get("BlockPosition")).getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE));
         if (versionId >= 293) {
            methodCache.put("World-getTypeKey", ((Class)classCache.get("World")).getDeclaredMethod("aa"));
         } else if (versionId >= 290) {
            methodCache.put("World-getTypeKey", ((Class)classCache.get("World")).getDeclaredMethod("Z"));
         } else if (versionId >= 280) {
            methodCache.put("World-getTypeKey", ((Class)classCache.get("World")).getDeclaredMethod("getTypeKey"));
         } else if (versionId >= 270) {
            fieldCache.put("World-typeKey", getAndPrepareField((Class)classCache.get("World"), "typeKey"));
         } else if (versionId >= 260) {
            fieldCache.put("World-dimensionKey", getAndPrepareField((Class)classCache.get("World"), "dimensionKey"));
            fieldCache.put("World-typeKey", getAndPrepareField((Class)classCache.get("World"), "typeKey"));
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }
}
