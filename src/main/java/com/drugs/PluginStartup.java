package com.drugs;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/** Keeps enable callbacks short and runs one ordered initialization phase per tick. */
final class PluginStartup {
    record Step(String name, Runnable action) {}

    private final Plugin plugin;
    private final Iterator<Step> steps;
    private final Runnable ready;
    private final Consumer<RuntimeException> failed;
    private BukkitTask task;
    private boolean stopped;

    PluginStartup(Plugin plugin, List<Step> steps, Runnable ready, Consumer<RuntimeException> failed) {
        this.plugin = plugin;
        this.steps = steps.iterator();
        this.ready = ready;
        this.failed = failed;
    }

    void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    private void tick() {
        if (stopped) return;
        if (steps.hasNext()) {
            Step step = steps.next();
            plugin.getLogger().info("Startup phase: " + step.name());
            long started = System.nanoTime();
            try {
                step.action().run();
            } catch (RuntimeException exception) {
                cancel();
                plugin.getLogger().severe("Startup failed during " + step.name());
                failed.accept(exception);
                return;
            } finally {
                reportDuration(plugin, step.name(), started);
            }
        }
        if (!steps.hasNext()) {
            cancel();
            ready.run();
        }
    }

    void cancel() {
        stopped = true;
        if (task != null) task.cancel();
    }

    static void reportDuration(Plugin plugin, String phase, long started) {
        long millis = (System.nanoTime() - started) / 1_000_000;
        if (millis >= 50) plugin.getLogger().warning(phase + " blocked the server thread for " + millis + " ms");
    }
}
