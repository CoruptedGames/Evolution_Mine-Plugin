package com.corruptedgamer.evolution_Mine.npc;

import com.corruptedgamer.evolution_Mine.Commands;
import com.corruptedgamer.evolution_Mine.Evolution_Mine;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import java.util.UUID;


public class NpcSelectorListener implements Listener {

    private final Evolution_Mine plugin; // Reference to your main plugin class

    public NpcSelectorListener(Evolution_Mine plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNpcRightClick(NPCRightClickEvent event) {
        Player clicker = event.getClicker();
        UUID playerUUID = clicker.getUniqueId();

        // 1. Check if the player is waiting for a click
        if (Commands.playersSettingNpcArea.containsKey(playerUUID)) {

            // Get the area name that the player was setting up
            String areaName = Commands.playersSettingNpcArea.get(playerUUID);

            // Prevent other plugins/default Citizens behaviour from interfering
            event.setCancelled(true);

            NPC npc = event.getNPC();
            int npcId = npc.getId();

            // 2. Determine the config path: areas.<areaName>.npcId
            String configPath = "areas." + areaName + ".npcId";

            // 3. Save the NPC ID to the YML configuration (You might need to use getAreaConfig() here)

            plugin.getAreaFile().getAreaConfig().set("areas." + areaName + ".npcId", npcId);
            plugin.getAreaFile().saveareaConfig();

            // 4. Inform the player and remove them from the map
            Commands.playersSettingNpcArea.remove(playerUUID);
            clicker.sendMessage(ChatColor.GREEN + "Successfully set NPC " + ChatColor.LIGHT_PURPLE + npcId + ChatColor.GREEN + " for area " + ChatColor.AQUA + areaName + ChatColor.GREEN + ".");
        }
    }
}
