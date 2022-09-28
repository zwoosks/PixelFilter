package me.zwoosks.pixelfilter.listeners;

import me.zwoosks.pixelfilter.PixelFilter;
import me.zwoosks.pixelfilter.utils.Messages;
import me.zwoosks.pixelfilter.utils.StaticVars;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class BlankMapInteractListener implements Listener {

    private PixelFilter plugin;

    public BlankMapInteractListener(PixelFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractWithMap(PlayerInteractEvent e) {
        if(StaticVars.cannotDrop(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.noInteract")));
            e.setCancelled(true);
        }
        if(e.getMaterial() == Material.EMPTY_MAP) {
            StaticVars.setLastOnMapInteract(e.getPlayer());
        }
    }

}
