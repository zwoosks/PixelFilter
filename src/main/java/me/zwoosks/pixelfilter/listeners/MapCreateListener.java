package me.zwoosks.pixelfilter.listeners;

import me.zwoosks.pixelfilter.PixelFilter;
import me.zwoosks.pixelfilter.utils.DatabaseManager;
import me.zwoosks.pixelfilter.utils.Messages;
import me.zwoosks.pixelfilter.utils.MyCanvas;
import me.zwoosks.pixelfilter.utils.StaticVars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MapCreateListener implements Listener {

    PixelFilter plugin;
    public MapCreateListener(PixelFilter plugin) {
        this.plugin = plugin;
    }

    private static HashMap<UUID, Long> mapDelay = new HashMap<>();
    private static HashMap<UUID, Long> mainDelay = new HashMap<>();

    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) throws IOException {
        Player player = StaticVars.getLastOnMapInteract();
        if(player.hasPermission("pf.bypass")) return;
        if((mapDelay.get(player.getUniqueId()) != null)
                && ((mapDelay.get(player.getUniqueId()) + plugin.getConfig().getInt("config.waitSeconds")*1000) > System.currentTimeMillis())) {
            cancelCreation(event.getMap(), player);
            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.wait")));
            return;
        }
        mapDelay.put(player.getUniqueId(), System.currentTimeMillis());
        // Do not proceed with more than 1 map in hand but at least put if inside the delay to avoid spam
        if(!onlyOneEmptyMapOnHand(player)) {
            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.onlyOneMap")));
            cancelCreation(event.getMap(), player);
            return;
        }

        // Add main delay
        if((mainDelay.get(player.getUniqueId()) != null)
                && ((mainDelay.get(player.getUniqueId()) + plugin.getConfig().getInt("config.waitMainMinutes")*60*1000) > System.currentTimeMillis())) {
            cancelCreation(event.getMap(), player);
            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.waitMain")));
            return;
        }
        mainDelay.put(player.getUniqueId(), System.currentTimeMillis());
        // Since we proceed here we add the player to a list that will prevent him from dropping the map
        // (it is important for render loading purposes)
        StaticVars.addNoDrop(player.getUniqueId());
        StaticVars.setPlayerMapOnProcess(player, event.getMap().getId());
        // Asynchronous task and a bit of delay to wait for the created map to be rendered
        new BukkitRunnable() {
            @Override
            public void run() {
                MapView map = event.getMap();
                try {
                    player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.startedSearching")));
                    DatabaseManager dbm = new DatabaseManager(plugin.getConfig(), plugin);
                    // compare created map with blacklisted ones
                    BufferedImage createdMap = getImageMap(map, player);
                    if(dbm.compareImages("blacklistedPixels", createdMap, plugin.getConfig().getDouble("config.blacklistMaxDifference"))) {
                        player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.mapBlacklisted")));
                        cancelCreation(map, player);
                    } else if(dbm.compareImages("allowedPixels", createdMap, plugin.getConfig().getDouble("config.allowMaxDifference"))) {
                        // player can create the image, do nothing
                        player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.mapCreated")));
                    } else {
                        if(dbm.compareImages("pendingPixels", createdMap, plugin.getConfig().getDouble("config.submitMaxDifference"))) {
                            // already submitted, wait for approval
                            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.alreadySubmitted")));
                            cancelCreation(map, player);
                        } else {
                            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.imgNotFound")));
                            File outputFile = new File(
                                    "plugins\\PixelFilter\\images\\tmp-" + player.getName().toLowerCase() + ".png");
                            ImageIO.write(getImageMap(map, player), "png", outputFile);
                            dbm.newWriteImage(createdMap, map.getCenterX(), map.getCenterZ(), player.getName().toLowerCase(),
                                    player.getUniqueId().toString(), plugin.getConfig(), "pendingPixels");
                            cancelCreation(map, player);
                            player.sendMessage(Messages.colorizer(plugin.getConfig().getString("messages.imgSubmitted")));

                        }
                    }

                    // clear images folder
                    File dir = new File(
                            "plugins\\PixelFilter\\images\\");
                    for(File file: dir.listFiles())
                        if (!file.isDirectory())
                            file.delete();
                    // As soon as everything is finished, remove the player from the "drop blacklist"
                    StaticVars.removeNoDrop(player.getUniqueId());
                    StaticVars.removePlayerMapOnProcess(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskLater(plugin, 20*5);
    }

    private boolean onlyOneEmptyMapOnHand(Player player) {
        ItemStack is = player.getItemInHand();
        if(is.getAmount() == 1) return true;
        else return false;
    }

    private BufferedImage getImageMap(MapView map, Player player) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(128,128, BufferedImage.TYPE_INT_RGB);
        MyCanvas canvas = new MyCanvas(map);
        for (MapRenderer renderer : map.getRenderers()) {
            renderer.render(map, canvas, player);
        }
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int pixel = canvas.getPixel(x, y);
                try {
                    pixel = MapPalette.getColor((byte) pixel).getRGB();
                } catch (Exception e) {}
                bufferedImage.setRGB(x, y, pixel);
            }
        }
        return bufferedImage;
    }

    private void cancelCreation(MapView map, Player player) {
        for (MapRenderer renderer : map.getRenderers()) {
            map.removeRenderer(renderer);
            int mapID = map.getId();
            Location location = new Location(map.getWorld(), map.getCenterX(), 64, map.getCenterZ());
            // Way to get the nearest player from the center of map, could give some errors if the map creator
            // isn't the nearest players to the center of the map
            // Player player = (Player) map.getWorld().getNearbyEntities(location, 200.0, 200.0, 200.0).stream().filter(e -> e instanceof Player).findFirst().orElse(null);
            removeMap(player.getInventory(), mapID, player);
            return;
        }
    }

    private void removeMap(Inventory inventory, int id, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(int i = 0; i < inventory.getSize(); i++){
                    ItemStack itm = inventory.getItem(i);
                    if(itm != null && itm.getType().equals(Material.MAP)) {
                        if(itm.getDurability() == id) {
                            int amt = itm.getAmount() - 1;
                            itm.setAmount(amt);
                            inventory.setItem(i, amt > 0 ? itm : null);
                            inventory.addItem(new ItemStack(Material.EMPTY_MAP, 1));
                            player.updateInventory();
                            break;
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 1);
    }

}
