package com.draconicvelum.SmartHarvest.util;

import org.bukkit.Material;

public class CropUtils {

    public static Material getSeed(Material crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}