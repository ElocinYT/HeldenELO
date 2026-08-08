package me.developer.minecrafthelden.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import me.developer.minecrafthelden.Minecraft_Helden;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LivesManager {

    private static final File livesFile = new File(Minecraft_Helden.getInstance().getDataFolder(), "Lives.yml");
    private static final YamlConfiguration livesConfig = YamlConfiguration.loadConfiguration(livesFile);

    static {
        if (!livesFile.exists()) {
            try {
                livesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        return livesConfig.contains("Players." + playerUUID);
    }

    public static int getLives(Player player) {
        UUID playerUUID = player.getUniqueId();
        return livesConfig.getInt("Players." + playerUUID + ".Lives", 0);
    }

    public static void setLives(Player player, int lives) {
        UUID playerUUID = player.getUniqueId();
        livesConfig.set("Players." + playerUUID + ".Lives", lives);
        saveConfig();
    }

    private static void saveConfig() {
        try {
            livesConfig.save(livesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadData() {
        try {
            livesConfig.load(livesFile);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load Lives.yml:");
            e.printStackTrace();
        }
    }
}
