package herr0w.aotomod.discord;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModerationAlert(String caseId, String violationType, String playerName, UUID uuid, String message, String detectionReason, String bannedWord, int level, String punishmentType, Duration duration, List<String> history, String serverName, Instant timestamp, String normalizedMessage, double similarityScore, boolean exactMatch) {
}
