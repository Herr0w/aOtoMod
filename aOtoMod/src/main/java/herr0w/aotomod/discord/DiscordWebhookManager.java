package herr0w.aotomod.discord;

import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.util.TimeFormatter;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public final class DiscordWebhookManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final HttpClient client = HttpClient.newHttpClient();

    public DiscordWebhookManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void send(ModerationAlert alert) {
        if (!configManager.discordEnabled() || !configManager.webhookEnabled() || configManager.webhookUrl().isBlank()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(configManager.webhookUrl()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload(alert)))
                        .build();
                client.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception exception) {
                plugin.getLogger().warning(exception.getMessage());
            }
        });
    }

    private String payload(ModerationAlert alert) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(configManager.dateFormat()).withZone(ZoneId.systemDefault());
        StringJoiner fields = new StringJoiner(",");
        fields.add(field("Oyuncu", alert.playerName(), true));
        fields.add(field("UUID", alert.uuid().toString(), true));
        fields.add(field("İhlal Türü", violationTitle(alert), true));
        fields.add(field("Tespit", blank(alert.bannedWord(), alert.detectionReason()), true));
        fields.add(field("Ceza", TimeFormatter.format(alert.duration()) + " " + punishmentType(alert), true));
        fields.add(field("Yöntem", method(alert), true));
        fields.add(field("Benzerlik Skoru", alert.similarityScore() > 0D && !alert.exactMatch() ? String.format("%.2f", alert.similarityScore()) : "-", true));
        fields.add(field("Sunucu", alert.serverName(), true));
        fields.add(field("Tarih", formatter.format(alert.timestamp()), true));
        fields.add(field("Mesaj", "\"" + blank(alert.message(), "-") + "\"", false));
        fields.add(field("Son Mesajlar", history(alert), false));
        return "{\"embeds\":[{\"title\":\"" + json(configManager.embedTitle()) + "\",\"color\":" + color(alert) + ",\"fields\":[" + fields + "],\"footer\":{\"text\":\"" + json(configManager.footerText()) + "\"},\"timestamp\":\"" + alert.timestamp() + "\"}]}";
    }

    private String field(String name, String value, boolean inline) {
        return "{\"name\":\"" + json(name) + "\",\"value\":\"" + json(value == null || value.isBlank() ? "-" : value) + "\",\"inline\":" + inline + "}";
    }

    private String history(ModerationAlert alert) {
        if (alert.history().isEmpty()) {
            return "Yok";
        }
        StringBuilder builder = new StringBuilder();
        int max = Math.min(configManager.discordRecentMessageCount(), alert.history().size());
        for (int i = 0; i < max; i++) {
            builder.append(i + 1).append(". ").append(alert.history().get(i));
            if (i + 1 < max) {
                builder.append("\\n");
            }
        }
        return builder.toString();
    }

    private int color(ModerationAlert alert) {
        if (alert.violationType().equalsIgnoreCase("Spam")) {
            return 3447003;
        }
        if (alert.violationType().equalsIgnoreCase("Reklam")) {
            return 10181046;
        }
        return switch (alert.level()) {
            case 1 -> 15844367;
            case 2 -> 15105570;
            case 3 -> 15158332;
            default -> 9807270;
        };
    }

    private String method(ModerationAlert alert) {
        if (alert.violationType().equalsIgnoreCase("Spam")) {
            return "Spam";
        }
        if (alert.violationType().equalsIgnoreCase("Reklam")) {
            return "Reklam";
        }
        return alert.exactMatch() ? "Tam Eşleşme" : "Benzerlik Algılama";
    }

    private String violationTitle(ModerationAlert alert) {
        if (alert.level() > 0 && alert.violationType().equalsIgnoreCase("Küfür")) {
            return "Level " + alert.level() + " Küfür";
        }
        return alert.violationType();
    }

    private String punishmentType(ModerationAlert alert) {
        return alert.punishmentType().equalsIgnoreCase("mute") ? "susturma" : alert.punishmentType();
    }

    private String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String json(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }
}
