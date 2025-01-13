package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowballRewardTask {

    private final SquidGamePacket plugin;
    private final Location rewardAreaCorner1;
    private final Location rewardAreaCorner2;
    private boolean isRaceActive;

    public SnowballRewardTask(SquidGamePacket plugin, Location corner1, Location corner2) {
        this.plugin = plugin;
        this.rewardAreaCorner1 = corner1;
        this.rewardAreaCorner2 = corner2;
        this.isRaceActive = false; // Default to inactive
    }

    public void setRaceActive(boolean active) {
        this.isRaceActive = active;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRaceActive) {
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location loc = player.getLocation();
                    if (isPlayerInArea(loc, rewardAreaCorner1, rewardAreaCorner2)) {
                        player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 140L); // Runs every 7 seconds (7 * 20 ticks)
    }

    private boolean isPlayerInArea(Location loc, Location corner1, Location corner2) {
        int x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        return loc.getBlockX() >= x1 && loc.getBlockX() <= x2 &&
                loc.getBlockY() >= y1 && loc.getBlockY() <= y2 &&
                loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
    }
}
