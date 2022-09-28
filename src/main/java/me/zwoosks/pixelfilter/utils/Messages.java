package me.zwoosks.pixelfilter.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Messages {

    public static String colorizer(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}