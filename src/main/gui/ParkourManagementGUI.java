package ignluc4s.parkour.gui;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCourse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParkourManagementGUI implements Listener {
    
    private final Parkour plugin;
    private static final String TITLE = "§8Parkour Management";
    
    public ParkourManagementGUI(Parkour plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open(Player player) {
        Map<String, ParkourCourse> parkours = plugin.getParkourManager().getAllParkours();
        
        int size = Math.min(54, ((parkours.size() + 8) / 9) * 9);
        if (size == 0) size = 9;
        
        Inventory inventory = Bukkit.createInventory(null, size, TITLE);
        
        int slot = 0;
        for (ParkourCourse parkour : parkours.values()) {
            ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§b§l" + parkour.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Checkpoints: §e" + parkour.getCheckpointCount());
            lore.add("");
            lore.add("§eLeft Click §7to view checkpoints");
            lore.add("§cRight Click §7to delete");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(slot++, item);
        }
        
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        
        if (item.getType() == Material.EMERALD_BLOCK) {
            String parkourName = item.getItemMeta().getDisplayName().replace("§b§l", "");
            
            if (event.isLeftClick()) {
                // Open checkpoint view
                player.closeInventory();
                new CheckpointViewGUI(plugin, parkourName).open(player);
            } else if (event.isRightClick()) {
                // Delete parkour
                plugin.getParkourManager().deleteParkour(parkourName);
                player.sendMessage(plugin.getConfigManager().formatMessage("parkour-deleted", "parkour", parkourName));
                player.closeInventory();
                
                // Reopen GUI
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> open(player), 1L);
            }
        }
    }
}
