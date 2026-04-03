package com.draconicvelum.SmartHarvest.manager;

import com.draconicvelum.SmartHarvest.SmartHarvestPlugin;
import com.draconicvelum.SmartHarvest.config.ConfigManager;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AuraSkillsHook {

    // Dynamic check instead of static final
    private static boolean isDisabled() {
        return !Bukkit.getPluginManager().isPluginEnabled("AuraSkills")
                || !ConfigManager.isAuraEnabled();
    }

    public static void addFarmingXP(Player player, double amount) {
        if (isDisabled()) return;
        if (player == null) return;

        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());

            if (user != null) {
                user.addSkillXp(Skills.FARMING, amount);
            }

        } catch (Exception e) {
            SmartHarvestPlugin.getInstance().getLogger().warning("AuraSkills XP failed.");
        }
    }

    public static int getFarmingLevel(Player player) {
        if (isDisabled()) return 0;
        if (player == null) return 0;

        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());

            if (user != null) {
                return user.getSkillLevel(Skills.FARMING);
            }

        } catch (Exception ignored) {}

        return 0;
    }
}