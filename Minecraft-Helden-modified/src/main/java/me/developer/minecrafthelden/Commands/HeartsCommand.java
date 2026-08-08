package me.developer.minecrafthelden.Commands;

import me.developer.minecrafthelden.Minecraft_Helden;
import me.developer.minecrafthelden.Utils.ColorUtils;
import me.developer.minecrafthelden.Utils.LivesManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HeartsCommand implements CommandExecutor {

    private final Minecraft_Helden plugin;

    public HeartsCommand(Minecraft_Helden plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed by a player or the console.");
            return true;
        }
        Player player = (Player) sender;
        player.sendMessage(ColorUtils.translateColors(
                plugin.getConfig().getString("commands.Lives.CheckLives")
                        .replace("%prefix%", Minecraft_Helden.getInstance().prefix)
                        .replace("%hearts%", String.valueOf(LivesManager.getLives(player)))));
        return true;
    }
}
