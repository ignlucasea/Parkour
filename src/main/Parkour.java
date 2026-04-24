package ignluc4s.parkour;

import ignluc4s.parkour.command.ParkourCommand;
import ignluc4s.parkour.database.DatabaseManager;
import ignluc4s.parkour.listener.PlayerMoveListener;
import ignluc4s.parkour.manager.ConfigManager;
import ignluc4s.parkour.manager.HologramManager;
import ignluc4s.parkour.manager.ParkourManager;
import ignluc4s.parkour.manager.SessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Parkour extends JavaPlugin {
    
    private static Parkour instance;
    private DatabaseManager databaseManager;
    private ParkourManager parkourManager;
    private SessionManager sessionManager;
    private HologramManager hologramManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        parkourManager = new ParkourManager(this);
        sessionManager = new SessionManager();
        hologramManager = new HologramManager(this);
        
        // Connect to database
        databaseManager.connect();
        
        // Load parkours
        parkourManager.loadAllParkours();
        
        // Register commands
        getCommand("parkour").setExecutor(new ParkourCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        
        getLogger().info("Parkour plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Remove all holograms
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        
        // Close database connection
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        
        getLogger().info("Parkour plugin has been disabled!");
    }
    
    public static Parkour getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ParkourManager getParkourManager() {
        return parkourManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
