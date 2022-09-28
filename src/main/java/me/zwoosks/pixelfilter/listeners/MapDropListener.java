package me.zwoosks.pixelfilter.listeners;

import me.zwoosks.pixelfilter.PixelFilter;
import me.zwoosks.pixelfilter.utils.Messages;
import me.zwoosks.pixelfilter.utils.StaticVars;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class MapDropListener implements Listener {

    private PixelFilter plugin;

    public MapDropListener(PixelFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMapDrop(PlayerDropItemEvent e) {
        if((e.getItemDrop().getItemStack().getType() == Material.MAP)
                && (StaticVars.cannotDrop(e.getPlayer().getUniqueId())))
        {
            e.getPlayer().sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.noDrop")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMapClick(InventoryClickEvent e) {
        if((e.getCurrentItem().getType() == Material.MAP)
                && (StaticVars.cannotDrop(e.getWhoClicked().getUniqueId())))
        {
            e.getWhoClicked().sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.noDrop")));
            e.setCancelled(true);
        }
    }

}