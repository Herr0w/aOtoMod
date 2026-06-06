package herr0w.aotomod.violation;

import java.util.UUID;

public record ViolationRecord(String id, String playerName, UUID uuid, String type, String originalMessage, String detectionReason, int severityLevel, String punishmentType, String punishmentDuration, long timestamp, String staffAction, String normalizedMessage, double similarityScore, boolean exactMatch) {
}
