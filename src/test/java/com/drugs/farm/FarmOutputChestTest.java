package com.drugs.farm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static org.mockito.Mockito.*;

class FarmOutputChestTest {
    @Test void storesInChestAndDropsOnlyOverflow() {
        var world = mock(World.class);
        var controller = new Location(world, 0, 64, 0);
        var farm = new Farm("test", controller);
        farm.setOutputChest(new Location(world, 2, 64, 0));
        when(world.isChunkLoaded(0, 0)).thenReturn(true);
        var block = mock(Block.class);
        when(world.getBlockAt(2, 64, 0)).thenReturn(block);
        when(world.getBlockAt(any(Location.class))).thenReturn(block);
        var chest = mock(Chest.class);
        var inventory = mock(Inventory.class);
        when(block.getState()).thenReturn(chest);
        when(chest.getInventory()).thenReturn(inventory);
        var harvest = mock(ItemStack.class);
        var overflow = mock(ItemStack.class);
        var leftovers = new HashMap<Integer, ItemStack>();
        when(inventory.addItem(harvest)).thenReturn(leftovers);
        FarmWorkerService.storeOutput(farm, harvest);
        verify(world, never()).dropItemNaturally(any(), any());
        leftovers.put(0, overflow);
        FarmWorkerService.storeOutput(farm, harvest);
        verify(world).dropItemNaturally(controller, overflow);
    }

    @Test void unloadedChestFallsBackWithoutLoadingChunk() {
        var world = mock(World.class);
        var controller = new Location(world, 0, 64, 0);
        var farm = new Farm("test", controller);
        farm.setOutputChest(new Location(world, 32, 64, 0));
        var harvest = mock(ItemStack.class);
        FarmWorkerService.storeOutput(farm, harvest);
        verify(world, never()).getBlockAt(anyInt(), anyInt(), anyInt());
        verify(world, never()).getBlockAt(any(Location.class));
        verify(world).dropItemNaturally(controller, harvest);
    }
}
