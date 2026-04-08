package com.corruptedgamer.evolution_Mine.Ymls;

import com.corruptedgamer.evolution_Mine.Evolution_Mine;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class areayml {
    private final Evolution_Mine plugin;
    private File areafile;
    private FileConfiguration config;

    public areayml(Evolution_Mine plugin) {
        this.plugin = plugin;
        createFile();
    }
    private void createFile() {
        areafile = new File(plugin.getDataFolder(), "areas.yml");
        if (!areafile.exists()) {
            try {
                areafile.getParentFile().mkdirs();
                areafile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(areafile);
    }
    public FileConfiguration getAreaConfig() {
        return config;
    }
    public void saveareaConfig() {
        try {
            config.save(areafile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void reloadareaConfig() {
        config = YamlConfiguration.loadConfiguration(areafile);
    }
}