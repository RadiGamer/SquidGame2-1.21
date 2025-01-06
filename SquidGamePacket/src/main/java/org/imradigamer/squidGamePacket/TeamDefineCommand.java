package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeamDefineCommand implements CommandExecutor {

    private final SquidGamePacket plugin;
    private final PlayerMovementListener playerMovementListener;
    private final Map<Integer, List<UUID>> finalizedTeams = new HashMap<>();
    private boolean timerActive = false;

    public TeamDefineCommand(SquidGamePacket plugin, PlayerMovementListener listener) {
        this.plugin = plugin;
        this.playerMovementListener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 0) {
            player.sendMessage("Uso: /teamdefine (sin argumentos)");
            return true;
        }

        if (timerActive) {
            player.sendMessage("El temporizador ya está en marcha.");
            return true;
        }

        player.sendMessage("El temporizador de formación de equipos ha comenzado. Tienen 30 segundos para entrar a sus áreas de equipo.");

        timerActive = true;
        playerMovementListener.startTimer();

        new BukkitRunnable() {
            @Override
            public void run() {
                timerActive = false;
                playerMovementListener.stopTimer();
                player.sendMessage("El tiempo ha terminado. Los equipos están bloqueados.");
                finalizeTeams();
            }
        }.runTaskLater(plugin, 20L * 30); // 30-second countdown

        return true;
    }

    private void finalizeTeams() {
        finalizedTeams.clear();

        plugin.getPlayerTeams().forEach((uuid, teamNumber) -> {
            finalizedTeams.computeIfAbsent(teamNumber, k -> new ArrayList<>()).add(uuid);
        });

        Bukkit.getLogger().info("Equipos finalizados: " + finalizedTeams);
    }

    public Map<Integer, List<UUID>> getFinalizedTeams() {
        return Collections.unmodifiableMap(finalizedTeams);
    }

    public List<UUID> getTeamMembers(int teamNumber) {
        return finalizedTeams.getOrDefault(teamNumber, Collections.emptyList());
    }

    public boolean isTimerActive() {
        return timerActive;
    }
    public Integer getTeamForPlayer(UUID playerUUID) {
        for (Map.Entry<Integer, List<UUID>> entry : finalizedTeams.entrySet()) {
            if (entry.getValue().contains(playerUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
