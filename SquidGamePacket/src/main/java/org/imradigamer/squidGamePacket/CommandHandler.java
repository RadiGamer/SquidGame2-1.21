package org.imradigamer.squidGamePacket;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {

    private final SquidGamePacket plugin;

    public CommandHandler(SquidGamePacket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("areadefine")) {
            return new AreaDefineCommand(plugin).handleCommand(sender, args);
        }
        return false;
    }
}