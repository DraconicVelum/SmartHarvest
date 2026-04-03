package com.draconicvelum.SmartHarvest.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AuraSkillsConfigReader {

    private static final Map<Material, Double> cropXP = new HashMap<>();

    public static void load() {
        cropXP.clear();

        try {
            File file = new File("plugins/AuraSkills/sources/farming.yml");
            if (!file.exists()) return;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            var section = config.getConfigurationSection("sources");
            if (section == null) return;

            for (String key : section.getKeys(false)) {

                var source = section.getConfigurationSection(key);
                if (source == null) continue;

                double xp = source.getDouble("xp", 0);

                // Single block
                if (source.contains("block")) {
                    String blockName = source.getString("block");

                    if (blockName != null && !blockName.isEmpty()) {
                        try {
                            Material mat = Material.valueOf(blockName.toUpperCase());
                            cropXP.put(mat, xp);
                        } catch (IllegalArgumentException ignored) {
                            System.out.println("[SmartHarvest] Invalid material: " + blockName);
                        }
                    }
                }

                // Multiple blocks
                if (source.contains("blocks")) {
                    for (String blockName : source.getStringList("blocks")) {

                        if (blockName == null || blockName.isEmpty()) continue;

                        try {
                            Material mat = Material.valueOf(blockName.toUpperCase());
                            cropXP.put(mat, xp);
                        } catch (IllegalArgumentException ignored) {
                            System.out.println("[SmartHarvest] Invalid material: " + blockName);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[SmartHarvest] Failed to load AuraSkills farming config.");
        }
    }

    public static double getXP(Material material) {
        return cropXP.getOrDefault(material, 0.0);
    }
}