package org.imradigamer.squidGamePacket;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NextRaceCommand implements CommandExecutor {

    private final RaceManager raceManager;

    public NextRaceCommand(RaceManager raceManager) {
        this.raceManager = raceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("Iniciando la próxima carrera...");
        raceManager.startNextRace();

        return true;
    }
}
