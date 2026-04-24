package ignluc4s.parkour.model;

public class ParkourSession {
    
    private final String parkourName;
    private final long startTime;
    private int currentCheckpoint;
    
    public ParkourSession(String parkourName) {
        this.parkourName = parkourName;
        this.startTime = System.currentTimeMillis();
        this.currentCheckpoint = 0;
    }
    
    public String getParkourName() {
        return parkourName;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }
    
    public void setCurrentCheckpoint(int checkpoint) {
        this.currentCheckpoint = checkpoint;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
