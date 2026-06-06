package herr0w.aotomod.mute;

import java.util.UUID;

public record MuteRecord(UUID uuid, String playerName, long expiresAt, int level, String reason, String bannedWord) {
    public boolean expired() {
        return expiresAt > 0L && System.currentTimeMillis() >= expiresAt;
    }

    public long remainingMillis() {
        if (expiresAt <= 0L) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, expiresAt - System.currentTimeMillis());
    }
}
