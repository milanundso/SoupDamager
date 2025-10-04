/*

Das ist die ganze Mechanik rund um den CrapDamager, wo einfach die items definiert werden und
random ausgelost wird, wie schnell welche items kommen, damit es wirklich random ist.

 */

package ltc.milan.soupdamager.Damagers;

import ltc.milan.soupdamager.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CrapDamager {

    private final Main plugin;
    private final Random random = new Random();
    private final List<ItemStack> dropItems = Arrays.asList(
            new ItemStack(Material.STONE_SWORD),
            new ItemStack(Material.SEEDS, 16),
            new ItemStack(Material.WOOD, 16),
            new ItemStack(Material.WOOD_SPADE),
            new ItemStack(Material.WOOD_PLATE, 8),
            new ItemStack(Material.STONE_SPADE),

            new ItemStack(Material.DEAD_BUSH),
            new ItemStack(Material.DIRT, 4),
            new ItemStack(Material.STRING, 2),
            new ItemStack(Material.EGG),
            new ItemStack(Material.BOWL, 2)
    );

    public CrapDamager(Main plugin) {
        this.plugin = plugin;
        scheduleNextDrop();
    }

    private void scheduleNextDrop() {
        int delay = 6 + random.nextInt(17);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getMechanics().isInDamager(player)) {
                    SoupZone zone = plugin.getSoupZones().stream()
                                .filter(z -> z.isInZone(player.getLocation()))
                                .findFirst().orElse(null);

                        if (zone != null && zone.getCenter().getBlockX() == -10016 &&
                                zone.getCenter().getBlockZ() == 109953) {
                            ItemStack originalDrop = dropItems.get(random.nextInt(dropItems.size()));
                            ItemStack drop = originalDrop.clone(); // kopie machen weil sich sonst die stacks modifien würden
                            player.getInventory().addItem(drop);

                        }
                    }
                }
                scheduleNextDrop();
            }
        }.runTaskLater(plugin, delay);
    }
}
