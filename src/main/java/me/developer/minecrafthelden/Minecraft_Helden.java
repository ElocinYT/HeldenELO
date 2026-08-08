package me.developer.minecrafthelden;

import me.developer.minecrafthelden.Commands.HeartsCommand;
import me.developer.minecrafthelden.Commands.ReloadConfigCommand;
import me.developer.minecrafthelden.Events.LivesListener;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Minecraft_Helden extends JavaPlugin {

    private static Minecraft_Helden instance;
    public String prefix = this.getConfig().getString("prefix");

    @Override
    public void onEnable() {
        this.getLogger().info("Plugin has been Enabled.");
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        this.reloadConfig();
        this.registerCMDS();
        this.registerEvents();
        instance = this;
    }

    private void registerCMDS() {
        this.getCommand("lives").setExecutor(new HeartsCommand(this));
        this.getCommand("mh").setExecutor(new ReloadConfigCommand(this));
        this.getCommand("mh").setTabCompleter(new ReloadConfigCommand(this));
        this.getCommand("hearts").setExecutor(new LivesListener(this));
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new LivesListener(this), this);
    }

    public static Minecraft_Helden getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Plugin has been Disabled.");
        this.saveConfig();
    }
}
