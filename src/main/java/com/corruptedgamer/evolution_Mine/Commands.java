package com.corruptedgamer.evolution_Mine;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.corruptedgamer.evolution_Mine.Ymls.areayml;
import java.util.*;

public class Commands implements CommandExecutor {
    areayml areaFile = Evolution_Mine.getInstance().getAreaFile();
    // Store players waiting to select an NPC

    public static Map<UUID, String> playersSettingNpcArea = new HashMap<>();
    public static Set<UUID> waitingForInput = new HashSet<>();
    Evolution_Mine plugin = Evolution_Mine.getInstance();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(!(sender instanceof Player)){
            sender.sendMessage("This command can only be run ingame. We will add console support later");
            return true;
        }//check if the command sender is a player

        Player player = (Player) sender;

        if (player.hasPermission("evomine.admin")) {
            if (args.length == 0) {
                player.sendMessage("§6§m--------------------------------");
                player.sendMessage(" §a§lWelcome to §b§lEvolution Mine!");
                player.sendMessage("");
                player.sendMessage(" §e➤ §7To get info about commands, type: §f/evo help");
                player.sendMessage(" §e➤ §7To get guide, type: §f/evo guide");
                player.sendMessage("");
                player.sendMessage("§6§m--------------------------------");

                return true;
            }//If no arguments, show basic info

            if (args[0].equalsIgnoreCase("reload")) {
                Evolution_Mine.getInstance().reloadConfig();
                plugin.reloadMessages();
                areaFile.reloadareaConfig();
                Evolution_Mine.getInstance().getAreaUpdateManager().startUpdater();
                Bukkit.getLogger().info(ChatColor.GREEN + "[EvolutionMine] Evolution Mine configuration reloaded.");
                player.sendMessage(ChatColor.GREEN + "[EvolutionMine] Config reloaded successfully!");
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {

                player.sendMessage(Component.text("✦✦✦ Evolution Mine Commands ✦✦✦")
                        .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                player.sendMessage(Component.text("──────────────────────────────────", NamedTextColor.DARK_GRAY));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setArea <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Create a new mine area", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setArea ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setPos1 <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set first corner position", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setPos1 ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setPos2 <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set second corner position", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setPos2 ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine listArea <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("List existing areas", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine listArea ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setMaxLvl <areaName> <maxlevel>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set max level for area", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setMaxLvl ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setItem <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set required upgrade item", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setItem ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setDelay <areaName> <seconds>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set refill delay", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setDelay ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setNpc <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set NPC for donation", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setNpc ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine linkArea <parent> <child>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Link a child area to a parent", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine linkArea ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine unlinkArea <parent> <child>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Unlink areas", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine unlinkArea ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine removeArea <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Delete an area completely", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine removeArea ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine addBlock <areaName> <level> <percentage>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Add block for level", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine addBlock ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine setReq <areaName> <level> <required>", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set item requirement", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine setReq ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo mine settype <areaName> <static/dynamic> [up/down] [layers]", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Set mine type", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo mine settype ")));

//                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
//                        .append(Component.text("/evo mine info <areaName>", NamedTextColor.AQUA, TextDecoration.BOLD))
//                        .hoverEvent(HoverEvent.showText(Component.text("Display mine info", NamedTextColor.GRAY)))
//                        .clickEvent(ClickEvent.suggestCommand("/evo mine info ")));

                player.sendMessage(Component.text(" ➤ ", NamedTextColor.GRAY)
                        .append(Component.text("/evo reload", NamedTextColor.AQUA, TextDecoration.BOLD))
                        .hoverEvent(HoverEvent.showText(Component.text("Reload plugin config", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.suggestCommand("/evo reload")));

                player.sendMessage(Component.text("──────────────────────────────────", NamedTextColor.DARK_GRAY));
                return true;
            }//Help command

            if (args[0].equalsIgnoreCase("guide")) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "===== 🪓 Evolution Mine Setup Guide 🪨 =====");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 1: " + ChatColor.RESET + "Create a Mine");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setArea <areaName>" + ChatColor.DARK_GRAY + " ➝ Create new mine region");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 2: " + ChatColor.RESET + "Set Area Positions");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setPos1 <areaName>" + ChatColor.DARK_GRAY + " ➝ Set first corner");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setPos2 <areaName>" + ChatColor.DARK_GRAY + " ➝ Set second corner");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 3: " + ChatColor.RESET + "Configure Mine Settings");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setMaxLvl <areaName> <maxLvl>" + ChatColor.DARK_GRAY + " ➝ Max upgrade level");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setItem <areaName>" + ChatColor.DARK_GRAY + " ➝ Set required item");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setReq <areaName> <level> <amount>" + ChatColor.DARK_GRAY + " ➝ Required per level");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setDelay <areaName> <seconds>" + ChatColor.DARK_GRAY + " ➝ Refill time");
                player.sendMessage(ChatColor.GRAY + "  /evo mine settype <areaName> <static/dynamic> [up/down] [layer]" + ChatColor.DARK_GRAY + " ➝ Mine type");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 4: " + ChatColor.RESET + "Add Blocks");
                player.sendMessage(ChatColor.GRAY + "  /evo mine addBlock <areaName> <level> <percentage>" + ChatColor.DARK_GRAY + " ➝ Block per level");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 5: " + ChatColor.RESET + "Set NPC");
                player.sendMessage(ChatColor.GRAY + "  /evo mine setNpc <areaName>" + ChatColor.DARK_GRAY + " ➝ Set NPC for donation");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 6: " + ChatColor.RESET + "Link / Unlink (Optional)");
                player.sendMessage(ChatColor.GRAY + "  /evo mine linkArea <parent> <child>" + ChatColor.DARK_GRAY + " ➝ Link areas");
                player.sendMessage(ChatColor.GRAY + "  /evo mine unlinkArea <parent> <child>" + ChatColor.DARK_GRAY + " ➝ Unlink areas");

                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "STEP 7: " + ChatColor.RESET + "Manage Mines");
                player.sendMessage(ChatColor.GRAY + "  /evo mine listArea" + ChatColor.DARK_GRAY + " ➝ List all mines");
                //player.sendMessage(ChatColor.GRAY + "  /evo mine info <areaName>" + ChatColor.DARK_GRAY + " ➝ Mine details");
                player.sendMessage(ChatColor.GRAY + "  /evo mine removeArea <areaName>" + ChatColor.DARK_GRAY + " ➝ Delete mine");
                player.sendMessage(ChatColor.GRAY + "  /evo reload" + ChatColor.DARK_GRAY + " ➝ Reload config");

                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "========================================");
                player.sendMessage(ChatColor.GREEN + "✅ Recommended Order: "
                        + ChatColor.GRAY + "setArea → setPos → setMaxLvl → setItem/Req → setType → addBlock → setNpc → link");


                return true;
            }//guide to command

            if (args[0].equalsIgnoreCase("mine")) {
                if (args.length >= 2) {

                    if (args[1].equalsIgnoreCase("setArea")) {//evo mine[0] setArea[1] <areaName>[2]
                        if (args.length < 3) {

                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine setArea <areaName>");
                            return true;
                        }
                        String area = args.length >= 3 ? args[2] : "default";

                        areaFile.getAreaConfig().set("areas." + area + ".world", player.getWorld().getName());
                        areaFile.getAreaConfig().set("areas." + area + ".currentLevel", 0);
                        areaFile.getAreaConfig().set("areas." + area + ".havedonated", 0);

                        areaFile.saveareaConfig();
                        player.sendMessage(ChatColor.BOLD + "" + ChatColor.WHITE + "New Area Created with name " + ChatColor.GREEN + area);
                        return true;


                    }//save config area name

                    if (args[1].equalsIgnoreCase("setPos1")) {
                        // get block player is facing
                        String area = args[2];
                        int targetdis = Evolution_Mine.getInstance().getConfig().getInt("target-block-distance", 100);
                        if (args.length >= 3 && area != null) {
                            Block target = player.getTargetBlockExact(targetdis);
                            if (target == null) {
                                player.sendMessage(ChatColor.RED + "No block in sight! Make sure you're looking at a block within " + targetdis + " blocks.");
                                return true;
                            }
                            Location loc = target.getLocation();

                            // save pos1
                            areaFile.getAreaConfig().set("areas." + area + ".pos1.x", loc.getX());
                            areaFile.getAreaConfig().set("areas." + area + ".pos1.y", loc.getY());
                            areaFile.getAreaConfig().set("areas." + area + ".pos1.z", loc.getZ());
                            areaFile.saveareaConfig();

                            player.sendMessage(ChatColor.GREEN + "Position 1 for area " + area + " set at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine setPos1 <areaName>");
                            return true;
                        }


                    }//save pos1

                    if (args[1].equalsIgnoreCase("setPos2")) {
                        String area = args[2];
                        int targetdis = Evolution_Mine.getInstance().getConfig().getInt("target-block-distance", 100);
                        if (args.length >= 3 && area != null) {
                            Block target = player.getTargetBlockExact(targetdis); // 100 block max distance
                            if (target == null) {
                                player.sendMessage(ChatColor.RED + "No block in sight! Make sure you're looking at a block within " + targetdis + " blocks.");
                                return true;
                            }
                            Location loc = target.getLocation();

                            // save pos2
                            areaFile.getAreaConfig().set("areas." + area + ".pos2.x", loc.getX());
                            areaFile.getAreaConfig().set("areas." + area + ".pos2.y", loc.getY());
                            areaFile.getAreaConfig().set("areas." + area + ".pos2.z", loc.getZ());
                            areaFile.saveareaConfig();
                            player.sendMessage(ChatColor.GREEN + "Position 2 for area " + area + " set at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine setPos2 <areaName>");
                            return true;
                        }
                    }//save pos2

                    if (args[1].equalsIgnoreCase("setloctp")) {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Usage: /evo mine settploc <area>");
                            return true;
                        }

                        String area = args[2];
                        Location loc = player.getLocation();

                        // Save each coordinate separately

                        areaFile.getAreaConfig().set("areas." + area + ".tplocy", loc.getY());

                        areaFile.saveareaConfig();

                        player.sendMessage(ChatColor.GREEN + "Teleport location for area " + area + " set at "
                                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        return true;
                    }//save tp loc

                    if (args[1].equalsIgnoreCase("listArea")) {
                        player.sendMessage(ChatColor.BOLD + "" + ChatColor.WHITE + "--- List of Areas ---");
                        ConfigurationSection areas = areaFile.getAreaConfig().getConfigurationSection("areas");

                        if (areas == null || areas.getKeys(false).isEmpty()) {
                            player.sendMessage(ChatColor.RED + "No areas found!");
                            return true;
                        }
                        for (String areaName : areaFile.getAreaConfig().getConfigurationSection("areas").getKeys(false)) {
                            player.sendMessage(ChatColor.GREEN + "- " + areaName);
                        }
                        return true;
                    }//list areas

                    if (args[1].equalsIgnoreCase("setMaxLvl")) {//evo mine[0] setMaxLvl[1] <areaName>[2] <maxLevel>[3]
                        String area = args[2];
                        String maxLvl = args[3];
                        if (args.length >= 4 && area != null && maxLvl != null) {
                            try {
                                int maxLevel = Integer.parseInt(maxLvl);
                                areaFile.getAreaConfig().set("areas." + area + ".maxLevel", maxLevel);
                                areaFile.saveareaConfig();
                                player.sendMessage(ChatColor.GREEN + "Max level for area " + area + " set to " + maxLevel);
                                return true;
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid number format for max level. Please enter a valid integer.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and max level. Usage: /evo mine setMaxLvl <areaName> <maxLevel>");
                            return true;
                        }
                    }//set max level for area

                    if (args[1].equalsIgnoreCase("setItem")) {//evo mine[0] setItem[1] <areaName>[2]
                        String area = args[2];
                        if (args.length >= 3 && area != null) {
                            // get item in player's hand
                            if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
                                player.sendMessage(ChatColor.RED + "You must be holding an item to set it for the area.");
                                return true;
                            }
                            String itemName = player.getInventory().getItemInMainHand().getType().toString();
                            areaFile.getAreaConfig().set("areas." + area + ".item", itemName);
                            areaFile.saveareaConfig();
                            player.sendMessage(ChatColor.GREEN + "Item for area " + area + " set to " + itemName);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine setItem <areaName>");
                            return true;
                        }
                    }//set item for area

                    if (args[1].equalsIgnoreCase("setDelay")) {//evo mine[0] setDelay[1] <areaName>[2] <delayInSeconds>[3]
                        String area = args[2];
                        String delayStr = args[3];
                        if (args.length >= 4 && area != null && delayStr != null) {
                            try {
                                int delay = Integer.parseInt(delayStr);
                                areaFile.getAreaConfig().set("areas." + area + ".delay", delay);
                                areaFile.saveareaConfig();
                                player.sendMessage(ChatColor.GREEN + "Delay for area " + area + " set to " + delay + " seconds");
                                return true;
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid number format for delay. Please enter a valid integer.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and delay in seconds. Usage: /evo mine setDelay <areaName> <delayInSeconds>");
                            return true;
                        }
                    }//set delay for area

                    if (args[1].equalsIgnoreCase("setNpc")) {//evo mine[0] setNpc[1] <areaName>[2]
                        String area = args[2];
                        if (args.length >= 3 && area != null) {
                            // Add player to waiting list
                            playersSettingNpcArea.put(player.getUniqueId(), area);
                            player.sendMessage(ChatColor.YELLOW + "Please right-click on the NPC you want to associate with area " + area + ".");
                            // Store the area name temporarily

                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine setNpc <areaName>");
                            return true;
                        }

                    }//set npc for area

                    if (args[1].equalsIgnoreCase("linkArea")) {//evo mine[0] linkArea[1] <areaName>[2] <thattobelinked>[3]
                        String area = args[2];
                        String linkArea = args[3];
                        if (args.length >= 4 && area != null && linkArea != null) {
                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + area) == null) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " does not exist.");
                                return true;
                            }
                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + linkArea) == null) {
                                player.sendMessage(ChatColor.RED + "Area to be linked " + linkArea + " does not exist.");
                                return true;
                            }
                            List<String> linkedAreas = areaFile.getAreaConfig().getStringList("areas." + area + ".linkedAreas");
                            if (linkedAreas.contains(linkArea)) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " is already linked to " + linkArea);
                                return true;
                            }
                            linkedAreas.add(linkArea);
                            areaFile.getAreaConfig().set("areas." + area + ".linkedAreas", linkedAreas);
                            areaFile.saveareaConfig();
                            player.sendMessage(ChatColor.GREEN + "Area " + area + " has been linked to " + linkArea);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and the area to be linked. Usage: /evo mine linkArea <areaName> <thatToBeLinked>");
                            return true;
                        }


                    }//link area to another area

                    if (args[1].equalsIgnoreCase("unlinkArea")) {//evo mine[0] unlinkArea[1] <areaName>[2] <thatToBeUnlinked>[3]
                        String area = args[2];
                        String unlinkArea = args[3];
                        if (args.length >= 3 && area != null && unlinkArea != null) {
                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + area) == null) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " does not exist.");
                                return true;
                            }
                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + unlinkArea) == null) {
                                player.sendMessage(ChatColor.RED + "Area to be unlinked " + unlinkArea + " does not exist.");
                                return true;
                            }
                            List<String> linkedAreas = areaFile.getAreaConfig().getStringList("areas." + area + ".linkedAreas");
                            if (!linkedAreas.contains(unlinkArea)) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " is not linked to " + unlinkArea);
                                return true;
                            }
                            linkedAreas.remove(unlinkArea);
                            areaFile.getAreaConfig().set("areas." + area + ".linkedAreas", linkedAreas);
                            areaFile.saveareaConfig();
                            player.sendMessage(ChatColor.GREEN + "Area " + area + " has been unlinked from " + unlinkArea);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and the area to be unlinked. Usage: /evo mine unlinkArea <areaName> <thatToBeUnlinked>");
                            return true;
                        }
                    }//unlink area from another area

                    if (args[1].equalsIgnoreCase("removeArea")) {//evo mine[0] removeArea[1] <areaName>[2]
                        String area = args[2];
                        if (args.length >= 3 && area != null) {
                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + area) == null) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " does not exist.");
                                return true;
                            }
                            areaFile.getAreaConfig().set("areas." + area, null);
                            areaFile.saveareaConfig();
                            player.sendMessage(ChatColor.GREEN + "Area " + area + " has been removed.");
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name. Usage: /evo mine removeArea <areaName>");
                            return true;
                        }
                    }//remove area

                    if (args[1].equalsIgnoreCase("addBlock")) {//evo mine[0] addBlock[1] <areaName>[2] <levelnumber>[3] <persentageinfloat>[4] (the block in hand will get seted )
                        String area = args[2];
                        String percentStr = args[4];
                        String levelStr = args[3];
                        int maxlvl = areaFile.getAreaConfig().getInt("areas." + area + ".maxLevel");
                        if (maxlvl == 0) {
                            player.sendMessage("First Set Max level");
                            return true;
                        }

                        if (args.length >= 5 && area != null && percentStr != null) {

                            if (areaFile.getAreaConfig().getConfigurationSection("areas." + area) == null) {
                                player.sendMessage(ChatColor.RED + "Area " + area + " does not exist.");
                                return true;
                            }
                            // get item in player's hand
                            if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
                                player.sendMessage(ChatColor.RED + "You must be holding a block to add it to the area.");
                                return true;
                            }
                            String blockName = player.getInventory().getItemInMainHand().getType().toString();
                            try {
                                float percentage = Float.parseFloat(percentStr);
                                int level = Integer.parseInt(levelStr);
                                if (maxlvl >= level) {
                                    if (percentage <= 0 || percentage > 100) {
                                        player.sendMessage(ChatColor.RED + "Percentage must be greater than 0 and less than or equal to 100.");
                                        return true;
                                    }

                                    areaFile.getAreaConfig().set("areas." + area + ".levels." + level + ".blocks." + blockName + ".persentage", percentage);

                                    areaFile.saveareaConfig();
                                    player.sendMessage(ChatColor.GREEN + "Block " + blockName + " with percentage " + percentage + "% added to area " + area + " For level " + level);
                                    return true;
                                } else {
                                    player.sendMessage("You are trying to set for bigger level than you set");
                                    player.sendMessage("you trying for " + level + " where max is seated to " + maxlvl);
                                    return true;
                                }

                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid number format for percentage. Please enter a valid float.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and percentage. Usage: /evo mine addBlock <areaName> <percentage>");
                            return true;
                        }
                    }//add block to area with percentage

                    if (args[1].equalsIgnoreCase("setReq")) {//evo mine[0] setReq[1] <areaName>[2] <level>[3] <requiredItemNo>[4]
                        String area = args[2];
                        String levelStr = args[3];
                        String reqStr = args[4];
                        int maxlvl = areaFile.getAreaConfig().getInt("areas." + area + ".maxLevel");
                        if (maxlvl == 0) {
                            player.sendMessage("First Set Max level");
                            return true;
                        }
                        if (args.length >= 5 && area != null && levelStr != null && reqStr != null) {

                            try {
                                int level = Integer.parseInt(levelStr);
                                int req = Integer.parseInt(reqStr);
                                if (maxlvl >= level) {
                                    areaFile.getAreaConfig().set("areas." + area + ".levels." + level + ".required", req);
                                    areaFile.saveareaConfig();
                                    player.sendMessage(ChatColor.GREEN + "Requirements for area " + area + " set to level " + level + " and item number " + req);
                                    return true;
                                } else {
                                    player.sendMessage("You can't set for more that max levl.");
                                    player.sendMessage("you trying to set for " + level + " whereas max level is " + maxlvl);
                                }


                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid number format for level or item number. Please enter valid integers.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name, level, and item number. Usage: /evo mine setReq <areaName> <level> <requiredItemNo>");
                            return true;
                        }
                    }//set requirements for area per level

                    if (args[1].equalsIgnoreCase("settype")) {//evo mine[0] settype[1] <areaName>[2] <type>[3]
                        String area = args[2];
                        String type = args[3];

                        if (args.length >= 4 && area != null && type != null) {
                            if (!type.equalsIgnoreCase("static") && !type.equalsIgnoreCase("dynamic")) {
                                player.sendMessage(ChatColor.RED + "Invalid type. Please specify 'static' , 'dynamic'.");
                                return true;
                            }

                            if (type.equalsIgnoreCase("dynamic")) {
                                String updn = args[4];
                                String layer = args[5];
                                if (!updn.equalsIgnoreCase("up") && !updn.equalsIgnoreCase("down")) {
                                    player.sendMessage(ChatColor.RED + "Invalid dynamic type. Please specify 'up' or 'down'.");
                                    return true;
                                }
                                if (layer == null) {
                                    player.sendMessage(ChatColor.RED + "Please specify a layers for dynamic type.");
                                    player.sendMessage("Correct usage: /evo mine settype <areaName> dynamic <up/down> <layer>");
                                    return true;
                                }
                                areaFile.getAreaConfig().set("areas." + area + ".type." + type + ".update", updn.toLowerCase());
                                areaFile.getAreaConfig().set("areas." + area + ".type." + type + ".layer", layer);
                                player.sendMessage("Mine Type set to Dynamic and will update " + updn + "ward and will change " + layer + " layer");
                                areaFile.saveareaConfig();
                                return true;
                            } else {
                                player.sendMessage("For Area " + area + " Mine Type set to " + type);
                                areaFile.getAreaConfig().set("areas." + area + ".type", type);
                                areaFile.saveareaConfig();
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please specify an area name and type. Usage: /evo mine settype <areaName> <type> [up/down if dynamic]");
                            return true;
                        }
                    }//set type for area

//                    if (args[1].equalsIgnoreCase("info")) {//evo mine[0] info[1] <areaName>[2]
//                        //will set later
//                    }//info area Have to setup later
                } else {
                    player.sendMessage("Unknown subcommand for /evo mine. Use /evo help for a list of commands.");
                    return true;
                }//if nothing after /evo mine
            }//mines
        }
        if (args[0].equalsIgnoreCase("yes")){
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("enter-number-number-of-item"));
            waitingForInput.add(player.getUniqueId());

            return true;
        }//yes
        if (args.length == 1 && args[0].equalsIgnoreCase("no")) {
            player.sendMessage(Evolution_Mine.getInstance().getMsg().get("cancel"));

            return true;
        }//no
        return true;
    }


}

