package herr0w.aotomod.discord;

import herr0w.aotomod.config.ConfigManager;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public final class DiscordButtonManager {
    private final ConfigManager configManager;

    public DiscordButtonManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<ActionRow> rows(DiscordActionRecord record) {
        List<Button> buttons = new ArrayList<>();
        add(buttons, "unmute", Button.success(id("unmute", record), label("unmute", "Yanlış İşlem / Mute Kaldır")), emoji("unmute", "✅"), record.unmuteDisabled());
        add(buttons, "extend-mute", Button.primary(id("extend", record), label("extend-mute", "Mute Süresini Uzat")), emoji("extend-mute", "➕"), record.extendDisabled());
        add(buttons, "ban", Button.danger(id("ban", record), label("ban", "Oyuncuyu Banla")), emoji("ban", "🔨"), record.banDisabled());
        add(buttons, "history", Button.secondary(id("history", record), label("history", "Geçmişi Göster")), emoji("history", "📋"), false);
        add(buttons, "review", Button.secondary(id("review", record), label("review", "İncelendi")), emoji("review", "🟢"), record.reviewDisabled());
        return buttons.isEmpty() ? List.of() : List.of(ActionRow.of(buttons));
    }

    private void add(List<Button> buttons, String key, Button button, String emoji, boolean disabled) {
        if (!configManager.buttonEnabled(key)) {
            return;
        }
        if (!emoji.isBlank()) {
            button = button.withEmoji(Emoji.fromUnicode(emoji));
        }
        buttons.add(disabled ? button.asDisabled() : button);
    }

    private String id(String action, DiscordActionRecord record) {
        return "automod:" + action + ":" + record.violationId();
    }

    private String label(String key, String fallback) {
        return configManager.buttonLabel(key, fallback);
    }

    private String emoji(String key, String fallback) {
        return configManager.buttonEmoji(key, fallback);
    }
}
