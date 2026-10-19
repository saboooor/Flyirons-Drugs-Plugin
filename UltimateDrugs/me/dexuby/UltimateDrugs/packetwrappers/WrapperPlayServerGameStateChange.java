package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerGameStateChange extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerGameStateChange() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerGameStateChange(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public int getReason() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerGameStateChange setReason(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   public float getValue() {
      return (Float)this.handle.getFloat().read(0);
   }

   public WrapperPlayServerGameStateChange setValue(float var1) {
      this.handle.getFloat().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.GAME_STATE_CHANGE;
   }
}
