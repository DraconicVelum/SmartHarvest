package com.draconicvelum.SmartHarvest.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigManager {

    private static FileConfiguration config;

    public static void load(JavaPlugin plugin) {
        config = plugin.getConfig();
    }

    // =========================
    // XP
    // =========================

    public static boolean isXPEnabled() {
        return config.getBoolean("xp.xp_drop_enabled", true);
    }

    public static boolean useXPOrbs() {
        return config.getBoolean("xp.use_orbs", true);
    }

    public static double getBaseXP() {
        return config.getDouble("xp.base_xp_amount", 1.0);
    }

    // =========================
    // Durability
    // =========================

    public static boolean isDurabilityEnabled() {
        return config.getBoolean("durability_loss", true);
    }

    // =========================
    // Crops
    // =========================

    public static List<Material> getCrops() {
        return config.getStringList("crop_settings.enabled")
                .stream()
                .map(name -> {
                    try {
                        return Material.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static boolean shouldReplant(Material mat) {
        return getCrops().contains(mat);
    }

    // =========================
    // Tool Multipliers
    // =========================

    public static double getDropMultiplier(String tool) {
        return config.getDouble("drop_multipliers." + tool, 1.0);
    }

    public static double getXPMultiplier(String tool) {
        return config.getDouble("multipliers." + tool, 1.0);
    }

    public static double getProbability(String tool) {
        return config.getDouble("probabilities." + tool, 1.0);
    }

    // =========================
    // AuraSkills
    // =========================

    public static boolean isAuraEnabled() {
        return config.getBoolean("auraskills.enabled", true);
    }

    public static double getAuraXPMultiplier() {
        return config.getDouble("auraskills.xp_multiplier", 1.0);
    }

    public static boolean isAuraBonusEnabled() {
        return config.getBoolean("auraskills.farming_bonus.enabled", true);
    }

    public static double getAuraPerLevel() {
        return config.getDouble("auraskills.farming_bonus.per_level", 0.01);
    }

    public static double getAuraMaxBonus() {
        return config.getDouble("auraskills.farming_bonus.max_bonus", 2.0);
    }

    // =========================
    // Fortune
    // =========================

    public static boolean isFortuneEnabled() {
        return config.getBoolean("fortune.enabled", true);
    }

    public static double getFortunePerLevel() {
        return config.getDouble("fortune.per_level", 0.2);
    }
}