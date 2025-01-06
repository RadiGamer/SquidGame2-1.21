package org.imradigamer.squidGamePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaceManager {

    private final JavaPlugin plugin;
    private final TeamDefineCommand teamDefineCommand;
    private final Map<Integer, List<Checkpoint>> courseCheckpoints = new HashMap<>();
    private final Queue<Integer> raceQueue = new LinkedList<>();
    private final Map<Integer, Integer> checkpointProgress = new HashMap<>();
    private final Map<Integer, Map<UUID, Set<String>>> teamPacketProgress = new HashMap<>();


    public RaceManager(JavaPlugin plugin, TeamDefineCommand teamDefineCommand, int totalTeams) {
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
                new Checkpoint(new Location(Bukkit.getWorld("world"), -372, 66, -414), new Location(Bukkit.getWorld("world"), -383, 69, -414), "CompleteArrow"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -401), new Location(Bukkit.getWorld("world"), -437, 69, -414),"CompleteCircle"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -450, 66, -448), new Location(Bukkit.getWorld("world"), -439, 69, -448),"CompleteSpam"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -479), new Location(Bukkit.getWorld("world"), -437, 69, -468),"")
        );

        List<Checkpoint> course1 = Arrays.asList(
                new Checkpoint(new Location(Bukkit.getWorld("world"), -384, 66, -532), new Location(Bukkit.getWorld("world"), -384, 69, -521),"CompleteSpam"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -383, 66, -586), new Location(Bukkit.getWorld("world"), -372, 69, -586),"CompleteArrow"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -588), new Location(Bukkit.getWorld("world"), -437, 69, -599),"CompleteCircle"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -439, 66, -552), new Location(Bukkit.getWorld("world"), -450, 69, -552),"CompleteSpam"),
                new Checkpoint(new Location(Bukkit.getWorld("world"), -437, 66, -532), new Location(Bukkit.getWorld("world"), -437, 69, -521),"CompleteButterfly")
        );

        courseCheckpoints.put(1, course1);
        courseCheckpoints.put(2, course2);
    }

    public void startNextRace() {
        if (raceQueue.size() < 2) {
            Bukkit.getLogger().info("No hay suficientes equipos para iniciar una nueva carrera.");
            return;
        }

        int team1 = raceQueue.poll();
        int team2 = raceQueue.poll();

        List<Player> team1Players = getPlayersFromTeam(team1);
        List<Player> team2Players = getPlayersFromTeam(team2);

        if (team1Players.isEmpty() || team2Players.isEmpty()) {
            Bukkit.getLogger().info("Uno o ambos equipos no tienen suficientes jugadores para la carrera.");
            return;
        }

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

        new RaceTask(this, team1Players, team2Players, team1, team2, 1, 2).runTaskTimer(plugin, 0L, 20L);
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
        private int timer = 300;
        private final int course1;
        private final int course2;
        private final RaceManager raceManager;


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
            checkTeamProgress(team1); //ERROR at team1Players, expected int provided List <org.bukkit.entity.Player>
            checkTeamProgress(team2);  //ERROR at team2Players, expected int provided List <org.bukkit.entity.Player>

            if (timer <= 0) {
                Bukkit.getLogger().info("Tiempo agotado. Ambos equipos han sido eliminados.");
                eliminateTeam(team1Players);
                eliminateTeam(team2Players);
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

            timer--;
        }


        private void checkTeamProgress(int team) {
            // Get the current checkpoint index
            int checkpointIndex = checkpointProgress.getOrDefault(team, 0);
            List<Checkpoint> checkpoints = courseCheckpoints.get(team);

            if (checkpointIndex >= checkpoints.size()) {
                return; // No more checkpoints to process
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



        public void handleScreenPacket(String packetString, Player player) {
            int teamNumber = teamDefineCommand.getTeamForPlayer(player.getUniqueId());
            if (teamNumber == -1) {
                plugin.getLogger().info("Player " + player.getName() + " is not part of any team.");
                return;
            }

            // Update teamPacketProgress
            teamPacketProgress.putIfAbsent(teamNumber, new HashMap<>());
            Map<UUID, Set<String>> playerProgress = teamPacketProgress.get(teamNumber);
            playerProgress.putIfAbsent(player.getUniqueId(), new HashSet<>());
            playerProgress.get(player.getUniqueId()).add(packetString);

            plugin.getLogger().info("Updated packet progress for Team " + teamNumber + ": " + playerProgress);

            // Revalidate team progress
            checkTeamProgress(teamNumber);
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

        private void eliminateTeam(List<Player> players) {
            for (Player player : players) {
                if (player != null) {
                    player.setGlowing(true);
                    player.sendMessage("Has sido eliminado de la carrera.");
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
            if (raceTask.isPlayerInRace(player)) {
                raceTask.handleScreenPacket(packetString, player);
                return;
            }
        }
        plugin.getLogger().warning("No active race found for player: " + player.getName());
    }


}
