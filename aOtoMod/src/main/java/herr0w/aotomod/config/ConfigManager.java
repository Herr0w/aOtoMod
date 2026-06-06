package herr0w.aotomod.config;

import herr0w.aotomod.util.DurationParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public String permission(String key) {
        return config.getString("permissions." + key, "automod." + key);
    }

    public boolean smartNormalization() {
        return config.getBoolean("filter.smart-normalization", true);
    }

    public boolean leetNormalization() {
        return config.getBoolean("filter.leet-normalization", true);
    }

    public List<String> words(int level) {
        List<String> words = config.getStringList("filter.level-" + level);
        if (words.isEmpty()) {
            words = config.getStringList("filter.level-" + level + "-words");
        }
        return words;
    }

    public List<String> whitelist() {
        return config.getStringList("filter.whitelist");
    }

    public Duration muteDuration(int level) {
        return DurationParser.parse(config.getString("mute.level-" + level + "-duration", "10m"));
    }

    public long cleanupIntervalTicks() {
        return Math.max(20L, config.getLong("mute.cleanup-interval-seconds", 60L) * 20L);
    }

    public int historySize() {
        return Math.max(0, config.getInt("history.size", 5));
    }

    public boolean discordEnabled() {
        return config.getBoolean("discord.enabled", false);
    }

    public boolean webhookEnabled() {
        return config.getBoolean("discord.webhook-enabled", false);
    }

    public boolean useBotButtons() {
        return config.getBoolean("discord.use-bot-buttons", config.getBoolean("discord.bot-enabled", false));
    }

    public String webhookUrl() {
        return config.getString("discord.webhook-url", "");
    }

    public String embedTitle() {
        return config.getString("discord.embed-title", "aOtoMod Uyarısı");
    }

    public String serverName() {
        return config.getString("discord.server-name", "Minecraft Sunucusu");
    }

    public boolean botEnabled() {
        return config.getBoolean("discord.bot-enabled", false);
    }

    public String botToken() {
        return config.getString("discord.bot-token", "");
    }

    public String guildId() {
        return config.getString("discord.guild-id", "");
    }

    public String channelId() {
        return config.getString("discord.channel-id", "");
    }

    public boolean buttonsEnabled() {
        return config.getBoolean("discord.buttons.enabled", config.getBoolean("discord.buttons-enabled", true));
    }

    public String buttonLabel(String key, String fallback) {
        return config.getString("discord.buttons." + key + ".label", fallback);
    }

    public String buttonAction(String key, String fallback) {
        return config.getString("discord.buttons." + key + ".action", fallback);
    }

    public boolean buttonEnabled(String key) {
        return config.getBoolean("discord.buttons." + key + ".enabled", true);
    }

    public String buttonEmoji(String key, String fallback) {
        return config.getString("discord.buttons." + key + ".emoji", fallback);
    }

    public boolean extendMuteUseModal() {
        return config.getBoolean("discord.buttons.extend-mute.use-modal", true);
    }

    public String extendMuteModalTitle() {
        return config.getString("discord.buttons.extend-mute.modal.title", "Mute Süresini Uzat");
    }

    public String extendMuteModalInputLabel() {
        return config.getString("discord.buttons.extend-mute.modal.input-label", "Ek süre");
    }

    public String extendMuteModalPlaceholder() {
        return config.getString("discord.buttons.extend-mute.modal.placeholder", "Örnek: 10m, 1h, 2d, 7d");
    }

    public int extendMuteModalMinLength() {
        return Math.max(1, config.getInt("discord.buttons.extend-mute.modal.min-length", 2));
    }

    public int extendMuteModalMaxLength() {
        return Math.max(extendMuteModalMinLength(), config.getInt("discord.buttons.extend-mute.modal.max-length", 10));
    }

    public Duration additionalMuteDuration() {
        return DurationParser.parse(config.getString("discord.buttons.extend-mute.duration", config.getString("discord.additional-mute-duration", "1h")));
    }

    public String additionalMuteDurationText() {
        return config.getString("discord.buttons.extend-mute.duration", config.getString("discord.additional-mute-duration", "1h"));
    }

    public String banCommand() {
        return config.getString("discord.buttons.ban.command", config.getString("discord.ban-command", "ban %player% Otomatik moderasyon"));
    }

    public String dateFormat() {
        return config.getString("discord.timestamp-format", config.getString("discord.date-format", "dd.MM.yyyy HH:mm:ss"));
    }

    public String footerText() {
        return config.getString("discord.footer-text", "AutoMod Sistemi");
    }

    public int discordRecentMessageCount() {
        return Math.max(0, config.getInt("discord.recent-message-count", historySize()));
    }

    public List<String> allowedRoleIds() {
        return config.getStringList("discord.allowed-role-ids");
    }

    public boolean consoleLogging() {
        return config.getBoolean("logging.console", true);
    }

    public boolean fileLogging() {
        return config.getBoolean("logging.file", true);
    }

    public boolean spamEnabled() {
        return config.getBoolean("spam.enabled", true);
    }

    public String spamBypassPermission() {
        return config.getString("spam.bypass-permission", "automod.spam.bypass");
    }

    public int spamMaxMessages() {
        return config.getInt("spam.max-messages", 5);
    }

    public int spamTimeWindowSeconds() {
        return config.getInt("spam.time-window-seconds", 3);
    }

    public int spamMaxSameMessageCount() {
        return config.getInt("spam.max-same-message-count", 3);
    }

    public int spamMaxCapsPercent() {
        return config.getInt("spam.max-caps-percent", 70);
    }

    public int spamMinLengthForCapsCheck() {
        return config.getInt("spam.min-length-for-caps-check", 8);
    }

    public int spamMaxRepeatedCharacterCount() {
        return config.getInt("spam.max-repeated-character-count", 5);
    }

    public int spamMaxRepeatedWordCount() {
        return config.getInt("spam.max-repeated-word-count", 4);
    }

    public int spamMaxSymbolPercent() {
        return config.getInt("spam.max-symbol-percent", 60);
    }

    public String spamPunishmentType() {
        return config.getString("spam.punishment.type", "mute");
    }

    public Duration spamPunishmentDuration() {
        return DurationParser.parse(config.getString("spam.punishment.duration", "30m"));
    }

    public boolean advertisementEnabled() {
        return config.getBoolean("advertisement.enabled", true);
    }

    public String advertisementBypassPermission() {
        return config.getString("advertisement.bypass-permission", "automod.advertisement.bypass");
    }

    public boolean detectIps() {
        return config.getBoolean("advertisement.detect-ips", true);
    }

    public boolean detectDomains() {
        return config.getBoolean("advertisement.detect-domains", true);
    }

    public boolean detectDiscordInvites() {
        return config.getBoolean("advertisement.detect-discord-invites", true);
    }

    public boolean detectObfuscatedLinks() {
        return config.getBoolean("advertisement.detect-obfuscated-links", true);
    }

    public List<String> allowedDomains() {
        return config.getStringList("advertisement.allowed-domains");
    }

    public List<String> allowedDiscordInvites() {
        return config.getStringList("advertisement.allowed-discord-invites");
    }

    public List<String> allowedIps() {
        return config.getStringList("advertisement.allowed-ips");
    }

    public String advertisementPunishmentType() {
        return config.getString("advertisement.punishment.type", "mute");
    }

    public Duration advertisementPunishmentDuration() {
        return DurationParser.parse(config.getString("advertisement.punishment.duration", "7d"));
    }

    public boolean similarityEnabled() {
        return config.getBoolean("similarity-detection.enabled", true);
    }

    public double similarityThreshold() {
        return config.getDouble("similarity-detection.threshold", 0.82D);
    }

    public boolean compressRepeatedLetters() {
        return config.getBoolean("similarity-detection.compress-repeated-letters", true);
    }

    public boolean removeSymbolsBetweenLetters() {
        return config.getBoolean("similarity-detection.remove-symbols-between-letters", true);
    }

    public boolean useLevenshtein() {
        return config.getBoolean("similarity-detection.use-levenshtein", true);
    }

    public boolean useJaroWinkler() {
        return config.getBoolean("similarity-detection.use-jaro-winkler", false);
    }

    public Map<String, Object> normalizationMap() {
        return config.getConfigurationSection("similarity-detection.normalization-map") == null ? Map.of() : config.getConfigurationSection("similarity-detection.normalization-map").getValues(false);
    }

    public boolean moderationPanelEnabled() {
        return config.getBoolean("moderation-panel.enabled", true);
    }

    public String moderationPanelPermission() {
        return config.getString("moderation-panel.permission", "automod.panel");
    }

    public String moderationPanelTitle() {
        return config.getString("moderation-panel.title", "&8AutoMod Paneli");
    }

    public int moderationPanelPageSize() {
        return Math.max(9, Math.min(45, config.getInt("moderation-panel.page-size", 45)));
    }

    public long moderationPanelRefreshTicks() {
        return Math.max(20L, config.getLong("moderation-panel.refresh-seconds", 10L) * 20L);
    }

    public void setPath(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
    }
}
