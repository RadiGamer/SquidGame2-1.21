package org.imradigamer.squidGamePacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AreaDefineCommand {

    private final SquidGamePacket plugin;

    public AreaDefineCommand(SquidGamePacket plugin) {
        this.plugin = plugin;
    }

    public boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage("Uso: /areadefine <NúmeroEquipo> <corner1|corner2>");
            return true;
        }

        int teamNumber;
        try {
            teamNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("El número de equipo debe ser un número válido.");
            return true;
        }

        String cornerType = args[1].toLowerCase();
        if (!cornerType.equals("corner1") && !cornerType.equals("corner2")) {
            player.sendMessage("El segundo argumento debe ser 'corner1' o 'corner2'.");
            return true;
        }

        Location loc = player.getLocation();
        String path = "teams." + teamNumber + "." + cornerType;

        FileConfiguration config = plugin.getConfiguration();
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        plugin.saveConfig();

        player.sendMessage("" + cornerType + " definido para el equipo " + teamNumber + " en tu ubicación actual.");
        return true;
    }

    public boolean handleTeamGetCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Uso: /teamget <Nickname>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage("Jugador no encontrado o no está en línea.");
            return true;
        }

        Integer teamNumber = plugin.getPlayerTeams().get(targetPlayer.getUniqueId());
        if (teamNumber == null) {
            sender.sendMessage("El jugador " + args[0] + " no pertenece a ningún equipo.");
        } else {
            sender.sendMessage("El jugador " + args[0] + " pertenece al equipo " + teamNumber + ".");
        }

        return true;
    }
}
