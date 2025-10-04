package ltc.milan.soupdamager.Passives;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Kit implements Listener {

    private final List<String> allowedNames = Arrays.asList("Miilaan", "lMarc", "Sypherox", "sarakuyt", "SuedlichImNorden");

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!allowedNames.contains(player.getName())) return;

        if (message.equalsIgnoreCase(".kit1")) {
            event.setCancelled(true);
            giveOPSoupKit(player);

        }
    }

    @EventHandler
    public void onChatKitRequest(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if(!allowedNames.contains(player.getName())) return;

        if (message.equalsIgnoreCase(".kit2")) {
            event.setCancelled(true);
            giveNormalSoupKit(player);
        }

    }

    private void giveNormalSoupKit(Player player){
        player.getInventory().clear();

        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));


        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        player.getInventory().setItem(0, sword);

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++) {
            if (i == 0 || i == 13 || i == 14 || i == 15) continue;
            player.getInventory().setItem(i, new ItemStack(Material.MUSHROOM_SOUP, 1));
        }
    }

    private void giveOPSoupKit(Player player) {
        player.getInventory().clear();

        player.getInventory().setHelmet(createArmorPiece(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(createArmorPiece(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(createArmorPiece(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(createArmorPiece(Material.DIAMOND_BOOTS));

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
        swordMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        sword.setItemMeta(swordMeta);
        player.getInventory().setItem(0, sword);

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++) {
            if (i == 0 || i == 13 || i == 14 || i == 15) continue;
            player.getInventory().setItem(i, new ItemStack(Material.MUSHROOM_SOUP, 1));
        }
    }

    private ItemStack createArmorPiece(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        item.setItemMeta(meta);
        return item;
    }
}
