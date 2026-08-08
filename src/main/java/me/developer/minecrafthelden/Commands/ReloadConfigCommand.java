package me.developer.minecrafthelden.Commands;

import java.util.ArrayList;
import java.util.List;
import me.developer.minecrafthelden.Minecraft_Helden;
import me.developer.minecrafthelden.Utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReloadConfigCommand implements CommandExecutor, TabCompleter {

    private final Minecraft_Helden plugin;

    public ReloadConfigCommand(Minecraft_Helden plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        String prefix = Minecraft_Helden.getInstance().prefix;
        String unknownCommandMessage = config.getString("messages.reload.Unknown-Command").replace("%prefix%", prefix);

        if (sender instanceof Player player) {
            String noPermissionMessage = config.getString("messages.reload.NO-PERMISSIONS").replace("%prefix%", prefix);
            if (!player.hasPermission(plugin.getConfig().getString("commands.Minecraft-Helden-help.Permission"))) {
                player.sendMessage(ColorUtils.translateColors(noPermissionMessage));
                return false;
            }
        }

        if (args.length < 1) {
            List<String> usageMessages = config.getStringList("commands.Minecraft-Helden-help.Usage");
            for (String message : usageMessages) {
                sender.sendMessage(ColorUtils.translateColors(message.replace("%prefix%", prefix)));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("reload")) {
            plugin.reloadConfig();
            plugin.saveConfig();
            sender.sendMessage(ColorUtils.translateColors(config.getString("messages.reload.Message").replace("%prefix%", prefix)));
        } else {
            sender.sendMessage(ColorUtils.translateColors(unknownCommandMessage));
        }
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            return filterCompletions(args[0], completions);
        }
        return null;
    }

    private List<String> filterCompletions(String arg, List<String> completions) {
        ArrayList<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(arg.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}
