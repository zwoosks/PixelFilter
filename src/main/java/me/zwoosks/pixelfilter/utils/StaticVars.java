package me.zwoosks.pixelfilter.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StaticVars {

    private static Player lastOnMapInteract = null;
    private static List<UUID> noDrop = new ArrayList<>();
    private static HashMap<String, Integer> playerMapOnProcess= new HashMap<String, Integer>();

    public static void setPlayerMapOnProcess(Player player, int mapID) { playerMapOnProcess.put(player.toString().toLowerCase(), mapID); }
    public static void removePlayerMapOnProcess(Player player) { playerMapOnProcess.remove(player.toString().toLowerCase()); }
    public static int getPlayerMapOnProcess(Player player) { return playerMapOnProcess.get(player.toString().toLowerCase()); }

    public static void setLastOnMapInteract(Player player) {
        lastOnMapInteract = player;
    }
    public static Player getLastOnMapInteract() {
        return lastOnMapInteract;
    }

    public static void addNoDrop(UUID uuid) { noDrop.add(uuid); }
    public static void removeNoDrop(UUID uuid) { noDrop.remove(uuid); }
    public static boolean cannotDrop(UUID uuid) { return noDrop.contains(uuid); }

}