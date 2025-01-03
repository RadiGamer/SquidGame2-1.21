package org.imradigamer.squidGamePacket;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.IPacket;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.PacketTCP;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class SquidGamePacket extends JavaPlugin implements PluginMessageListener {

    @Override
    public void onEnable() {

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "squidgame2:open_screen");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "squidgame2:open_screen", this);

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
        sendStringScreenPacketToClient((Player) sender, minigame);
        return true;
    }
    public static void sendStringScreenPacketToClient(Player targetPlayer,String string) {
        try {
            IPacket packet = new ScreenPacket(string);
            packetSendInfo(packet, targetPlayer);
            targetPlayer.sendPluginMessage(SquidGamePacket.getInstance(), PacketTCP.PACKET_CHANNEL, PacketTCP.write(packet));
        } catch (PacketSerializationException e) {
            throw new RuntimeException(e);
        }
    }
    public static SquidGamePacket getInstance() {
        return getPlugin(SquidGamePacket.class);
    }

    private static void packetSendInfo(IPacket packet, Player targetPlayer) {
        SquidGamePacket.getInstance().getLogger().info("Packet: " + packet.getPacketId() + ", sent to the client: " + targetPlayer.getName());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {

    }
}
