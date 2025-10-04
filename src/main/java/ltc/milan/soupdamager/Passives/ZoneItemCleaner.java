/*

Diese Klasse checkt jeden tick, ob in einer Zone für mehr als 5 Sekunden kein spieler war.
Wenn das tatsächlich der Fall war, und kein Spieler in der Zone war, werden sämtliche Items gecleared.
Das wird gemacht, damit nicht ewig die pilze oder volle suppen herumliegen.

 */

package ltc.milan.soupdamager.Passives;

import ltc.milan.soupdamager.Damagers.SoupZone;
import ltc.milan.soupdamager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ZoneItemCleaner {

    private final Main plugin;
    private final Map<SoupZone, Long> emptySinceMap = new HashMap<>();
    private final long checkIntervalTicks = 20L; // jede sekunde
    private final long delayThresholdMillis = 1000; // 5 sekunden

    public ZoneItemCleaner(Main plugin) {
        this.plugin = plugin;
        startCleaningTask();
    }

    private void startCleaningTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                for (SoupZone zone : plugin.getSoupZones()) {
                    boolean hasPlayer = false;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (zone.isInZone(player.getLocation())) {
                            hasPlayer = true;
                            break;
                        }
                    }

                    if (!hasPlayer) {
                        emptySinceMap.putIfAbsent(zone, currentTime);

                        long emptySince = emptySinceMap.get(zone);
                        if (currentTime - emptySince >= delayThresholdMillis) {
                            clearItemsInZone(zone);
                            emptySinceMap.remove(zone);
                        }

                    } else {
                        emptySinceMap.remove(zone);
                    }
                }
            }
        }.runTaskTimer(plugin, checkIntervalTicks, checkIntervalTicks);
    }

    private void clearItemsInZone(SoupZone zone) {
        Location center = zone.getCenter();
        World world = center.getWorld();

        if (world == null) return;

        int radius = 5;
        int minY = 41;
        int maxY = 45;

        for (Item item : world.getEntitiesByClass(Item.class)) {
            Location loc = item.getLocation();
            if (!loc.getWorld().equals(center.getWorld())) continue;

            if ((loc.getBlockY() >= minY && loc.getBlockY() <= maxY) &&
                    Math.abs(loc.getX() - center.getX()) <= radius &&
                    Math.abs(loc.getZ() - center.getZ()) <= radius) {
                item.remove();
            }
        }
    }
}
