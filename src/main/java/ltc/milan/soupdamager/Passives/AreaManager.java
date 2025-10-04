package ltc.milan.soupdamager.Passives;

import ltc.milan.soupdamager.SoupMechanics.SpawnSetter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AreaManager implements Listener {

    private final JavaPlugin plugin;
    private final Location spawnLocation;
    private final double spawnRadius = 1000.0;
    private final Set<Player> playersInSpawn = new HashSet<>();
    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();

    public AreaManager(JavaPlugin plugin) {
        this.plugin = plugin;

        World world = Bukkit.getWorld("world");
        this.spawnLocation = new Location(world, -10002, 49.5, 109970);
        this.spawnLocation.setYaw(180f);
        this.spawnLocation.setPitch(0f);

        Bukkit.getPluginManager().registerEvents(this, plugin);

        startZoneChecker();
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();
        Location loc = player.getLocation();

        if(isInSpawnZone(loc)){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if(isInSpawnZone(loc)){
            event.setCancelled(true);
        }
    }


    private void startZoneChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean inZone = isInSpawnZone(player.getLocation());

                    if (inZone && !playersInSpawn.contains(player)) {
                        playersInSpawn.add(player);
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        lastDeathLocations.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location deathLocation = lastDeathLocations.get(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setHealth(20);
            }, 2L);

        boolean hasCustomSpawn = false;
        if (SpawnSetter.instance != null) {
            hasCustomSpawn = SpawnSetter.instance.hasCustomSpawn(player);
        }

        if (deathLocation != null && isInSpawnZone(deathLocation) && !hasCustomSpawn) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawnLocation);
                lastDeathLocations.remove(player.getUniqueId());
            }, 1L);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Location victimLoc = event.getEntity().getLocation();
        if (isInSpawnZone(victimLoc)) {
            event.setCancelled(true);
        }
    }

    private boolean isInSpawnZone(Location location) {
        return location.getWorld().equals(spawnLocation.getWorld()) &&
                location.distance(spawnLocation) <= spawnRadius;
    }
}
