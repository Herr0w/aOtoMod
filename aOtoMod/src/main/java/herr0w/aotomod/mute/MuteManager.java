package herr0w.aotomod.mute;

import herr0w.aotomod.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class MuteManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, MuteRecord> mutes = new ConcurrentHashMap<>();
    private File file;
    private FileConfiguration data;
    private BukkitTask cleanupTask;

    public MuteManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public synchronized void load() {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                plugin.getLogger().severe(exception.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        mutes.clear();
        ConfigurationSection section = data.getConfigurationSection("mutes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String base = "mutes." + key + ".";
                    MuteRecord record = new MuteRecord(uuid, data.getString(base + "player-name", "Bilinmiyor"), data.getLong(base + "expires-at"), data.getInt(base + "level"), data.getString(base + "reason", ""), data.getString(base + "banned-word", ""));
                    if (!record.expired()) {
                        mutes.put(uuid, record);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        save();
    }

    public synchronized void reload() {
        save();
        load();
    }

    public void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            boolean changed = mutes.entrySet().removeIf(entry -> entry.getValue().expired());
            if (changed) {
                save();
            }
        }, configManager.cleanupIntervalTicks(), configManager.cleanupIntervalTicks());
    }

    public synchronized void mute(UUID uuid, String playerName, Duration duration, int level, String reason, String bannedWord) {
        long expiresAt = duration.toMillis() >= Long.MAX_VALUE / 2L ? 0L : System.currentTimeMillis() + duration.toMillis();
        mutes.put(uuid, new MuteRecord(uuid, playerName, expiresAt, level, reason, bannedWord));
        save();
    }

    public synchronized void extend(UUID uuid, Duration duration) {
        MuteRecord current = mutes.get(uuid);
        if (current == null) {
            return;
        }
        long base = current.expiresAt() <= 0L ? System.currentTimeMillis() : Math.max(current.expiresAt(), System.currentTimeMillis());
        long expiresAt = base + duration.toMillis();
        mutes.put(uuid, new MuteRecord(uuid, current.playerName(), expiresAt, current.level(), current.reason(), current.bannedWord()));
        save();
    }

    public synchronized boolean extendMute(UUID uuid, Duration duration) {
        if (getMute(uuid).isEmpty()) {
            return false;
        }
        extend(uuid, duration);
        return true;
    }

    public synchronized boolean unmute(UUID uuid) {
        boolean removed = mutes.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Optional<MuteRecord> getMute(UUID uuid) {
        MuteRecord record = mutes.get(uuid);
        if (record == null) {
            return Optional.empty();
        }
        if (record.expired()) {
            mutes.remove(uuid);
            save();
            return Optional.empty();
        }
        return Optional.of(record);
    }

    public boolean isMuted(UUID uuid) {
        return getMute(uuid).isPresent();
    }

    public Collection<MuteRecord> activeMutes() {
        mutes.entrySet().removeIf(entry -> entry.getValue().expired());
        return List.copyOf(mutes.values());
    }

    public Optional<MuteRecord> findByName(String name) {
        for (MuteRecord record : mutes.values()) {
            if (record.playerName().equalsIgnoreCase(name)) {
                return Optional.of(record);
            }
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        return getMute(offlinePlayer.getUniqueId());
    }

    public synchronized void save() {
        if (data == null || file == null) {
            return;
        }
        data.set("mutes", null);
        for (MuteRecord record : mutes.values()) {
            if (record.expired()) {
                continue;
            }
            String base = "mutes." + record.uuid() + ".";
            data.set(base + "player-name", record.playerName());
            data.set(base + "expires-at", record.expiresAt());
            data.set(base + "level", record.level());
            data.set(base + "reason", record.reason());
            data.set(base + "banned-word", record.bannedWord());
        }
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(exception.getMessage());
        }
    }
}
