package ltc.milan.soupdamager.SoupMechanics;

import ltc.milan.soupdamager.Damagers.SoupZone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnSetter implements Listener {

    public static SpawnSetter instance;

    private final JavaPlugin plugin;
    private final Location centralSpawn;
    private final double radius = 100.0;
    private final Map<UUID, Location> customSpawns = new HashMap<>();
    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();
    private final Set<UUID> recentlyDropped = new HashSet<>();
    private final ItemStack spawnItem;
    private final List<SoupZone> damagerZones;

    public SpawnSetter(JavaPlugin plugin, List<SoupZone> damagerZones) {
        instance = this;
        this.plugin = plugin;
        this.damagerZones = damagerZones;

        World world = Bukkit.getWorld("world");
        this.centralSpawn = new Location(world, -10002, 49.5, 109970);

        this.spawnItem = new ItemStack(Material.ENDER_PORTAL_FRAME);
        ItemMeta meta = this.spawnItem.getItemMeta();
        meta.setDisplayName("§aSet Spawnpoint");
        this.spawnItem.setItemMeta(meta);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startGiveItemTask();
    }

    public boolean hasCustomSpawn(Player player) {
        return customSpawns.containsKey(player.getUniqueId());
    }

    private boolean isSpawnItem(ItemStack item) {
        if (item == null || item.getType() != Material.ENDER_PORTAL_FRAME) return false;
        if (!item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().equals("§aSet Spawnpoint");
    }

    private boolean isInSpawnZone(Location location) {
        return location.getWorld().equals(centralSpawn.getWorld()) &&
                location.distance(centralSpawn) <= radius;
    }

    private boolean isInAnyDamagerZone(Location loc) {
        for (SoupZone zone : damagerZones) {
            if (zone.isInZone(loc)) {
                return true;
            }
        }
        return false;
    }

    private void startGiveItemTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean inSpawn = isInSpawnZone(player.getLocation());
                    boolean inDamager = isInAnyDamagerZone(player.getLocation());
                    ItemStack current = player.getInventory().getItem(8);

                    if (inSpawn && !inDamager) {
                        if ((current == null || isSpawnItem(current)) && !recentlyDropped.contains(player.getUniqueId())) {
                            player.getInventory().setItem(8, spawnItem);
                        }
                    } else {
                        if (current != null && isSpawnItem(current)) {
                            player.getInventory().setItem(8, null);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (isSpawnItem(item)) {
            event.setCancelled(true);
            return;
        }

        if (item.getType() == Material.ENDER_PORTAL_FRAME) {
            recentlyDropped.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyDropped.remove(player.getUniqueId()), 40L);
        }
    }

    @EventHandler
    public void onMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (isSpawnItem(clicked)) {
            if (isInSpawnZone(player.getLocation()) && !isInAnyDamagerZone(player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }


        @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (!isSpawnItem(item)) return;

        if (!isInSpawnZone(player.getLocation())) {
            player.sendMessage("§l§6§lKO§b§lID §7▎ §r§cYou can only set your spawn inside the spawn zone!");
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
            return;
        }

        Location location = player.getLocation().clone();

        Location below = location.clone().subtract(0, 1, 0);
        if (!below.getBlock().getType().isSolid()) {
            player.sendMessage("§l§6§lKO§b§lID §7▎ §r §cYou must be standing on a solid block to set your spawn!");
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
            return;
        }

        location.setY(below.getBlockY() + 1.1);


        customSpawns.put(player.getUniqueId(), location);
        player.sendMessage("§l§6§lKO§b§lID §7▎ §r §aYou have successfully set your spawn at your current position.");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        lastDeathLocations.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        Location deathLoc = lastDeathLocations.get(player.getUniqueId());

        if (deathLoc != null && isInSpawnZone(deathLoc)) {
            final Location custom = customSpawns.get(player.getUniqueId());
            if (custom != null && isInSpawnZone(custom)) {
                final Location safeLoc = custom.clone();
                safeLoc.setY(safeLoc.getBlockY() + 1.5);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(safeLoc);
                    lastDeathLocations.remove(player.getUniqueId());
                }, 1L);


            }
        }
    }
}
