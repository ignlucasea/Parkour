package ignluc4s.parkour.gui;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCourse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public class CheckpointViewGUI implements Listener {
    
    private final Parkour plugin;
    private final String parkourName;
    private static final String TITLE_PREFIX = "§8Checkpoints: ";
    
    public CheckpointViewGUI(Parkour plugin, String parkourName) {
        this.plugin = plugin;
        this.parkourName = parkourName;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void open(Player player) {
        ParkourCourse parkour = plugin.getParkourManager().getParkour(parkourName);
        if (parkour == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cParkour not found!");
            return;
        }
        
        int size = Math.min(54, ((parkour.getCheckpointCount() + 8) / 9) * 9);
        if (size == 0) size = 9;
        
        Inventory inventory = Bukkit.createInventory(null, size, TITLE_PREFIX + parkourName);
        
        int slot = 0;
        for (ParkourCheckpoint checkpoint : parkour.getCheckpoints()) {
            Material material;
            String typeName;
            
            if (checkpoint.isStart()) {
                material = Material.LIME_WOOL;
                typeName = "§a§lSTART";
            } else if (checkpoint.isFinish()) {
                material = Material.RED_WOOL;
                typeName = "§c§lFINISH";
            } else {
                material = Material.YELLOW_WOOL;
                typeName = "§e§lCHECKPOINT";
            }
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(typeName + " §7#" + checkpoint.getNumber());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            Location loc = checkpoint.getLocation();
            lore.add("§7World: §f" + loc.getWorld().getName());
            lore.add("§7X: §f" + String.format("%.2f", loc.getX()));
            lore.add("§7Y: §f" + String.format("%.2f", loc.getY()));
            lore.add("§7Z: §f" + String.format("%.2f", loc.getZ()));
            lore.add("");
            lore.add("§eClick to teleport");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(slot++, item);
        }
        
        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cBack");
        backButton.setItemMeta(backMeta);
        inventory.setItem(size - 1, backButton);
        
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(TITLE_PREFIX)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        
        if (item.getType() == Material.ARROW) {
            // Back button
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> 
                new ParkourManagementGUI(plugin).open(player), 1L);
            return;
        }
        
        // Teleport to checkpoint
        if (item.getType() == Material.LIME_WOOL || item.getType() == Material.YELLOW_WOOL || item.getType() == Material.RED_WOOL) {
            String displayName = item.getItemMeta().getDisplayName();
            String numberStr = displayName.split("#")[1];
            int checkpointNumber = Integer.parseInt(numberStr);
            
            ParkourCourse parkour = plugin.getParkourManager().getParkour(parkourName);
            if (parkour != null) {
                ParkourCheckpoint checkpoint = parkour.getCheckpoint(checkpointNumber);
                if (checkpoint != null) {
                    player.teleport(checkpoint.getLocation());
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aTeleported to checkpoint #" + checkpointNumber);
                    player.closeInventory();
                }
            }
        }
    }
}
