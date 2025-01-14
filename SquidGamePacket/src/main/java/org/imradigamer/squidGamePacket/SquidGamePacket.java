package org.imradigamer.squidGamePacket;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketInstantiationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.exceptions.PacketSerializationException;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.PacketTCP;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SquidGamePacket extends JavaPlugin implements PluginMessageListener {

    private FileConfiguration config;
    private HashMap<UUID, Integer> playerTeams;
    private PlayerMovementListener playerMovementListener;
    @Getter
    private RaceManager raceManager;
    private TeamDefineCommand teamDefineCommand;

    private final List<Area> areas = new ArrayList<>();



    private static Economy econ = null;


    @Override
    public void onEnable() {

        areas.add(new Area(
                new Location(Bukkit.getWorld("world"),-385, 66, -467),
                new Location(Bukkit.getWorld("world"), -385, 70, -480),
                "ScreenSpam"
        ));

        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -385, 66, -533),
                new Location(Bukkit.getWorld("world"), -385, 70, -520),
                "ScreenSpam"
        ));

        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -438, 66, -480),
                new Location(Bukkit.getWorld("world"), -438, 70, -467),
                "ScreenArrow"
        ));

        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -438, 66, -533),
                new Location(Bukkit.getWorld("world"), -438, 70, -520),
                "ScreenArrow"
        ));

        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -436, 66, -400),
                new Location(Bukkit.getWorld("world"), -436, 70, -413),
                "ScreenCircle"
        ));
        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -436, 66, -587),
                new Location(Bukkit.getWorld("world"), -436, 70, -600),
                "ScreenCircle"
        ));
        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -438, 66, -553),
                new Location(Bukkit.getWorld("world"), -451, 70, -553),
                "ScreenSpin"
        ));
        areas.add(new Area(
                new Location(Bukkit.getWorld("world"), -438, 66, -447),
                new Location(Bukkit.getWorld("world"), -451, 70, -447),
                "ScreenSpin"
        ));
        Bukkit.getLogger().info("Areas added: " + areas.size());


        new AreaCheckTask().runTaskTimer(this, 0L, 20L); // Runs every second

        new MoneyListener(this);

        setupEconomy();

        saveDefaultConfig();
        config = getConfig();
        playerTeams = new HashMap<>();

        playerMovementListener = new PlayerMovementListener(this);
        teamDefineCommand = new TeamDefineCommand(this, playerMovementListener);


        raceManager = new RaceManager(this,teamDefineCommand, 40);
        getCommand("nextrace").setExecutor(new NextRaceCommand(raceManager));
        getCommand("teamdefine").setExecutor(teamDefineCommand);

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
        // We only care about the "/screen" command
        if (!cmd.getName().equalsIgnoreCase("screen")) {
            return false;
        }

        // Usage check
        if (args.length != 2) {
            sender.sendMessage("Usage: /screen <minigame> <playerName|@a|@alive>");
            return true;
        }

        String string = args[0];
        String targetSelector = args[1];

        // Handle @a (all players)
        if (targetSelector.equalsIgnoreCase("@a")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendStringScreenPacketToClient(player, string);
            }
            return true;
        }

        // Handle @alive (players in Survival or Adventure mode)
        if (targetSelector.equalsIgnoreCase("@alive")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                    sendStringScreenPacketToClient(player, string);
                }
            }
            return true;
        }

        // Handle specific player name
        Player target = Bukkit.getPlayerExact(targetSelector);
        if (target == null) {
            sender.sendMessage("Player " + targetSelector + " is not online!");
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

        givePoints(player, 1);

        raceManager.handleScreenPacket(packetString, player);

        switch (packetString) {
            case "CompleteSpin":
            case "CompleteSpam":
            case "CompleteArrow":
            case "CompleteCircle":
                break;
            default:
                getLogger().warning("Unhandled packet: " + packetString);
        }
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public static Economy getEconomy() {
        return econ;
    }
    public void givePoints(Player player, int points) {
        if (econ != null) {
            econ.depositPlayer(player, points);
        }
    }
    public void removePoints(Player player, int points) {
        if (econ != null) {
            econ.withdrawPlayer(player, points);
        }
    }



    private class AreaCheckTask extends BukkitRunnable {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();

                for (Area area : areas) {
                    if (area.isPlayerInArea(playerLocation)) {
                        // Player is inside the area
                        sendStringScreenPacketToClient(player, area.getMessage());
                        Bukkit.getLogger().info("Player " + player.getName() + " is inside area: " + area.getMessage());

                        // Teleport the player based on their view direction
                        teleportPlayerInViewDirection(player);
                    }
                }
            }
        }

        private void teleportPlayerInViewDirection(Player player) {
            // Get the player's current location and direction
            Location currentLocation = player.getLocation();
            Location targetLocation = currentLocation.clone();

            // Calculate a normalized direction vector
            Vector direction = currentLocation.getDirection().normalize();

            // Keep the direction aligned with the XZ plane (horizontal only)
            direction.setY(0); // Ignore Y-axis to keep the player at the same height
            direction.normalize(); // Re-normalize after modifying Y-axis

            // Move 2 blocks forward based on the player's direction
            targetLocation.add(direction.multiply(2));

            // Set the target location's pitch and yaw to match the player's view
            targetLocation.setYaw(currentLocation.getYaw());
            targetLocation.setPitch(currentLocation.getPitch());

            // Teleport the player
            player.teleport(targetLocation);
            Bukkit.getLogger().info("Teleported " + player.getName() + " to " + targetLocation);
        }
    }
}
