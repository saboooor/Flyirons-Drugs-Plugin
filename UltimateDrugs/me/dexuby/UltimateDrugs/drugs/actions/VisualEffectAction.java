package me.dexuby.UltimateDrugs.drugs.actions;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import java.util.Collections;
import java.util.UUID;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.Drug;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerCamera;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerEntityDestroy;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerEntityMetadata;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerRespawn;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerRespawnNew;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerSetBorderCenter;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerSetBorderSize;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerSetBorderWarningDistance;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerSpawnEntity;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerSpawnEntityLiving;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerUpdateHealth;
import me.dexuby.UltimateDrugs.packetwrappers.WrapperPlayServerWorldBorder;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class VisualEffectAction extends TimedConsumeAction {
   private VisualEffectType type;
   private Main main;

   public VisualEffectAction(int var1, double var2, int var4, int var5, String var6, int var7, VisualEffectType var8) {
      super(var1, var2, var4, var5, var6, var7);
      this.type = var8;
      this.main = (Main)Bukkit.getPluginManager().getPlugin("UltimateDrugs");
   }

   public void execute(UUID var1, Drug var2) {
      if (Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
         Player var3 = Bukkit.getPlayer(var1);
         if (var3 == null || !var3.isValid() || !var3.isOnline()) {
            return;
         }

         if (this.getTargetSelector() != null) {
            for(Entity var6 : ReflectionUtils.getEntitiesFromSelector(var3, this.getTargetSelector())) {
               if (var6 instanceof Player) {
                  this.apply((Player)var6);
               }
            }
         } else {
            this.apply(var3);
         }
      }

   }

   private void apply(Player var1) {
      int var2 = ReflectionUtils.getVersionId();
      if (var1 != null && var1.isValid() && var1.isOnline()) {
         switch (this.type) {
            case CREEPER_VISION:
            case SPIDER_VISION:
            case ENDERMAN_VISION:
               Location var3 = var1.getLocation();
               World var4 = var1.getWorld();
               GameMode var5 = var1.getGameMode();
               boolean var6 = var1.isSprinting();
               WrapperPlayServerSpawnEntity var7 = (new WrapperPlayServerSpawnEntity()).setEntityId(-69);
               byte var8;
               switch (this.type) {
                  case CREEPER_VISION:
                     if (var2 >= 290) {
                        var7.setTypeNew(EntityType.CREEPER);
                        var8 = 0;
                     } else if (var2 >= 270) {
                        var8 = 13;
                     } else if (var2 >= 250) {
                        var8 = 12;
                     } else if (var2 >= 240) {
                        var8 = 11;
                     } else {
                        var8 = 10;
                     }
                     break;
                  case SPIDER_VISION:
                     if (var2 >= 290) {
                        var7.setTypeNew(EntityType.SPIDER);
                        var8 = 0;
                     } else if (var2 >= 270) {
                        var8 = 85;
                     } else if (var2 >= 262) {
                        var8 = 80;
                     } else if (var2 >= 260) {
                        var8 = 79;
                     } else if (var2 >= 250) {
                        var8 = 73;
                     } else if (var2 >= 240) {
                        var8 = 72;
                     } else {
                        var8 = 69;
                     }
                     break;
                  case ENDERMAN_VISION:
                  default:
                     if (var2 >= 290) {
                        var7.setTypeNew(EntityType.ENDERMAN);
                        var8 = 0;
                     } else if (var2 >= 270) {
                        var8 = 21;
                     } else if (var2 >= 250) {
                        var8 = 20;
                     } else if (var2 >= 240) {
                        var8 = 19;
                     } else {
                        var8 = 18;
                     }
               }

               if (var2 >= 290) {
                  var7.setUniqueId(UUID.randomUUID()).setX(var3.getX()).setY(var3.getY()).setZ(var3.getZ()).sendPacket(var1);
               } else {
                  (new WrapperPlayServerSpawnEntityLiving()).setEntityId(-69).setUniqueId(UUID.randomUUID()).setType(var8).setX(var3.getX()).setY(var3.getY()).setZ(var3.getZ()).sendPacket(var1);
               }

               if (var2 < 290) {
                  WrapperPlayServerEntityMetadata var9 = (new WrapperPlayServerEntityMetadata()).setEntityId(-69);
                  WrappedDataWatcher var10 = new WrappedDataWatcher();
                  var10.setObject(0, Registry.get(Byte.class), (byte)32);
                  var9.setMetadata(var10.getWatchableObjects()).sendPacket(var1);
               }

               var1.setGameMode(GameMode.SPECTATOR);
               (new WrapperPlayServerCamera()).setCameraId(-69).sendPacket(var1);
               (new WrapperPlayServerUpdateHealth()).setHealth(-10.0F).sendPacket(var1);
               var1.setGameMode(var5);
               if (ReflectionUtils.getVersionId() >= 270) {
                  (new WrapperPlayServerEntityDestroy()).set17EntityIds(Collections.singletonList(-69)).sendPacket(var1);
               } else {
                  (new WrapperPlayServerEntityDestroy()).setEntityIds(new int[]{-69}).sendPacket(var1);
               }

               if (var2 >= 300) {
                  (new WrapperPlayServerRespawnNew(ReflectionUtils.createRespawnPacket1_20(var4, var5))).sendPacket(var1);
               } else if (var2 >= 293) {
                  (new WrapperPlayServerRespawnNew(ReflectionUtils.createRespawnPacket1_19_3(var4, var5))).sendPacket(var1);
               } else if (var2 >= 290) {
                  (new WrapperPlayServerRespawnNew()).updateWorldSettings(var4).setSeed(var4.getSeed()).setGameMode(var5).setPreviousGameMode(var5).setDebug(false).setFlat(var4.getWorldType() == WorldType.FLAT).sendPacket(var1);
               } else if (var2 >= 260) {
                  (new WrapperPlayServerRespawn()).updateWorldSettings(var4).setSeed(var4.getSeed()).setGameMode(NativeGameMode.valueOf(var5.name())).setPreviousGameMode(NativeGameMode.valueOf(var5.name())).setIsFlat(var4.getWorldType() == WorldType.FLAT).sendPacket(var1);
               } else {
                  (new WrapperPlayServerRespawn()).setDimension(var4.getEnvironment().getId()).setGameMode(NativeGameMode.valueOf(var5.name())).setWorldType(var4.getWorldType()).sendPacket(var1);
               }

               var1.teleport(var3);
               var1.updateInventory();
               var1.setSprinting(var6);
               break;
            case RED_BORDER:
               if (ReflectionUtils.getVersionId() >= 270) {
                  (new WrapperPlayServerSetBorderSize()).setDiameter((double)250000.0F).sendPacket(var1);
                  (new WrapperPlayServerSetBorderCenter()).setCenterX(var1.getLocation().getX()).setCenterZ(var1.getLocation().getZ()).sendPacket(var1);
                  (new WrapperPlayServerSetBorderWarningDistance()).setWarningDistance(Integer.MAX_VALUE).sendPacket(var1);
               } else {
                  (new WrapperPlayServerWorldBorder()).setAction(WorldBorderAction.SET_SIZE).setRadius((double)250000.0F).setOldRadius((double)250000.0F).sendPacket(var1);
                  (new WrapperPlayServerWorldBorder()).setAction(WorldBorderAction.SET_CENTER).setCenterX(var1.getLocation().getX()).setCenterZ(var1.getLocation().getZ()).sendPacket(var1);
                  (new WrapperPlayServerWorldBorder()).setAction(WorldBorderAction.SET_WARNING_BLOCKS).setWarningDistance(Integer.MAX_VALUE).sendPacket(var1);
               }
         }

      }
   }

   public void end(Player var1) {
      switch (this.type) {
         case CREEPER_VISION:
         case SPIDER_VISION:
         case ENDERMAN_VISION:
            (new WrapperPlayServerCamera()).setCameraId(var1.getEntityId()).sendPacket(var1);
            break;
         case RED_BORDER:
            if (ReflectionUtils.getVersionId() >= 270) {
               (new WrapperPlayServerSetBorderWarningDistance()).setWarningDistance(0).sendPacket(var1);
            } else {
               (new WrapperPlayServerWorldBorder()).setAction(WorldBorderAction.SET_WARNING_BLOCKS).setWarningDistance(0).sendPacket(var1);
            }
      }

   }

   public VisualEffectType getType() {
      return this.type;
   }

   public VisualEffectAction clone() {
      return new VisualEffectAction(this.getDelay(), this.getChance(), this.getRepetitions(), this.getRepetitionTickDelay(), this.getTargetSelector(), this.getDuration(), this.type);
   }
}
