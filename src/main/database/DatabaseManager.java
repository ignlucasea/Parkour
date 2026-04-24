package ignluc4s.parkour.database;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCourse;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    
    private final Parkour plugin;
    private Connection connection;
    
    public DatabaseManager(Parkour plugin) {
        this.plugin = plugin;
    }
    
    public void connect() {
        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String database = plugin.getConfig().getString("database.database");
            String username = plugin.getConfig().getString("database.username");
            String password = plugin.getConfig().getString("database.password");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Successfully connected to MySQL database!");
            
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database!");
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Disconnected from MySQL database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Create parkours table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS parkours (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) UNIQUE NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            
            // Create checkpoints table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS checkpoints (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "parkour_id INT NOT NULL," +
                    "checkpoint_number INT NOT NULL," +
                    "world VARCHAR(255) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "is_start BOOLEAN DEFAULT FALSE," +
                    "is_finish BOOLEAN DEFAULT FALSE," +
                    "FOREIGN KEY (parkour_id) REFERENCES parkours(id) ON DELETE CASCADE" +
                    ")");
            
            // Create times table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS parkour_times (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "parkour_name VARCHAR(255) NOT NULL," +
                    "time_ms BIGINT NOT NULL," +
                    "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_parkour (parkour_name)," +
                    "INDEX idx_player (player_uuid)" +
                    ")");
            
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables!");
            e.printStackTrace();
        }
    }
    
    public void saveParkour(ParkourCourse parkour) {
        try {
            // First, insert or update parkour
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO parkours (name) VALUES (?) ON DUPLICATE KEY UPDATE name = ?",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, parkour.getName());
            stmt.setString(2, parkour.getName());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            int parkourId;
            if (rs.next()) {
                parkourId = rs.getInt(1);
            } else {
                // Get existing ID
                PreparedStatement getStmt = connection.prepareStatement("SELECT id FROM parkours WHERE name = ?");
                getStmt.setString(1, parkour.getName());
                ResultSet getResult = getStmt.executeQuery();
                getResult.next();
                parkourId = getResult.getInt("id");
                getResult.close();
                getStmt.close();
            }
            rs.close();
            stmt.close();
            
            // Delete existing checkpoints
            PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM checkpoints WHERE parkour_id = ?");
            deleteStmt.setInt(1, parkourId);
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Insert checkpoints
            PreparedStatement checkpointStmt = connection.prepareStatement(
                    "INSERT INTO checkpoints (parkour_id, checkpoint_number, world, x, y, z, yaw, pitch, is_start, is_finish) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            
            for (ParkourCheckpoint checkpoint : parkour.getCheckpoints()) {
                Location loc = checkpoint.getLocation();
                checkpointStmt.setInt(1, parkourId);
                checkpointStmt.setInt(2, checkpoint.getNumber());
                checkpointStmt.setString(3, loc.getWorld().getName());
                checkpointStmt.setDouble(4, loc.getX());
                checkpointStmt.setDouble(5, loc.getY());
                checkpointStmt.setDouble(6, loc.getZ());
                checkpointStmt.setFloat(7, loc.getYaw());
                checkpointStmt.setFloat(8, loc.getPitch());
                checkpointStmt.setBoolean(9, checkpoint.isStart());
                checkpointStmt.setBoolean(10, checkpoint.isFinish());
                checkpointStmt.executeUpdate();
            }
            
            checkpointStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save parkour: " + parkour.getName());
            e.printStackTrace();
        }
    }
    
    public List<ParkourCourse> loadAllParkours() {
        List<ParkourCourse> parkours = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM parkours");
            
            while (rs.next()) {
                String name = rs.getString("name");
                int parkourId = rs.getInt("id");
                
                ParkourCourse parkour = new ParkourCourse(name);
                
                // Load checkpoints
                PreparedStatement checkpointStmt = connection.prepareStatement(
                        "SELECT * FROM checkpoints WHERE parkour_id = ? ORDER BY checkpoint_number"
                );
                checkpointStmt.setInt(1, parkourId);
                ResultSet checkpointRs = checkpointStmt.executeQuery();
                
                while (checkpointRs.next()) {
                    String world = checkpointRs.getString("world");
                    double x = checkpointRs.getDouble("x");
                    double y = checkpointRs.getDouble("y");
                    double z = checkpointRs.getDouble("z");
                    float yaw = checkpointRs.getFloat("yaw");
                    float pitch = checkpointRs.getFloat("pitch");
                    int number = checkpointRs.getInt("checkpoint_number");
                    boolean isStart = checkpointRs.getBoolean("is_start");
                    boolean isFinish = checkpointRs.getBoolean("is_finish");
                    
                    Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                    ParkourCheckpoint checkpoint = new ParkourCheckpoint(number, location, isStart, isFinish);
                    parkour.addCheckpoint(checkpoint);
                }
                
                checkpointRs.close();
                checkpointStmt.close();
                parkours.add(parkour);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load parkours from database!");
            e.printStackTrace();
        }
        
        return parkours;
    }
    
    public void deleteParkour(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM parkours WHERE name = ?");
            stmt.setString(1, name);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete parkour: " + name);
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }
    
    public void saveTime(java.util.UUID playerUuid, String playerName, String parkourName, long timeMs) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO parkour_times (player_uuid, player_name, parkour_name, time_ms) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerName);
            stmt.setString(3, parkourName);
            stmt.setLong(4, timeMs);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save time for player " + playerName + " on parkour " + parkourName);
            e.printStackTrace();
        }
    }
}
