package com.draconicvelum.SmartHarvest.listener;

import com.draconicvelum.SmartHarvest.SmartHarvestPlugin;
import com.draconicvelum.SmartHarvest.manager.CoreProtectHook;
import com.draconicvelum.SmartHarvest.manager.CropManager;
import com.draconicvelum.SmartHarvest.manager.ToolManager;
import com.draconicvelum.SmartHarvest.manager.XPManager;
import com.draconicvelum.SmartHarvest.util.SchedulerUtil;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SHListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        Material type = block.getType();

        if (!isAllowedHarvestTool(tool)) return;

        // =========================
        // COCOA
        // =========================
        if (type == Material.COCOA) {

            if (!(block.getBlockData() instanceof Cocoa cocoa)) return;
            if (cocoa.getAge() != cocoa.getMaximumAge()) return;

            event.setCancelled(true);

            final var facing = cocoa.getFacing();
            final var loc = block.getLocation();
            final Block b = block;

            Collection<ItemStack> drops = new ArrayList<>(b.getDrops(tool));
            if (ToolManager.isHoe(tool)) {
                drops = ToolManager.applyDropMultiplier(drops, player, tool);
            }

            final Collection<ItemStack> finalDrops = drops;

            SchedulerUtil.runAtLocation(
                    SmartHarvestPlugin.getInstance(),
                    loc,
                    () -> {
                        b.setType(Material.AIR);

                        for (ItemStack drop : finalDrops) {
                            b.getWorld().dropItemNaturally(loc, drop);
                        }

                        b.setType(Material.COCOA);

                        if (b.getBlockData() instanceof Cocoa newCocoa) {
                            newCocoa.setFacing(facing);
                            newCocoa.setAge(0);
                            b.setBlockData(newCocoa);
                        }
                    }
            );

            XPManager.handleXP(player, tool, loc, type);

            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }
            return;
        }

        // =========================
        // MELON / PUMPKIN
        // =========================
        if (CropManager.isInstantBreakCrop(type)) {
            if (CoreProtectHook.isBlockPlaced(block)) return;

            event.setCancelled(true);

            final var loc = block.getLocation();
            final Block b = block;

            Collection<ItemStack> drops = new ArrayList<>(b.getDrops(tool));

            if (ToolManager.isHoe(tool)) {
                drops = ToolManager.applyDropMultiplier(drops, player, tool);
            }

            final Collection<ItemStack> finalDrops = drops;

            SchedulerUtil.runAtLocation(
                    SmartHarvestPlugin.getInstance(),
                    loc,
                    () -> {
                        b.setType(Material.AIR);

                        for (ItemStack drop : finalDrops) {
                            b.getWorld().dropItemNaturally(loc, drop);
                        }
                    }
            );

            XPManager.handleXP(player, tool, loc, type);

            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }
            return;
        }

        // =========================
        // VERTICAL CROPS (FIXED)
        // =========================
        if (CropManager.isVerticalCrop(type)) {
            event.setCancelled(true);

            breakVerticalCropSafe(block, player, tool);

            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }
            return;
        }

        // =========================
        // NORMAL CROPS
        // =========================
        if (!CropManager.isHarvestable(type)) return;
        if (!CropManager.isFullyGrown(block)) return;

        event.setCancelled(true);
        CropManager.harvest(block, player, tool);
    }

    private boolean isAllowedHarvestTool(ItemStack tool) {
        return tool == null || tool.getType().isAir() || ToolManager.isHoe(tool);
    }

    private void breakVerticalCropSafe(Block start, Player player, ItemStack tool) {

        Material type = start.getType();

        List<Block> blocks = new ArrayList<>();
        Block current = start;

        // READ ONLY scan
        while (true) {
            blocks.add(current);

            Block above = current.getRelative(0, 1, 0);
            if (above.getType() != type) break;

            current = above;
        }

        int naturalBlocks = 0;

        for (Block block : blocks) {

            final Block b = block;
            final var loc = b.getLocation();

            boolean isNatural = !CoreProtectHook.isBlockPlaced(b);
            if (isNatural) naturalBlocks++;

            final Collection<ItemStack> drops = new ArrayList<>(b.getDrops(tool));

            SchedulerUtil.runAtLocation(
                    SmartHarvestPlugin.getInstance(),
                    loc,
                    () -> {
                        b.setType(Material.AIR);

                        for (ItemStack drop : drops) {
                            b.getWorld().dropItemNaturally(loc, drop);
                        }
                    }
            );
        }

        if (naturalBlocks > 0) {
            XPManager.handleXP(player, tool, start.getLocation(), type, naturalBlocks);
        }
    }
}
