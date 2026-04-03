package com.draconicvelum.SmartHarvest.listener;

import com.draconicvelum.SmartHarvest.SmartHarvestPlugin;
import com.draconicvelum.SmartHarvest.manager.CoreProtectHook;
import com.draconicvelum.SmartHarvest.manager.CropManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        // If CoreProtect is active → DO NOTHING
        if (CoreProtectHook.isEnabled()) return;

        Material type = event.getBlock().getType();

        if (!CropManager.isVerticalCrop(type)) return;

        event.getBlock().setMetadata(
                "placed_crop",
                new FixedMetadataValue(SmartHarvestPlugin.getInstance(), true)
        );
    }
}