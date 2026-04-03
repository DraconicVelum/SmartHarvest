package com.draconicvelum.SmartHarvest.manager;

import com.draconicvelum.SmartHarvest.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class XPManager {

    public static void handleXP(Player player, ItemStack tool, Location loc, Material crop, int blocks) {

        if (!ConfigManager.isXPEnabled()) return;
        if (player == null) return;
        if (blocks <= 0) return;

        String toolKey = ToolManager.getToolKey(tool);

        double baseXP = ConfigManager.getBaseXP();
        double multiplier = ConfigManager.getXPMultiplier(toolKey);
        multiplier *= ConfigManager.getAuraXPMultiplier();

        int xp = (int) Math.round(baseXP * multiplier * blocks);

        if (xp <= 0) return;

        if (ConfigManager.useXPOrbs() && loc != null && loc.getWorld() != null) {
            ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
            orb.setExperience(xp);
        } else {
            player.giveExp(xp);
        }

        // AuraSkills XP
        double auraXP = AuraSkillsConfigReader.getXP(crop);

        if (auraXP <= 0) {
            auraXP = xp;
        } else {
            auraXP *= blocks;
        }

        AuraSkillsHook.addFarmingXP(player, auraXP);
    }

    public static void handleXP(Player player, ItemStack tool, Location loc, Material crop) {
        handleXP(player, tool, loc, crop, 1);
    }
}