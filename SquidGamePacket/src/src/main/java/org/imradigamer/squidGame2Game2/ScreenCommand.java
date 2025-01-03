package org.imradigamer.squidGame2Game2;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenCommand implements CommandExecutor {

    private final SquidGame2Game2 plugin;

    public ScreenCommand(SquidGame2Game2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /screen <screentype> <player>");
            return true;
        }

        String screenType = args[0];
        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        // Send the plugin message
        sendScreenMessage(targetPlayer, screenType);
        sender.sendMessage("§aSent screen type '" + screenType + "' to player " + targetPlayer.getName());
        return true;
    }

    private void sendScreenMessage(Player player, String screenType) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);

            // Write the screen type to the message
            dataOut.writeUTF(screenType);

            // Send the plugin message
            player.sendPluginMessage(plugin, "squidgamegame2screens:open_screen", out.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
