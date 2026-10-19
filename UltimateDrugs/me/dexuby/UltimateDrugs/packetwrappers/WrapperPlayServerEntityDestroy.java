package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;

public class WrapperPlayServerEntityDestroy extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerEntityDestroy() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityDestroy(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public int getCount() {
      return ((int[])this.handle.getIntegerArrays().read(0)).length;
   }

   public int[] getEntityIds() {
      return (int[])this.handle.getIntegerArrays().read(0);
   }

   public List<Integer> get17EntityIds() {
      return (List)this.handle.getIntLists().read(0);
   }

   public WrapperPlayServerEntityDestroy setEntityIds(int[] var1) {
      this.handle.getIntegerArrays().write(0, var1);
      return this;
   }

   public WrapperPlayServerEntityDestroy set17EntityIds(List<Integer> var1) {
      this.handle.getIntLists().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.ENTITY_DESTROY;
   }
}
