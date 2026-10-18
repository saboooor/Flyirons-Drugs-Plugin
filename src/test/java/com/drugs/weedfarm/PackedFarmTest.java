package com.drugs.weedfarm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PackedFarmTest {
    @TempDir Path directory;

    @Test void roundTripMovesAreaAndPreservesIdentityWorkersAndSettingsAcrossRestart() {
        World world = mock(World.class);
        UUID worldId = UUID.randomUUID();
        when(world.getUID()).thenReturn(worldId);
        when(world.getMinHeight()).thenReturn(-64);
        when(world.getMaxHeight()).thenReturn(320);
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld(worldId)).thenReturn(world);
            var manager = new WeedFarmManager(directory.toFile());
            var farm = manager.createFarm(new Location(world, 10, 64, 10));
            farm.setRegion(world, new Location(world, 11, 64, 11), new Location(world, 15, 66, 15));
            farm.setEnabled(false);
            var worker = UUID.randomUUID();
            farm.getAssignedVillagers().add(worker);
            var villager = mock(org.bukkit.entity.Villager.class);
            when(villager.isValid()).thenReturn(true);
            bukkit.when(() -> Bukkit.getEntity(worker)).thenReturn(villager);
            String encoded = WeedFarmItems.encodeFarm(farm);
            manager.packFarm(farm);
            assertTrue(manager.getFarms().isEmpty());
            assertNull(manager.getByController(farm.getControllerLocation()));
            assertNotNull(manager.getByFarmId(farm.getFarmId()));
            manager = new WeedFarmManager(directory.toFile());
            manager.load();
            assertTrue(manager.getFarms().isEmpty());
            var restored = WeedFarmItems.decodeFarm(encoded, new Location(world, 100, 70, 100));
            assertEquals(farm.getFarmId(), restored.getFarmId());
            assertEquals(101, restored.getMinX());
            assertEquals(72, restored.getMaxY());
            assertEquals(105, restored.getMaxZ());
            assertFalse(restored.isEnabled());
            assertTrue(restored.getAssignedVillagers().contains(worker));
            assertTrue(manager.restoreFarm(restored));
            assertFalse(manager.restoreFarm(restored));
            assertEquals(restored, manager.getByVillager(worker));
            assertEquals(1, manager.getFarms().size());
        }
    }

    @Test void restoringRemovesMissingDeadAndInvalidWorkersFromFarmAndIndex() {
        World world = mock(World.class);
        UUID worldId = UUID.randomUUID();
        when(world.getUID()).thenReturn(worldId);
        try (var bukkit = mockStatic(Bukkit.class)) {
            var manager = new WeedFarmManager(directory.toFile());
            var farm = manager.createFarm(new Location(world, 10, 64, 10));
            UUID missing = UUID.randomUUID(), dead = UUID.randomUUID(), invalid = UUID.randomUUID();
            farm.getAssignedVillagers().addAll(java.util.List.of(missing, dead, invalid));
            var deadVillager = mock(org.bukkit.entity.Villager.class);
            when(deadVillager.isValid()).thenReturn(true);
            when(deadVillager.isDead()).thenReturn(true);
            var invalidVillager = mock(org.bukkit.entity.Villager.class);
            bukkit.when(() -> Bukkit.getEntity(dead)).thenReturn(deadVillager);
            bukkit.when(() -> Bukkit.getEntity(invalid)).thenReturn(invalidVillager);
            manager.packFarm(farm);
            assertTrue(manager.restoreFarm(farm));
            assertTrue(farm.getAssignedVillagers().isEmpty());
            assertNull(manager.getByVillager(missing));
            assertNull(manager.getByVillager(dead));
            assertNull(manager.getByVillager(invalid));
        }
    }

    @Test void unsetAreaStaysUnsetAndMalformedDataIsRejected() {
        World world = mock(World.class);
        var location = new Location(world, 10, 64, 10);
        var farm = new WeedFarm("test", location);
        var restored = WeedFarmItems.decodeFarm(WeedFarmItems.encodeFarm(farm), location);
        assertFalse(restored.hasRegion());
        assertThrows(IllegalArgumentException.class, () -> WeedFarmItems.decodeFarm("id: test", location));
    }
}
