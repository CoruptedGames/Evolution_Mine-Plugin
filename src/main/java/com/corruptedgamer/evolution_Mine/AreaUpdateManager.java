package com.corruptedgamer.evolution_Mine;

import com.corruptedgamer.evolution_Mine.Ymls.areayml;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

/**
 * Area update manager.
 *
 * Added WorldGuard integration:
 * - Ensures a cuboid region exists for each mine and updates its min/max to match the mine bounds.
 * - Sets region priority high (configurable per-area if needed).
 * - Sets the build flag to ALLOW so block breaking is allowed inside the mine region.
 *
 * Added player-safety:
 * - Before refilling, teleports any players inside the mine bounding box to the top/center of the mine to avoid suffocation.
 *
 * Notes:
 * - This code uses WorldEdit/WorldGuard 7.x style API (BukkitAdapter, BlockVector3, WorldGuard.getInstance()).
 * - You must add WorldEdit and WorldGuard as compile/runtime dependencies and declare them in plugin.yml (soft-depend or depend).
 * - This code writes the region ID into the area's ConfigurationSection in memory (area.set("region", regionId)).
 *   Persisting this to disk requires calling your areayml save method. I didn't call a concrete save method to avoid
 *   depending on a specific areayml API that wasn't provided. If your areayml implementation provides a method like
 *   saveAreas() or saveAreaConfig(), add a call after setting the region value to persist it.
 */
public class AreaUpdateManager {
    private final Evolution_Mine plugin;
    private final areayml areaFile;

    // Track running tasks per area (static so reloads are always cleaned up)
    private static final Map<String, BukkitRunnable> runningTasks = new HashMap<>();

    public AreaUpdateManager(Evolution_Mine plugin) {
        this.plugin = plugin;
        this.areaFile = plugin.getAreaFile();
    }

    public boolean isDebug() {
        return plugin.getConfig().getBoolean("mine-updater-debug", false);
    }

    /**
     * Starts the area updater for all areas.
     * Cancels all previous updaters before starting new ones.
     */
    public void startUpdater() {
        // Cancel previous update tasks
        for (BukkitRunnable task : runningTasks.values()) {
            task.cancel();
        }
        runningTasks.clear();

        ConfigurationSection areas = areaFile.getAreaConfig().getConfigurationSection("areas");
        if (areas == null) {
            Bukkit.getLogger().warning("[EvolutionMine] No areas found in config!");
            return;
        }

        for (String areaName : areas.getKeys(false)) {
            ConfigurationSection area = areas.getConfigurationSection(areaName);
            if (area == null) continue;
            int delay = area.getInt("delay", 60);

            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    updateArea(areaName, area);
                }
            };
            runnable.runTaskTimer(plugin, 20L, delay * 20L);
            runningTasks.put(areaName, runnable);

            if (isDebug()) {
                Bukkit.getLogger().info("[EvolutionMine] Started updater for area " + areaName + " with delay " + delay + "s.");
            }
        }
    }

    /**
     * Refills an area according to its current level and block settings.
     * Also ensures a WorldGuard region exists for the mine and teleports players out of the fill area.
     */
    private void updateArea(String areaName, ConfigurationSection area) {
        if (isDebug()) {
            Bukkit.getLogger().info("[EvolutionMine] updateArea called for: " + areaName);
        }

        String worldName = area.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("[EvolutionMine] World not found: " + worldName + " for area: " + areaName);
            return;
        }

        double x1 = area.getDouble("pos1.x");
        double y1 = area.getDouble("pos1.y");
        double z1 = area.getDouble("pos1.z");
        double x2 = area.getDouble("pos2.x");
        double y2 = area.getDouble("pos2.y");
        double z2 = area.getDouble("pos2.z");

        int minX = (int) Math.min(x1, x2);
        int maxX = (int) Math.max(x1, x2);
        int minY = (int) Math.min(y1, y2);
        int maxY = (int) Math.max(y1, y2);
        int minZ = (int) Math.min(z1, z2);
        int maxZ = (int) Math.max(z1, z2);

        int currentLevel = area.getInt("currentLevel", 1);
        if (isDebug()) {
            Bukkit.getLogger().info("[EvolutionMine] Area: " + areaName + " currentLevel: " + currentLevel);
        }

        // Ensure and update WorldGuard region for this area
        try {
            ensureWorldGuardRegion(areaName, area, world, minX, minY, minZ, maxX, maxY, maxZ);
        } catch (Exception e) {
            Bukkit.getLogger().severe("[EvolutionMine] Failed to ensure WorldGuard region for area " + areaName + ": " + e.getMessage());
            if (isDebug()) e.printStackTrace();
        }

        // Teleport players inside the area to the top/center of the mine prior to refill to prevent suffocation
        int teleported = teleportPlayersOutOfBounds(world, minX, minY, minZ, maxX, maxY, maxZ, areaName);
        if (isDebug()) {
            Bukkit.getLogger().info("[EvolutionMine] Teleported " + teleported + " players out of area " + areaName + " before refill.");
        }

        ConfigurationSection levels = area.getConfigurationSection("levels");
        if (levels == null) {
            Bukkit.getLogger().warning("[EvolutionMine] No levels found for area: " + areaName);
            return;
        }
        ConfigurationSection levelSection = levels.getConfigurationSection(String.valueOf(currentLevel));
        if (levelSection == null) {
            Bukkit.getLogger().warning("[EvolutionMine] No config for currentLevel: " + currentLevel + " in area: " + areaName);
            return;
        }

        ConfigurationSection blocksSection = levelSection.getConfigurationSection("blocks");
        if (blocksSection == null) {
            Bukkit.getLogger().warning("[EvolutionMine] No blocks section for level: " + currentLevel + " in area: " + areaName);
            return;
        }

        Map<Material, Float> blockChances = new LinkedHashMap<>();
        for (String blockName : blocksSection.getKeys(false)) {
            Material mat = Material.matchMaterial(blockName);
            if (mat == null) {
                Bukkit.getLogger().warning("[EvolutionMine] Invalid block type: " + blockName + " in area: " + areaName);
                continue;
            }
            float chance = (float) blocksSection.getDouble(blockName + ".persentage", 0.0);
            if (isDebug()) {
                Bukkit.getLogger().info("[EvolutionMine] Block: " + blockName + " chance: " + chance);
            }
            blockChances.put(mat, chance);
        }
        if (blockChances.isEmpty()) {
            Bukkit.getLogger().warning("[EvolutionMine] No valid blocks to refill for area: " + areaName);
            return;
        }

        Random rand = new Random();
        int blockCount = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material chosenMat = getRandomBlock(blockChances, rand);
                    if (chosenMat != null) {
                        block.setType(chosenMat);
                        blockCount++;
                    }
                }
            }
        }
        if (isDebug()) {
            Bukkit.getLogger().info("[EvolutionMine] Refilling area: " + areaName + " completed. Blocks changed: " + blockCount);
        }
    }

    /**
     * Ensures a WorldGuard cuboid region exists for the given mine area and updates it to match the provided bounds.
     * If the region does not exist it will be created and its ID will be written into the area's configuration (in memory).
     *
     * IMPORTANT: Persisting the region ID to disk requires calling your areayml save method. See notes at top of file.
     */
    private void ensureWorldGuardRegion(String areaName, ConfigurationSection area, World world,
                                        int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            if (container == null) {
                if (isDebug()) Bukkit.getLogger().info("[EvolutionMine] WorldGuard RegionContainer is null; skipping region creation for " + areaName);
                return;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            if (regions == null) {
                if (isDebug()) Bukkit.getLogger().info("[EvolutionMine] RegionManager for world " + world.getName() + " is null; skipping region creation for " + areaName);
                return;
            }

            // Determine region id: prefer stored one in config, otherwise create a consistent one
            String regionId = area.getString("region", null);
            if (regionId == null || regionId.isEmpty()) {
                // sanitize areaName to a WG-compatible id (lowercase, no spaces)
                regionId = "evo_mine_" + areaName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
                // write back into the area's configuration (in memory)
                area.set("region", regionId);
                // NOTE: to persist this change to disk, call your areayml save method here (not included)
            }

            // If region exists, remove it and recreate so min/max update will apply cleanly for cuboid
            if (regions.hasRegion(regionId)) {
                regions.removeRegion(regionId);
            }

            BlockVector3 min = BlockVector3.at(minX, minY, minZ);
            BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);

            // Priority high so it won't conflict with other regions. Make this configurable if you want.
            int priority = area.getInt("region-priority", 1000);
            region.setPriority(priority);

            // Allow building (block breaking/placing). WorldGuard's BUILD flag controls many block-place/break checks.
            try {
                region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
                region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);

            } catch (Exception flagEx) {
                // Some WG distributions may differ in flag classes; log in debug but continue
                if (isDebug()) {
                    Bukkit.getLogger().info("[EvolutionMine] Could not set BUILD flag on region " + regionId + ": " + flagEx.getMessage());
                }
            }

            regions.addRegion(region);

            if (isDebug()) {
                Bukkit.getLogger().info("[EvolutionMine] WorldGuard region ensured: " + regionId + " for area " + areaName +
                        " bounds: [" + minX + "," + minY + "," + minZ + "] -> [" + maxX + "," + maxY + "," + maxZ + "]");
            }

        } catch (Throwable t) {
            Bukkit.getLogger().severe("[EvolutionMine] Exception while ensuring WorldGuard region for area " + areaName + ": " + t.getMessage());
            if (isDebug()) t.printStackTrace();
        }
    }

    /**
     * Teleports players who are inside the bounding box to the top-center of the mine (maxY + 1) to avoid suffocation.
     * Returns the number of players teleported.
     */
    private int teleportPlayersOutOfBounds(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String areaName) {
        int teleported = 0;

        // compute top/center teleport location (use center x/z of the area)
        double centerX = (minX + maxX) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;
//        double teleportY = Math.max(maxY, world.getHighestBlockYAt((int) centerX, (int) centerZ)) + 1.0;
        double teleportY = plugin.getAreaFile().getAreaConfig().getDouble("areas."+areaName+".tplocy");
        Location tpLocation = new Location(world, centerX + 0.5, teleportY, centerZ + 0.5);

        for (Player player : world.getPlayers()) {
            Location loc = player.getLocation();
            int px = loc.getBlockX();
            int py = loc.getBlockY();
            int pz = loc.getBlockZ();

            boolean inside = px >= minX && px <= maxX
                    && py >= minY && py <= maxY
                    && pz >= minZ && pz <= maxZ;

            if (inside) {
                // Teleport to top-center of the mine
                try {
                    player.teleport(tpLocation);
                    teleported++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[EvolutionMine] Failed to teleport player " + player.getName() + " out of mine area: " + e.getMessage());
                }
            }
        }
        return teleported;
    }

    /**
     * Picks a random block type based on percentage chances.
     */
    private Material getRandomBlock(Map<Material, Float> blockChances, Random rand) {
        float total = 0;
        for (float c : blockChances.values()) total += c;
        if (total <= 0) return null;

        float r = rand.nextFloat() * total;
        float cumulative = 0;
        for (Map.Entry<Material, Float> entry : blockChances.entrySet()) {
            cumulative += entry.getValue();
            if (r <= cumulative) return entry.getKey();
        }
        for (Material m : blockChances.keySet()) return m;
        return null;
    }
}