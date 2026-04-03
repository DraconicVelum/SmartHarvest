package com.draconicvelum.SmartHarvest.listener;

import com.draconicvelum.SmartHarvest.manager.CoreProtectHook;
import com.draconicvelum.SmartHarvest.manager.CropManager;
import com.draconicvelum.SmartHarvest.manager.ToolManager;
import com.draconicvelum.SmartHarvest.manager.XPManager;

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

import java.util.Collection;

public class SHListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Prevent off-hand triggering twice
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        Material type = block.getType();

        // Cocoa (special handling)
        if (type == Material.COCOA) {

            if (!(block.getBlockData() instanceof Cocoa cocoa)) return;

            if (cocoa.getAge() != cocoa.getMaximumAge()) return;

            event.setCancelled(true);

            // Save facing BEFORE breaking
            var facing = cocoa.getFacing();

            var loc = dropWithMultiplier(block, player, tool);

            // Replant cocoa
            block.setType(Material.COCOA);

            if (block.getBlockData() instanceof Cocoa newCocoa) {
                newCocoa.setFacing(facing);
                newCocoa.setAge(0);
                block.setBlockData(newCocoa);
            }

            XPManager.handleXP(player, tool, loc, type);

            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }

            return;
        }

        // Pumpkin & Melon
        if (CropManager.isInstantBreakCrop(type)) {
            event.setCancelled(true);

            var loc = dropWithMultiplier(block, player, tool);

            XPManager.handleXP(player, tool, loc, type);
            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }
            return;
        }

        // Sugar cane, Cactus & Bamboo
        if (CropManager.isVerticalCrop(type)) {
            event.setCancelled(true);

            breakVerticalCrop(block, player, tool);
            if (ToolManager.isHoe(tool)) {
                ToolManager.damageTool(player, tool);
            }
            return;
        }

        // Normal crops
        if (!CropManager.isHarvestable(type)) return;

        if (!CropManager.isFullyGrown(block)) return;

        event.setCancelled(true);

        CropManager.harvest(block, player, tool);
    }

    private org.bukkit.Location dropWithMultiplier(Block block, Player player, ItemStack tool) {

        var loc = block.getLocation();
        var world = block.getWorld();

        Collection<ItemStack> drops = block.getDrops(tool);
        if (ToolManager.isHoe(tool)) {
            drops = ToolManager.applyDropMultiplier(drops, player, tool);
        }

        block.setType(Material.AIR);

        for (ItemStack drop : drops) {
            world.dropItemNaturally(loc, drop);
        }

        return loc;
    }

    private void breakVerticalCrop(Block block, Player player, ItemStack tool) {

        var world = block.getWorld();
        Material type = block.getType();
        int naturalBlocks = 0;
        org.bukkit.Location lastLoc;

        Block current = block;

        while (true) {

            var loc = current.getLocation();
            lastLoc = loc;

            if (!CoreProtectHook.isBlockPlaced(current)) {
                naturalBlocks++;
            }

            Collection<ItemStack> drops = current.getDrops(tool);

            current.setType(Material.AIR);

            for (ItemStack drop : drops) {
                world.dropItemNaturally(loc, drop);
            }

            Block above = current.getRelative(0, 1, 0);

            if (above.getType() != type) break;

            current = above;
        }
        // XP only for natural blocks
        if (naturalBlocks > 0) {
            XPManager.handleXP(player, tool, lastLoc, type, naturalBlocks);
        }
    }
}