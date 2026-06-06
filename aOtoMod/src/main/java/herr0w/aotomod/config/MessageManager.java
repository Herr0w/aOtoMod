package herr0w.aotomod.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageManager {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration messages;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String path) {
        String prefix = color(messages.getString("prefix", ""));
        String raw = messages.getString(path, path);
        return color(raw.replace("%prefix%", prefix));
    }

    public String get(String path, Map<String, String> replacements) {
        String value = get(path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return value;
    }

    public String plain(String path) {
        return ChatColor.stripColor(get(path));
    }

    public List<String> list(String path, Map<String, String> replacements) {
        List<String> values = messages.getStringList(path);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            String line = color(value.replace("%prefix%", color(messages.getString("prefix", ""))));
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                line = line.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            result.add(line);
        }
        return result;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
