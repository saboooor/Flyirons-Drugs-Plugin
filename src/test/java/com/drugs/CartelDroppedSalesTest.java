package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.Mockito.*;

class CartelDroppedSalesTest {
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<CartelDealer> prices;
    private CartelDealer dealer;
    private CartelEnforcer enforcer;
    private CartelDroppedSales sales;
    private Player owner;
    private Item item;

    @BeforeEach void setup() {
        bukkit = mockStatic(Bukkit.class);
        prices = mockStatic(CartelDealer.class);
        dealer = mock(CartelDealer.class);
        enforcer = mock(CartelEnforcer.class);
        YamlConfiguration config = new YamlConfiguration();
        when(dealer.settings()).thenReturn(config);
        prices.when(() -> CartelDealer.price(any(), eq(config))).thenReturn(15.0);
        sales = new CartelDroppedSales(mock(DrugsV2.class), dealer, enforcer);
        owner = mock(Player.class);
        when(owner.getUniqueId()).thenReturn(UUID.randomUUID());
        when(owner.isOnline()).thenReturn(true);
        when(owner.hasPermission("drugs.cartel.sell")).thenReturn(true);
        World world = mock(World.class);
        when(owner.getWorld()).thenReturn(world);
        item = mock(Item.class);
        when(item.getUniqueId()).thenReturn(UUID.randomUUID());
        when(item.isValid()).thenReturn(true);
        when(item.getTicksLived()).thenReturn(5);
        when(item.getWorld()).thenReturn(world);
        when(item.getItemStack()).thenReturn(mock(ItemStack.class));
        bukkit.when(() -> Bukkit.getPlayer(owner.getUniqueId())).thenReturn(owner);
    }

    @AfterEach void cleanup() { prices.close(); bukkit.close(); }

    private void drop() {
        PlayerDropItemEvent event = mock(PlayerDropItemEvent.class);
        when(event.getPlayer()).thenReturn(owner);
        when(event.getItemDrop()).thenReturn(item);
        sales.onDrop(event);
    }

    @Test void onlyTrackedDropsAreDeliveredToTheirOriginalOwnerOnce() {
        sales.tick();
        verify(enforcer, never()).acceptDrop(any(), any());
        drop();
        when(enforcer.acceptDrop(owner, item)).thenReturn(true);
        sales.tick();
        sales.tick();
        verify(enforcer, times(1)).acceptDrop(owner, item);
        verify(dealer, never()).acceptDrop(any(), any());
    }

    @Test void dropsRemainTrackedUntilTheyReachABuyer() {
        drop();
        sales.tick();
        when(dealer.acceptDrop(owner, item)).thenReturn(true);
        sales.tick();
        sales.tick();
        verify(enforcer, times(2)).acceptDrop(owner, item);
        verify(dealer, times(2)).acceptDrop(owner, item);
    }

    @Test void mergingTrackedItemsIntoOtherStacksIsBlocked() {
        drop();
        Item other = mock(Item.class);
        when(other.getUniqueId()).thenReturn(UUID.randomUUID());
        ItemMergeEvent source = mock(ItemMergeEvent.class);
        when(source.getEntity()).thenReturn(item);
        when(source.getTarget()).thenReturn(other);
        sales.onMerge(source);
        verify(source).setCancelled(true);
        ItemMergeEvent target = mock(ItemMergeEvent.class);
        when(target.getEntity()).thenReturn(other);
        when(target.getTarget()).thenReturn(item);
        sales.onMerge(target);
        verify(target).setCancelled(true);
    }

    @Test void invalidOrUnpermittedDropsNeverSell() {
        drop();
        when(item.isValid()).thenReturn(false);
        sales.tick();
        when(item.isValid()).thenReturn(true);
        when(owner.hasPermission("drugs.cartel.sell")).thenReturn(false);
        drop();
        sales.tick();
        verify(enforcer, never()).acceptDrop(any(), any());
        verify(dealer, never()).acceptDrop(any(), any());
    }

    @Test void cartelNpcVanillaPickupIsCancelled() {
        CartelNpcs npcs = mock(CartelNpcs.class);
        when(dealer.npcs()).thenReturn(npcs);
        Player npc = mock(Player.class);
        CartelNpcs.Character character = mock(CartelNpcs.Character.class);
        when(npcs.find(npc, "enforcer")).thenReturn(character);
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        when(event.getEntity()).thenReturn(npc);
        sales.onPickup(event);
        verify(event).setCancelled(true);
    }
}
