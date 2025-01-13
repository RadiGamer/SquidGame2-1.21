package org.imradigamer.squidGame2Game2;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.PacketTCP;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class SquidGame2Game2 extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "squidgamegame2screens:open_screen");

        // 2) Register the "/screen" command executor
        if (getCommand("screen") != null) {
            getCommand("screen").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // We only care about "/screen" command
        if (!cmd.getName().equalsIgnoreCase("screen")) {
            return false;
        }

        // Usage check
        if (args.length != 2) {
            sender.sendMessage("Usage: /screen <minigame> <playerName>");
            return true;
        }

        String minigame = args[0];
        String playerName = args[1];

        // Get the online Player by name
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("Player " + playerName + " is not online!");
            return true;
        }
        sendStringScreenPacketToClient((Player) sender, "minigame1");
        return true;
    }
    public static void sendStringScreenPacketToClient(Player targetPlayer,String string) {
        try {
            IPacket packet = new ScreenPacket(string);
            packetSendInfo(packet, targetPlayer);
            targetPlayer.sendPluginMessage(SquidGame2Game2.getInstance(), PacketTCP.PACKET_CHANNEL, PacketTCP.write(packet));
        } catch (PacketSerializationException e) {
            throw new RuntimeException(e);
        }
    }
    public static SquidGame2Game2 getInstance() {
        return getPlugin(SquidGame2Game2.class);
    }
    private static void packetSendInfo(IPacket packet, Player targetPlayer) {
        SquidGame2Game2.getInstance().getLogger().info("Packet: " + packet.getPacketId() + ", sent to the client: " + targetPlayer.getName());
    }

}
