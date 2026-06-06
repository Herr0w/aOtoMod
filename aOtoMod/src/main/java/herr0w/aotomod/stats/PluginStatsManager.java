package herr0w.aotomod.stats;

import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.violation.ViolationManager;

public final class PluginStatsManager {
    private final ViolationManager violationManager;
    private final MuteManager muteManager;

    public PluginStatsManager(ViolationManager violationManager, MuteManager muteManager) {
        this.violationManager = violationManager;
        this.muteManager = muteManager;
    }

    public int totalViolations() {
        return violationManager.count();
    }

    public int activeMutes() {
        return muteManager.activeMutes().size();
    }

    public int profanityViolations() {
        return violationManager.countType("Küfür");
    }

    public int spamViolations() {
        return violationManager.countType("Spam");
    }

    public int advertisementViolations() {
        return violationManager.countType("Reklam");
    }
}
