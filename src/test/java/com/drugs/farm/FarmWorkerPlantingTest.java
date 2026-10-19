package com.drugs.farm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;

class FarmWorkerPlantingTest {
    @Test void assignedWorkersCannotPlantVanillaCrops() {
        var manager = mock(FarmManager.class);
        var worker = mock(Villager.class);
        UUID id = UUID.randomUUID();
        when(worker.getUniqueId()).thenReturn(id);
        // Applies while stopped or packed too, as long as the villager is assigned.
        when(manager.getByVillager(id)).thenReturn(new Farm("test", new Location(null, 0, 0, 0)));
        var listener = new FarmControllerListener(manager);
        for (Material crop : new Material[]{Material.WHEAT, Material.CARROTS, Material.POTATOES,
                Material.BEETROOTS, Material.TORCHFLOWER_CROP, Material.PITCHER_CROP}) {
            var event = mock(EntityChangeBlockEvent.class);
            when(event.getEntity()).thenReturn(worker);
            when(event.getTo()).thenReturn(crop);
            listener.onWorkerPlant(event);
            verify(event).setCancelled(true);
        }
    }

    @Test void unassignedVillagersCanStillPlantNormally() {
        var manager = mock(FarmManager.class);
        var worker = mock(Villager.class);
        when(worker.getUniqueId()).thenReturn(UUID.randomUUID());
        var event = mock(EntityChangeBlockEvent.class);
        when(event.getEntity()).thenReturn(worker);
        when(event.getTo()).thenReturn(Material.WHEAT);
        new FarmControllerListener(manager).onWorkerPlant(event);
        verify(event, never()).setCancelled(anyBoolean());
    }
}
