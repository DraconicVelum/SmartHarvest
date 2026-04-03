package com.draconicvelum.SmartHarvest.manager;

import com.draconicvelum.SmartHarvest.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.GameMode;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ToolManager {

    public static double getMultiplier(ItemStack tool) {
        String key = getToolKey(tool);
        return ConfigManager.getDropMultiplier(key);
    }

    public static Collection<ItemStack> applyDropMultiplier(
            Collection<ItemStack> drops,
            Player player,
            ItemStack tool
    ) {

        String toolKey = getToolKey(tool);

        // Probability check
        double chance = ConfigManager.getProbability(toolKey);
        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return new ArrayList<>(drops);
        }

        double multiplier = getTotalMultiplier(player, tool);

        List<ItemStack> newDrops = new ArrayList<>();

        for (ItemStack drop : drops) {
            ItemStack newDrop = drop.clone();
            int newAmount = (int) Math.round(newDrop.getAmount() * multiplier);
            newDrop.setAmount(Math.max(newAmount, 1));
            newDrops.add(newDrop);
        }

        return newDrops;
    }

    public static void damageTool(Player player, ItemStack tool) {
        if (!ConfigManager.isDurabilityEnabled()) return;
        if (tool == null) return;

        Material mat = tool.getType();
        if (mat.isAir()) return;
        if (!isHoe(mat)) return;
        if (player == null) return;

        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        int unbreaking = tool.getEnchantmentLevel(Enchantment.UNBREAKING);

        if (unbreaking > 0) {
            double chance = 1.0 / (unbreaking + 1);
            if (ThreadLocalRandom.current().nextDouble() > chance) return;
        }

        int newDamage = damageable.getDamage() + 1;

        if (newDamage >= mat.getMaxDurability()) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            return;
        }

        damageable.setDamage(newDamage);
        tool.setItemMeta(damageable);
    }

    private static boolean isHoe(org.bukkit.Material mat) {
        return mat.name().endsWith("_HOE");
    }

    public static boolean isHoe(ItemStack tool) {
        return tool != null && isHoe(tool.getType());
    }

    public static String getToolKey(ItemStack tool) { // 🔥 made public for XPManager
        if (tool == null || tool.getType().isAir()) return "hand";

        Material mat = tool.getType();

        return switch (mat) {
            case WOODEN_HOE -> "wooden_hoe";
            case STONE_HOE -> "stone_hoe";
            case IRON_HOE -> "iron_hoe";
            case DIAMOND_HOE -> "diamond_hoe";
            case GOLDEN_HOE -> "golden_hoe";
            case NETHERITE_HOE -> "netherite_hoe";
            default -> "hand";
        };
    }

    public static double getTotalMultiplier(Player player, ItemStack tool) {

        double toolMultiplier = getMultiplier(tool);
        double auraMultiplier = 1.0;
        double fortuneBonus = 1.0;

        // AuraSkills bonus
        if (ConfigManager.isAuraBonusEnabled() && player != null) {

            int level = AuraSkillsHook.getFarmingLevel(player);

            double perLevel = ConfigManager.getAuraPerLevel();
            double maxBonus = ConfigManager.getAuraMaxBonus();

            auraMultiplier = 1 + (level * perLevel);
            auraMultiplier = Math.min(auraMultiplier, maxBonus);
        }

        // Fortune bonus
        if (ConfigManager.isFortuneEnabled() && tool != null && !tool.getType().isAir()) {

            int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);

            if (fortuneLevel > 0) {
                fortuneBonus = 1 + (fortuneLevel * ConfigManager.getFortunePerLevel());
            }
        }

        return toolMultiplier * auraMultiplier * fortuneBonus;
    }
}