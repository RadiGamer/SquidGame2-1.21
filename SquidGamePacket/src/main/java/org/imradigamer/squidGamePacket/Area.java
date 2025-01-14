package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Area {
    private final Location minCorner;
    private final Location maxCorner;
    private final String message;

    public Area(Location minCorner, Location maxCorner, String message) {
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.message = message;
    }

    public Location getMinCorner() {
        return minCorner;
    }

    public Location getMaxCorner() {
        return maxCorner;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPlayerInArea(Location loc) {
        // Get block-level coordinates for the player's location
        int playerX = loc.getBlockX();
        int playerY = loc.getBlockY();
        int playerZ = loc.getBlockZ();

        // Get block-level bounds for the area
        int minX = Math.min(minCorner.getBlockX(), maxCorner.getBlockX());
        int maxX = Math.max(minCorner.getBlockX(), maxCorner.getBlockX());
        int minY = Math.min(minCorner.getBlockY(), maxCorner.getBlockY());
        int maxY = Math.max(minCorner.getBlockY(), maxCorner.getBlockY());
        int minZ = Math.min(minCorner.getBlockZ(), maxCorner.getBlockZ());
        int maxZ = Math.max(minCorner.getBlockZ(), maxCorner.getBlockZ());

        // Check if the player is within the bounds
        boolean inWorld = loc.getWorld().equals(minCorner.getWorld());
        boolean inX = playerX >= minX && playerX <= maxX;
        boolean inY = playerY >= minY && playerY <= maxY;
        boolean inZ = playerZ >= minZ && playerZ <= maxZ;


        return inWorld && inX && inY && inZ;
    }

}
