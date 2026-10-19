package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerSetBorderWarningDistance extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerSetBorderWarningDistance() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerSetBorderWarningDistance(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public int getWarningDistance() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerSetBorderWarningDistance setWarningDistance(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.SET_BORDER_WARNING_DISTANCE;
   }
}
