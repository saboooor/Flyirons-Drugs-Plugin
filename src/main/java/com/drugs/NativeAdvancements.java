package com.drugs;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** Uses Minecraft's advancement registry, UI, notifications and player persistence. */
public final class NativeAdvancements implements Listener {
    private static final NamespacedKey ROOT = key("root");
    private static final Set<NamespacedKey> registered = new LinkedHashSet<>();

    private static NamespacedKey key(String id) {
        return new NamespacedKey("flyironsdrugs", "achievements/" + id);
    }

    @SuppressWarnings("deprecation")
    public static void reload() {
        if (!AchievementSettingsLoader.isEnabled()) return;
        Map<UUID, Map<String, Collection<String>>> completed = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<String, Collection<String>> ids = new HashMap<>();
            for (NamespacedKey key : registered) {
                Advancement advancement = Bukkit.getAdvancement(key);
                if (advancement != null) {
                    ids.put(key.getKey().substring("achievements/".length()),
                            new ArrayList<>(player.getAdvancementProgress(advancement).getAwardedCriteria()));
                }
            }
            completed.put(player.getUniqueId(), ids);
        }
        List<NamespacedKey> old = new ArrayList<>(registered);
        Collections.reverse(old);
        for (NamespacedKey key : old) Bukkit.getUnsafe().removeAdvancement(key);
        registered.clear();
        register(ROOT, definition("Drugs", "Drug milestones", Material.BREWING_STAND, true));
        for (CustomAchievement achievement : CustomAchievementLoader.getAllAchievements()) {
            if (achievement.isEnabled()) {
                try {
                    JsonObject json = definition(achievement.getTitle(),
                            achievement.getDescription(), achievement.getCompletedIcon(), false);
                    if (achievement.getTrigger().equals("all_drugs_used") && !DrugRegistry.getRegisteredDrugNames().isEmpty()) {
                        JsonObject criteria = new JsonObject();
                        for (String drug : DrugRegistry.getRegisteredDrugNames()) {
                            JsonObject criterion = new JsonObject();
                            criterion.addProperty("trigger", "minecraft:impossible");
                            criteria.add("used_" + drug, criterion);
                        }
                        json.add("criteria", criteria);
                    }
                    register(key(achievement.getId()), json);
                } catch (IllegalArgumentException exception) {
                    DrugsV2.getInstance().getLogger().warning("Cannot register advancement "
                            + achievement.getId() + ": " + exception.getMessage());
                }
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (var entry : completed.get(player.getUniqueId()).entrySet()) {
                Advancement advancement = Bukkit.getAdvancement(key(entry.getKey()));
                if (advancement != null) {
                    for (String criterion : entry.getValue()) {
                        if (advancement.getCriteria().contains(criterion)) {
                            player.getAdvancementProgress(advancement).awardCriteria(criterion);
                        }
                    }
                }
            }
            prepare(player);
        }
    }

    @SuppressWarnings("deprecation")
    private static void register(NamespacedKey key, JsonObject definition) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) advancement = Bukkit.getUnsafe().loadAdvancement(key, definition.toString());
        if (advancement == null) throw new IllegalArgumentException("Server rejected " + key);
        registered.add(key);
    }

    static JsonObject definition(String title, String description, Material icon, boolean root) {
        JsonObject json = new JsonObject();
        if (!root) json.addProperty("parent", ROOT.toString());
        JsonObject display = new JsonObject();
        JsonObject item = new JsonObject();
        item.addProperty("id", icon.getKey().toString());
        display.add("icon", item);
        display.addProperty("title", plain(title));
        display.addProperty("description", plain(description));
        display.addProperty("frame", "task");
        display.addProperty("show_toast", !root && AchievementSettingsLoader.isSoundNotificationsEnabled());
        display.addProperty("announce_to_chat", !root && AchievementSettingsLoader.isChatNotificationsEnabled());
        display.addProperty("hidden", false);
        if (root) display.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/stone.png");
        json.add("display", display);
        JsonObject criterion = new JsonObject();
        criterion.addProperty("trigger", "minecraft:impossible");
        JsonObject criteria = new JsonObject();
        criteria.add("unlock", criterion);
        json.add("criteria", criteria);
        return json;
    }

    private static String plain(String text) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static void award(Player player, String id) {
        Advancement advancement = Bukkit.getAdvancement(key(id));
        if (advancement == null) return;
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) progress.awardCriteria(criterion);
    }

    public static boolean has(Player player, String id) {
        Advancement advancement = Bukkit.getAdvancement(key(id));
        return advancement != null && player.getAdvancementProgress(advancement).isDone();
    }

    public static void trackDrug(Player player, String drug) {
        if (!AchievementSettingsLoader.isEnabled()) return;
        for (CustomAchievement achievement : CustomAchievementLoader.getAchievementsByTrigger("all_drugs_used")) {
            if (!achievement.isEnabled()) continue;
            Advancement advancement = Bukkit.getAdvancement(key(achievement.getId()));
            if (advancement != null && advancement.getCriteria().contains("used_" + drug)) {
                player.getAdvancementProgress(advancement).awardCriteria("used_" + drug);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        prepare(event.getPlayer());
    }

    private static void prepare(Player player) {
        if (!AchievementSettingsLoader.isEnabled()) return;
        award(player, "root");
        File file = new File(DrugsV2.getInstance().getDataFolder(),
                "data/achievements/" + player.getUniqueId() + ".yml");
        if (!file.isFile()) return;
        YamlConfiguration legacy = YamlConfiguration.loadConfiguration(file);
        boolean changed = false;
        for (String drug : DrugRegistry.getRegisteredDrugNames()) {
            if (legacy.getBoolean("connoisseur-used-" + drug)
                    && !legacy.getBoolean("native-migrated.connoisseur-used-" + drug)) {
                trackDrug(player, drug);
                // Keep legacy tracking available if the connoisseur advancement is disabled.
                boolean migrated = CustomAchievementLoader.getAchievementsByTrigger("all_drugs_used").stream()
                        .anyMatch(a -> a.isEnabled() && Bukkit.getAdvancement(key(a.getId())) != null);
                if (migrated) {
                    legacy.set("native-migrated.connoisseur-used-" + drug, true);
                    changed = true;
                }
            }
        }
        for (CustomAchievement achievement : CustomAchievementLoader.getAllAchievements()) {
            String id = achievement.getId();
            if (achievement.isEnabled() && legacy.getBoolean(id) && !legacy.getBoolean("native-migrated." + id)) {
                award(player, id);
                if (has(player, id)) {
                    legacy.set("native-migrated." + id, true);
                    changed = true;
                }
            }
        }
        if (changed) {
            try {
                legacy.save(file);
            } catch (IOException exception) {
                DrugsV2.getInstance().getLogger().warning("Could not save advancement migration: " + exception.getMessage());
            }
        }
    }
}
