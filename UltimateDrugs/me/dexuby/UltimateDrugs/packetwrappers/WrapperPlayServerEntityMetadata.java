package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerEntityMetadata extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerEntityMetadata() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerEntityMetadata(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public Entity getEntity(World var1) {
      return (Entity)this.handle.getEntityModifier(var1).read(0);
   }

   public Entity getEntity(PacketEvent var1) {
      return this.getEntity(var1.getPlayer().getWorld());
   }

   public int getEntityId() {
      return (Integer)this.handle.getIntegers().read(0);
   }

   public WrapperPlayServerEntityMetadata setEntityId(int var1) {
      this.handle.getIntegers().write(0, var1);
      return this;
   }

   public List<WrappedWatchableObject> getMetadata() {
      return (List)this.handle.getWatchableCollectionModifier().read(0);
   }

   public WrapperPlayServerEntityMetadata setMetadata(List<WrappedWatchableObject> var1) {
      this.handle.getWatchableCollectionModifier().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.ENTITY_METADATA;
   }
}
