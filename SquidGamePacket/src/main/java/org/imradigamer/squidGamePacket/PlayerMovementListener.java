package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerMovementListener implements Listener {

    private final SquidGamePacket plugin;
    private boolean isTimerActive = false;

    public PlayerMovementListener(SquidGamePacket plugin) {
        this.plugin = plugin;
    }

    public void startTimer() {
        isTimerActive = true;
        Bukkit.getLogger().info("Ya pueden formar equipos");
    }

    public void stopTimer() {
        isTimerActive = false;
        Bukkit.getLogger().info("Equipos bloqueados");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        Integer team = plugin.getPlayerTeams().get(player.getUniqueId());

        if (!isTimerActive) {
            return; // Ignore movements if the timer is not active
        }

        FileConfiguration config = plugin.getConfiguration();
        HashMap<UUID, Integer> playerTeams = plugin.getPlayerTeams();

        for (String key : config.getConfigurationSection("teams").getKeys(false)) {
            int teamNumber = Integer.parseInt(key);

            Location corner1 = getCornerLocation(config, teamNumber, "corner1");
            Location corner2 = getCornerLocation(config, teamNumber, "corner2");

            if (corner1 != null && corner2 != null && isWithinArea(loc, corner1, corner2)) {
                if (!playerTeams.containsKey(player.getUniqueId()) || playerTeams.get(player.getUniqueId()) != teamNumber) {
                    playerTeams.put(player.getUniqueId(), teamNumber);
                    player.sendMessage("¡Te has unido al equipo " + teamNumber + "!");
                }
                return;
            }
        }

        if (playerTeams.containsKey(player.getUniqueId())) {
            playerTeams.remove(player.getUniqueId());
            player.sendMessage("Has salido de tu equipo.");
        }
    }

    private Location getCornerLocation(FileConfiguration config, int teamNumber, String corner) {
        String path = "teams." + teamNumber + "." + corner;

        if (!config.contains(path)) {
            return null;
        }

        String worldName = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private boolean isWithinArea(Location loc, Location corner1, Location corner2) {
        double x1 = Math.min(corner1.getX(), corner2.getX());
        double y1 = Math.min(corner1.getY(), corner2.getY());
        double z1 = Math.min(corner1.getZ(), corner2.getZ());

        double x2 = Math.max(corner1.getX(), corner2.getX());
        double y2 = Math.max(corner1.getY(), corner2.getY());
        double z2 = Math.max(corner1.getZ(), corner2.getZ());

        return loc.getX() >= x1 && loc.getX() <= x2 &&
                loc.getY() >= y1 && loc.getY() <= y2 &&
                loc.getZ() >= z1 && loc.getZ() <= z2;
    }

}