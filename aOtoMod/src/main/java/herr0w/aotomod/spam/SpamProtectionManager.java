package herr0w.aotomod.spam;

import herr0w.aotomod.config.ConfigManager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpamProtectionManager {
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+", Pattern.UNICODE_CHARACTER_CLASS);
    private final ConfigManager configManager;
    private final Map<UUID, Queue<MessageEntry>> messages = new ConcurrentHashMap<>();

    public SpamProtectionManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public SpamCheckResult check(UUID uuid, String message) {
        if (!configManager.spamEnabled()) {
            return SpamCheckResult.clean();
        }
        long now = System.currentTimeMillis();
        Queue<MessageEntry> queue = messages.computeIfAbsent(uuid, ignored -> new ArrayDeque<>());
        long window = Math.max(1L, configManager.spamTimeWindowSeconds()) * 1000L;
        queue.removeIf(entry -> now - entry.timestamp() > window);
        queue.add(new MessageEntry(normalize(message), message, now));
        if (queue.size() >= configManager.spamMaxMessages()) {
            return new SpamCheckResult(true, "Çok hızlı mesaj gönderimi");
        }
        int same = 0;
        String normalized = normalize(message);
        for (MessageEntry entry : queue) {
            if (entry.normalized().equals(normalized)) {
                same++;
            }
        }
        if (same >= configManager.spamMaxSameMessageCount()) {
            return new SpamCheckResult(true, "Aynı mesaj tekrarı");
        }
        if (capsPercent(message) >= configManager.spamMaxCapsPercent() && letters(message) >= configManager.spamMinLengthForCapsCheck()) {
            return new SpamCheckResult(true, "Aşırı büyük harf kullanımı");
        }
        if (maxRepeatedCharacter(message) > configManager.spamMaxRepeatedCharacterCount()) {
            return new SpamCheckResult(true, "Aşırı karakter tekrarı");
        }
        if (maxRepeatedWord(message) >= configManager.spamMaxRepeatedWordCount()) {
            return new SpamCheckResult(true, "Aşırı kelime tekrarı");
        }
        if (symbolPercent(message) >= configManager.spamMaxSymbolPercent() && message.length() >= 6) {
            return new SpamCheckResult(true, "Aşırı sembol kullanımı");
        }
        if (message.trim().length() <= 3 && queue.size() >= Math.max(3, configManager.spamMaxMessages() - 1)) {
            return new SpamCheckResult(true, "Kısa mesaj floodu");
        }
        return SpamCheckResult.clean();
    }

    public void clear(UUID uuid) {
        messages.remove(uuid);
    }

    private String normalize(String message) {
        return message == null ? "" : message.trim().replaceAll("\\s+", " ").toLowerCase(Locale.forLanguageTag("tr-TR"));
    }

    private int letters(String message) {
        int count = 0;
        for (int i = 0; i < message.length(); i++) {
            if (Character.isLetter(message.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private int capsPercent(String message) {
        int letters = 0;
        int caps = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) {
                    caps++;
                }
            }
        }
        return letters == 0 ? 0 : (caps * 100) / letters;
    }

    private int maxRepeatedCharacter(String message) {
        int max = 0;
        int current = 0;
        char previous = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = Character.toLowerCase(message.charAt(i));
            if (c == previous) {
                current++;
            } else {
                current = 1;
                previous = c;
            }
            max = Math.max(max, current);
        }
        return max;
    }

    private int maxRepeatedWord(String message) {
        Matcher matcher = WORD.matcher(message.toLowerCase(Locale.forLanguageTag("tr-TR")));
        Map<String, Integer> counts = new HashMap<>();
        int max = 0;
        while (matcher.find()) {
            int count = counts.merge(matcher.group(), 1, Integer::sum);
            max = Math.max(max, count);
        }
        return max;
    }

    private int symbolPercent(String message) {
        if (message.isBlank()) {
            return 0;
        }
        int symbols = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                symbols++;
            }
        }
        return (symbols * 100) / message.length();
    }

    private record MessageEntry(String normalized, String original, long timestamp) {
    }
}
