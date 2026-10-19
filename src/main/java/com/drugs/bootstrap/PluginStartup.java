package com.drugs.bootstrap;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Consumer;

/** Keeps enable callbacks short and runs one ordered initialization phase per tick. */
public final class PluginStartup {
    public record Step(String name, Runnable action, java.util.function.Supplier<List<Step>> expansion) {
        public Step(String name, Runnable action) { this(name, action, null); }
        public static Step expand(String name, java.util.function.Supplier<List<Step>> expansion) { return new Step(name, () -> {}, expansion); }
    }

    private final Plugin plugin;
    private final java.util.Deque<Step> steps;
    private final Runnable ready;
    private final Consumer<RuntimeException> failed;
    private BukkitTask task;
    private boolean stopped;

    public PluginStartup(Plugin plugin, List<Step> steps, Runnable ready, Consumer<RuntimeException> failed) {
        this.plugin = plugin;
        this.steps = new java.util.ArrayDeque<>(steps);
        this.ready = ready;
        this.failed = failed;
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    private void tick() {
        if (stopped) return;
        if (!steps.isEmpty()) {
            Step step = steps.removeFirst();
            plugin.getLogger().info("Startup phase: " + step.name());
            long started = System.nanoTime();
            try {
                if (step.expansion() != null) {
                    List<Step> expanded = step.expansion().get();
                    for (int index = expanded.size() - 1; index >= 0; index--) steps.addFirst(expanded.get(index));
                } else step.action().run();
            } catch (RuntimeException exception) {
                cancel();
                plugin.getLogger().severe("Startup failed during " + step.name());
                failed.accept(exception);
                return;
            } finally {
                reportDuration(plugin, step.name(), started);
            }
        }
        if (steps.isEmpty()) {
            cancel();
            ready.run();
        }
    }

    public void cancel() {
        stopped = true;
        if (task != null) task.cancel();
    }

    public static void reportDuration(Plugin plugin, String phase, long started) {
        long millis = (System.nanoTime() - started) / 1_000_000;
        if (millis >= 50) plugin.getLogger().warning(phase + " blocked the server thread for " + millis + " ms");
    }
}
