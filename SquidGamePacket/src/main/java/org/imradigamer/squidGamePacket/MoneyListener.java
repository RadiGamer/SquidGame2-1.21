package org.imradigamer.squidGamePacket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MoneyListener {

    private final SquidGamePacket plugin;
    private final Economy economy;
    private final Map<Player, Double> playerBalances;

    public MoneyListener(SquidGamePacket plugin) {
        this.plugin = plugin;

        // Get the economy service
        var provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            this.economy = provider.getProvider();
        } else {
            this.economy = null;
            plugin.getLogger().severe("Vault economy provider not found! Disabling MoneyListener.");
        }

        this.playerBalances = new HashMap<>();

        // Initialize player balances and start monitoring task
        if (this.economy != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerBalances.put(player, economy.getBalance(player));
                    }
                }
            }.runTaskLater(plugin, 20L); // Delay initialization by 1 second

            // Start the balance monitoring task
            startMonitoringTask();
        }
    }

    private void startMonitoringTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double currentBalance = economy.getBalance(player);
                    double previousBalance = playerBalances.getOrDefault(player, 0.0);

                    if (currentBalance == 4) {
                        // Teleport the player
                        Location teleportLocation = new Location(Bukkit.getWorld("world"),503,59,-457);

                        player.teleport(teleportLocation);

                        player.sendTitle("§aFelicidades!", "§eHaz completado Triatlon!", 10, 70, 20);
                        SquidGamePacket.sendStringScreenPacketToClient(player, "ADMovementdisable");
                        plugin.removePoints(player, 4);

                        playerBalances.put(player, currentBalance);
                    } else {
                        playerBalances.put(player, currentBalance);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second (20 ticks)
    }
}
