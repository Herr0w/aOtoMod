package herr0w.aotomod.discord;

import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.mute.MuteRecord;
import herr0w.aotomod.util.DurationParser;
import herr0w.aotomod.util.TimeFormatter;
import herr0w.aotomod.violation.ViolationRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public final class DiscordEmbedBuilder {
    private final ConfigManager configManager;
    private final MuteManager muteManager;

    public DiscordEmbedBuilder(ConfigManager configManager, MuteManager muteManager) {
        this.configManager = configManager;
        this.muteManager = muteManager;
    }

    public MessageEmbed moderation(ModerationAlert alert, DiscordActionRecord actionRecord) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(configManager.dateFormat()).withZone(ZoneId.systemDefault());
        String method = method(alert);
        String recent = numbered(alert.history().stream().limit(configManager.discordRecentMessageCount()).toList());
        String remaining = remaining(alert);
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(configManager.embedTitle())
                .setColor(color(alert))
                .addField("Oyuncu", alert.playerName(), true)
                .addField("UUID", alert.uuid().toString(), true)
                .addField("İhlal Türü", violationTitle(alert), true)
                .addField("Tespit", blank(alert.bannedWord(), alert.detectionReason()), true)
                .addField("Ceza", punishment(alert), true)
                .addField("Kalan Süre", remaining, true)
                .addField("Yöntem", method, true)
                .addField("Benzerlik Skoru", alert.similarityScore() > 0D && !alert.exactMatch() ? String.format("%.2f", alert.similarityScore()) : "-", true)
                .addField("Sunucu", alert.serverName(), true)
                .addField("Mesaj", quote(alert.message()), false)
                .addField("Son Mesajlar", recent.isBlank() ? "Yok" : recent, false)
                .addField("Tarih", formatter.format(alert.timestamp()), true)
                .setFooter(configManager.footerText())
                .setTimestamp(alert.timestamp());
        if (actionRecord != null && !actionRecord.status().isBlank()) {
            builder.addField("Durum", actionRecord.status(), true);
            if (!actionRecord.actionDetail().isBlank()) {
                builder.addField("Eklenen Süre", actionRecord.actionDetail(), true);
            }
            builder.addField("İşlemi Yapan", blank(actionRecord.actor(), "-"), true);
            builder.addField("İşlem Tarihi", actionRecord.actionTimestamp() > 0L ? formatter.format(Instant.ofEpochMilli(actionRecord.actionTimestamp())) : "-", true);
        }
        return builder.build();
    }

    public MessageEmbed moderation(ViolationRecord violation, DiscordActionRecord actionRecord, List<String> history) {
        Duration duration = DurationParser.parse(violation.punishmentDuration());
        ModerationAlert alert = new ModerationAlert(violation.id(), violation.type(), violation.playerName(), violation.uuid(), violation.originalMessage(), violation.detectionReason(), violation.detectionReason(), violation.severityLevel(), violation.punishmentType(), duration, history, configManager.serverName(), Instant.ofEpochMilli(violation.timestamp()), violation.normalizedMessage(), violation.similarityScore(), violation.exactMatch());
        return moderation(alert, actionRecord);
    }

    private String remaining(ModerationAlert alert) {
        Optional<MuteRecord> mute = muteManager.getMute(alert.uuid());
        return mute.map(record -> TimeFormatter.format(Duration.ofMillis(record.remainingMillis()))).orElse("Yok");
    }

    private String punishment(ModerationAlert alert) {
        String type = alert.punishmentType().equalsIgnoreCase("mute") ? "susturma" : alert.punishmentType();
        return TimeFormatter.format(alert.duration()) + " " + type;
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

    private Color color(ModerationAlert alert) {
        if (alert.violationType().equalsIgnoreCase("Spam")) {
            return new Color(52, 152, 219);
        }
        if (alert.violationType().equalsIgnoreCase("Reklam")) {
            return new Color(155, 89, 182);
        }
        return switch (alert.level()) {
            case 1 -> new Color(241, 196, 15);
            case 2 -> new Color(230, 126, 34);
            case 3 -> new Color(231, 76, 60);
            default -> new Color(149, 165, 166);
        };
    }

    private String numbered(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            builder.append(i + 1).append(". ").append(values.get(i));
            if (i + 1 < values.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private String quote(String value) {
        return "\"" + blank(value, "-") + "\"";
    }

    private String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
