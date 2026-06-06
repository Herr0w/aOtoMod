package herr0w.aotomod.util;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern PART = Pattern.compile("(\\d+)\\s*(saniye|dakika|saat|hafta|gün|gun|sn|dk|s|m|h|d|w)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRICT = Pattern.compile("^\\s*(\\d+)\\s*([smhdw])\\s*$", Pattern.CASE_INSENSITIVE);

    private DurationParser() {
    }

    public static Duration parse(String value) {
        if (value == null || value.isBlank()) {
            return Duration.ZERO;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("permanent") || normalized.equals("kalici") || normalized.equals("kalıcı")) {
            return Duration.ofMillis(Long.MAX_VALUE);
        }
        Matcher matcher = PART.matcher(normalized);
        Duration duration = Duration.ZERO;
        boolean found = false;
        while (matcher.find()) {
            found = true;
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            duration = duration.plus(switch (unit) {
                case "saniye", "sn", "s" -> Duration.ofSeconds(amount);
                case "dakika", "dk", "m" -> Duration.ofMinutes(amount);
                case "saat", "h" -> Duration.ofHours(amount);
                case "gün", "gun", "d" -> Duration.ofDays(amount);
                case "hafta", "w" -> Duration.ofDays(amount * 7L);
                default -> Duration.ZERO;
            });
        }
        if (!found) {
            return Duration.ofMinutes(10);
        }
        return duration;
    }

    public static Optional<Duration> parseStrict(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = STRICT.matcher(value.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return Optional.empty();
        }
        long amount;
        try {
            amount = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
        if (amount <= 0L) {
            return Optional.empty();
        }
        Duration duration = switch (matcher.group(2)) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            case "w" -> Duration.ofDays(amount * 7L);
            default -> Duration.ZERO;
        };
        return duration.isZero() ? Optional.empty() : Optional.of(duration);
    }
}
