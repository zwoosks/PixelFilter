package me.zwoosks.pixelfilter;

import me.zwoosks.pixelfilter.listeners.AntiGlitchListeners;
import me.zwoosks.pixelfilter.listeners.BlankMapInteractListener;
import me.zwoosks.pixelfilter.listeners.MapCreateListener;
import me.zwoosks.pixelfilter.listeners.MapDropListener;
import me.zwoosks.pixelfilter.utils.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PixelFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        createDir();
        getServer().getPluginManager().registerEvents(new MapCreateListener(this), this);
        getServer().getPluginManager().registerEvents(new BlankMapInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new MapDropListener(this), this);
        getServer().getPluginManager().registerEvents(new AntiGlitchListeners(this), this);
        this.getCommand("pixelfilter").setExecutor(new CommandManager());
        DatabaseManager dbm = new DatabaseManager(this.getConfig(), this);
        dbm.checkTables();
    }

    private void createDir() {
        String path = "plugins\\PixelFilter\\images";
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

}
