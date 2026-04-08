package com.corruptedgamer.evolution_Mine;

import com.corruptedgamer.evolution_Mine.Ymls.areayml;
import com.corruptedgamer.evolution_Mine.npc.NPCClicked;
import com.corruptedgamer.evolution_Mine.npc.NpcSelectorListener;
import com.corruptedgamer.evolution_Mine.placeholderexpansion.placeholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class Evolution_Mine extends JavaPlugin {
    private areayml areaFile;
    private static Evolution_Mine intance;
    public static Evolution_Mine getInstance() {
        return intance;
    }
    private AreaUpdateManager areaUpdateManager;
    private MessageManager msg;
    private boolean outdated = false;
    private String latestVersion;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        msg = new MessageManager(this);
        intance = this;
        areaFile = new areayml(this);
        areaUpdateManager = new AreaUpdateManager(this);
        areaUpdateManager.startUpdater();
        int pluginId = 30515; // your bStats plugin ID
        Metrics metrics = new Metrics(this, pluginId);

        Bukkit.getLogger().info("__________________________________________");
        Bukkit.getLogger().info("|[Evolution_Mine] Enabled                 |");
        Bukkit.getLogger().info("|[Evolution_Mine] Version: " + getDescription().getVersion()+"           |");
        Bukkit.getLogger().info("|[Evolution_Mine] Author: " + getDescription().getAuthors()+"|");
        Bukkit.getLogger().info("__________________________________________");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            String latest = getLatestVersion();
            latestVersion = getLatestVersion();
            String current = getDescription().getVersion();

            if (latest != null) {
                if (!current.equalsIgnoreCase(latest)) {
                    outdated = true;



                    getLogger().warning("[Evolution_Mine] Plugin is OUTDATED!");
                    getLogger().warning("[Evolution_Mine] Current: " + current + " | Latest: " + latest);
                } else {

                    getLogger().info("[Evolution_Mine] Plugin is up to date!");
                }
            }

        });
        this.getCommand("evo").setExecutor(new Commands());
        this.getCommand("evo").setTabCompleter(new commandCompletion());
        new AreaUpdateManager(this).startUpdater();
        new placeholders(this).register();
        this.getServer().getPluginManager().registerEvents(new UpdateNotifyListener(this), this);
        this.getServer().getPluginManager().registerEvents(new NPCClicked(this), this);
        this.getServer().getPluginManager().registerEvents(new NpcSelectorListener(this), this);
    }
    @Override
    public void onDisable() {
        areaFile.saveareaConfig();
        saveConfig();
    }
    public MessageManager getMsg() {
        return msg;
    }
    public void reloadMessages() {
        msg.load();
    }
    public AreaUpdateManager getAreaUpdateManager() {
        return areaUpdateManager;
    }
    public areayml getAreaFile() {
        return areaFile;
    }
    public String getLatestVersion() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/CoruptedGames/Evolution_Mine-Plugin/main/version.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return reader.readLine();
        } catch (Exception e) {
            getLogger().warning("[Evolution_Mine] Could not check for updates.");
            return null;
        }
    }
    public boolean isOutdated() {
        return outdated;
    }
    public String getLatestVersionCached() {
        return latestVersion;
    }
}