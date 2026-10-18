package com.drugs;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NativeAdvancementsTest {
    @Test void grantAwardsOnlyRemainingNativeCriteria() {
        try (var bukkit = mockStatic(Bukkit.class)) {
            Player player = mock(Player.class);
            Advancement advancement = mock(Advancement.class);
            AdvancementProgress progress = mock(AdvancementProgress.class);
            bukkit.when(() -> Bukkit.getAdvancement(new NamespacedKey("LumiDrugs", "achievements/first-dose")))
                    .thenReturn(advancement);
            when(player.getAdvancementProgress(advancement)).thenReturn(progress);
            when(progress.getRemainingCriteria()).thenReturn(List.of("unlock"));
            NativeAdvancements.award(player, "first-dose");
            verify(progress).awardCriteria("unlock");
            when(progress.getRemainingCriteria()).thenReturn(List.of());
            NativeAdvancements.award(player, "first-dose");
            verify(progress, times(1)).awardCriteria("unlock");
            verify(player, never()).sendMessage(anyString());
        }
    }

    @Test void completionUsesMinecraftProgress() {
        try (var bukkit = mockStatic(Bukkit.class)) {
            Player player = mock(Player.class);
            Advancement advancement = mock(Advancement.class);
            AdvancementProgress progress = mock(AdvancementProgress.class);
            bukkit.when(() -> Bukkit.getAdvancement(new NamespacedKey("LumiDrugs", "achievements/first-dose")))
                    .thenReturn(advancement);
            when(player.getAdvancementProgress(advancement)).thenReturn(progress);
            when(progress.isDone()).thenReturn(false, true);
            assertFalse(NativeAdvancements.has(player, "first-dose"));
            assertTrue(NativeAdvancements.has(player, "first-dose"));
            assertFalse(NativeAdvancements.has(player, "missing"));
            NativeAdvancements.award(player, "missing");
        }
    }
}
