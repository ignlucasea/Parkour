package ignluc4s.parkour.manager;

import ignluc4s.parkour.Parkour;
import org.bukkit.ChatColor;

public class ConfigManager {
    
    private final Parkour plugin;
    
    public ConfigManager(Parkour plugin) {
        this.plugin = plugin;
    }
    
    public String getMessage(String path) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&bParkour&8] &7");
        String message = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
    
    public String getMessageWithoutPrefix(String path) {
        String message = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String formatMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
}
