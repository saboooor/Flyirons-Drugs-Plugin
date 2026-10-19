package com.drugs.achievements;

import org.bukkit.entity.Player;

import java.util.*;

public class AchievementManager {

    /**
     * Grants an achievement to a player
     * 
     * @param player The player to grant the achievement to
     * @param achievementId The ID of the achievement to grant
     */
    public static void grant(Player player, String achievementId) {
        CustomAchievement achievement = CustomAchievementLoader.getAchievement(achievementId);
        if (achievement == null) {
            return; // Achievement doesn't exist
        }
        
        // Skip if achievements are disabled or this achievement is disabled
        if (!AchievementSettingsLoader.isEnabled() || !achievement.isEnabled()) {
            return;
        }
        
        NativeAdvancements.award(player, achievementId);
    }

    /**
     * Checks if a player has an achievement
     * 
     * @param player The player to check
     * @param achievementId The ID of the achievement to check
     * @return true if the player has the achievement, false otherwise
     */
    public static boolean has(Player player, String achievementId) {
        CustomAchievement achievement = CustomAchievementLoader.getAchievement(achievementId);
        if (achievement == null) {
            return false; // Achievement doesn't exist
        }
        
        // If achievements are disabled, act as if player has all achievements
        if (!AchievementSettingsLoader.isEnabled() || !achievement.isEnabled()) {
            return true;
        }
        
        return NativeAdvancements.has(player, achievementId);
    }

    /**
     * Gets all unlocked achievements for a player
     * 
     * @param player The player to check
     * @return A set of achievement IDs that the player has unlocked
     */
    public static Set<String> getUnlocked(Player player) {
        // If achievements are disabled, return empty set
        if (!AchievementSettingsLoader.isEnabled()) {
            return new HashSet<>();
        }
        
        Set<String> unlocked = new HashSet<>();
        for (CustomAchievement achievement : CustomAchievementLoader.getAllAchievements()) {
            if (NativeAdvancements.has(player, achievement.getId())) unlocked.add(achievement.getId());
        }
        return unlocked;
    }
    
    /**
     * Checks if the achievement system is enabled
     */
    public static boolean isEnabled() {
        return AchievementSettingsLoader.isEnabled();
    }
    
    /**
     * Processes an achievement trigger event
     * 
     * @param player The player who triggered the event
     * @param trigger The trigger type
     * @param context Additional context data (optional)
     */
    public static void processTrigger(Player player, String trigger, Map<String, Object> context) {
        if (!isEnabled()) return;
        
        List<CustomAchievement> matchingAchievements = CustomAchievementLoader.getAchievementsByTrigger(trigger);
        
        for (CustomAchievement achievement : matchingAchievements) {
            // Skip if the player already has this achievement
            if (has(player, achievement.getId())) continue;
            
            // Check for specific drug requirements
            if (trigger.equals("use_specific_drug") || trigger.equals("craft_specific")) {
                String drugId = (String) context.getOrDefault("drug_id", "");
                String requiredDrugId = achievement.getId(); // Simplified for now
                
                if (!drugId.equalsIgnoreCase(requiredDrugId)) {
                    continue; // Skip if not the right drug
                }
            }
            
            // Check for count requirements
            if (trigger.equals("use_count")) {
                int count = (int) context.getOrDefault("count", 0);
                int requiredCount = 100; // Default, should be loaded from config
                
                if (count < requiredCount) {
                    continue; // Skip if count not reached
                }
            }
            
            // Grant the achievement
            grant(player, achievement.getId());
        }
    }
    
    /**
     * Helper method to create a context map for trigger processing
     */
    public static Map<String, Object> createContext() {
        return new HashMap<>();
    }
}
