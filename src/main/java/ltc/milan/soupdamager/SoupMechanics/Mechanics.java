package ltc.milan.soupdamager.SoupMechanics;

import ltc.milan.soupdamager.Damagers.SoupZone;
import ltc.milan.soupdamager.Main;
import ltc.milan.soupdamager.Statistics.SoupStatsManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Mechanics implements Listener {

    private final Plugin plugin;
    private final Main main;
    private final Set<Player> playersInZone = new HashSet<>();
    private final Map<Player, ItemStack[]> previousInventories = new HashMap<>();
    private final Map<Player, ItemStack[]> previousArmor = new HashMap<>();
    private final Map<Player, SoupZone> playerZones = new HashMap<>();
    private final long currentTickInterval = 12L;
    private BukkitRunnable damageTask;


    public Mechanics(Main main) {
        this.main = main;
        this.plugin = main;
    }



    public void startDamageTask() {
        if (damageTask != null) damageTask.cancel();

        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playersInZone) {
                    if (!player.isOnline() || player.isDead()) continue;
                    SoupZone zone = playerZones.get(player);
                    if (zone != null) {
                        double newHealth = Math.max(0.0, player.getHealth() - zone.getDamage());
                        player.setHealth(newHealth);
                        player.damage(0.1);
                    }
                }
            }

        };
        damageTask.runTaskTimer(plugin, currentTickInterval, currentTickInterval);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), -10002, 49, 109970);

        player.teleport(spawnLocation);

        ItemStack spawnItem = new ItemStack(Material.ENDER_PORTAL_FRAME);
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName("§aSet Spawnpoint");
        spawnItem.setItemMeta(meta);
        player.getInventory().setItem(8, spawnItem);
    }



    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        SoupZone currentZone = null;
        for (SoupZone zone : main.getSoupZones()) {
            if (zone.isInZone(loc)) {
                currentZone = zone;
                break;
            }
        }

        boolean wasInZone = playersInZone.contains(player);

        if (currentZone != null && !wasInZone) {
            enterZone(player, currentZone);

        } else if (currentZone == null && wasInZone) {
            exitZone(player);
        }

        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), -10002, 49, 109970);
        double radius = 1000.0;

        boolean isInSoupZone = (currentZone != null);
        boolean isInSpawnArea = loc.getWorld().equals(spawnLocation.getWorld()) &&
                loc.distance(spawnLocation) <= radius;

        float defaultSpeed = 0.2f; // vanilla default in 1.7

        if (isInSpawnArea && !isInSoupZone) {
            if (player.getWalkSpeed() == defaultSpeed) {
                player.setWalkSpeed(0.3f); // das ist 50% schneller wegen +0,1
            }
        } else {
            if (player.getWalkSpeed() != defaultSpeed) {
                player.setWalkSpeed(defaultSpeed); // hier wird es resetted
            }
        }
    }











    private void enterZone(Player player, SoupZone zone) {

        playersInZone.add(player);
        playerZones.put(player, zone);

        previousInventories.put(player, player.getInventory().getContents().clone());
        previousArmor.put(player, player.getInventory().getArmorContents().clone());

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        // remove spawn item
        player.getInventory().setItem(8, null);

        player.setGameMode(GameMode.ADVENTURE);
        setupSoupInventory(player);
        main.getStatsManager().startTracking(player);
    }







    public void exitZone(Player player) {
        playersInZone.remove(player);
        playerZones.remove(player);

        ItemStack spawnItem = new ItemStack(Material.ENDER_PORTAL_FRAME);
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName("§aSet Spawnpoint");
        spawnItem.setItemMeta(meta);
        player.getInventory().setItem(8, spawnItem);


        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        if (previousInventories.containsKey(player)) {
            player.getInventory().setContents(previousInventories.remove(player));
        }
        if (previousArmor.containsKey(player)) {
            player.getInventory().setArmorContents(previousArmor.remove(player));
        }

        main.getStatsManager().stopTracking(player);
        player.removePotionEffect(PotionEffectType.WITHER);
    }






    private void setupSoupInventory(Player player) {
        player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD, 1));
        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++) {
            if (i == 0 || i == 13 || i == 14 || i == 15) continue;
            player.getInventory().setItem(i, new ItemStack(Material.MUSHROOM_SOUP, 1));
        }

        player.updateInventory();
    }







    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (playersInZone.contains(player)) {
        }
    }






    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (playersInZone.contains(player)) {
            event.getDrops().clear();
            SoupStatsManager.Stats stats = main.getStatsManager().getStats(player);
            if (stats != null) {
                long survivalTimeSec = (System.currentTimeMillis() - stats.joinTime) / 1000;
                player.sendMessage("§8§m--------------------------------------------------");
                player.sendMessage("§6§l   ✦ Soup Damager Stats ✦");
                player.sendMessage("§e    Survived: §f" + survivalTimeSec + " Seconds");
                player.sendMessage("§e    Soups eaten: §f" + stats.soupsEaten);
                player.sendMessage("§e    Soups dropped: §f" + stats.soupsDropped);
                player.sendMessage("§e    Hearts regenerated: §f" + String.format("%.1f", stats.heartsRegenerated));
                player.sendMessage("§8§m--------------------------------------------------");
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.spigot().respawn();
                }
            }.runTaskLater(plugin, 1L);
        }
    }










    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        if (playersInZone.contains(damager) || playersInZone.contains(victim)) {
            event.setCancelled(true);
        }
    }





    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playersInZone.contains(player)) {
            exitZone(player);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), -10002, 49, 109970);
        double radius = 1000.0;

        boolean isInSoupZone = main.getSoupZones().stream().anyMatch(zone -> zone.isInZone(loc));
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        Material type = droppedItem.getType();

        if (type == Material.MUSHROOM_SOUP) {
            main.getStatsManager().incrementSoupsDropped(player);
        }

        List<Material> crapMaterials = Arrays.asList(
                Material.STONE_SWORD, Material.SEEDS, Material.WOOD,
                Material.WOOD_SPADE, Material.WOOD_PLATE,
                Material.STONE_SPADE, Material.DEAD_BUSH, Material.DIRT,
                Material.STRING, Material.EGG, Material.BOWL
        );

        if (crapMaterials.contains(type)) {
            event.getItemDrop().remove();
            return;
        }

        if (loc.getWorld().equals(spawnLocation.getWorld()) &&
                loc.distance(spawnLocation) <= radius && !isInSoupZone) {
            event.setCancelled(true);
            return;
        }

        if (playersInZone.contains(player)) {
            if (type == Material.BOWL) {
                event.getItemDrop().remove();
            }
        }
    }




    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;


        Player player = (Player) event.getWhoClicked();
        boolean inDamager = main.getMechanics().isInDamager(player);
        Location loc = player.getLocation();
        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), -10002, 49, 109970);
        double radius = 1000.0;

        boolean isInSpawnArea = loc.getWorld().equals(spawnLocation.getWorld()) &&
                loc.distance(spawnLocation) <= radius;

        if (isInSpawnArea && !isInDamager(player)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onSoup(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (p.getInventory().getItemInHand().getType() == Material.MUSHROOM_SOUP &&
                (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (p.getHealth() < p.getMaxHealth()) {
                double healAmount = Math.min(7.0, p.getMaxHealth() - p.getHealth());
                double newHealth = Math.min(p.getMaxHealth(), p.getHealth() + healAmount);
                p.setHealth(newHealth);
                p.getItemInHand().setType(Material.BOWL);
                main.getStatsManager().incrementSoupsEaten(p, healAmount);
            } else {
                e.setCancelled(true);
                p.updateInventory();
            }
        }
    }




    public boolean isInDamager(Player player) {
        return playersInZone.contains(player);

    }



}
