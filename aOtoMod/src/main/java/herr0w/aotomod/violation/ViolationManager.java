package herr0w.aotomod.violation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ViolationManager {
    private final JavaPlugin plugin;
    private final List<ViolationRecord> records = new CopyOnWriteArrayList<>();
    private File file;
    private FileConfiguration data;

    public ViolationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "violations.yml");
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
        ConfigurationSection section = data.getConfigurationSection("violations");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                String base = "violations." + id + ".";
                try {
                    records.add(new ViolationRecord(id, data.getString(base + "player-name", "Bilinmiyor"), UUID.fromString(data.getString(base + "uuid", UUID.randomUUID().toString())), data.getString(base + "type", ""), data.getString(base + "original-message", ""), data.getString(base + "detection-reason", ""), data.getInt(base + "severity-level"), data.getString(base + "punishment-type", ""), data.getString(base + "punishment-duration", ""), data.getLong(base + "timestamp"), data.getString(base + "staff-action", ""), data.getString(base + "normalized-message", ""), data.getDouble(base + "similarity-score"), data.getBoolean(base + "exact-match")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        records.sort(Comparator.comparingLong(ViolationRecord::timestamp).reversed());
    }

    public void add(ViolationRecord record) {
        records.add(0, record);
        save();
    }

    public List<ViolationRecord> recent(int limit) {
        return records.stream().limit(limit).toList();
    }

    public List<ViolationRecord> byPlayer(String player) {
        return records.stream().filter(record -> record.playerName().equalsIgnoreCase(player) || record.uuid().toString().equalsIgnoreCase(player)).toList();
    }

    public Optional<ViolationRecord> byId(String id) {
        return records.stream().filter(record -> record.id().equals(id)).findFirst();
    }

    public int count() {
        return records.size();
    }

    public int countType(String type) {
        return (int) records.stream().filter(record -> record.type().equalsIgnoreCase(type)).count();
    }

    public void staffAction(String id, String action) {
        Optional<ViolationRecord> existing = byId(id);
        if (existing.isEmpty()) {
            return;
        }
        ViolationRecord record = existing.get();
        records.remove(record);
        records.add(new ViolationRecord(record.id(), record.playerName(), record.uuid(), record.type(), record.originalMessage(), record.detectionReason(), record.severityLevel(), record.punishmentType(), record.punishmentDuration(), record.timestamp(), action, record.normalizedMessage(), record.similarityScore(), record.exactMatch()));
        records.sort(Comparator.comparingLong(ViolationRecord::timestamp).reversed());
        save();
    }

    public void save() {
        if (data == null || file == null) {
            return;
        }
        data.set("violations", null);
        for (ViolationRecord record : new ArrayList<>(records)) {
            String base = "violations." + record.id() + ".";
            data.set(base + "player-name", record.playerName());
            data.set(base + "uuid", record.uuid().toString());
            data.set(base + "type", record.type());
            data.set(base + "original-message", record.originalMessage());
            data.set(base + "detection-reason", record.detectionReason());
            data.set(base + "severity-level", record.severityLevel());
            data.set(base + "punishment-type", record.punishmentType());
            data.set(base + "punishment-duration", record.punishmentDuration());
            data.set(base + "timestamp", record.timestamp());
            data.set(base + "staff-action", record.staffAction());
            data.set(base + "normalized-message", record.normalizedMessage());
            data.set(base + "similarity-score", record.similarityScore());
            data.set(base + "exact-match", record.exactMatch());
        }
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning(exception.getMessage());
        }
    }
}
