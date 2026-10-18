package com.drugs;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DrugGrowingAutomationTest {
    @Test
    void onlyMatureTrackedIngredientCropsAreHarvested() throws Exception {
        var growing = mock(DrugGrowing.class, CALLS_REAL_METHODS);
        var world = mock(World.class);
        var block = mock(Block.class);
        UUID id = UUID.randomUUID();
        when(world.getUID()).thenReturn(id);
        when(block.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(Material.WHEAT);
        var field = DrugGrowing.class.getDeclaredField("plants");
        field.setAccessible(true);
        var plants = new HashMap<String, Object>();
        field.set(growing, plants);
        doNothing().when(growing).save();
        assertTrue(growing.harvestMature(block).isEmpty()); // Vanilla wheat.
        var cropField = DrugGrowing.class.getDeclaredField("crops");
        cropField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var crops = (Map<String, DrugGrowing.Crop>) cropField.get(null);
        var previous = new HashMap<>(crops);
        var plantType = Class.forName("com.drugs.DrugGrowing$Plant");
        var constructor = plantType.getDeclaredConstructor(String.class, int.class);
        constructor.setAccessible(true);
        try (var items = mockConstruction(ItemStack.class, (item, context) -> {
            when(item.getItemMeta()).thenReturn(mock(ItemMeta.class));
            when(item.asQuantity(anyInt())).thenReturn(item);
        }); var metadata = mockStatic(DrugItemMetadata.class)) {
            for (String crop : new String[]{"poppy", "mushroom", "ephedra", "coca", "sassafras", "ergot", "salvia", "resin"}) {
                crops.put(crop, new DrugGrowing.Crop(crop, crop, Material.POPPY, Material.POPPY, 10));
                plants.put(id + ":0:0:0", constructor.newInstance(crop, 9));
                assertTrue(growing.harvestMature(block).isEmpty());
                plants.put(id + ":0:0:0", constructor.newInstance(crop, 10));
                assertEquals(2, growing.harvestMature(block).size());
                assertTrue(plants.isEmpty());
                var seed = mock(ItemStack.class);
                var meta = mock(ItemMeta.class);
                when(seed.hasItemMeta()).thenReturn(true);
                when(seed.getItemMeta()).thenReturn(meta);
                metadata.when(() -> DrugItemMetadata.getItemType(meta)).thenReturn("seed_" + crop);
                var soil = mock(Block.class);
                var farmland = mock(org.bukkit.block.data.type.Farmland.class);
                when(block.getRelative(0, -1, 0)).thenReturn(soil);
                when(soil.getBlockData()).thenReturn(farmland);
                when(block.getType()).thenReturn(Material.AIR);
                when(block.isEmpty()).thenReturn(true);
                when(farmland.getMoisture()).thenReturn(0);
                assertFalse(growing.plantSeed(block, seed));
                when(farmland.getMoisture()).thenReturn(7);
                assertTrue(growing.plantSeed(block, seed));
                assertEquals(1, plants.size());
                plants.clear();
                when(block.getType()).thenReturn(Material.WHEAT);
            }
        } finally {
            crops.clear();
            crops.putAll(previous);
        }
        verify(block, times(8)).setType(Material.AIR, false);
    }
}
