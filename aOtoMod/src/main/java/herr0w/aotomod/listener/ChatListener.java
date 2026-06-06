package herr0w.aotomod.listener;

import herr0w.aotomod.AOtoMod;
import herr0w.aotomod.advertisement.AdvertisementCheckResult;
import herr0w.aotomod.advertisement.AdvertisementProtectionManager;
import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.config.MessageManager;
import herr0w.aotomod.discord.DiscordBotManager;
import herr0w.aotomod.discord.DiscordWebhookManager;
import herr0w.aotomod.discord.ModerationAlert;
import herr0w.aotomod.filter.FilterResult;
import herr0w.aotomod.filter.WordFilter;
import herr0w.aotomod.history.PlayerMessageHistoryManager;
import herr0w.aotomod.logging.ModerationLogger;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.mute.MuteRecord;
import herr0w.aotomod.spam.SpamCheckResult;
import herr0w.aotomod.spam.SpamProtectionManager;
import herr0w.aotomod.util.TimeFormatter;
import herr0w.aotomod.violation.ViolationManager;
import herr0w.aotomod.violation.ViolationRecord;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ChatListener implements Listener {
    private final AOtoMod plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final MuteManager muteManager;
    private final WordFilter wordFilter;
    private final PlayerMessageHistoryManager historyManager;
    private final DiscordWebhookManager webhookManager;
    private final DiscordBotManager botManager;
    private final SpamProtectionManager spamProtectionManager;
    private final AdvertisementProtectionManager advertisementProtectionManager;
    private final ViolationManager violationManager;
    private final ModerationLogger moderationLogger;

    public ChatListener(AOtoMod plugin, ConfigManager configManager, MessageManager messageManager, MuteManager muteManager, WordFilter wordFilter, PlayerMessageHistoryManager historyManager, DiscordWebhookManager webhookManager, DiscordBotManager botManager, SpamProtectionManager spamProtectionManager, AdvertisementProtectionManager advertisementProtectionManager, ViolationManager violationManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.muteManager = muteManager;
        this.wordFilter = wordFilter;
        this.historyManager = historyManager;
        this.webhookManager = webhookManager;
        this.botManager = botManager;
        this.spamProtectionManager = spamProtectionManager;
        this.advertisementProtectionManager = advertisementProtectionManager;
        this.violationManager = violationManager;
        this.moderationLogger = new ModerationLogger(plugin, configManager);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!configManager.isEnabled() || player.hasPermission(configManager.permission("bypass"))) {
            return;
        }
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        UUID uuid = player.getUniqueId();
        MuteRecord activeMute = muteManager.getMute(uuid).orElse(null);
        if (activeMute != null) {
            event.setCancelled(true);
            Duration remaining = Duration.ofMillis(activeMute.remainingMillis());
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(messageManager.get("mute-remaining", Map.of("remaining", TimeFormatter.format(remaining)))));
            return;
        }
        historyManager.add(uuid, message);
        FilterResult word = wordFilter.check(message);
        if (word.detected()) {
            punish(event, player, "Küfür", word.exactMatch() ? "Yasaklı kelime" : "Benzerlik eşleşmesi", word.word(), word.level(), "mute", configManager.muteDuration(word.level()), word.normalizedMessage(), word.similarityScore(), word.exactMatch());
            return;
        }
        if (!player.hasPermission(configManager.advertisementBypassPermission())) {
            AdvertisementCheckResult advertisement = advertisementProtectionManager.check(message);
            if (advertisement.detected()) {
                punish(event, player, "Reklam", advertisement.type(), advertisement.detectedValue(), 0, configManager.advertisementPunishmentType(), configManager.advertisementPunishmentDuration(), "", 0D, true);
                return;
            }
        }
        if (!player.hasPermission(configManager.spamBypassPermission())) {
            SpamCheckResult spam = spamProtectionManager.check(uuid, message);
            if (spam.detected()) {
                punish(event, player, "Spam", spam.reason(), spam.reason(), 0, configManager.spamPunishmentType(), configManager.spamPunishmentDuration(), "", 0D, true);
            }
        }
    }

    private void punish(AsyncChatEvent event, Player player, String type, String reason, String detected, int level, String punishmentType, Duration duration, String normalizedMessage, double similarityScore, boolean exactMatch) {
        event.setCancelled(true);
        if (punishmentType.equalsIgnoreCase("mute")) {
            muteManager.mute(player.getUniqueId(), player.getName(), duration, level, reason, detected);
        }
        String caseId = Long.toHexString(System.currentTimeMillis()) + Integer.toHexString(player.getUniqueId().hashCode());
        ViolationRecord record = new ViolationRecord(caseId, player.getName(), player.getUniqueId(), type, PlainTextComponentSerializer.plainText().serialize(event.message()), reason, level, punishmentType, TimeFormatter.format(duration), System.currentTimeMillis(), "", normalizedMessage, similarityScore, exactMatch);
        violationManager.add(record);
        ModerationAlert alert = new ModerationAlert(caseId, type, player.getName(), player.getUniqueId(), record.originalMessage(), reason, detected, level, punishmentType, duration, historyManager.get(player.getUniqueId()), configManager.serverName(), Instant.now(), normalizedMessage, similarityScore, exactMatch);
        moderationLogger.log(alert);
        webhookManager.send(alert);
        if (botManager.canSendButtons()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> botManager.send(alert));
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(messageManager.get("mute-notification", Map.of("duration", TimeFormatter.format(duration), "level", String.valueOf(level), "word", detected, "type", type, "reason", reason)));
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission(configManager.permission("reload")) || online.hasPermission(configManager.moderationPanelPermission()) || online.isOp()) {
                    online.sendMessage(messageManager.get("staff-alert", Map.of("player", player.getName(), "level", String.valueOf(level), "word", detected, "type", type, "reason", reason)));
                }
            }
        });
    }
}
