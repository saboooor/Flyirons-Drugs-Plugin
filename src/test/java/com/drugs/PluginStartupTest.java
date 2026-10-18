package com.drugs;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluginStartupTest {
    private record Harness(PluginStartup startup, Runnable tick, BukkitTask task) {}

    private Harness schedule(List<PluginStartup.Step> steps, Runnable ready,
                             java.util.function.Consumer<RuntimeException> failed) {
        Plugin plugin = mock(Plugin.class);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        BukkitTask task = mock(BukkitTask.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(server.getScheduler()).thenReturn(scheduler);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), eq(1L), eq(1L))).thenReturn(task);
        PluginStartup startup = new PluginStartup(plugin, steps, ready, failed);
        startup.start();
        var callback = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskTimer(eq(plugin), callback.capture(), eq(1L), eq(1L));
        return new Harness(startup, callback.getValue(), task);
    }

    @Test void enableReturnsBeforeWorkAndPhasesRunOnSeparateTicks() {
        List<String> events = new ArrayList<>();
        Harness harness = schedule(List.of(
                new PluginStartup.Step("config", () -> events.add("config")),
                new PluginStartup.Step("data", () -> events.add("data"))),
                () -> events.add("ready"), error -> fail(error));
        assertTrue(events.isEmpty());
        harness.tick().run();
        assertEquals(List.of("config"), events);
        harness.tick().run();
        assertEquals(List.of("config", "data", "ready"), events);
        verify(harness.task()).cancel();
        harness.tick().run();
        assertEquals(3, events.size());
    }

    @Test void unloadingDuringInitializationCancelsRemainingWork() {
        Runnable work = mock(Runnable.class);
        Runnable ready = mock(Runnable.class);
        Harness harness = schedule(List.of(new PluginStartup.Step("data", work)), ready, error -> fail(error));
        harness.startup().cancel();
        harness.tick().run();
        verifyNoInteractions(work, ready);
        verify(harness.task()).cancel();
    }

    @Test void failureStopsLaterPhasesAndDoesNotReportReady() {
        List<RuntimeException> errors = new ArrayList<>();
        RuntimeException failure = new IllegalStateException("broken configuration");
        Runnable later = mock(Runnable.class);
        Runnable ready = mock(Runnable.class);
        Harness harness = schedule(List.of(
                new PluginStartup.Step("config", () -> { throw failure; }),
                new PluginStartup.Step("data", later)), ready, errors::add);
        harness.tick().run();
        harness.tick().run();
        assertEquals(List.of(failure), errors);
        verifyNoInteractions(later, ready);
        verify(harness.task()).cancel();
    }
}
