package com.corruptedgamer.evolution_Mine.placeholderexpansion;

import com.corruptedgamer.evolution_Mine.Evolution_Mine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class placeholders extends PlaceholderExpansion {

    private final Evolution_Mine plugin;

    public placeholders(Evolution_Mine plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "evomine";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        /**if (params.equalsIgnoreCase("placeholder1")) {
            return "this is also working 1";
        }*/

        /**if (params.equalsIgnoreCase("placeholder2")) {
            return "this is working 2";
        }*/

        if (params.startsWith("havedonated_")) {
            String areaName = params.replace("havedonated_","");
            int donated = plugin.getAreaFile().getAreaConfig().getInt("areas." + areaName + ".havedonated");
            return String.valueOf(donated);
        }

        if (params.startsWith("required_")) {
            String areaName = params.replace("required_","");

            int currentLevel = plugin.getAreaFile().getAreaConfig().getInt("areas." + areaName + ".currentLevel");
            int nextLevel;
            if(currentLevel >= plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+".maxLevel"))
                nextLevel = currentLevel;
            else 
                nextLevel = currentLevel + 1;
            int required = plugin.getAreaFile().getAreaConfig().getInt("areas." + areaName + ".levels." + nextLevel + ".required");
            return String.valueOf(required);
        }
        if (params.startsWith("clvl_")) {
            String areaName = params.replace("clvl_","");
            int currentLevel = plugin.getAreaFile().getAreaConfig().getInt("areas." + areaName + ".currentLevel");
            return String.valueOf(currentLevel);
        }
        if (params.startsWith("pos1x_")) {
            String areaName = params.replace("pos1x_", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos1.x");
            return String.valueOf(pos);
        }
        if (params.startsWith("pos1y_")) {
            String areaName = params.replace("pos1y_", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos1.y");
            return String.valueOf(pos);
        }
        if (params.startsWith("pos1z_")) {
            String areaName = params.replace("pos1z_", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos1.z");
            return String.valueOf(pos);
        }
        if (params.startsWith("pos2x_")) {
            String areaName = params.replace("pos2x", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos2.x");
            return String.valueOf(pos);
        }
        if (params.startsWith("pos2y_")) {
            String areaName = params.replace("pos2y", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos2.y");
            return String.valueOf(pos);
        }
        if (params.startsWith("pos2z_")) {
            String areaName = params.replace("pos2z", "");
            int pos = plugin.getAreaFile().getAreaConfig().getInt("areas."+areaName+ ".pos2.z");
            return String.valueOf(pos);
        }


        return null;
    }
}