package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.util.UUID;
import org.bukkit.entity.EntityType;

public class WrapperPlayServerSpawnEntity extends AbstractPacket {
   public static final PacketType PACKET_TYPE;
   private static PacketConstructor entityConstructor;

   public WrapperPlayServerSpawnEntity() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerSpawnEntity(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public int getEntityId() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerSpawnEntity setEntityId(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   public UUID getUniqueId() {
      return (UUID)this.handle.getUUIDs().read(0);
   }

   public WrapperPlayServerSpawnEntity setUniqueId(UUID var1) {
      this.handle.getUUIDs().write(0, var1);
      return this;
   }

   public EntityType getType() {
      return EntityType.fromId((Integer)this.handle.getIntegers().read(1));
   }

   public WrapperPlayServerSpawnEntity setType(EntityType var1) {
      this.handle.getIntegers().write(1, var1.getTypeId());
      return this;
   }

   public WrapperPlayServerSpawnEntity setType(int var1) {
      this.handle.getIntegers().write(1, var1);
      return this;
   }

   public WrapperPlayServerSpawnEntity setTypeNew(EntityType var1) {
      this.handle.getEntityTypeModifier().write(0, var1);
      return this;
   }

   public double getX() {
      return (Double)this.handle.getDoubles().read(0);
   }

   public WrapperPlayServerSpawnEntity setX(double var1) {
      this.handle.getDoubles().write(0, var1);
      return this;
   }

   public double getY() {
      return (Double)this.handle.getDoubles().read(1);
   }

   public WrapperPlayServerSpawnEntity setY(double var1) {
      this.handle.getDoubles().write(1, var1);
      return this;
   }

   public double getZ() {
      return (Double)this.handle.getDoubles().read(2);
   }

   public WrapperPlayServerSpawnEntity setZ(double var1) {
      this.handle.getDoubles().write(2, var1);
      return this;
   }

   public float getYaw() {
      return (float)(Byte)this.handle.getBytes().read(0) * 360.0F / 256.0F;
   }

   public WrapperPlayServerSpawnEntity setYaw(float var1) {
      this.handle.getBytes().write(0, (byte)((int)(var1 * 256.0F / 360.0F)));
      return this;
   }

   public float getPitch() {
      return (float)(Byte)this.handle.getBytes().read(1) * 360.0F / 256.0F;
   }

   public WrapperPlayServerSpawnEntity setPitch(float var1) {
      this.handle.getBytes().write(1, (byte)((int)(var1 * 256.0F / 360.0F)));
      return this;
   }

   public float getHeadPitch() {
      return (float)(Byte)this.handle.getBytes().read(2) * 360.0F / 256.0F;
   }

   public WrapperPlayServerSpawnEntity setHeadPitch(float var1) {
      this.handle.getBytes().write(2, (byte)((int)(var1 * 256.0F / 360.0F)));
      return this;
   }

   public double getVelocityX() {
      return (double)(Integer)this.handle.getIntegers().read(2) / (double)8000.0F;
   }

   public WrapperPlayServerSpawnEntity setVelocityX(double var1) {
      this.handle.getIntegers().write(2, (int)(var1 * (double)8000.0F));
      return this;
   }

   public double getVelocityY() {
      return (double)(Integer)this.handle.getIntegers().read(3) / (double)8000.0F;
   }

   public WrapperPlayServerSpawnEntity setVelocityY(double var1) {
      this.handle.getIntegers().write(3, (int)(var1 * (double)8000.0F));
      return this;
   }

   public double getVelocityZ() {
      return (double)(Integer)this.handle.getIntegers().read(4) / (double)8000.0F;
   }

   public WrapperPlayServerSpawnEntity setVelocityZ(double var1) {
      this.handle.getIntegers().write(4, (int)(var1 * (double)8000.0F));
      return this;
   }

   public WrappedDataWatcher getMetadata() {
      return (WrappedDataWatcher)this.handle.getDataWatcherModifier().read(0);
   }

   public WrapperPlayServerSpawnEntity setMetadata(WrappedDataWatcher var1) {
      this.handle.getDataWatcherModifier().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.SPAWN_ENTITY;
   }
}
