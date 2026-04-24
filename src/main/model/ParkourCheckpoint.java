package ignluc4s.parkour.model;

import org.bukkit.Location;

public class ParkourCheckpoint {
    
    private final int number;
    private final Location location;
    private final boolean isStart;
    private final boolean isFinish;
    
    public ParkourCheckpoint(int number, Location location, boolean isStart, boolean isFinish) {
        this.number = number;
        this.location = location;
        this.isStart = isStart;
        this.isFinish = isFinish;
    }
    
    public int getNumber() {
        return number;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public boolean isStart() {
        return isStart;
    }
    
    public boolean isFinish() {
        return isFinish;
    }
}
