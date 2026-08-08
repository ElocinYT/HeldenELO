package me.developer.minecrafthelden.Events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.developer.minecrafthelden.Minecraft_Helden;
import me.developer.minecrafthelden.Utils.ColorUtils;
import me.developer.minecrafthelden.Utils.LivesManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * GEAENDERT gegenueber dem Original: statt einer BossBar (die oben mittig
 * am Bildschirmrand haengt) wird jetzt die Action Bar genutzt - das ist
 * die Textzeile direkt UEBER der Hotbar (dort wo z.B. "Diamantschwert"
 * aufploppt, wenn man ein Item in die Hand nimmt).
 *
 * Da Action-Bar-Texte nach ein paar Sekunden von selbst verblassen, wird
 * hier ein wiederkehrender Task pro Spieler gestartet, der die Anzeige
 * regelmaessig auffrischt, damit sie dauerhaft sichtbar bleibt.
 *
 * Alles andere (Kommandos, Konfiguration, Ban-Logik, Nachrichten) ist
 * 1:1 wie im Original.
 */
public class LivesListener implements CommandExecutor, Listener {

    private final Minecraft_Helden plugin;
    private final Set<UUID> activePlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    public LivesListener(Minecraft_Helden plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        int amount;
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed by a player or the console.");
            return true;
        }
        String prefix = Minecraft_Helden.getInstance().prefix;
        if (sender instanceof Player player) {
            String set_permission = plugin.getConfig().getString("commands.Hearts.Set-PERMISSIONS");
            String add_permission = plugin.getConfig().getString("commands.Hearts.Add-PERMISSIONS");
            String remove_permission = plugin.getConfig().getString("commands.Hearts.Remove-PERMISSIONS");
            String no_permission = plugin.getConfig().getString("commands.Hearts.NO-PERMISSIONS");
            if (!(player.hasPermission(set_permission) && player.hasPermission(add_permission) && player.hasPermission(remove_permission))) {
                player.sendMessage(ColorUtils.translateColors(no_permission.replace("%prefix%", prefix)));
                return true;
            }
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Arguments").replace("%prefix%", prefix)));
            return true;
        }
        String action = args[0];
        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Player-Offline").replace("%player%", targetPlayerName).replace("%prefix%", prefix)));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Arguments").replace("%prefix%", prefix)));
            return true;
        }
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Number").replace("%prefix%", prefix)));
            return true;
        }
        int playerLives = LivesManager.getLives(targetPlayer);
        int maxLives = plugin.getConfig().getInt("commands.Hearts.Max_Lives");
        switch (action.toLowerCase()) {
            case "set" -> {
                if (!sender.hasPermission(plugin.getConfig().getString("commands.Hearts.Set-PERMISSIONS"))) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.NO-PERMISSIONS").replace("%prefix%", prefix)));
                    return true;
                }
                if (amount < 0 || amount > maxLives) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Number").replace("%prefix%", prefix)));
                    return true;
                }
                LivesManager.setLives(targetPlayer, amount);
                updateActionBar(targetPlayer);
                sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Set-Amount").replace("%hearts%", String.valueOf(amount)).replace("%prefix%", prefix).replace("%target%", targetPlayerName)));
            }
            case "add" -> {
                if (!sender.hasPermission(plugin.getConfig().getString("commands.Hearts.Add-PERMISSIONS"))) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.NO-PERMISSIONS").replace("%prefix%", prefix)));
                    return true;
                }
                if (amount < 0 || playerLives + amount > maxLives) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Number").replace("%prefix%", prefix)));
                    return true;
                }
                LivesManager.setLives(targetPlayer, playerLives + amount);
                updateActionBar(targetPlayer);
                sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Add-Amount").replace("%hearts%", String.valueOf(amount)).replace("%prefix%", prefix).replace("%target%", targetPlayerName)));
            }
            case "remove" -> {
                if (!sender.hasPermission(plugin.getConfig().getString("commands.Hearts.Remove-PERMISSIONS"))) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.NO-PERMISSIONS").replace("%prefix%", prefix)));
                    return true;
                }
                if (amount < 0 || playerLives - amount < 0) {
                    sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Number").replace("%prefix%", prefix)));
                    return true;
                }
                LivesManager.setLives(targetPlayer, playerLives - amount);
                updateActionBar(targetPlayer);
                sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Remove-Amount").replace("%hearts%", String.valueOf(amount)).replace("%prefix%", prefix).replace("%target%", targetPlayerName)));
            }
            default -> sender.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("commands.Hearts.Invalid-Arguments").replace("%prefix%", prefix)));
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int defaultLives = plugin.getConfig().getInt("Lives.Default_Lives");
        if (!LivesManager.hasPlayer(player)) {
            LivesManager.setLives(player, defaultLives);
        }
        if (LivesManager.getLives(player) <= 0) {
            player.kickPlayer(getBanMessage());
            return;
        }

        activePlayers.add(player.getUniqueId());
        // Alle 40 Ticks (2 Sekunden) auffrischen, damit die Action Bar
        // nicht verblasst - Action-Bar-Text haelt normalerweise nur ~3s.
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.isOnline()) {
                sendActionBar(player);
            }
        }, 0L, 40L);
        actionBarTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        activePlayers.remove(player.getUniqueId());
        BukkitTask task = actionBarTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();
        int playerLives = LivesManager.getLives(victim);
        if (killer != null && !killer.equals(victim)) {
            LivesManager.setLives(victim, playerLives - 1);
            updateActionBar(victim);
            victim.sendMessage(ColorUtils.translateColors(plugin.getConfig().getString("Lives.Lose_Lives_Message").replace("%lives%", String.valueOf(playerLives - 1)).replace("%prefix%", Minecraft_Helden.getInstance().prefix)));
            if (playerLives - 1 <= 0) {
                victim.kickPlayer(getBanMessage());
                Bukkit.getBanList(BanList.Type.NAME).addBan(victim.getName(), getBanMessage(), null, null);
            }
        }
    }

    public void updateActionBar(Player player) {
        LivesManager.loadData();
        if (activePlayers.contains(player.getUniqueId())) {
            sendActionBar(player);
        }
    }

    private void sendActionBar(Player player) {
        int lives = LivesManager.getLives(player);
        String text = lives >= 3 ? "&b&l\u2764\u2764\u2764" : (lives == 2 ? "&b&l\u2764\u2764&7&l\u2764" : "&b&l\u2764&7&l\u2764\u2764");
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ColorUtils.translateColors(text))
        );
    }

    private String getBanMessage() {
        List<String> banMessages = plugin.getConfig().getStringList("Lives.Banned_Reason");
        return ColorUtils.translateColors(String.join("\n", banMessages));
    }
}
