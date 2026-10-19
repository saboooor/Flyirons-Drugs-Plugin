package com.drugs.farm;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class WorkContractTest {
    @Test
    void assignsClickedVillagerWithoutLookingAtController() {
        verifyContract(false, true);
    }

    @Test
    void alreadyAssignedVillagerDoesNotConsumeContract() {
        verifyContract(true, false);
    }

    @Test
    void refundsEveryInvalidContractAndKeepsValidContracts() {
        var manager = mock(FarmManager.class);
        var player = mock(Player.class);
        var inventory = mock(PlayerInventory.class);
        var invalid = mock(ItemStack.class);
        var valid = mock(ItemStack.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getSize()).thenReturn(2);
        when(inventory.getItem(0)).thenReturn(invalid);
        when(inventory.getItem(1)).thenReturn(valid);
        when(invalid.getAmount()).thenReturn(3);
        when(manager.getByFarmId("valid")).thenReturn(new Farm("valid", new Location(null, 0, 0, 0)));
        when(inventory.addItem(any(ItemStack.class))).thenReturn(new java.util.HashMap<>());
        try (var items = mockStatic(FarmItems.class);
             var refunds = mockConstruction(ItemStack.class, (item, context) -> {
                 org.junit.jupiter.api.Assertions.assertEquals(org.bukkit.Material.EMERALD, context.arguments().get(0));
                 org.junit.jupiter.api.Assertions.assertEquals(3, context.arguments().get(1));
             })) {
            items.when(() -> FarmItems.parseFarmId(invalid, FarmItems.ITEM_TYPE_CONTRACT)).thenReturn("missing");
            items.when(() -> FarmItems.parseFarmId(valid, FarmItems.ITEM_TYPE_CONTRACT)).thenReturn("valid");
            new FarmControllerListener(manager).refundInvalidContracts(player);
            org.junit.jupiter.api.Assertions.assertEquals(1, refunds.constructed().size());
            verify(inventory).addItem(refunds.constructed().getFirst());
        }
        verify(inventory).setItem(0, null);
        verify(inventory, never()).setItem(eq(1), any());
        verify(manager, never()).assignVillager(any(), any());
    }

    private void verifyContract(boolean assigned, boolean consumes) {
        var manager = mock(FarmManager.class);
        var player = mock(Player.class);
        var villager = mock(Villager.class);
        var inventory = mock(PlayerInventory.class);
        var item = mock(ItemStack.class);
        var id = UUID.randomUUID();
        var farm = new Farm("test", new Location(null, 0, 0, 0));
        if (assigned) farm.getAssignedVillagers().add(id);
        when(villager.getUniqueId()).thenReturn(id);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(item);
        when(item.getAmount()).thenReturn(2);
        when(manager.getByFarmId("test")).thenReturn(farm);
        when(manager.assignVillager(farm, id)).thenReturn(true);
        var event = new PlayerInteractEntityEvent(player, villager, EquipmentSlot.HAND);
        try (var items = mockStatic(FarmItems.class)) {
            items.when(() -> FarmItems.parseFarmId(item, FarmItems.ITEM_TYPE_CONTRACT)).thenReturn("test");
            new FarmControllerListener(manager).onVillagerContract(event);
        }
        org.junit.jupiter.api.Assertions.assertTrue(event.isCancelled());
        verify(player, never()).rayTraceBlocks(anyDouble());
        verify(item, consumes ? times(1) : never()).setAmount(1);
        verify(manager, consumes ? times(1) : never()).assignVillager(farm, id);
    }
}
