package org.imradigamer.squidGamePacket;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketInstantiationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.PacketTCP;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public final class SquidGamePacket extends JavaPlugin implements PluginMessageListener {

    private FileConfiguration config;
    private HashMap<UUID, Integer> playerTeams;
    private PlayerMovementListener playerMovementListener;
    @Getter
    private RaceManager raceManager;
    private TeamDefineCommand teamDefineCommand;


    @Override
    public void onEnable() {

        saveDefaultConfig();
        config = getConfig();
        playerTeams = new HashMap<>();

        playerMovementListener = new PlayerMovementListener(this); // Initialize the listener
        teamDefineCommand = new TeamDefineCommand(this, playerMovementListener);





        raceManager = new RaceManager(this,teamDefineCommand, 40);
        getCommand("nextrace").setExecutor(new NextRaceCommand(raceManager));
        getCommand("teamdefine").setExecutor(teamDefineCommand);

        Location course1Start = new Location(Bukkit.getWorld("world"), -428, 66, -526);
        Location course2Start = new Location(Bukkit.getWorld("world"), -428, 66, -472);
        raceManager.setCourseStartLocations(course1Start, course2Start);

        raceManager.setFinishArea(1, new Location(Bukkit.getWorld("world"), -429, 66, -532), new Location(Bukkit.getWorld("world"), -429, 69, -521));
        raceManager.setFinishArea(2, new Location(Bukkit.getWorld("world"), -429, 66, -479), new Location(Bukkit.getWorld("world"), -429, 69, -468));

        raceManager.addPacketArea(2, new Location(Bukkit.getWorld("world"), -385, 66, -479), new Location(Bukkit.getWorld("world"), -385, 69, -468), "ScreenSpam");
//        raceManager.addPacketArea(2, new Location(Bukkit.getWorld("world"), -372, 66, -415), new Location(Bukkit.getWorld("world"), -383, 69, -415), "ScreenArrow");
        raceManager.addPacketArea(2, new Location(Bukkit.getWorld("world"), -436, 66, -401), new Location(Bukkit.getWorld("world"), -436, 69, -414),"ScreenCircle");
        raceManager.addPacketArea(2, new Location(Bukkit.getWorld("world"), -450, 66, -447), new Location(Bukkit.getWorld("world"), -439, 69, -447),"ScreenSpam");
        raceManager.addPacketArea(1, new Location(Bukkit.getWorld("world"), -438, 66, -479), new Location(Bukkit.getWorld("world"), -438, 69, -468),"ScreenArrow");

        raceManager.addPacketArea(1, new Location(Bukkit.getWorld("world"), -385, 66, -532), new Location(Bukkit.getWorld("world"), -385, 69, -521),"ScreenSpam");
//        raceManager.addPacketArea(1, new Location(Bukkit.getWorld("world"), -382, 66, -585), new Location(Bukkit.getWorld("world"), -372, 69, -585 ),"ScreenArrow");
        raceManager.addPacketArea(1, new Location(Bukkit.getWorld("world"), -436, 66, -588), new Location(Bukkit.getWorld("world"), -436, 69, -599),"ScreenCircle");
        raceManager.addPacketArea(1, new Location(Bukkit.getWorld("world"), -439, 66, -553), new Location(Bukkit.getWorld("world"), -450, 69, -553),"ScreenSpam");
        raceManager.addPacketArea(2, new Location(Bukkit.getWorld("world"), -438, 66, -532), new Location(Bukkit.getWorld("world"), -438, 69, -521),"ScreenArrow");



        getServer().getPluginManager().registerEvents(playerMovementListener, this);

        getCommand("areadefine").setExecutor(new CommandHandler(this));
        getCommand("teamget").setExecutor(new CommandHandler(this));
        getCommand("teamget").setExecutor(new TeamGetCommand(teamDefineCommand));




        // Register the outgoing and incoming plugin channels
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, PacketTCP.PACKET_CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, PacketTCP.PACKET_CHANNEL, this);

        // Register the "/screen" command executor
        if (getCommand("screen") != null) {
            getCommand("screen").setExecutor(this);
        }
    }

    public FileConfiguration getConfiguration() {
        return config;
    }
    public PlayerMovementListener getPlayerMovementListener() {
        return playerMovementListener;
    }

    public HashMap<UUID, Integer> getPlayerTeams() {
        return playerTeams;
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

        String string = args[0];
        String playerName = args[1];

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage("Player " + playerName + " is not online!");
            return true;
        }

        sendStringScreenPacketToClient(target, string);
        return true;
    }

    public static void sendStringScreenPacketToClient(Player targetPlayer, String string) {
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
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(PacketTCP.PACKET_CHANNEL)) {
            return;
        }
        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(message))) {
            ByteArrayDataInput dataInput = ByteStreams.newDataInput(inputStream.readAllBytes());

            // Deserialize the packet
            IPacket packet = PacketTCP.read(dataInput);
            handlePacket(packet, player);
        } catch (IOException | PacketSerializationException e) {
            getLogger().severe("Error while processing incoming packet: " + e.getMessage());
            e.printStackTrace();
        } catch (PacketInstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePacket(IPacket packet, Player player) {
        if (packet instanceof ScreenPacket) {
            handleScreenPacket((ScreenPacket) packet, player);
        } else {
            getLogger().warning("Unhandled packet type: " + packet.getPacketId());
        }
    }

    public void handleScreenPacket(ScreenPacket packet, Player player) {
        String packetString = packet.getPacket();

        getLogger().info("Received ScreenPacket from " + player.getName() + ": " + packet.getPacket());

        raceManager.handleScreenPacket(packetString, player);


        switch (packetString) {
            case "CompleteSpin":
            case "CompleteSpam":
            case "CompleteArrow":
            case "CompleteCircle":
                //TODO Hacer algo con los paquetes recibidos
                break;
            default:
                getLogger().warning("Unhandled packet: " + packetString);
        }
    }

}
