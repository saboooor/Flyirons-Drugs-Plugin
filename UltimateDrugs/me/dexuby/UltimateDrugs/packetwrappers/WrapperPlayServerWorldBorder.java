package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

public class WrapperPlayServerWorldBorder extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerWorldBorder() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerWorldBorder(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public EnumWrappers.WorldBorderAction getAction() {
      return (EnumWrappers.WorldBorderAction)this.handle.getWorldBorderActions().read(0);
   }

   public WrapperPlayServerWorldBorder setAction(EnumWrappers.WorldBorderAction var1) {
      this.handle.getWorldBorderActions().write(0, var1);
      return this;
   }

   public int getPortalTeleportBoundary() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerWorldBorder setPortalTeleportBoundary(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   public double getCenterX() {
      return (Double)this.handle.getDoubles().read(0);
   }

   public WrapperPlayServerWorldBorder setCenterX(double var1) {
      this.handle.getDoubles().write(0, var1);
      return this;
   }

   public double getCenterZ() {
      return (Double)this.handle.getDoubles().read(1);
   }

   public WrapperPlayServerWorldBorder setCenterZ(double var1) {
      this.handle.getDoubles().write(1, var1);
      return this;
   }

   public double getOldRadius() {
      return (Double)this.handle.getDoubles().read(2);
   }

   public WrapperPlayServerWorldBorder setOldRadius(double var1) {
      this.handle.getDoubles().write(2, var1);
      return this;
   }

   public double getRadius() {
      return (Double)this.handle.getDoubles().read(3);
   }

   public WrapperPlayServerWorldBorder setRadius(double var1) {
      this.handle.getDoubles().write(3, var1);
      return this;
   }

   public long getSpeed() {
      return (Long)this.handle.getLongs().read(0);
   }

   public WrapperPlayServerWorldBorder setSpeed(long var1) {
      this.handle.getLongs().write(0, var1);
      return this;
   }

   public int getWarningTime() {
      return (Integer)this.handle.getIntegers().read(1);
   }

   public WrapperPlayServerWorldBorder setWarningTime(int var1) {
      this.handle.getIntegers().write(1, var1);
      return this;
   }

   public int getWarningDistance() {
      return (Integer)this.handle.getIntegers().read(2);
   }

   public WrapperPlayServerWorldBorder setWarningDistance(int var1) {
      this.handle.getIntegers().write(2, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.WORLD_BORDER;
   }
}
