package com.draconicvelum.SmartHarvest.manager;

import com.draconicvelum.SmartHarvest.config.ConfigManager;
import com.draconicvelum.SmartHarvest.util.CropUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class CropManager {

    public static boolean isHarvestable(Material mat) {
        return ConfigManager.getCrops().contains(mat);
    }

    public static boolean isFullyGrown(Block block) {
        if (!(block.getBlockData() instanceof Ageable age)) return false;
        return age.getAge() == age.getMaximumAge();
    }

    public static void harvest(Block block, Player player, ItemStack tool) {
        Material type = block.getType();
        var loc = block.getLocation();
        var world = block.getWorld();

        // Get drops BEFORE changing block
        Collection<ItemStack> drops = block.getDrops(tool);

        // Apply multiplier
        if (ToolManager.isHoe(tool)) {
            drops = ToolManager.applyDropMultiplier(drops, player, tool);
        }

        // Adjust drops to prevent duplication (before breaking block)
        if (ConfigManager.shouldReplant(type)) {
            adjustForReplant(drops, type);
        }

        // Remove crop
        block.setType(Material.AIR);

        // Drop items naturally
        for (ItemStack drop : drops) {
            if (drop.getAmount() > 0) { // avoid dropping empty stacks
                world.dropItemNaturally(loc, drop);
            }
        }

        // Replant if applicable
        if (ConfigManager.shouldReplant(type)) {
            replant(block, type);
        }

        // Handle XP
        XPManager.handleXP(player, tool, loc, type);

        // Damage tool
        if (ToolManager.isHoe(tool)) {
            ToolManager.damageTool(player, tool);
        }
    }

    private static void replant(Block block, Material type) {
        block.setType(type);

        BlockData data = block.getBlockData();

        if (data instanceof Ageable age) {
            age.setAge(0);
            block.setBlockData(age);
        }
    }

    // Prevent seed duplication
    private static void adjustForReplant(Collection<ItemStack> drops, Material cropType) {

        Material seed = CropUtils.getSeed(cropType);
        if (seed == null) return;

        for (ItemStack drop : drops) {
            if (drop.getType() == seed) {
                drop.setAmount(Math.max(0, drop.getAmount() - 1));
                break;
            }
        }
    }

    public static boolean isInstantBreakCrop(Material mat) {
        return mat == Material.MELON
                || mat == Material.PUMPKIN;
    }

    public static boolean isVerticalCrop(Material mat) {
        return mat == Material.SUGAR_CANE
                || mat == Material.CACTUS
                || mat == Material.BAMBOO;
    }
}