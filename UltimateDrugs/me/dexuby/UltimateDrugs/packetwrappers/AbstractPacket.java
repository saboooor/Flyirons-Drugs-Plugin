package me.dexuby.UltimateDrugs.packetwrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Objects;
import org.bukkit.entity.Player;

public class AbstractPacket {
   protected PacketContainer handle;

   protected AbstractPacket(PacketContainer var1, PacketType var2) {
      if (var1 == null) {
         throw new IllegalArgumentException("Packet handle was null");
      } else if (!Objects.equal(var1.getType(), var2)) {
         throw new IllegalArgumentException("Packet type not matching");
      } else {
         this.handle = var1;
      }
   }

   public PacketContainer getHandle() {
      return this.handle;
   }

   public void sendPacket(Player var1) {
      try {
         ProtocolLibrary.getProtocolManager().sendServerPacket(var1, this.getHandle());
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public void sendPacket(Player... var1) {
      try {
         for(Player var5 : var1) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(var5, this.getHandle());
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public void receivePacket(Player var1) {
      try {
         ProtocolLibrary.getProtocolManager().recieveClientPacket(var1, this.getHandle());
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }
}
