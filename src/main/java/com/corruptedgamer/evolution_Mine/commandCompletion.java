package com.corruptedgamer.evolution_Mine;

import com.corruptedgamer.evolution_Mine.Ymls.areayml;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;

public class commandCompletion implements TabCompleter {
    areayml areaFile = Evolution_Mine.getInstance().getAreaFile();
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player player) || !player.isOp() || !player.hasPermission("evomine.admin")) {
            return Collections.emptyList(); // No suggestions for non-OPs
        }
        // List to store all possible completions
        List<String> completions = new ArrayList<>();
        // List to hold the filtered suggestions
        final List<String> suggestions = new ArrayList<>();

        // --- Get available area names from your YAML ---

        Set<String> areaNames = areaFile.getAreaConfig().getConfigurationSection("areas") != null ?
                areaFile.getAreaConfig().getConfigurationSection("areas").getKeys(false) :
                Collections.emptySet();


        // ARGUMENT 1: /evo <arg1>
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("help", "guide", "reload", "mine", "yes", "no"));
        }

        // ARGUMENT 2: /evo mine <arg2>
        else if (args.length == 2 && args[0].equalsIgnoreCase("mine")) {
            suggestions.addAll(Arrays.asList(
                    "setArea", "setPos1", "setPos2", "listArea", "setMaxLvl",
                    "setItem", "setDelay", "setNpc", "removeArea",
                    "linkArea", "unlinkArea", "addBlock", "setReq", "settype", "setloctp"
            ));
        }

        // ARGUMENT 3: /evo mine <subcommand> <arg3>
        else if (args.length == 3 && args[0].equalsIgnoreCase("mine")) {
            String subCommand = args[1].toLowerCase();

            // Subcommands that require an existing <areaName>
            if (subCommand.matches("setpos1|setpos2|setmaxlvl|setitem|setdelay|setnpc|removearea|info|setreq|setloctp|settype")) {
                suggestions.addAll(areaNames);
            }
            // Subcommands that require a NEW <areaName>
            else if (subCommand.equals("setarea")) {
                // If you want to suggest new names, but usually you don't for 'setArea'
                // Returning an empty list or 'null' is often best here.
                return Collections.emptyList();
            }
            // Subcommands that require an existing <areaName> for linking
            else if (subCommand.matches("linkarea|unlinkarea")) {
                suggestions.addAll(areaNames);
            }
            // Subcommand that requires an existing <areaName> and level
            else if (subCommand.equals("addblock")) {
                suggestions.addAll(areaNames);
            }
        }

        // ARGUMENT 4: /evo mine <subcommand> <areaName> <arg4>
        else if (args.length == 4 && args[0].equalsIgnoreCase("mine")) {
            String subCommand = args[1].toLowerCase();
            String areaName = args[2];

            if (areaNames.contains(areaName)) {
                // /evo mine setMaxLvl <areaName> <maxLevel>
                if (subCommand.equals("setmaxlvl") || subCommand.equals("setdelay")) {
                    suggestions.addAll(Arrays.asList("10", "20", "50", "100")); // Suggest common numbers
                }
                // /evo mine linkArea <areaName> <thatToBeLinked>
                else if (subCommand.matches("linkarea|unlinkarea")) {
                    suggestions.addAll(areaNames);
                }
                // /evo mine addBlock <areaName> <levelnumber>
                else if (subCommand.equals("addblock") || subCommand.equals("setreq")) {
                    // Suggest max levels based on config
                    int maxLevel = areaFile.getAreaConfig().getInt("areas." + areaName + ".maxLevel", 0);
                    for (int i = 1; i <= maxLevel; i++) {
                        suggestions.add(String.valueOf(i));
                    }
                }
                // /evo mine settype <areaName> <type>
                else if (subCommand.equals("settype")) {
                    suggestions.addAll(Arrays.asList("static", "dynamic"));
                }
            }
        }

        // ARGUMENT 5: /evo mine settype <areaName> dynamic <arg5>
        else if (args.length == 5 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("settype") && args[3].equalsIgnoreCase("dynamic")) {
            // /evo mine settype <areaName> dynamic <up/down>
            suggestions.addAll(Arrays.asList("up", "down"));
        }

        // ARGUMENT 6: /evo mine settype <areaName> dynamic <up/down> <arg6>
        else if (args.length == 6 && args[0].equalsIgnoreCase("mine") && args[1].equalsIgnoreCase("settype") && args[3].equalsIgnoreCase("dynamic")) {
            // /evo mine settype <areaName> dynamic <up/down> <layer>
            suggestions.addAll(Arrays.asList("1", "2", "3")); // Suggest common layer numbers
        }

        // --- Filter and Return ---
        if (suggestions.isEmpty()) {
            // If no custom suggestions are found, return null to fall back to the default (e.g., player names)
            return null;
        }

        // Use Bukkit's utility to filter the list based on what the player is currently typing
        StringUtil.copyPartialMatches(args[args.length - 1], suggestions, completions);
        Collections.sort(completions); // Sort the final list

        return completions;
    }
}
