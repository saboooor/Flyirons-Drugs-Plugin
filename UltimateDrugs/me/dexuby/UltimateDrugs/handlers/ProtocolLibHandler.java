package me.dexuby.UltimateDrugs.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.dexuby.UltimateDrugs.Main;
import me.dexuby.UltimateDrugs.drugs.ActiveDrugEffect;
import me.dexuby.UltimateDrugs.managers.DrugManager;

public class ProtocolLibHandler {
   private final DrugManager drugManager;

   public ProtocolLibHandler(Main var1) {
      this.drugManager = var1.getDrugManager();
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(var1, ListenerPriority.NORMAL, new PacketType[]{Server.NAMED_SOUND_EFFECT, Server.CUSTOM_SOUND_EFFECT, Server.ENTITY_SOUND}) {
         public void onPacketSending(PacketEvent var1) {
            ActiveDrugEffect var2 = (ActiveDrugEffect)ProtocolLibHandler.this.drugManager.getActiveDrugEffects(var1.getPlayer()).stream().filter((var0) -> System.currentTimeMillis() < var0.getExpirationTimestamp() && (var0.getDrug().getGlobalSoundVolumeModifier() != (double)0.0F || var0.getDrug().getGlobalSoundPitchModifier() != (double)0.0F)).findFirst().orElse((Object)null);
            if (var2 != null) {
               if (var2.getDrug().getGlobalSoundVolumeModifier() != (double)0.0F) {
                  double var3 = var2.getDrug().getGlobalSoundVolumeModifier();
                  float var5 = (Float)var1.getPacket().getFloat().read(0) + (float)var3;
                  var1.getPacket().getFloat().write(0, var5);
               }

               if (var2.getDrug().getGlobalSoundPitchModifier() != (double)0.0F) {
                  double var6 = var2.getDrug().getGlobalSoundPitchModifier();
                  float var7 = (Float)var1.getPacket().getFloat().read(1) + (float)var6;
                  var1.getPacket().getFloat().write(1, var7);
               }
            }

         }
      });
   }
}
