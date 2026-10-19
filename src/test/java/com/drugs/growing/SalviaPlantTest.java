package com.drugs.growing;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalviaPlantTest {
    @Test void lilacNeedsTwoBlocksAndSetsBothHalves() throws Exception {
        var field = DrugGrowing.class.getDeclaredField("plantBlocks");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var appearances = (Map<String, Material>) field.get(null);
        Material previous = appearances.put("salvia", Material.LILAC);
        try {
            Block bottom = mock(Block.class), top = mock(Block.class);
            when(bottom.getRelative(0, 1, 0)).thenReturn(top);
            assertFalse(DrugGrowing.placeModel(bottom, "salvia"));
            verify(bottom, never()).setType(any(), anyBoolean());
            when(top.isEmpty()).thenReturn(true);
            var lower = mock(Bisected.class);
            var upper = mock(Bisected.class);
            when(bottom.getBlockData()).thenReturn(lower);
            when(top.getBlockData()).thenReturn(upper);
            assertTrue(DrugGrowing.placeModel(bottom, "salvia"));
            verify(bottom).setType(Material.LILAC, false);
            verify(top).setType(Material.LILAC, false);
            verify(lower).setHalf(Bisected.Half.BOTTOM);
            verify(upper).setHalf(Bisected.Half.TOP);
        } finally {
            if (previous == null) appearances.remove("salvia");
            else appearances.put("salvia", previous);
        }
    }
}
