package ignluc4s.parkour.manager;

import ignluc4s.parkour.model.ParkourSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    
    private final Map<UUID, ParkourSession> sessions;
    
    public SessionManager() {
        this.sessions = new HashMap<>();
    }
    
    public void startSession(UUID playerId, String parkourName) {
        ParkourSession session = new ParkourSession(parkourName);
        sessions.put(playerId, session);
    }
    
    public void endSession(UUID playerId) {
        sessions.remove(playerId);
    }
    
    public ParkourSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }
    
    public boolean hasSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }
}
