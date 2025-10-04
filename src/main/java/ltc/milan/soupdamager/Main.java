package ltc.milan.soupdamager;

import ltc.milan.soupdamager.Damagers.CrapDamager;
import ltc.milan.soupdamager.Damagers.SoupZone;
import ltc.milan.soupdamager.Passives.AreaManager;
import ltc.milan.soupdamager.Passives.Hologramme;
import ltc.milan.soupdamager.Passives.Kit;
import ltc.milan.soupdamager.Passives.ZoneItemCleaner;
import ltc.milan.soupdamager.SoupMechanics.Mechanics;
import ltc.milan.soupdamager.SoupMechanics.SpawnSetter;
import ltc.milan.soupdamager.Statistics.SoupStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";

    private Hologramme hologramme;
    private final List<SoupZone> soupZones = new ArrayList<>();
    private final SoupStatsManager statsManager = new SoupStatsManager();
    private Mechanics mechanics;

    @Override
    public void onEnable() {
        if (Bukkit.getWorlds().isEmpty()) {
            getLogger().severe(ANSI_RED + "[SoupDamager] keine world gefunden --> plugin wird deaktiviert" + ANSI_RESET);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logBanner("Soup Damager Plugin - Made by Milan");

        // Hologramme spawnen
        this.hologramme = new Hologramme(this);
        hologramme.cleanupOldHolograms();
        hologramme.spawnHolograms();


        // helferklassen werden gestartet
        new AreaManager(this);
        new ZoneItemCleaner(this);
        new SpawnSetter(this, soupZones);


        getServer().getPluginManager().registerEvents(new Kit(), this);


        // Soup-Zonen initialisieren
        org.bukkit.World world = Bukkit.getWorlds().get(0);
        soupZones.add(new SoupZone(new Location(world, -9993, 41, 109946), 5.0));
        soupZones.add(new SoupZone(new Location(world, -10002, 41, 109945), 7.0));
        soupZones.add(new SoupZone(new Location(world, -10011, 41, 109946), 10.0));
        soupZones.add(new SoupZone(new Location(world, -10016, 41, 109953), 6.0));





        // Mechanics initialisieren
        this.mechanics = new Mechanics(this);
        getServer().getPluginManager().registerEvents(mechanics, this);
        mechanics.startDamageTask();
        getLogger().info(ANSI_GREEN + "[SoupDamager] Mechanics wurden aktiviert" + ANSI_RESET);


        // CrapDamager zuletzt initialisieren
        new CrapDamager(this);
    }

    @Override
    public void onDisable() {
        if (hologramme != null) {
            hologramme.removeHolograms();
        }
        getLogger().info(ANSI_RED + "[SoupDamager] Plugin deaktiviert." + ANSI_RESET);
    }

    public List<SoupZone> getSoupZones() {
        return soupZones;
    }

    public SoupStatsManager getStatsManager() {
        return statsManager;
    }

    public Mechanics getMechanics() {
        return mechanics;
    }

    private void logBanner(String title) {
        getLogger().info(ANSI_YELLOW + "======================================" + ANSI_RESET);
        getLogger().info(ANSI_YELLOW + "       " + title + ANSI_RESET);
        getLogger().info(ANSI_YELLOW + "======================================" + ANSI_RESET);
    }
}
