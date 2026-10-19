package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerSetBorderSize extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerSetBorderSize() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerSetBorderSize(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public double getDiameter() {
      return (Double)this.handle.getDoubles().read(0);
   }

   public WrapperPlayServerSetBorderSize setDiameter(double var1) {
      this.handle.getDoubles().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.SET_BORDER_SIZE;
   }
}
