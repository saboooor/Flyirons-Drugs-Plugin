package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.google.common.hash.Hashing;
import me.dexuby.UltimateDrugs.utils.ReflectionUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;

public class WrapperPlayServerRespawnNew extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerRespawnNew() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerRespawnNew(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public String getDimensionType() {
      return (String)this.handle.getStrings().read(0);
   }

   public WrapperPlayServerRespawnNew updateWorldSettings(World var1) {
      ReflectionUtils.prepareRespawnPacketContainerNew(this.handle, var1);
      return this;
   }

   public WrapperPlayServerRespawnNew setDimensionType(String var1) {
      this.handle.getStrings().write(0, var1);
      return this;
   }

   public String getDimensionName() {
      return (String)this.handle.getStrings().read(1);
   }

   public WrapperPlayServerRespawnNew setDimensionName(String var1) {
      this.handle.getStrings().write(1, var1);
      return this;
   }

   public long getHashedSeed() {
      return (Long)this.handle.getLongs().read(0);
   }

   public WrapperPlayServerRespawnNew setSeed(long var1) {
      return this.setHashedSeed(Hashing.sha256().hashLong(var1).asLong());
   }

   public WrapperPlayServerRespawnNew setHashedSeed(long var1) {
      this.handle.getLongs().write(0, var1);
      return this;
   }

   public GameMode getGameMode() {
      return ((EnumWrappers.NativeGameMode)this.handle.getGameModes().read(0)).toBukkit();
   }

   public WrapperPlayServerRespawnNew setGameMode(GameMode var1) {
      this.handle.getGameModes().write(0, NativeGameMode.fromBukkit(var1));
      return this;
   }

   public GameMode getPreviousGameMode() {
      return ((EnumWrappers.NativeGameMode)this.handle.getGameModes().read(1)).toBukkit();
   }

   public WrapperPlayServerRespawnNew setPreviousGameMode(GameMode var1) {
      this.handle.getGameModes().write(1, NativeGameMode.fromBukkit(var1));
      return this;
   }

   public boolean isDebug() {
      return (Boolean)this.handle.getBooleans().read(0);
   }

   public WrapperPlayServerRespawnNew setDebug(boolean var1) {
      this.handle.getBooleans().write(0, var1);
      return this;
   }

   public boolean isFlat() {
      return (Boolean)this.handle.getBooleans().read(1);
   }

   public WrapperPlayServerRespawnNew setFlat(boolean var1) {
      this.handle.getBooleans().write(1, var1);
      return this;
   }

   public boolean doCopyMetadata() {
      return (Boolean)this.handle.getBooleans().read(2);
   }

   public WrapperPlayServerRespawnNew setCopyMetadata(boolean var1) {
      this.handle.getBooleans().write(2, var1);
      return this;
   }

   public boolean hasDeathLocation() {
      return (Boolean)this.handle.getBooleans().read(3);
   }

   public WrapperPlayServerRespawnNew setHasDeathLocation(boolean var1) {
      this.handle.getBooleans().write(3, var1);
      return this;
   }

   public String getDeathDimensionName() {
      return (String)this.handle.getStrings().read(2);
   }

   public WrapperPlayServerRespawnNew setDeathDimensionName(String var1) {
      this.handle.getStrings().write(2, var1);
      return this;
   }

   public long getDeathLocation() {
      return (Long)this.handle.getLongs().read(1);
   }

   public WrapperPlayServerRespawnNew setDeathLocation(Location var1) {
      this.handle.getLongs().write(1, (long)(var1.getBlockX() & 67108863) << 38 | (long)(var1.getBlockZ() & 67108863) << 12 | (long)(var1.getBlockY() & 4095));
      return this;
   }

   static {
      PACKET_TYPE = Server.RESPAWN;
   }
}
