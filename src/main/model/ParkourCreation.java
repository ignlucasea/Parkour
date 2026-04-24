package ignluc4s.parkour.model;

import java.util.ArrayList;
import java.util.List;

public class ParkourCreation {
    
    private final String name;
    private final List<ParkourCheckpoint> checkpoints;
    
    public ParkourCreation(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public List<ParkourCheckpoint> getCheckpoints() {
        return checkpoints;
    }
    
    public void addCheckpoint(ParkourCheckpoint checkpoint) {
        checkpoints.add(checkpoint);
    }
    
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
