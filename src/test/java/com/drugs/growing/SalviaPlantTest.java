package com.drugs.growing;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalviaPlantTest {
    @Test void lilacUsesOnlyTopHalfWithoutChangingBlockAbove() throws Exception {
        var field = DrugGrowing.class.getDeclaredField("plantBlocks");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var appearances = (Map<String, Material>) field.get(null);
        Material previous = appearances.put("salvia", Material.LILAC);
        try {
            Block bottom = mock(Block.class), top = mock(Block.class);
            when(bottom.getRelative(0, 1, 0)).thenReturn(top);
            var flower = mock(Bisected.class);
            when(bottom.getBlockData()).thenReturn(flower);
            assertTrue(DrugGrowing.placeModel(bottom, "salvia"));
            verify(bottom).setType(Material.LILAC, false);
            verify(flower).setHalf(Bisected.Half.TOP);
            verifyNoInteractions(top);
        } finally {
            if (previous == null) appearances.remove("salvia");
            else appearances.put("salvia", previous);
        }
    }
}
