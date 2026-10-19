package me.dexuby.UltimateDrugs.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

public class Constants {
   public static final Set<Material> AUTO_CONNECT_MATERIALS;
   public static final Set<String> POSITIVE_EFFECT_TYPES;
   public static final Set<String> NEGATIVE_EFFECT_TYPES;

   static {
      AUTO_CONNECT_MATERIALS = new HashSet(Arrays.asList(Material.COCOA, Material.ATTACHED_PUMPKIN_STEM, Material.ATTACHED_MELON_STEM));
      POSITIVE_EFFECT_TYPES = new HashSet(Arrays.asList("ABSORPTION", "CONDUIT_POWER", "DAMAGE_RESISTANCE", "DOLPHINS_GRACE", "FAST_DIGGING", "FIRE_RESISTANCE", "HEALTH_BOOST", "INCREASE_DAMAGE", "INVISIBILITY", "JUMP", "LUCK", "NIGHT_VISION", "REGENERATION", "SATURATION", "SLOW_FALLING", "SPEED", "WATER_BREATHING"));
      NEGATIVE_EFFECT_TYPES = new HashSet(Arrays.asList("BLINDNESS", "CONFUSION", "GLOWING", "HUNGER", "LEVITATION", "POISON", "SLOW", "SLOW_DIGGING", "UNLUCK", "WEAKNESS", "WITHER"));
   }
}
