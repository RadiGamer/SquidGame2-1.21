package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TeamGetCommand implements CommandExecutor {

    private final TeamDefineCommand teamDefineCommand;

    public TeamGetCommand(TeamDefineCommand teamDefineCommand) {
        this.teamDefineCommand = teamDefineCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Uso: /teamget <nombreJugador>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage("El jugador " + args[0] + " no está en línea.");
            return true;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        Integer teamNumber = teamDefineCommand.getTeamForPlayer(targetUUID);

        if (teamNumber == null) {
            sender.sendMessage("El jugador " + targetPlayer.getName() + " no pertenece a ningún equipo.");
            return true;
        }

        List<UUID> teamMembers = teamDefineCommand.getTeamMembers(teamNumber);
        StringBuilder membersList = new StringBuilder("Miembros del equipo " + teamNumber + ": ");
        for (UUID uuid : teamMembers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                membersList.append(player.getName()).append(" ");
            }
        }

        sender.sendMessage(membersList.toString().trim());
        return true;
    }
}
