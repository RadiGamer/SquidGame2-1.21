package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class TeamDefineCommand {

    private final SquidGamePacket plugin;

    public TeamDefineCommand(SquidGamePacket plugin) {
        this.plugin = plugin;
    }

    public boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage("La dinámica de equipos ha comenzado. Se bloquearán en 10 minutos.");

        // Start the timer using the listener instance
        PlayerMovementListener listener = plugin.getPlayerMovementListener();
        listener.startTimer();

        startTeamLockingTask(listener);

        return true;
    }

    private void startTeamLockingTask(PlayerMovementListener listener) {
        new BukkitRunnable() {
            @Override
            public void run() {
                listener.stopTimer();
                lockTeams();
                highlightPlayersWithoutTeam();
            }
        }.runTaskLater(plugin, 10 * 60 * 20); // 10 minutes in ticks
    }

    private void lockTeams() {
        HashMap<UUID, Integer> playerTeams = plugin.getPlayerTeams();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            if (!playerTeams.containsKey(playerId)) {
                player.sendMessage("No estás en un equipo. ¡Encuentra uno rápidamente!");
            } else {
                player.sendMessage("Los equipos ahora están bloqueados.");
            }
        }
    }

    private void highlightPlayersWithoutTeam() {
        HashMap<UUID, Integer> playerTeams = plugin.getPlayerTeams();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            if (!playerTeams.containsKey(playerId)) {
                player.setGlowing(true);
                player.sendMessage("¡Estás destacado porque no tienes equipo!");
            } else {
                player.setGlowing(false);
            }
        }
    }
}
