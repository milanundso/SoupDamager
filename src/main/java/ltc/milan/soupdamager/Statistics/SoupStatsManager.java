/*

Das ist basically ein listener, der die statistics checkt und wieder geben kann, wieviele Suppen man gegessen hat,
wie lange man im damager war, usw.

 */

package ltc.milan.soupdamager.Statistics;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoupStatsManager {

    public static class Stats {
        public long joinTime;
        public int soupsEaten;
        public int soupsDropped;
        public double heartsRegenerated;

        public Stats(long joinTime) {
            this.joinTime = joinTime;
        }
    }

    private final Map<Player, Stats> playerStats = new HashMap<>();

    public void startTracking(Player player) {
        playerStats.put(player, new Stats(System.currentTimeMillis()));
    }

    public void stopTracking(Player player) {
        playerStats.remove(player);
    }

    public Stats getStats(Player player) {
        return playerStats.get(player);
    }

    public void incrementSoupsEaten(Player player, double heartsHealed) {
        Stats stats = playerStats.get(player);
        if (stats != null) {
            stats.soupsEaten++;
            stats.heartsRegenerated += heartsHealed / 2.0; // das ist ein herz weil 2hp ein herz sind
        }
    }

    public void incrementSoupsDropped(Player player) {
        Stats stats = playerStats.get(player);
        if (stats != null) {
            stats.soupsDropped++;
        }
    }
}
