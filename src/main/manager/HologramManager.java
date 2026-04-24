package ignluc4s.parkour.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCourse;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    
    private final Parkour plugin;
    private final Map<String, List<String>> hologramIds;
    
    public HologramManager(Parkour plugin) {
        this.plugin = plugin;
        this.hologramIds = new HashMap<>();
    }
    
    public void createParkourHolograms(ParkourCourse parkour) {
        List<String> ids = new ArrayList<>();
        
        for (ParkourCheckpoint checkpoint : parkour.getCheckpoints()) {
            String holoId = createCheckpointHologram(parkour, checkpoint);
            if (holoId != null) {
                ids.add(holoId);
            }
        }
        
        hologramIds.put(parkour.getName(), ids);
    }
    
    private String createCheckpointHologram(ParkourCourse parkour, ParkourCheckpoint checkpoint) {
        String holoId = "parkour_" + parkour.getName() + "_" + checkpoint.getNumber();
        
        // Remove existing hologram if it exists
        if (DHAPI.getHologram(holoId) != null) {
            DHAPI.getHologram(holoId).delete();
        }
        
        Location holoLocation = checkpoint.getLocation().clone().add(0.5, 2.5, 0.5);
        
        List<String> lines = new ArrayList<>();
        
        if (checkpoint.isStart()) {
            List<String> configLines = plugin.getConfig().getStringList("holograms.start");
            for (String line : configLines) {
                line = line.replace("{parkour}", parkour.getName());
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else if (checkpoint.isFinish()) {
            List<String> configLines = plugin.getConfig().getStringList("holograms.finish");
            for (String line : configLines) {
                line = line.replace("{parkour}", parkour.getName());
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
            List<String> configLines = plugin.getConfig().getStringList("holograms.checkpoint");
            for (String line : configLines) {
                line = line.replace("{parkour}", parkour.getName());
                line = line.replace("{number}", String.valueOf(checkpoint.getNumber()));
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        
        try {
            DHAPI.createHologram(holoId, holoLocation, lines);
            return holoId;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create hologram: " + holoId);
            return null;
        }
    }
    
    public void removeParkourHolograms(ParkourCourse parkour) {
        List<String> ids = hologramIds.remove(parkour.getName());
        if (ids != null) {
            for (String id : ids) {
                Hologram hologram = DHAPI.getHologram(id);
                if (hologram != null) {
                    hologram.delete();
                }
            }
        }
    }
    
    public void removeAllHolograms() {
        for (List<String> ids : hologramIds.values()) {
            for (String id : ids) {
                Hologram hologram = DHAPI.getHologram(id);
                if (hologram != null) {
                    hologram.delete();
                }
            }
        }
        hologramIds.clear();
    }
}
