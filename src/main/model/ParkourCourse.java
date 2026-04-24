package ignluc4s.parkour.model;

import java.util.ArrayList;
import java.util.List;

public class ParkourCourse {
    
    private final String name;
    private final List<ParkourCheckpoint> checkpoints;
    
    public ParkourCourse(String name) {
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
    
    public ParkourCheckpoint getStart() {
        for (ParkourCheckpoint checkpoint : checkpoints) {
            if (checkpoint.isStart()) {
                return checkpoint;
            }
        }
        return null;
    }
    
    public ParkourCheckpoint getFinish() {
        for (ParkourCheckpoint checkpoint : checkpoints) {
            if (checkpoint.isFinish()) {
                return checkpoint;
            }
        }
        return null;
    }
    
    public ParkourCheckpoint getCheckpoint(int number) {
        for (ParkourCheckpoint checkpoint : checkpoints) {
            if (checkpoint.getNumber() == number) {
                return checkpoint;
            }
        }
        return null;
    }
    
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
