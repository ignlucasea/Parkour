package ignluc4s.parkour.manager;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCourse;
import ignluc4s.parkour.model.ParkourCreation;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkourManager {
    
    private final Parkour plugin;
    private final Map<String, ParkourCourse> parkours;
    private final Map<UUID, ParkourCreation> creations;
    
    public ParkourManager(Parkour plugin) {
        this.plugin = plugin;
        this.parkours = new HashMap<>();
        this.creations = new HashMap<>();
    }
    
    public void loadAllParkours() {
        List<ParkourCourse> loadedParkours = plugin.getDatabaseManager().loadAllParkours();
        for (ParkourCourse parkour : loadedParkours) {
            parkours.put(parkour.getName(), parkour);
            plugin.getHologramManager().createParkourHolograms(parkour);
        }
        plugin.getLogger().info("Loaded " + loadedParkours.size() + " parkours from database!");
    }
    
    public void createParkour(UUID playerId, String name) {
        ParkourCreation creation = new ParkourCreation(name);
        creations.put(playerId, creation);
    }
    
    public void addCheckpoint(UUID playerId, Location location, boolean isStart, boolean isFinish) {
        ParkourCreation creation = creations.get(playerId);
        if (creation != null) {
            int number = creation.getCheckpointCount();
            ParkourCheckpoint checkpoint = new ParkourCheckpoint(number, location, isStart, isFinish);
            creation.addCheckpoint(checkpoint);
        }
    }
    
    public void finishCreation(UUID playerId) {
        ParkourCreation creation = creations.remove(playerId);
        if (creation != null) {
            ParkourCourse parkour = new ParkourCourse(creation.getName());
            for (ParkourCheckpoint checkpoint : creation.getCheckpoints()) {
                parkour.addCheckpoint(checkpoint);
            }
            
            parkours.put(parkour.getName(), parkour);
            plugin.getDatabaseManager().saveParkour(parkour);
            plugin.getHologramManager().createParkourHolograms(parkour);
        }
    }
    
    public ParkourCreation getCreation(UUID playerId) {
        return creations.get(playerId);
    }
    
    public ParkourCourse getParkour(String name) {
        return parkours.get(name);
    }
    
    public Map<String, ParkourCourse> getAllParkours() {
        return new HashMap<>(parkours);
    }
    
    public void deleteParkour(String name) {
        ParkourCourse parkour = parkours.remove(name);
        if (parkour != null) {
            plugin.getDatabaseManager().deleteParkour(name);
            plugin.getHologramManager().removeParkourHolograms(parkour);
        }
    }
    
    public boolean parkourExists(String name) {
        return parkours.containsKey(name);
    }
    
    public ParkourCheckpoint getCheckpointAt(Location location) {
        for (ParkourCourse parkour : parkours.values()) {
            for (ParkourCheckpoint checkpoint : parkour.getCheckpoints()) {
                if (isSameBlock(checkpoint.getLocation(), location)) {
                    return checkpoint;
                }
            }
        }
        return null;
    }
    
    public ParkourCourse getParkourByCheckpoint(ParkourCheckpoint checkpoint) {
        for (ParkourCourse parkour : parkours.values()) {
            if (parkour.getCheckpoints().contains(checkpoint)) {
                return parkour;
            }
        }
        return null;
    }
    
    private boolean isSameBlock(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        
        // Check if X and Z match
        if (loc1.getBlockX() != loc2.getBlockX() || loc1.getBlockZ() != loc2.getBlockZ()) {
            return false;
        }
        
        // Check if player is standing on the checkpoint block (Y or Y+1)
        int y1 = loc1.getBlockY();
        int y2 = loc2.getBlockY();
        return y1 == y2 || y1 == y2 + 1 || y2 == y1 + 1;
    }
}
