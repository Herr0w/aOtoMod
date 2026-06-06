package herr0w.aotomod.discord;

import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.config.MessageManager;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.util.DurationParser;
import herr0w.aotomod.violation.ViolationManager;
import herr0w.aotomod.violation.ViolationRecord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DiscordBotManager implements DiscordManager {
    private static final String EXTEND_INPUT_ID = "automod-extra-duration";
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final MuteManager muteManager;
    private final DiscordActionRegistry actionRegistry;
    private final DiscordPermissionChecker permissionChecker;
    private final DiscordEmbedBuilder embedBuilder;
    private final DiscordButtonManager buttonManager;
    private JDA jda;
    private ViolationManager violationManager;

    public DiscordBotManager(JavaPlugin plugin, ConfigManager configManager, MessageManager messageManager, MuteManager muteManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.muteManager = muteManager;
        this.actionRegistry = new DiscordActionRegistry(plugin);
        this.permissionChecker = new DiscordPermissionChecker(configManager);
        this.embedBuilder = new DiscordEmbedBuilder(configManager, muteManager);
        this.buttonManager = new DiscordButtonManager(configManager);
        actionRegistry.load();
    }

    @Override
    public void start() {
        if (!configManager.discordEnabled() || !configManager.useBotButtons() || configManager.botToken().isBlank()) {
            return;
        }
        try {
            jda = JDABuilder.createDefault(configManager.botToken()).addEventListeners(new DiscordInteractionListener(this)).build();
        } catch (Exception exception) {
            plugin.getLogger().warning(exception.getMessage());
        }
    }

    public void setViolationManager(ViolationManager violationManager) {
        this.violationManager = violationManager;
    }

    @Override
    public void restart() {
        shutdown();
        actionRegistry.load();
        start();
    }

    @Override
    public void shutdown() {
        actionRegistry.save();
        if (jda != null) {
            jda.shutdownNow();
            jda = null;
        }
    }

    public boolean canSendButtons() {
        return configManager.discordEnabled() && configManager.useBotButtons() && configManager.buttonsEnabled() && jda != null;
    }

    @Override
    public void send(ModerationAlert alert) {
        if (!canSendButtons() || configManager.channelId().isBlank()) {
            return;
        }
        if (!awaitReady()) {
            return;
        }
        TextChannel channel = channel();
        if (channel == null) {
            return;
        }
        DiscordActionRecord record = new DiscordActionRecord(alert.caseId(), alert.uuid(), alert.playerName(), alert.caseId(), channel.getId(), "", System.currentTimeMillis(), "", "", "", 0L, false, false, false, false);
        channel.sendMessageEmbeds(embedBuilder.moderation(alert, record)).setComponents(buttonManager.rows(record)).queue(message -> actionRegistry.put(record.withMessage(channel.getId(), message.getId())));
    }

    public void handleButton(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("automod:")) {
            return;
        }
        if (!permissionChecker.allowed(event.getMember())) {
            event.reply(messageManager.plain("discord-no-action-permission")).setEphemeral(true).queue();
            return;
        }
        String[] parts = id.split(":", 3);
        if (parts.length < 3) {
            event.reply(messageManager.plain("discord-invalid-action")).setEphemeral(true).queue();
            return;
        }
        Optional<DiscordActionRecord> optional = actionRegistry.get(parts[2]);
        if (optional.isEmpty()) {
            event.reply(messageManager.plain("discord-action-not-found")).setEphemeral(true).queue();
            return;
        }
        DiscordActionRecord record = optional.get();
        if (parts[1].equals("extend") && configManager.extendMuteUseModal()) {
            if (muteManager.getMute(record.playerUuid()).isEmpty()) {
                event.reply(messageManager.plain("discord-player-not-muted")).setEphemeral(true).queue();
                return;
            }
            event.replyModal(extendModal(record)).queue();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> execute(event, parts[1], record));
    }

    public void handleModal(ModalInteractionEvent event) {
        String id = event.getModalId();
        if (!id.startsWith("automod-modal:extend:")) {
            return;
        }
        if (!permissionChecker.allowed(event.getMember())) {
            event.reply(messageManager.plain("discord-no-action-permission")).setEphemeral(true).queue();
            return;
        }
        String violationId = id.substring("automod-modal:extend:".length());
        Optional<DiscordActionRecord> optional = actionRegistry.get(violationId);
        if (optional.isEmpty()) {
            event.reply(messageManager.plain("discord-action-not-found")).setEphemeral(true).queue();
            return;
        }
        String value = event.getValue(EXTEND_INPUT_ID) == null ? "" : event.getValue(EXTEND_INPUT_ID).getAsString().trim();
        Optional<Duration> duration = DurationParser.parseStrict(value);
        if (duration.isEmpty()) {
            event.reply(messageManager.plain("discord-invalid-duration")).setEphemeral(true).queue();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> submitExtendModal(event, optional.get(), value, duration.get()));
    }

    private void execute(ButtonInteractionEvent event, String action, DiscordActionRecord record) {
        String actor = event.getUser().getName();
        switch (action) {
            case "unmute" -> {
                muteManager.unmute(record.playerUuid());
                update(record.action("Mute kaldırıldı", actor, true, record.extendDisabled(), record.banDisabled(), record.reviewDisabled()), event);
                event.reply(messageManager.plain("discord-button-unmute")).setEphemeral(true).queue();
            }
            case "extend" -> {
                Duration duration = configManager.additionalMuteDuration();
                if (!muteManager.extendMute(record.playerUuid(), duration)) {
                    event.reply(messageManager.plain("discord-player-not-muted")).setEphemeral(true).queue();
                    return;
                }
                String formatted = configManager.additionalMuteDurationText();
                update(record.action("Mute süresi uzatıldı", formatted, actor, record.unmuteDisabled(), false, record.banDisabled(), record.reviewDisabled()), event);
                event.reply(messageManager.get("discord-extend-success", Map.of("duration", formatted))).setEphemeral(true).queue();
            }
            case "ban" -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(record.playerUuid());
                String command = configManager.banCommand().replace("%player%", player.getName() == null ? record.playerName() : player.getName()).replace("%uuid%", record.playerUuid().toString()).replace("%reason%", "AutoMod");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                update(record.action("Oyuncu banlandı", actor, record.unmuteDisabled(), record.extendDisabled(), true, record.reviewDisabled()), event);
                event.reply(messageManager.plain("discord-button-ban")).setEphemeral(true).queue();
            }
            case "history" -> event.reply(history(record)).setEphemeral(true).queue();
            case "review" -> {
                update(record.action("İncelendi", actor, record.unmuteDisabled(), record.extendDisabled(), record.banDisabled(), true), event);
                event.reply(messageManager.plain("discord-button-review")).setEphemeral(true).queue();
            }
            default -> event.reply(messageManager.plain("discord-unknown-action")).setEphemeral(true).queue();
        }
    }

    private void submitExtendModal(ModalInteractionEvent event, DiscordActionRecord record, String rawDuration, Duration duration) {
        if (!muteManager.extendMute(record.playerUuid(), duration)) {
            event.reply(messageManager.plain("discord-player-not-muted")).setEphemeral(true).queue();
            return;
        }
        DiscordActionRecord updated = record.action("Mute süresi uzatıldı", rawDuration, event.getUser().getName(), record.unmuteDisabled(), false, record.banDisabled(), record.reviewDisabled());
        actionRegistry.put(updated);
        if (violationManager != null) {
            violationManager.staffAction(updated.violationId(), updated.status() + " " + rawDuration + " - " + updated.actor());
        }
        editStoredMessage(updated);
        event.reply(messageManager.get("discord-extend-success", Map.of("duration", rawDuration))).setEphemeral(true).queue();
    }

    private void update(DiscordActionRecord record, ButtonInteractionEvent event) {
        actionRegistry.put(record);
        if (violationManager != null) {
            violationManager.staffAction(record.violationId(), record.status() + " - " + record.actor());
        }
        Optional<ViolationRecord> violation = violationManager == null ? Optional.empty() : violationManager.byId(record.violationId());
        if (violation.isPresent()) {
            event.getMessage().editMessageEmbeds(embedBuilder.moderation(violation.get(), record, List.of())).setComponents(buttonManager.rows(record)).queue();
        }
    }

    private void editStoredMessage(DiscordActionRecord record) {
        if (violationManager == null || record.channelId().isBlank() || record.messageId().isBlank() || jda == null) {
            return;
        }
        Optional<ViolationRecord> violation = violationManager.byId(record.violationId());
        TextChannel channel = jda.getTextChannelById(record.channelId());
        if (violation.isPresent() && channel != null) {
            channel.editMessageEmbedsById(record.messageId(), embedBuilder.moderation(violation.get(), record, List.of())).setComponents(buttonManager.rows(record)).queue();
        }
    }

    private Modal extendModal(DiscordActionRecord record) {
        TextInput input = TextInput.create(EXTEND_INPUT_ID, configManager.extendMuteModalInputLabel(), TextInputStyle.SHORT)
                .setPlaceholder(configManager.extendMuteModalPlaceholder())
                .setRequired(true)
                .setMinLength(configManager.extendMuteModalMinLength())
                .setMaxLength(configManager.extendMuteModalMaxLength())
                .build();
        return Modal.create("automod-modal:extend:" + record.violationId(), configManager.extendMuteModalTitle()).addComponents(ActionRow.of(input)).build();
    }

    private String history(DiscordActionRecord record) {
        if (violationManager == null) {
            return messageManager.plain("discord-history-empty");
        }
        List<ViolationRecord> records = violationManager.byPlayer(record.playerUuid().toString()).stream().limit(5).toList();
        if (records.isEmpty()) {
            return messageManager.plain("discord-history-empty");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(configManager.dateFormat()).withZone(ZoneId.systemDefault());
        StringBuilder builder = new StringBuilder("Son ihlaller:\n");
        for (int i = 0; i < records.size(); i++) {
            ViolationRecord violation = records.get(i);
            builder.append(i + 1).append(". ").append(violation.type()).append(" - ").append(violation.detectionReason()).append(" - ").append(formatter.format(java.time.Instant.ofEpochMilli(violation.timestamp())));
            if (i + 1 < records.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private boolean awaitReady() {
        try {
            jda.awaitReady();
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private TextChannel channel() {
        if (!configManager.guildId().isBlank()) {
            Guild guild = jda.getGuildById(configManager.guildId());
            if (guild != null) {
                return guild.getTextChannelById(configManager.channelId());
            }
        }
        return jda.getTextChannelById(configManager.channelId());
    }
}
