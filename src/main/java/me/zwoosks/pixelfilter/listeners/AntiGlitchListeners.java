package me.zwoosks.pixelfilter.listeners;

import me.zwoosks.pixelfilter.PixelFilter;
import me.zwoosks.pixelfilter.utils.DatabaseManager;
import me.zwoosks.pixelfilter.utils.Messages;
import me.zwoosks.pixelfilter.utils.StaticVars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AntiGlitchListeners implements Listener {

    private PixelFilter plugin;

    public AntiGlitchListeners(PixelFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        // Add phase where player cannot interact in any way with maps to avoid bugs
        StaticVars.addNoDrop(e.getPlayer().getUniqueId());
        // check if on database and remove map if necessary
        new BukkitRunnable() {
            @Override
            public void run() {
                DatabaseManager dbm = new DatabaseManager(plugin.getConfig(), plugin);
                dbm.checkIfRemovableMaps(e.getPlayer());
                // remove the player from the noDrop list when everything finishes
                StaticVars.removeNoDrop(e.getPlayer().getUniqueId());
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e) {
        // check if the process was running behind and add to mysql list
        if(StaticVars.cannotDrop(e.getPlayer().getUniqueId())) {
            DatabaseManager dbm = new DatabaseManager(plugin.getConfig(), plugin);
            int mapID = StaticVars.getPlayerMapOnProcess(e.getPlayer());
            dbm.insertToRemoveMap(e.getPlayer().getName(), e.getPlayer().getUniqueId().toString(), mapID);
            StaticVars.removeNoDrop(e.getPlayer().getUniqueId());
            StaticVars.removePlayerMapOnProcess(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if(StaticVars.cannotDrop(player.getUniqueId())) {
                player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.noDamage")));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if(StaticVars.cannotDrop(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.noCommands")));
            e.setCancelled(true);
        }
    }

}
