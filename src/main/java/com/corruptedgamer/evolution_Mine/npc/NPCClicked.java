package com.corruptedgamer.evolution_Mine.npc;

import com.corruptedgamer.evolution_Mine.Commands;
import com.corruptedgamer.evolution_Mine.Evolution_Mine;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
public class NPCClicked implements Listener {

    private final Evolution_Mine plugin;
    private final Map<UUID, String> playerAreas = new HashMap<>();
    private final Set<UUID> interactionCooldown = new HashSet<>();

    public NPCClicked(Evolution_Mine plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC clickedNPC = event.getNPC();
        int ID = clickedNPC.getId();
        FileConfiguration config = plugin.getAreaFile().getAreaConfig();

        String foundArea = null;
        ConfigurationSection areasSection = config.getConfigurationSection("areas");
        if (areasSection == null) return;
        for (String areaName : areasSection.getKeys(false)) {
            if (ID == config.getInt("areas." + areaName + ".npcId")) {
                foundArea = areaName;
                break;
            }
        }
        if (foundArea == null) return;

        if (interactionCooldown.contains(player.getUniqueId())) {
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("stop-spam"));
            return;
        }

        String path = "areas." + foundArea + ".";
        int currentLevel = config.getInt(path + "currentLevel", 0);
        int maxLevel = config.getInt(path + "maxLevel", 0);

        if (currentLevel >= maxLevel) {
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("already-max"));
            return;
        }

        String itemName = config.getString(path + "item");
        Material material = (itemName == null) ? null : Material.matchMaterial(itemName);

        if (material == null) {
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("missing-setitem") + foundArea);
            return;
        }

        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) total += item.getAmount();
        }

        if (total <= 0) {
            boolean auto = Evolution_Mine.getInstance().getConfig().getBoolean("settings.autoname-item");
            if (auto) {
                player.sendMessage(Evolution_Mine.getInstance().getMsg().get("no-item.before-itemname") + material.name() + Evolution_Mine.getInstance().getMsg().get("no-item.after-itemname"));
            }
            else{
                player.sendMessage(Evolution_Mine.getInstance().getMsg().get("no-item.complete"));
            }
            return;
        }
        if (Evolution_Mine.getInstance().getConfig().getBoolean("settings.autoname-item")) {
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("have-item.telling.msg") + total + " " + material.name() + ".");
        }
        else{
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("have-item.telling.complete-before")+ total + Evolution_Mine.getInstance().getMsg().get("have-item.telling.complete-after"));
        }

        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("have-item.asking"));

        playerAreas.put(player.getUniqueId(), foundArea);
        com.corruptedgamer.evolution_Mine.Commands.waitingForInput.add(player.getUniqueId());

        int delaySeconds = plugin.getConfig().getInt("delay-time-intraction", 3);
        interactionCooldown.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                interactionCooldown.remove(player.getUniqueId());
                if (player.isOnline()) player.sendMessage(Evolution_Mine.getInstance().getMsg().get("interact-again"));
            }
        }.runTaskLater(plugin, delaySeconds * 20L);

        TextComponent yes = new TextComponent("[YES]");
        yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        yes.setBold(true);
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/evo yes"));

        TextComponent no = new TextComponent("[NO]");
        no.setColor(net.md_5.bungee.api.ChatColor.RED);
        no.setBold(true);
        no.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/evo no"));

        player.spigot().sendMessage(yes, new TextComponent(" "), no);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!com.corruptedgamer.evolution_Mine.Commands.waitingForInput.contains(player.getUniqueId())) return;

        event.setCancelled(true);
        String message = event.getMessage();
        String area = playerAreas.get(player.getUniqueId());

        if (area == null) {
            com.corruptedgamer.evolution_Mine.Commands.waitingForInput.remove(player.getUniqueId());
            playerAreas.remove(player.getUniqueId());
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("no-session"));
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                int enteredNumber = Integer.parseInt(message.trim());
                if (enteredNumber <= 0) {
                    player.sendMessage(Evolution_Mine.getInstance().getMsg().get("invalid-number"));
                    return;
                }

                FileConfiguration config = plugin.getAreaFile().getAreaConfig();
                String path = "areas." + area + ".";
                String itemName = config.getString(path + "item");
                Material material = (itemName == null) ? null : Material.matchMaterial(itemName);

                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Configured item for this mine is invalid. Contact an admin.");
                    return;
                }

                int total = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == material) total += item.getAmount();
                }

                if (enteredNumber > total) {
                    if(Evolution_Mine.getInstance().getConfig().getBoolean("settings.autoname-item")){
                        player.sendMessage( Evolution_Mine.getInstance().getMsg().get("big-number.not-have")  + material.name() +".");
                    }
                    else {
                        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("big-number.custom-msg"));
                    }

                    player.sendMessage(Evolution_Mine.getInstance().getMsg().get("big-number.have") + total +".");


                    return;
                }

                int toProcess = enteredNumber;
                int currentLevel = config.getInt(path + "currentLevel", 0);
                int maxLevel = config.getInt(path + "maxLevel", 0);
                int haveDonated = config.getInt(path + "havedonated", 0);

                int needed = 0;
                int tempLevel = currentLevel;
                int tempDonated = haveDonated;
                while (tempLevel < maxLevel) {
                    int required = config.getInt(path + "levels." + (tempLevel + 1) + ".required", -1);
                    if (required < 0) break;
                    needed += (required - tempDonated);
                    tempDonated = 0;
                    tempLevel++;
                }

                int itemsToRemove = Math.min(toProcess, needed);

                // Remove only items actually needed
                int toRemove = itemsToRemove;
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item != null && item.getType() == material && toRemove > 0) {
                        int stackAmount = item.getAmount();
                        if (stackAmount > toRemove) {
                            item.setAmount(stackAmount - toRemove);
                            toRemove = 0;
                        } else {
                            toRemove -= stackAmount;
                            contents[i].setAmount(0);
                        }
                    }
                    if (toRemove <= 0) break;
                }
                player.getInventory().setContents(contents);

                int remainingToProcess = itemsToRemove;
                int clvl = currentLevel;
                int havedona = haveDonated;

                while (remainingToProcess > 0 && clvl < maxLevel) {
                    int nextLevel = clvl + 1;
                    String levelPath = path + "levels." + nextLevel + ".required";
                    if (!config.contains(levelPath)) {
                        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("lvl-config-missing") + nextLevel + Evolution_Mine.getInstance().getMsg().get("lvl-config-missing-after"));

                        break;
                    }
                    int required = config.getInt(levelPath);
                    int need = required - havedona;

                    if (need <= 0) {
                        clvl = Math.min(clvl + 1, maxLevel);
                        havedona = 0;
                        config.set(path + "currentLevel", clvl);
                        config.set(path + "havedonated", havedona);
                        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("mine-upgraded") + clvl + Evolution_Mine.getInstance().getMsg().get("mine-upgraded-after"));

                        // Linked areas
                        List<String> linkedAreas = config.getStringList(path + "linkedAreas");
                        if (linkedAreas != null && !linkedAreas.isEmpty()) {
                            for (String linked : linkedAreas) {
                                String linkedPath = "areas." + linked + ".";
                                int linkedCurrent = config.getInt(linkedPath + "currentLevel", 0);
                                int linkedMax = config.getInt(linkedPath + "maxLevel", 0);
                                if (linkedCurrent >= linkedMax) {
                                    player.sendMessage(ChatColor.YELLOW + "Linked area " + linked + " is already maxed");
                                    continue;
                                }
                                config.set(linkedPath + "currentLevel", linkedCurrent + 1);
                                player.sendMessage(ChatColor.AQUA + "Area Linked - " + linked + " upgraded to level " + (linkedCurrent + 1));
                            }
                        }
                        // Dynamic y upgrade (see your previous logic)
                        ConfigurationSection typeSection = config.getConfigurationSection(path + "type");
                        if (typeSection != null && typeSection.contains("dynamic")) {
                            String upgradeto = config.getString(path + "type.dynamic.update", "up");
                            String layerString = config.getString(path + "type.dynamic.layer", "0");
                            int layeradd = Integer.parseInt(layerString);
                            int y1 = config.getInt(path + "pos1.y");
                            int y2 = config.getInt(path + "pos2.y");
                            int layerupgrade = 0;
                            int posup;
                            if ("up".equalsIgnoreCase(upgradeto)) {
                                layerupgrade = Math.max(y1, y2);
                                posup = layerupgrade + layeradd;
                            } else if ("down".equalsIgnoreCase(upgradeto)) {
                                layerupgrade = Math.min(y1, y2);
                                posup = layerupgrade - layeradd;
                            } else {
                                player.sendMessage(Evolution_Mine.getInstance().getMsg().get("mine-not-config"));
                                break;
                            }
                            if (layerupgrade == y1) {
                                config.set(path + "pos1.y", posup);
                            } else {
                                config.set(path + "pos2.y", posup);
                            }
                        }
                    } else if (remainingToProcess >= need) {
                        remainingToProcess -= need;
                        clvl = Math.min(clvl + 1, maxLevel);
                        havedona = 0;
                        config.set(path + "currentLevel", clvl);
                        config.set(path + "havedonated", havedona);
                        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("mine-upgraded") + clvl + Evolution_Mine.getInstance().getMsg().get("mine-upgraded-after"));
                        // Linked areas and dynamic upgrade as above...
                        List<String> linkedAreas = config.getStringList(path + "linkedAreas");
                        if (linkedAreas != null && !linkedAreas.isEmpty()) {
                            for (String linked : linkedAreas) {
                                String linkedPath = "areas." + linked + ".";
                                int linkedCurrent = config.getInt(linkedPath + "currentLevel", 0);
                                int linkedMax = config.getInt(linkedPath + "maxLevel", 0);
                                if (linkedCurrent >= linkedMax) {
                                    player.sendMessage(ChatColor.YELLOW + "Linked area " + linked + " is already maxed");
                                    continue;
                                }
                                config.set(linkedPath + "currentLevel", linkedCurrent + 1);
                                player.sendMessage(ChatColor.AQUA + "Area Linked - " + linked + " upgraded to level " + (linkedCurrent + 1));
                            }
                        }
                        ConfigurationSection typeSection = config.getConfigurationSection(path + "type");
                        if (typeSection != null && typeSection.contains("dynamic")) {
                            String upgradeto = config.getString(path + "type.dynamic.update", "up");
                            String layerString = config.getString(path + "type.dynamic.layer", "0");
                            int layeradd = Integer.parseInt(layerString);
                            int y1 = config.getInt(path + "pos1.y");
                            int y2 = config.getInt(path + "pos2.y");
                            int layerupgrade = 0;
                            int posup;
                            if ("up".equalsIgnoreCase(upgradeto)) {
                                layerupgrade = Math.max(y1, y2);
                                posup = layerupgrade + layeradd;
                            } else if ("down".equalsIgnoreCase(upgradeto)) {
                                layerupgrade = Math.min(y1, y2);
                                posup = layerupgrade - layeradd;
                            } else {
                                player.sendMessage(Evolution_Mine.getInstance().getMsg().get("mine-not-config"));
                                break;
                            }
                            if (layerupgrade == y1) {
                                config.set(path + "pos1.y", posup);
                            } else {
                                config.set(path + "pos2.y", posup);
                            }
                        }
                    } else {
                        havedona += remainingToProcess;
                        config.set(path + "havedonated", havedona);
                        player.sendMessage(Evolution_Mine.getInstance().getMsg().get("item-accepted") + havedona + "/" + required);
                        remainingToProcess = 0;
                    }
                }

                if (clvl >= maxLevel) {
                    config.set(path + "currentLevel", maxLevel);
                    player.sendMessage(Evolution_Mine.getInstance().getMsg().get("reached-max"));
                }

                plugin.getAreaFile().saveareaConfig();

                if (itemsToRemove < enteredNumber) {
                    player.sendMessage(Evolution_Mine.getInstance().getMsg().get("only-need") + itemsToRemove + Evolution_Mine.getInstance().getMsg().get("only-need-after") +
                            "\n" + Evolution_Mine.getInstance().getMsg().get("remain") + (enteredNumber - itemsToRemove) + Evolution_Mine.getInstance().getMsg().get("remain-after"));
                }

            } catch (NumberFormatException ex) {
                player.sendMessage(Evolution_Mine.getInstance().getMsg().get("not-valid") + message + Evolution_Mine.getInstance().getMsg().get("not-valid-after")+Evolution_Mine.getInstance().getMsg().get("invalid-number"));
            } finally {
                com.corruptedgamer.evolution_Mine.Commands.waitingForInput.remove(player.getUniqueId());
                playerAreas.remove(player.getUniqueId());
            }
        });
    }
}