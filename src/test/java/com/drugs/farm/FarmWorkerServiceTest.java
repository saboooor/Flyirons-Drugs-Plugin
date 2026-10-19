package com.drugs.farm;

import com.drugs.growing.DrugGrowing;
import com.drugs.config.MechanicsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

class FarmWorkerServiceTest {
    @Test void soilSelectionFindsCropAndWorkerStandingOnFarmlandCanHarvest() {
        runFarm(0.5, true);
    }

    @Test void distantWorkerWalksToCropBeforeHarvesting() {
        runFarm(10, false);
    }

    private void runFarm(double workerX, boolean nearby) {
        var world = mock(World.class);
        var id = UUID.randomUUID();
        when(world.getUID()).thenReturn(id);
        var player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);
        when(world.getPlayers()).thenReturn(List.of(player));
        var soil = mock(Block.class);
        var crop = mock(Block.class);
        var head = mock(Block.class);
        var blocked = mock(Block.class);
        when(world.getBlockAt(0, 63, 0)).thenReturn(soil);
        when(world.getBlockAt((int) Math.floor(workerX), 63, 0)).thenReturn(soil);
        when(soil.getType()).thenReturn(Material.FARMLAND);
        when(soil.isPassable()).thenReturn(false);
        when(soil.getRelative(0, 1, 0)).thenReturn(crop);
        when(crop.getType()).thenReturn(Material.WHEAT);
        when(crop.getWorld()).thenReturn(world);
        when(crop.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        when(crop.isPassable()).thenReturn(true);
        when(crop.getRelative(0, 1, 0)).thenReturn(head);
        when(crop.getRelative(0, -1, 0)).thenReturn(soil);
        when(head.isPassable()).thenReturn(true);
        when(crop.getRelative(1, 0, 0)).thenReturn(blocked);
        when(crop.getRelative(-1, 0, 0)).thenReturn(blocked);
        when(crop.getRelative(0, 0, 1)).thenReturn(blocked);
        when(crop.getRelative(0, 0, -1)).thenReturn(blocked);
        var worker = mock(Villager.class, RETURNS_DEEP_STUBS);
        var workerId = UUID.randomUUID();
        when(worker.getUniqueId()).thenReturn(workerId);
        when(worker.getWorld()).thenReturn(world);
        when(worker.isValid()).thenReturn(true);
        // Feet are inside the farmland block because its top is at y=63.9375.
        when(worker.getLocation()).thenReturn(new Location(world, workerX, 63.9375, 0.5));
        var farm = new Farm("test", new Location(world, 0, 64, 0));
        farm.setRegion(world, new Location(world, 0, 63, 0), new Location(world, 0, 63, 0));
        farm.getAssignedVillagers().add(workerId);
        var manager = mock(FarmManager.class);
        when(manager.getFarms()).thenReturn(List.of(farm));
        var growing = mock(DrugGrowing.class);
        when(growing.isMature(crop)).thenReturn(true);
        when(growing.harvestMature(crop)).thenReturn(List.of());
        try (var bukkit = mockStatic(Bukkit.class); var mechanics = mockStatic(MechanicsConfig.class)) {
            bukkit.when(() -> Bukkit.getWorld(id)).thenReturn(world);
            bukkit.when(() -> Bukkit.getEntity(workerId)).thenReturn(worker);
            new FarmWorkerService(manager, growing).run();
            verify(growing, nearby ? times(1) : never()).harvestMature(crop);
            verify(worker.getPathfinder(), nearby ? never() : times(1)).moveTo(any(Location.class));
        }
    }
}
