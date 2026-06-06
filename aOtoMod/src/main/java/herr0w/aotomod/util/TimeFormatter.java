package herr0w.aotomod.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class TimeFormatter {
    private TimeFormatter() {
    }

    public static String format(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "0 saniye";
        }
        if (duration.toDays() > 36500L) {
            return "kalıcı";
        }
        long seconds = duration.getSeconds();
        long days = seconds / 86400L;
        seconds %= 86400L;
        long hours = seconds / 3600L;
        seconds %= 3600L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + " gün");
        }
        if (hours > 0) {
            parts.add(hours + " saat");
        }
        if (minutes > 0) {
            parts.add(minutes + " dakika");
        }
        if (seconds > 0 && parts.size() < 2) {
            parts.add(seconds + " saniye");
        }
        if (parts.isEmpty()) {
            return "0 saniye";
        }
        return String.join(" ", parts.subList(0, Math.min(2, parts.size())));
    }
}
