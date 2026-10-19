package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerUpdateHealth extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerUpdateHealth() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerUpdateHealth(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public float getHealth() {
      return (Float)this.handle.getFloat().read(0);
   }

   public WrapperPlayServerUpdateHealth setHealth(float var1) {
      this.handle.getFloat().write(0, var1);
      return this;
   }

   public int getFood() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerUpdateHealth setFood(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   public float getFoodSaturation() {
      return (Float)this.handle.getFloat().read(1);
   }

   public WrapperPlayServerUpdateHealth setFoodSaturation(float var1) {
      this.handle.getFloat().write(1, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.UPDATE_HEALTH;
   }
}
