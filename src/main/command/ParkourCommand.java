package ignluc4s.parkour.command;

import ignluc4s.parkour.Parkour;
import ignluc4s.parkour.gui.ParkourManagementGUI;
import ignluc4s.parkour.model.ParkourCheckpoint;
import ignluc4s.parkour.model.ParkourCreation;
import ignluc4s.parkour.model.ParkourSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParkourCommand implements CommandExecutor, TabCompleter {
    
    private final Parkour plugin;
    
    public ParkourCommand(Parkour plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "checkpoint":
                handleCheckpoint(player, args);
                break;
            case "finish":
                handleFinish(player, args);
                break;
            case "restart":
                handleRestart(player);
                break;
            case "gui":
                handleGUI(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("parkour.create")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cUsage: /parkour create <name>");
            return;
        }
        
        String name = args[1];
        
        if (plugin.getParkourManager().parkourExists(name)) {
            player.sendMessage(plugin.getConfigManager().formatMessage("parkour-already-exists", "parkour", name));
            return;
        }
        
        plugin.getParkourManager().createParkour(player.getUniqueId(), name);
        player.sendMessage(plugin.getConfigManager().formatMessage("parkour-created", "parkour", name));
    }
    
    private void handleCheckpoint(Player player, String[] args) {
        if (!player.hasPermission("parkour.create")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cUsage: /parkour checkpoint <name>");
            return;
        }
        
        String name = args[1];
        ParkourCreation creation = plugin.getParkourManager().getCreation(player.getUniqueId());
        
        if (creation == null || !creation.getName().equals(name)) {
            player.sendMessage(plugin.getConfigManager().formatMessage("parkour-not-found", "parkour", name));
            return;
        }
        
        boolean isStart = creation.getCheckpointCount() == 0;
        plugin.getParkourManager().addCheckpoint(player.getUniqueId(), player.getLocation(), isStart, false);
        
        int number = creation.getCheckpointCount() - 1;
        player.sendMessage(plugin.getConfigManager().formatMessage("checkpoint-added", "parkour", name, "number", String.valueOf(number)));
    }
    
    private void handleFinish(Player player, String[] args) {
        if (!player.hasPermission("parkour.create")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cUsage: /parkour finish <name>");
            return;
        }
        
        String name = args[1];
        ParkourCreation creation = plugin.getParkourManager().getCreation(player.getUniqueId());
        
        if (creation == null || !creation.getName().equals(name)) {
            player.sendMessage(plugin.getConfigManager().formatMessage("parkour-not-found", "parkour", name));
            return;
        }
        
        if (creation.getCheckpointCount() < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou need at least 1 checkpoint (start) before setting finish!");
            return;
        }
        
        // Add finish checkpoint at player's location
        plugin.getParkourManager().addCheckpoint(player.getUniqueId(), player.getLocation(), false, true);
        
        int checkpointCount = creation.getCheckpointCount();
        plugin.getParkourManager().finishCreation(player.getUniqueId());
        
        player.sendMessage(plugin.getConfigManager().formatMessage("parkour-finished", "parkour", name, "checkpoints", String.valueOf(checkpointCount)));
    }
    
    private void handleRestart(Player player) {
        ParkourSession session = plugin.getSessionManager().getSession(player.getUniqueId());
        
        if (session == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-parkour"));
            return;
        }
        
        if (session.getCurrentCheckpoint() == 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-checkpoint"));
            return;
        }
        
        String parkourName = session.getParkourName();
        ParkourCheckpoint checkpoint = plugin.getParkourManager().getParkour(parkourName)
                .getCheckpoint(session.getCurrentCheckpoint());
        
        if (checkpoint != null) {
            player.teleport(checkpoint.getLocation());
            player.sendMessage(plugin.getConfigManager().formatMessage("restarted", "number", String.valueOf(session.getCurrentCheckpoint())));
        }
    }
    
    private void handleGUI(Player player) {
        if (!player.hasPermission("parkour.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        new ParkourManagementGUI(plugin).open(player);
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§8§m------------------------");
        player.sendMessage("§b§lParkour Commands");
        player.sendMessage("");
        player.sendMessage("§e/parkour create <name> §7- Start creating a parkour");
        player.sendMessage("§e/parkour checkpoint <name> §7- Add a checkpoint");
        player.sendMessage("§e/parkour finish <name> §7- Finish and save the parkour");
        player.sendMessage("§e/parkour restart §7- Return to latest checkpoint");
        player.sendMessage("§e/parkour gui §7- Open management GUI");
        player.sendMessage("§8§m------------------------");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "checkpoint", "finish", "restart", "gui"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("checkpoint") || args[0].equalsIgnoreCase("finish"))) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ParkourCreation creation = plugin.getParkourManager().getCreation(player.getUniqueId());
                if (creation != null) {
                    completions.add(creation.getName());
                }
            }
        }
        
        return completions;
    }
}
