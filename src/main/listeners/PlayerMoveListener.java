package ignluc4s.parkour.listener;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCourse;
import ignluc4s.parkour.model.ParkourSession;
import ignluc4s.parkour.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    
    private final Parkour plugin;
    
    public PlayerMoveListener(Parkour plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        
        // Check if player actually moved to a different block
        if (to.getBlockX() == from.getBlockX() && 
            to.getBlockY() == from.getBlockY() && 
            to.getBlockZ() == from.getBlockZ()) {
            return;
        }
        
        ParkourCheckpoint checkpoint = plugin.getParkourManager().getCheckpointAt(to);
        if (checkpoint == null) {
            return;
        }
        
        ParkourCourse parkour = plugin.getParkourManager().getParkourByCheckpoint(checkpoint);
        if (parkour == null) {
            return;
        }
        
        handleCheckpointInteraction(player, parkour, checkpoint);
    }
    
    private void handleCheckpointInteraction(Player player, ParkourCourse parkour, ParkourCheckpoint checkpoint) {
        ParkourSession session = plugin.getSessionManager().getSession(player.getUniqueId());
        
        // Start checkpoint
        if (checkpoint.isStart()) {
            if (session != null && session.getParkourName().equals(parkour.getName())) {
                // Reset time
                plugin.getSessionManager().endSession(player.getUniqueId());
            }
            plugin.getSessionManager().startSession(player.getUniqueId(), parkour.getName());
            player.sendMessage(plugin.getConfigManager().formatMessage("parkour-started", "parkour", parkour.getName()));
            return;
        }
        
        // Check if player has an active session for this parkour
        if (session == null || !session.getParkourName().equals(parkour.getName())) {
            return;
        }
        
        // Finish checkpoint
        if (checkpoint.isFinish()) {
            long elapsedTime = session.getElapsedTime();
            String formattedTime = TimeUtil.formatTime(elapsedTime);
            
            // Send completion message
            player.sendMessage(plugin.getConfigManager().formatMessage("parkour-completed", 
                    "parkour", parkour.getName(), "time", formattedTime));
            
            // Save time to database
            plugin.getDatabaseManager().saveTime(player.getUniqueId(), player.getName(), 
                    parkour.getName(), elapsedTime);
            
            // Teleport to spawn
            ParkourCheckpoint start = parkour.getStart();
            if (start != null) {
                player.teleport(start.getLocation());
            }
            
            // End session
            plugin.getSessionManager().endSession(player.getUniqueId());
            return;
        }
        
        // Regular checkpoint
        if (checkpoint.getNumber() > session.getCurrentCheckpoint()) {
            session.setCurrentCheckpoint(checkpoint.getNumber());
            player.sendMessage(plugin.getConfigManager().formatMessage("checkpoint-reached", 
                    "number", String.valueOf(checkpoint.getNumber())));
        }
    }
}
