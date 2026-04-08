package com.corruptedgamer.evolution_Mine;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageManager {

    private FileConfiguration messages;
    private FileConfiguration fallback;
    private final JavaPlugin plugin;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadAllLanguageFiles(); // 🔥 ADD THIS LINE
        load(); // load on startup
    }

    // 🔥 ADD THIS NEW METHOD
    private void loadAllLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        String[] languages = {
                "en.yml", "zh_CN.yml", "es_ES.yml", "pt_BR.yml",
                "ru_RU.yml", "fr_FR.yml", "de_DE.yml", "ko_KR.yml",
                "ja_JP.yml", "it_IT.yml", "pl_PL.yml", "tr_TR.yml",
                "hi.yml", "hi_en.yml"
        };

        for (String language : languages) {
            File langFile = new File(langFolder, language);
            if (!langFile.exists()) {
                plugin.saveResource("lang/" + language, false);
            }
        }
    }

    // 🔥 LOAD / RELOAD METHOD (YOUR EXISTING CODE - NO CHANGES)
    public void load() {

        String lang = plugin.getConfig().getString("language", "en");

        File folder = new File(plugin.getDataFolder(), "lang");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Selected language file
        File file = new File(folder, lang + ".yml");

        if (!file.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);

        // 🔥 Fallback (English)
        File fallbackFile = new File(folder, "en.yml");

        if (!fallbackFile.exists()) {
            plugin.saveResource("lang/en.yml", false);  // 🔥 FIXED: Changed from "messages/en.yml"
        }

        fallback = YamlConfiguration.loadConfiguration(fallbackFile);
    }

    // 🔹 BASIC GET (YOUR EXISTING CODE - NO CHANGES)
    public String get(String path) {
        String message = messages.getString(path);

        // fallback if missing
        if (message == null) {
            message = fallback.getString(path, "&cMissing message: " + path);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 🔹 WITH PLACEHOLDERS (YOUR EXISTING CODE - NO CHANGES)
    public String get(String path, String... replacements) {

        String message = get(path);

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return message;
    }
}