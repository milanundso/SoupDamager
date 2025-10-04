/*

Diese Klasse ist ganz simpel für die Hologramme gemacht. Es werden unsichtbare, unzerstörbare armor stands generiert,
die als Name Tag einfach das haben, was das hologramm sein soll. Sie sind auf den hard codeten koordinaten von jedem damager

 */

package ltc.milan.soupdamager.Passives;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Hologramme {

    private final Plugin plugin;
    private final List<ArmorStand> armorStands = new ArrayList<>();

    public Hologramme(Plugin plugin) {
        this.plugin = plugin;
    }

    public void spawnHolograms() {
        spawnHologram(new Location(Bukkit.getWorlds().get(0), -9993, 47.5, 109946),
                "§eMedium", "§72.5 ♥ damage");

        spawnHologram(new Location(Bukkit.getWorlds().get(0), -10002, 47.5, 109945),
                "§cHard", "§73.5 ♥ damage");

        spawnHologram(new Location(Bukkit.getWorlds().get(0), -10011, 47.5, 109946),
                "§5Impossible", "§75.0 ♥ damage");

        spawnHologram(new Location(Bukkit.getWorlds().get(0), -10016, 47.5, 109953),
                "§3Crap-Damager", "§73.0 ♥ damage");

        spawnHologram(new Location(Bukkit.getWorlds().get(0), -9988, 47.5, 109953),
                "§8 Custom-Damager", "§7??? ♥ damage");

    }

    private void spawnHologram(Location baseLocation, String line1, String line2) {
        baseLocation.add(0.5, 0, 0.5);

        ArmorStand top = createArmorStand(baseLocation.clone().add(0, 0.3, 0), line1);
        ArmorStand bottom = createArmorStand(baseLocation, line2);

        armorStands.add(top);
        armorStands.add(bottom);
    }

    public void cleanupOldHolograms() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                Location loc = stand.getLocation();
                if (isNear(loc, -9993, 47.5, 109946) ||
                        isNear(loc, -10002, 47.5, 109945) ||
                        isNear(loc, -10011, 47.5, 109946) ||
                        isNear(loc, -10016, 47.5, 109953) ||
                        isNear(loc, -3.9, 201, 2.9) ||
                        isNear(loc, -9988, 47.5, 109953)) {
                    stand.remove();
                }
            }
        }
    }

    private boolean isNear(Location loc, double x, double y, double z) {
        return loc.getWorld().equals(Bukkit.getWorlds().get(0)) &&
                loc.distanceSquared(new Location(loc.getWorld(), x, y, z)) < 1;
    }



    private ArmorStand createArmorStand(Location location, String text) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);
        stand.setGravity(false);
        stand.setBasePlate(false);
        return stand;
    }


    public void removeHolograms() {
        for (ArmorStand stand : armorStands) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        armorStands.clear();
    }
}
