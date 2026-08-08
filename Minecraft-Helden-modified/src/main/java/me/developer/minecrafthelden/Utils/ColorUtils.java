package me.developer.minecrafthelden.Utils;

import org.bukkit.ChatColor;

public class ColorUtils {
    public static String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
