package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/** Tracks genuine player drops and preserves ownership until the sale is attempted. */
final class CartelDroppedSales implements Listener {
    private final DrugsV2 plugin;
    private final CartelDealer dealer;
    private final CartelEnforcer enforcer;
    private final Map<UUID, Drop> pending = new HashMap<>();
    private BukkitTask task;

    CartelDroppedSales(DrugsV2 plugin, CartelDealer dealer, CartelEnforcer enforcer) {
        this.plugin = plugin;
        this.dealer = dealer;
        this.enforcer = enforcer;
    }

    void start() { task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 5L, 5L); }
    void shutdown() { if (task != null) task.cancel(); pending.clear(); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("drugs.cartel.sell") || player.hasMetadata("NPC")
                || CartelDealer.price(event.getItemDrop().getItemStack(), dealer.settings()) == 0) return;
        if (pending.size() >= 4096) return;
        Item item = event.getItemDrop();
        pending.put(item.getUniqueId(), new Drop(item, player.getUniqueId(), System.currentTimeMillis()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onMerge(ItemMergeEvent event) {
        // Merging two players' stacks would lose the original seller attribution.
        if (pending.containsKey(event.getEntity().getUniqueId()) || pending.containsKey(event.getTarget().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        CartelNpcs npcs = dealer.npcs();
        if (npcs != null && (npcs.find(event.getEntity(), "enforcer") != null
                || npcs.find(event.getEntity(), "buyer") != null)) event.setCancelled(true);
    }

    void tick() {
        long now = System.currentTimeMillis();
        // Snapshot before calling economy providers, which may invoke other plugin callbacks.
        for (Map.Entry<UUID, Drop> entry : new ArrayList<>(pending.entrySet())) {
            Drop drop = entry.getValue();
            if (!drop.item.isValid() || now - drop.created > 300_000L) { pending.remove(entry.getKey()); continue; }
            Player player = Bukkit.getPlayer(drop.owner);
            if (player == null || !player.isOnline() || player.isDead() || !player.hasPermission("drugs.cartel.sell")
                    || player.getWorld() != drop.item.getWorld()) { pending.remove(entry.getKey()); continue; }
            // At least one tick of flight before collection; continue tracking if not near a buyer yet.
            if (drop.item.getTicksLived() < 5) continue;
            pending.remove(entry.getKey());
            if (!enforcer.acceptDrop(player, drop.item) && !dealer.acceptDrop(player, drop.item)) {
                pending.putIfAbsent(entry.getKey(), drop);
            }
        }
    }

    private record Drop(Item item, UUID owner, long created) {}
}
