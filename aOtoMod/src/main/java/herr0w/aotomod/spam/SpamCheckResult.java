package herr0w.aotomod.spam;

public record SpamCheckResult(boolean detected, String reason) {
    public static SpamCheckResult clean() {
        return new SpamCheckResult(false, "");
    }
}
