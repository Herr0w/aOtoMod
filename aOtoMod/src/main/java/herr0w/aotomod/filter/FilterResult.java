package herr0w.aotomod.filter;

public record FilterResult(boolean detected, int level, String word, String normalizedMessage, double similarityScore, boolean exactMatch) {
    public static FilterResult clean() {
        return new FilterResult(false, 0, "", "", 0D, false);
    }
}
