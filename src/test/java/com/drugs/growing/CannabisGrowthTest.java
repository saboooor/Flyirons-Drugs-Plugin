package com.drugs.growing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Farmland;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CannabisGrowthTest {
    @Test void sharedTimerPausesAndMaturesAtConfiguredDuration() throws Exception {
        var growing = mock(DrugGrowing.class, CALLS_REAL_METHODS);
        var plantsField = DrugGrowing.class.getDeclaredField("plants");
        plantsField.setAccessible(true);
        var plants = new HashMap<String, Object>();
        plantsField.set(growing, plants);
        var cropsField = DrugGrowing.class.getDeclaredField("crops");
        cropsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var crops = (Map<String, DrugGrowing.Crop>) cropsField.get(null);
        var previous = crops.put("cannabis", new DrugGrowing.Crop("cannabis", "Cannabis Buds", Material.FERN, Material.FERN, 8));
        var world = mock(World.class);
        UUID id = UUID.randomUUID();
        when(world.getUID()).thenReturn(id);
        when(world.isChunkLoaded(0, 0)).thenReturn(true);
        var root = mock(Block.class);
        var top = mock(Block.class);
        var soil = mock(Block.class);
        when(world.getBlockAt(0, 0, 0)).thenReturn(root);
        when(root.getWorld()).thenReturn(world);
        when(root.getRelative(0, 1, 0)).thenReturn(top);
        when(root.getRelative(0, -1, 0)).thenReturn(soil);
        when(soil.getType()).thenReturn(Material.FARMLAND);
        var farmland = mock(Farmland.class);
        when(soil.getBlockData()).thenReturn(farmland);
        when(farmland.getMoisture()).thenReturn(7);
        when(top.isEmpty()).thenReturn(true);
        when(root.getBlockData()).thenReturn(mock(Bisected.class));
        when(top.getBlockData()).thenReturn(mock(Bisected.class));
        var type = new AtomicReference<>(Material.FERN);
        when(root.getType()).thenAnswer(call -> type.get());
        doAnswer(call -> { type.set(call.getArgument(0)); return null; }).when(root).setType(any(), eq(false));
        var plantClass = Class.forName("com.drugs.growing.DrugGrowing$Plant");
        var constructor = plantClass.getDeclaredConstructor(String.class, int.class);
        constructor.setAccessible(true);
        plants.put(id + ":0:0:0", constructor.newInstance("cannabis", 0));
        var tick = DrugGrowing.class.getDeclaredMethod("tick");
        tick.setAccessible(true);
        try (var bukkit = mockStatic(Bukkit.class); var registry = mockStatic(CannabisPlantRegistry.class)) {
            registry.when(CannabisPlantRegistry::getPlantsSnapshot).thenReturn(Map.of());
            bukkit.when(() -> Bukkit.getWorld(id)).thenReturn(world);
            for (int i = 0; i < 10; i++) tick.invoke(growing); // Darkness pauses growth.
            when(root.getLightLevel()).thenReturn((byte) 15);
            for (int i = 0; i < 7; i++) tick.invoke(growing);
            assertEquals(Material.FERN, type.get());
            assertFalse(growing.isMature(root));
            tick.invoke(growing);
            assertEquals(Material.LARGE_FERN, type.get());
            assertTrue(growing.isMature(root));
            verify(top).setType(Material.LARGE_FERN, false);
        } finally {
            if (previous == null) crops.remove("cannabis");
            else crops.put("cannabis", previous);
        }
    }
}
