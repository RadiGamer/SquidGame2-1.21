package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaceManager {

    private final SquidGamePacket plugin;
    private final TeamDefineCommand teamDefineCommand;
    private final Map<Integer, List<Checkpoint>> courseCheckpoints = new HashMap<>();
    private final Queue<Integer> raceQueue = new LinkedList<>();
    private final Map<Integer, Integer> checkpointProgress = new HashMap<>();
    private final Map<Integer, Map<UUID, Set<String>>> teamPacketProgress = new HashMap<>();
    private Location course1Start;
    private Location course2Start;
    private final Map<Integer, List<PacketArea>> packetAreas = new HashMap<>();
    private final Map<UUID, Set<PacketArea>> sentPackets = new HashMap<>();
    private final Map<UUID, Integer> playerProgress = new HashMap<>();



    public RaceManager(SquidGamePacket plugin, TeamDefineCommand teamDefineCommand, int totalTeams) {
        this.plugin = plugin;
        this.teamDefineCommand = teamDefineCommand;

        for (int i = 1; i <= totalTeams; i++) {
            raceQueue.add(i);
        }
        initializeCourses();
    }

    private void initializeCourses() {
        List<Checkpoint> course2 = Arrays.asList(
                new Checkpoint(new Location(Bukkit.getWorld("world"), -384, 66, -479), new Location(Bukkit.getWorld("world"), -384, 69, -468), "CompleteSpam"), //ERROR Expected 3 arguments but found 2
//                new Checkpoint(new Location(Bukkit.getWorld("world"), -372, 66, -414), new Location(Bukkit.getWorld("world"), -383, 69, -414), "CompleteArrow"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -401), new Location(Bukkit.getWorld("world"), -437, 69, -414),"CompleteCircle"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -450, 66, -448), new Location(Bukkit.getWorld("world"), -439, 69, -448),"CompleteSpam"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -479), new Location(Bukkit.getWorld("world"), -437, 69, -468),"CompleteArrow")
        );

        List<Checkpoint> course1 = Arrays.asList(
                new Checkpoint(new Location(Bukkit.getWorld("world"), -384, 66, -532), new Location(Bukkit.getWorld("world"), -384, 69, -521),"CompleteSpam"),
//                new Checkpoint(new Location(Bukkit.getWorld("world"), -383, 66, -586), new Location(Bukkit.getWorld("world"), -372, 69, -586),"CompleteArrow"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -588), new Location(Bukkit.getWorld("world"), -437, 69, -599),"CompleteCircle"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -439, 66, -552), new Location(Bukkit.getWorld("world"), -450, 69, -552),"CompleteSpam"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -532), new Location(Bukkit.getWorld("world"), -437, 69, -521),"CompleteArrow")
        );

        courseCheckpoints.put(1, course1);
        courseCheckpoints.put(2, course2);
    }
    public void setCourseStartLocations(Location course1Start, Location course2Start) {
        this.course1Start = course1Start;
        this.course2Start = course2Start;
    }

    public void startNextRace() {
        if (raceQueue.size() < 2) {
            Bukkit.getLogger().info("No hay suficientes equipos para iniciar una nueva carrera.");
            return;
        }

        Integer team1 = null;
        Integer team2 = null;

        // Find the first team with players
        while (!raceQueue.isEmpty()) {
            team1 = raceQueue.poll();
            if (!getPlayersFromTeam(team1).isEmpty()) {
                break;
            }
            team1 = null; // Reset if team is empty
        }

        // Find the next team with players
        while (!raceQueue.isEmpty()) {
            team2 = raceQueue.poll();
            if (!getPlayersFromTeam(team2).isEmpty()) {
                break;
            }
            team2 = null; // Reset if team is empty
        }

        // Check if we have two valid teams
        if (team1 == null || team2 == null) {
            Bukkit.getLogger().info("No hay suficientes equipos con jugadores para iniciar una nueva carrera.");
            if (team1 != null) {
                raceQueue.add(team1); // Add back the valid team if only one was found
            }
            return;
        }

        List<Player> team1Players = getPlayersFromTeam(team1);
        List<Player> team2Players = getPlayersFromTeam(team2);

        // Log team information
        StringBuilder team1Info = new StringBuilder("Equipo " + team1 + ": ");
        for (Player player : team1Players) {
            team1Info.append(player.getName()).append(" ");
        }

        StringBuilder team2Info = new StringBuilder("Equipo " + team2 + ": ");
        for (Player player : team2Players) {
            team2Info.append(player.getName()).append(" ");
        }

        Bukkit.getLogger().info(team1Info.toString().trim());
        Bukkit.getLogger().info(team2Info.toString().trim());

        resetCourseBarriers(1);
        resetCourseBarriers(2);

        // Teleport players to starting locations
        teleportPlayers(team1Players, course1Start);
        teleportPlayers(team2Players, course2Start);

        new RaceTask(this, team1Players, team2Players, team1, team2, 1, 2).runTaskTimer(plugin, 0L, 20L);
    }

    public void addPacketArea(int courseNumber, Location corner1, Location corner2, String packet) {
        packetAreas.putIfAbsent(courseNumber, new ArrayList<>());
        packetAreas.get(courseNumber).add(new PacketArea(corner1, corner2, packet));
    }
    private static class PacketArea {
        private final Location corner1;
        private final Location corner2;
        private final String packet;

        public PacketArea(Location corner1, Location corner2, String packet) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.packet = packet;
        }

        public boolean isPlayerInArea(Player player) {
            Location loc = player.getLocation();
            int x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            return loc.getBlockX() >= x1 && loc.getBlockX() <= x2 &&
                    loc.getBlockY() >= y1 && loc.getBlockY() <= y2 &&
                    loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
        }

        public void sendPacket(Player player, Set<PacketArea> sentAreas) {
            if (!sentAreas.contains(this)) {
                SquidGamePacket.sendStringScreenPacketToClient(player, packet);
                sentAreas.add(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PacketArea that = (PacketArea) o;
            return corner1.equals(that.corner1) && corner2.equals(that.corner2) && packet.equals(that.packet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(corner1, corner2, packet);
        }
    }



    private void teleportPlayers(List<Player> players, Location location) {
        if (location == null) {
            Bukkit.getLogger().warning("Starting location is not set!");
            return;
        }

        for (Player player : players) {
            player.teleport(location);
        }
    }


    private List<Player> getPlayersFromTeam(int teamNumber) {
        List<UUID> memberUUIDs = teamDefineCommand.getTeamMembers(teamNumber);
        List<Player> players = new ArrayList<>();
        for (UUID uuid : memberUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private void resetCourseBarriers(int courseNumber) {
        List<Checkpoint> checkpoints = courseCheckpoints.get(courseNumber);
        if (checkpoints != null) {
            for (Checkpoint checkpoint : checkpoints) {
                for (Location blockLocation : checkpoint.getWallBlocks()) {
                    blockLocation.getBlock().setType(Material.BARRIER);
                }
            }
        }
    }

    private final Map<Integer, FinishArea> finishAreas = new HashMap<>();

    public void setFinishArea(int courseNumber, Location corner1, Location corner2) {
        finishAreas.put(courseNumber, new FinishArea(corner1, corner2));
    }

    private static class Checkpoint {
        private final Location corner1;
        private final Location corner2;
        private final String requiredPacket;


        public Checkpoint(Location corner1, Location corner2, String requiredPacket) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.requiredPacket = requiredPacket;
        }
        public List<Location> getWallBlocks() {
            List<Location> blocks = new ArrayList<>();
            int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        blocks.add(new Location(corner1.getWorld(), x, y, z));
                    }
                }
            }

            return blocks;
        }
    }

    private class RaceTask extends BukkitRunnable {
        private final List<Player> team1Players;
        private final List<Player> team2Players;
        private final int team1;
        private final int team2;
        private final int course1;
        private final int course2;
        private final RaceManager raceManager;
        private boolean countdownActive = true;
        private int countdown = 5; // Pre-race countdown in seconds
        private int timer = 300;   // Main race timer in seconds (5 minutes)
        private final Set<UUID> crossedPlayers = new HashSet<>();




        public RaceTask(RaceManager raceManager,List<Player> team1Players, List<Player> team2Players, int team1, int team2, int course1, int course2) {
            this.raceManager = raceManager;
            this.team1Players = team1Players;
            this.team2Players = team2Players;
            this.team1 = team1;
            this.team2 = team2;
            this.course1 = course1;
            this.course2 = course2;
            checkpointProgress.put(team1, 0);
            checkpointProgress.put(team2, 0);
            this.raceManager.registerRaceTask(this); // Register the task

        }

        @Override
        public void run() {

            if (countdownActive) {
                // Handle the pre-race countdown
                if (countdown > 0) {
                    startCountdown();
                    updateRaceTimer(countdown);
                    countdown--;
                } else {
                    countdownActive = false;
                    startRace();
                }
                return;
            }

            checkPacketAreas(course1, team1Players);
            checkPacketAreas(course2, team2Players);

            checkForRaceCompletion(team1, team1Players, course1);
            checkForRaceCompletion(team2, team2Players, course2);

            checkTeamProgress(team1); //ERROR at team1Players, expected int provided List <org.bukkit.entity.Player>
            checkTeamProgress(team2);  //ERROR at team2Players, expected int provided List <org.bukkit.entity.Player>

            if (timer <= 0) {
                Bukkit.getLogger().info("Tiempo agotado. Ambos equipos han sido eliminados.");
                eliminateTeam(team1Players);
                eliminateTeam(team2Players);
                updateRaceTimer(0);
                cancel();

                return;
            }

            if (hasAnyReachedFinishLine(team1Players)) {
                Bukkit.getLogger().info("El equipo " + team1 + " ha ganado la carrera. El equipo " + team2 + " ha sido eliminado.");
                eliminateTeam(team2Players);
                cancel();
                return;
            }

            if (hasAnyReachedFinishLine(team2Players)) {
                Bukkit.getLogger().info("El equipo " + team2 + " ha ganado la carrera. El equipo " + team1 + " ha sido eliminado.");
                eliminateTeam(team1Players);
                cancel();
                return;
            }

            updateRaceTimer(timer);
            timer--;
        }


        private void checkTeamProgress(int team) {
            // Get the current checkpoint index
            int checkpointIndex = checkpointProgress.getOrDefault(team, 0);
            List<Checkpoint> checkpoints = courseCheckpoints.getOrDefault(team, null);

            // If no checkpoints are defined for the team, log a warning and return
            if (checkpoints == null || checkpoints.isEmpty()) {
                Bukkit.getLogger().warning("No checkpoints defined for team " + team + ". Skipping progress check.");
                return;
            }

            if (checkpointIndex >= checkpoints.size()) {
                return;
            }

            // Get the current checkpoint
            Checkpoint currentCheckpoint = checkpoints.get(checkpointIndex);

            // Ensure all players in the team have sent the required packet
            if (allPlayersSentPacket(team, currentCheckpoint.requiredPacket)) {
                // Remove the barrier for this checkpoint
                for (Location blockLocation : currentCheckpoint.getWallBlocks()) {
                    blockLocation.getBlock().setType(Material.AIR);
                }
                // Advance to the next checkpoint
                checkpointProgress.put(team, checkpointIndex + 1);
                plugin.getLogger().info("Team " + team + " completed checkpoint " + checkpointIndex + "!");
            }
        }


        private boolean hasCompletedAllPackets(int team, UUID playerId) {
            Map<UUID, Set<String>> playerProgress = teamPacketProgress.getOrDefault(team, new HashMap<>());
            Set<String> packets = playerProgress.getOrDefault(playerId, Collections.emptySet());

            // List of required packets (replace with actual requirements)
            List<String> requiredPackets = Arrays.asList("CompleteSpin", "CompleteSpam", "CompleteArrow", "CompleteCircle"); //TODO AGREGAR BUTTERFLY

            return packets.containsAll(requiredPackets);
        }

        private boolean hasTeamCrossed(List<Player> players) {
            for (Player player : players) {
                if (!crossedPlayers.contains(player.getUniqueId())) {
                    return false; // A team member has not crossed the finish line
                }
            }
            return true; // All team members have crossed the finish line
        }

        private void declareTeamWinner(int team, List<Player> players) {
            plugin.getLogger().info("Team " + team + " has finished the race!");
            for (Player player : players) {
                player.sendMessage("§aYour team has completed the race! Congratulations!");
                SquidGamePacket.sendStringScreenPacketToClient(player, "ADMovementdisable");
                player.sendTitle("§aVictory!", "Your team has won the race!", 10, 70, 20);
            }

            // Declare winners and end the race
            endRaceForTeam(team);
        }

        private void startCountdown() {
            // Freeze players
            freezePlayers(team1Players, true);
            freezePlayers(team2Players, true);

            // Display countdown in title with color
            String color = countdown <= 1 ? "§c" : countdown <= 3 ? "§e" : "§a"; // Red, yellow, green
            String title = color + countdown;
            String subtitle = "§fPreparados!";
            for (Player player : team1Players) {
                player.sendTitle(title, subtitle, 0, 20, 0);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }
            for (Player player : team2Players) {
                player.sendTitle(title, subtitle, 0, 20, 0);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }
        }

        private void freezePlayers(List<Player> players, boolean freeze) {
            for (Player player : players) {
                player.setWalkSpeed(freeze ? 0 : 0.2f);
                if(!freeze) {
                    SquidGamePacket.sendStringScreenPacketToClient(player, "ADMovementenable");
                }
            }
        }
        private void startRace() {
            Bukkit.getLogger().info("Race started!");

            // Unfreeze players
            freezePlayers(team1Players, false);
            freezePlayers(team2Players, false);

            // Notify players and play start sound
            for (Player player : team1Players) {
                player.sendTitle("§aFuera!", " ", 10, 40, 10);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            for (Player player : team2Players) {
                player.sendTitle("§aFuera!", " ", 10, 40, 10);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }

            // Randomly choose a song to play
            String[] songs = {
                    "minecraft:carrera_1",
                    "minecraft:carrera_2",
                    "minecraft:carrera_3",
                    "minecraft:carrera_4",
                    "minecraft:carrera_5"
            };
            String selectedSong = songs[new Random().nextInt(songs.length)];

            // Play the selected song for everyone
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(selectedSong), 1f, 1f);
            }

            // Start the snowball reward task
            SnowballRewardTask rewardTask = new SnowballRewardTask(
                    plugin,
                    new Location(Bukkit.getWorld("world"), -382, 66, -585),
                    new Location(Bukkit.getWorld("world"), -372, 69, -585)
            );
            rewardTask.setRaceActive(true);
            rewardTask.start();
        }



        private boolean allPlayersSentPacket(int team, String requiredPacket) {
            // Get all players in the team
            List<Player> teamPlayers = getPlayersFromTeam(team);

            // Get packet progress for the team
            Map<UUID, Set<String>> playerProgress = teamPacketProgress.getOrDefault(team, new HashMap<>());

            for (Player player : teamPlayers) {
                UUID uuid = player.getUniqueId();
                Set<String> packets = playerProgress.getOrDefault(uuid, Collections.emptySet());
                if (!packets.contains(requiredPacket)) {
                    return false;
                }
            }

            return true;
        }
        private void checkPacketAreas(int course, List<Player> players) {
            List<PacketArea> areas = packetAreas.get(course);
            if (areas == null) return; // No packet areas defined for this course

            for (Player player : players) {
                UUID playerId = player.getUniqueId();
                sentPackets.putIfAbsent(playerId, new HashSet<>());

                for (PacketArea area : areas) {
                    if (area.isPlayerInArea(player) && !sentPackets.get(playerId).contains(area)) {
                        area.sendPacket(player, sentPackets.get(playerId));

                        sentPackets.get(playerId).add(area);
                        plugin.getLogger().info("Sent packet " + area.packet + " to " + player.getName() + " for area " + area);
                    }
                }
            }
        }




        public void handleScreenPacket(String packetString, Player player) {

            plugin.givePoints(player, 1);

            teleportPlayerInViewDirection(player);

        }


        private boolean hasAnyReachedFinishLine(List<Player> players) {
            for (Player player : players) {
                if (player != null && hasReachedFinishLine(player)) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasReachedFinishLine(Player player) {
            // Replace with actual logic to check if the player has reached the finish line
            return false; // Placeholder
        }


        private void checkForRaceCompletion(int team, List<Player> players, int course) {
            FinishArea finishArea = finishAreas.get(course);
            if (finishArea == null) {
                return; // No finish area defined for this course
            }

            for (Player player : players) {
                UUID playerId = player.getUniqueId();

                // Skip if player already marked as crossed
                if (crossedPlayers.contains(playerId)) {
                    continue;
                }

                // Check if the player is in the finish area and has completed all packets
                if (finishArea.isPlayerInArea(player) && hasCompletedAllPackets(team, playerId)) {
                    crossedPlayers.add(playerId); // Mark player as crossed
                    player.sendMessage("§aYou have crossed the finish line!");
                    plugin.getLogger().info("Player " + player.getName() + " from Team " + team + " crossed the finish line.");
                }
            }

            // Check if the entire team has crossed
            if (hasTeamCrossed(players)) {
                declareTeamWinner(team, players);
            }
        }

        private void endRaceForTeam(int team) {
            // Eliminate the opposing team
            if (team == team1) {
                eliminateTeam(team2Players);
            } else if (team == team2) {
                eliminateTeam(team1Players);
            }

            // Broadcast the winning message
            Bukkit.broadcastMessage("§aEl equipo " + team + " ha ganado la carrera!");

            SnowballRewardTask rewardTask = new SnowballRewardTask(
                    plugin,
                    new Location(Bukkit.getWorld("world"), -382, 66, -585),
                    new Location(Bukkit.getWorld("world"), -372, 69, -585)
            );
            rewardTask.setRaceActive(false);
            // Cancel the current race task
            cancel();
        }


        private boolean allPlayersSentAllPackets(int team) {
            Map<UUID, Set<String>> playerProgress = teamPacketProgress.getOrDefault(team, new HashMap<>());

            // Define all required packets (replace with your actual requirements)
            List<String> requiredPackets = Arrays.asList("CompleteSpin", "CompleteSpam", "CompleteArrow", "CompleteCircle"); //TODO AGREGAR OTRO PACKET AQUI

            for (UUID uuid : playerProgress.keySet()) {
                Set<String> packets = playerProgress.getOrDefault(uuid, Collections.emptySet());
                if (!packets.containsAll(requiredPackets)) {
                    return false; // A player has not completed all packets
                }
            }

            return true; // All players have completed all packets
        }

        private void eliminateTeam(List<Player> players) {
            for (Player player : players) {
                if (player != null) {
                    player.setGlowing(true);
                    player.sendMessage("Has sido eliminado de la carrera.");
                    SquidGamePacket.sendStringScreenPacketToClient(player, "ADMovementdisable");
                }
            }
        }
        public boolean isPlayerInRace(Player player) {
            return team1Players.contains(player) || team2Players.contains(player);
        }

        @Override
        public void cancel() {
            super.cancel();
            raceManager.unregisterRaceTask(this);
        }

    }
    private final List<RaceTask> activeRaces = new ArrayList<>();

    public void registerRaceTask(RaceTask raceTask) {
        activeRaces.add(raceTask);
    }

    public void unregisterRaceTask(RaceTask raceTask) {
        activeRaces.remove(raceTask);
    }

    public void handleScreenPacket(String packetString, Player player) {
        for (RaceTask raceTask : activeRaces) {
                raceTask.handleScreenPacket(packetString, player);
                return;
        }
        plugin.getLogger().warning("No active race found for player: " + player.getName());
    }
    private static class FinishArea {
        private final Location corner1;
        private final Location corner2;

        public FinishArea(Location corner1, Location corner2) {
            this.corner1 = corner1;
            this.corner2 = corner2;
        }

        public boolean isPlayerInArea(Player player) {
            Location loc = player.getLocation();
            int x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            return loc.getBlockX() >= x1 && loc.getBlockX() <= x2 &&
                    loc.getBlockY() >= y1 && loc.getBlockY() <= y2 &&
                    loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
        }

        public boolean isTeamInArea(List<Player> players) {
            for (Player player : players) {
                if (!isPlayerInArea(player)) {
                    return false; // A player is outside the area
                }
            }
            return true; // All players are inside the area
        }
    }
    private void updateRaceTimer(int timeInSeconds) {
        String formattedTime = formatTime(timeInSeconds);

        // Find all entities with the "RaceTimer" tag and update their text
        for (Entity entity : Bukkit.getWorld("world").getEntities()) { // Replace "world" with the correct world name if needed
            if (entity.getScoreboardTags().contains("RaceTimer") && entity instanceof TextDisplay) {
                TextDisplay textDisplay = (TextDisplay) entity;
                textDisplay.setText(formattedTime);
            }
        }
    }
    private String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public void teleportPlayerInViewDirection(Player player) {
        // Get the player's current location
        Location currentLocation = player.getLocation();

        // Get the direction the player is facing
        @NotNull Vector direction = currentLocation.getDirection();

        direction.normalize();

        // Scale the direction vector to 2 blocks
        direction.multiply(2);

        // Add the scaled direction vector to the current location
        Location targetLocation = currentLocation.add(direction);

        // Teleport the player to the target location
        player.teleport(targetLocation);
    }

}
