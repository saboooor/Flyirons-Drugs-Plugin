package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.World;

public class WrapperPlayServerBlockChange extends AbstractPacket {
   public static final PacketType PACKET_TYPE;

   public WrapperPlayServerBlockChange() {
      super(new PacketContainer(PACKET_TYPE), PACKET_TYPE);
      this.handle.getModifier().writeDefaults();
   }

   public WrapperPlayServerBlockChange(PacketContainer var1) {
      super(var1, PACKET_TYPE);
   }

   public BlockPosition getLocation() {
      return (BlockPosition)this.handle.getBlockPositionModifier().read(0);
   }

   public WrapperPlayServerBlockChange setLocation(BlockPosition var1) {
      this.handle.getBlockPositionModifier().write(0, var1);
      return this;
   }

   public Location getBukkitLocation(World var1) {
      return this.getLocation().toVector().toLocation(var1);
   }

   public WrappedBlockData getBlockData() {
      return (WrappedBlockData)this.handle.getBlockData().read(0);
   }

   public WrapperPlayServerBlockChange setBlockData(WrappedBlockData var1) {
      this.handle.getBlockData().write(0, var1);
      return this;
   }

   static {
      PACKET_TYPE = Server.BLOCK_CHANGE;
   }
}
