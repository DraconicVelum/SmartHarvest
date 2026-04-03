package com.draconicvelum.SmartHarvest.manager;

import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CoreProtectHook {

    private static CoreProtectAPI api;

    public static void init() {

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");

        if (plugin == null || !plugin.isEnabled()) {
            return;
        }

        try {
            Class<?> coreProtectClass = Class.forName("net.coreprotect.CoreProtect");

            if (!coreProtectClass.isInstance(plugin)) return;

            CoreProtectAPI api = ((net.coreprotect.CoreProtect) plugin).getAPI();

            if (api != null && api.isEnabled()) {
                CoreProtectHook.api = api;
            }

        } catch (Throwable ignored) {
            CoreProtectHook.api = null;
        }
    }

    public static boolean isEnabled() {
        return api != null;
    }

    public static boolean wasPlaced(Block block) {
        if (api == null) return false;

        try {
            List<String[]> data = api.blockLookup(block, 1);

            if (data == null || data.isEmpty()) return false;

            String[] entry = data.get(0);

            int action = Integer.parseInt(entry[0]);

            // 1 = placed, 0 = removed
            return action == 1;

        } catch (Exception ignored) {
            return false;
        }
    }
    public static boolean isBlockPlaced(Block block) {

        // Use CoreProtect if available
        if (isEnabled()) {
            return wasPlaced(block);
        }

        // Fallback to metadata
        return block.hasMetadata("placed_crop");
    }
}