package com.draconicvelum.SmartHarvest;

import com.draconicvelum.SmartHarvest.listener.BlockPlaceListener;
import com.draconicvelum.SmartHarvest.listener.SHListener;
import com.draconicvelum.SmartHarvest.config.ConfigManager;
import com.draconicvelum.SmartHarvest.manager.AuraSkillsConfigReader;
import com.draconicvelum.SmartHarvest.manager.CoreProtectHook;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class SmartHarvestPlugin extends JavaPlugin {

    private static SmartHarvestPlugin instance;
    public static NamespacedKey PLACED_KEY;

    @Override
    public void onEnable() {
        instance = this;
        AuraSkillsConfigReader.load();
        // Load config
        saveDefaultConfig();
        ConfigManager.load(this);
        PLACED_KEY = new NamespacedKey(this, "placed_crop");

        // Register events
        getServer().getPluginManager().registerEvents(new SHListener(), this);

        getLogger().info("SmartHarvest enabled successfully!");

        if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
            getLogger().info("AuraSkills detected and hooked.");
        } else {
            getLogger().info("AuraSkills not found, running without it.");
        }

        CoreProtectHook.init();

        if (CoreProtectHook.isEnabled()) {
            getLogger().info("CoreProtect hooked successfully!");
        } else {
            getLogger().warning("CoreProtect not found, using metadata fallback.");

            getServer().getPluginManager().registerEvents(
                    new BlockPlaceListener(),
                    this
            );
        }

    }


    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("SmartHarvest disabled.");
    }

    public static SmartHarvestPlugin getInstance() {
        return instance;
    }

    public void reloadPlugin() {
        reloadConfig();
        ConfigManager.load(this);
        AuraSkillsConfigReader.load();
        getLogger().info("SmartHarvest config reloaded.");
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender,
                             org.bukkit.command.Command command,
                             String label,
                             String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("smartharvest.reload")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }

            reloadPlugin();
            sender.sendMessage("§aSmartHarvest reloaded!");
            return true;
        }

        sender.sendMessage("§eUsage: /" + label + " reload");
        return true;
    }
}