package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.hash.Hashing;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.World;
import org.bukkit.WorldType;

public class WrapperPlayServerRespawn extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerRespawn() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerRespawn(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public int getDimension() {
      return (Integer)this.handle.getDimensions().read(0);
   }

   public WrapperPlayServerRespawn setDimension(int var1) {
      this.handle.getDimensions().write(0, var1);
      return this;
   }

   public EnumWrappers.Difficulty getDifficulty() {
      return (EnumWrappers.Difficulty)this.handle.getDifficulties().read(0);
   }

   public WrapperPlayServerRespawn setDifficulty(EnumWrappers.Difficulty var1) {
      this.handle.getDifficulties().write(0, var1);
      return this;
   }

   public EnumWrappers.NativeGameMode getGameMode() {
      return (EnumWrappers.NativeGameMode)this.handle.getGameModes().read(0);
   }

   public WrapperPlayServerRespawn setGameMode(EnumWrappers.NativeGameMode var1) {
      this.handle.getGameModes().write(0, var1);
      return this;
   }

   public WorldType getWorldType() {
      return (WorldType)this.handle.getWorldTypeModifier().read(0);
   }

   public WrapperPlayServerRespawn setWorldType(WorldType var1) {
      this.handle.getWorldTypeModifier().write(0, var1);
      return this;
   }

   public WrapperPlayServerRespawn updateWorldSettings(World var1) {
      ReflectionUtils.prepareRespawnPacketContainer(this.handle, var1);
      return this;
   }

   public WrapperPlayServerRespawn setPreviousGameMode(EnumWrappers.NativeGameMode var1) {
      this.handle.getGameModes().write(1, var1);
      return this;
   }

   public WrapperPlayServerRespawn setSeed(long var1) {
      this.handle.getLongs().write(0, Hashing.sha256().hashLong(var1).asLong());
      return this;
   }

   public WrapperPlayServerRespawn setIsDebug(boolean var1) {
      this.handle.getBooleans().write(0, var1);
      return this;
   }

   public WrapperPlayServerRespawn setIsFlat(boolean var1) {
      this.handle.getBooleans().write(1, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.RESPAWN;
   }
}
