/*

Hier wird einfach nur festgelegt was eine zone ist, nämlich die koordinate die ich angegeben habe + 5 blöcke in jede richtung
bzw. 2 blöcke nach oben ebenfalls.

 */

package ltc.milan.soupdamager.Damagers;

import org.bukkit.Location;

public class SoupZone {
    private final Location center;
    private final double damage;
    private final int radius = 5;
    private final int fixedY = 41;

    public SoupZone(Location center, double damage) {
        this.center = center;
        this.damage = damage;
    }

    public boolean isInZone(Location loc) {
        int y = loc.getBlockY();
        return loc.getWorld().equals(center.getWorld()) &&
                (y >= fixedY && y <= fixedY + 2) &&
                Math.abs(loc.getX() - center.getX()) <= radius &&
                Math.abs(loc.getZ() - center.getZ()) <= radius;
    }


    public double getDamage() {
        return damage;
    }

    public Location getCenter() {
        return center;
    }
}
