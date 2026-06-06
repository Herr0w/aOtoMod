package herr0w.aotomod.logging;

import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.discord.ModerationAlert;
import herr0w.aotomod.util.TimeFormatter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public final class ModerationLogger {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public ModerationLogger(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void log(ModerationAlert alert) {
        String line = "[" + DateTimeFormatter.ofPattern(configManager.dateFormat()).withZone(ZoneId.systemDefault()).format(alert.timestamp()) + "] " + alert.violationType() + " " + alert.playerName() + " (" + alert.uuid() + ") sebep: " + alert.detectionReason() + " seviye: " + alert.level() + " değer: " + alert.bannedWord() + " ceza: " + alert.punishmentType() + " " + TimeFormatter.format(alert.duration()) + " mesaj: " + alert.message();
        if (configManager.consoleLogging()) {
            plugin.getLogger().info(line);
        }
        if (configManager.fileLogging()) {
            CompletableFuture.runAsync(() -> write(line));
        }
    }

    private void write(String line) {
        try {
            Path directory = plugin.getDataFolder().toPath().resolve("logs");
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("automod.log"), line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            plugin.getLogger().warning(exception.getMessage());
        }
    }
}
