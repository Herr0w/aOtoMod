package herr0w.aotomod.filter;

public record WordMatchResult(boolean detected, String originalMessage, String normalizedMessage, String bannedWord, double similarityScore, int severityLevel, boolean exactMatch) {
    public static WordMatchResult clean(String originalMessage, String normalizedMessage) {
        return new WordMatchResult(false, originalMessage, normalizedMessage, "", 0D, 0, false);
    }
}
