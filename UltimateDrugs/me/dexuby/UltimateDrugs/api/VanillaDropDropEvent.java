package me.dexuby.UltimateDrugs.api;

import java.util.Collection;
import me.dexuby.UltimateDrugs.drugs.Drop;
import me.dexuby.UltimateDrugs.drugs.vanilla.VanillaDrop;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VanillaDropDropEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private VanillaDrop vanillaDrop;
   private Location location;
   private Collection<Drop> drops;
   private Object[] extraData;

   public VanillaDropDropEvent(VanillaDrop var1, Location var2, Collection<Drop> var3, Object... var4) {
      this.vanillaDrop = var1;
      this.location = var2;
      this.drops = var3;
      this.extraData = var4;
   }

   public VanillaDrop getVanillaDrop() {
      return this.vanillaDrop;
   }

   public Location getLocation() {
      return this.location;
   }

   public Collection<Drop> getDrops() {
      return this.drops;
   }

   public Object[] getExtraData() {
      return this.extraData;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean var1) {
      this.cancel = var1;
   }
}
