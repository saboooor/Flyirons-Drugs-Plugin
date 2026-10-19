package com.drugs.growing;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CannabisFarmlandTest {
    @Test void cannabisRequiresHydratedFarmland() {
        Block plant = mock(Block.class), soil = mock(Block.class);
        when(plant.getRelative(0, -1, 0)).thenReturn(soil);
        when(soil.getBlockData()).thenReturn(mock(BlockData.class));
        assertFalse(CannabisPlantListener.hasHydratedFarmland(plant));
        var farmland = mock(Farmland.class);
        when(soil.getBlockData()).thenReturn(farmland);
        assertFalse(CannabisPlantListener.hasHydratedFarmland(plant));
        when(farmland.getMoisture()).thenReturn(7);
        assertTrue(CannabisPlantListener.hasHydratedFarmland(plant));
    }

    @Test void registrationRejectsCannabisOnOtherSoil() {
        Block plant = mock(Block.class), soil = mock(Block.class);
        ItemStack seed = mock(ItemStack.class);
        when(seed.getType()).thenReturn(Material.FERN);
        when(plant.getRelative(0, -1, 0)).thenReturn(soil);
        when(soil.getBlockData()).thenReturn(mock(BlockData.class));
        try (var registry = mockStatic(CannabisPlantRegistry.class)) {
            assertFalse(CannabisPlantListener.applyPlacedCannabis(plant, seed));
            registry.verifyNoInteractions();
        }
    }
}
