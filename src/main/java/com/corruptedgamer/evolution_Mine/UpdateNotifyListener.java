package com.corruptedgamer.evolution_Mine;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    private final Evolution_Mine plugin;

    public UpdateNotifyListener(Evolution_Mine plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (!plugin.getConfig().getBoolean("update-notify", true)) return;

        Player p = event.getPlayer();

        if (!(p.isOp() || p.hasPermission("evomine.admin"))) return;

        if (!plugin.isOutdated()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            p.sendMessage("§cEvolution_Mine is outdated! §7Current: " + plugin.getDescription().getVersion()+" §7Latest: " + plugin.getLatestVersionCached());
        }, 40L);
    }
}