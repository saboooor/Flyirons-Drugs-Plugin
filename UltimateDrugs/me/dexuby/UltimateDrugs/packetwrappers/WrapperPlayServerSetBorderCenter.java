package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerSetBorderCenter extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerSetBorderCenter() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerSetBorderCenter(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public double getCenterX() {
      return (Double)this.handle.getDoubles().read(0);
   }

   public WrapperPlayServerSetBorderCenter setCenterX(double var1) {
      this.handle.getDoubles().write(0, var1);
      return this;
   }

   public double getCenterZ() {
      return (Double)this.handle.getDoubles().read(1);
   }

   public WrapperPlayServerSetBorderCenter setCenterZ(double var1) {
      this.handle.getDoubles().write(1, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.SET_BORDER_CENTER;
   }
}
