package herr0w.aotomod.discord;

import java.util.UUID;

public record DiscordActionRecord(String violationId, UUID playerUuid, String playerName, String muteId, String channelId, String messageId, long timestamp, String status, String actionDetail, String actor, long actionTimestamp, boolean unmuteDisabled, boolean extendDisabled, boolean banDisabled, boolean reviewDisabled) {
    public DiscordActionRecord withMessage(String channelId, String messageId) {
        return new DiscordActionRecord(violationId, playerUuid, playerName, muteId, channelId, messageId, timestamp, status, actionDetail, actor, actionTimestamp, unmuteDisabled, extendDisabled, banDisabled, reviewDisabled);
    }

    public DiscordActionRecord action(String status, String actor, boolean unmuteDisabled, boolean extendDisabled, boolean banDisabled, boolean reviewDisabled) {
        return action(status, "", actor, unmuteDisabled, extendDisabled, banDisabled, reviewDisabled);
    }

    public DiscordActionRecord action(String status, String actionDetail, String actor, boolean unmuteDisabled, boolean extendDisabled, boolean banDisabled, boolean reviewDisabled) {
        return new DiscordActionRecord(violationId, playerUuid, playerName, muteId, channelId, messageId, timestamp, status, actionDetail, actor, System.currentTimeMillis(), unmuteDisabled, extendDisabled, banDisabled, reviewDisabled);
    }
}
