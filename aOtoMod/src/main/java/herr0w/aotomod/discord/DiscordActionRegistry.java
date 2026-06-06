package herr0w.aotomod.discord;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DiscordActionRegistry {
    private final JavaPlugin plugin;
    private final Map<String, DiscordActionRecord> records = new ConcurrentHashMap<>();
    private File file;
    private FileConfiguration data;

    public DiscordActionRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "discord-actions.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                plugin.getLogger().warning(exception.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        records.clear();
        ConfigurationSection section = data.getConfigurationSection("actions");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                String base = "actions." + id + ".";
                try {
                    records.put(id, new DiscordActionRecord(id, UUID.fromString(data.getString(base + "player-uuid", UUID.randomUUID().toString())), data.getString(base + "player-name", "Bilinmiyor"), data.getString(base + "mute-id", id), data.getString(base + "channel-id", ""), data.getString(base + "message-id", ""), data.getLong(base + "timestamp"), data.getString(base + "status", ""), data.getString(base + "action-detail", ""), data.getString(base + "actor", ""), data.getLong(base + "action-timestamp"), data.getBoolean(base + "unmute-disabled"), data.getBoolean(base + "extend-disabled"), data.getBoolean(base + "ban-disabled"), data.getBoolean(base + "review-disabled")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void put(DiscordActionRecord record) {
        records.put(record.violationId(), record);
        save();
    }

    public Optional<DiscordActionRecord> get(String violationId) {
        return Optional.ofNullable(records.get(violationId));
    }

    public void save() {
        if (data == null || file == null) {
            return;
        }
        data.set("actions", null);
        for (DiscordActionRecord record : records.values()) {
            String base = "actions." + record.violationId() + ".";
            data.set(base + "player-uuid", record.playerUuid().toString());
            data.set(base + "player-name", record.playerName());
            data.set(base + "mute-id", record.muteId());
            data.set(base + "channel-id", record.channelId());
            data.set(base + "message-id", record.messageId());
            data.set(base + "timestamp", record.timestamp());
            data.set(base + "status", record.status());
            data.set(base + "action-detail", record.actionDetail());
            data.set(base + "actor", record.actor());
            data.set(base + "action-timestamp", record.actionTimestamp());
            data.set(base + "unmute-disabled", record.unmuteDisabled());
            data.set(base + "extend-disabled", record.extendDisabled());
            data.set(base + "ban-disabled", record.banDisabled());
            data.set(base + "review-disabled", record.reviewDisabled());
        }
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning(exception.getMessage());
        }
    }
}
