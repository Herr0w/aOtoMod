package herr0w.aotomod.filter;

import herr0w.aotomod.config.ConfigManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WordFilter {
    private final ConfigManager configManager;
    private final TextNormalizer normalizer;
    private final SimilarityMatcher matcher;
    private List<Entry> entries = List.of();
    private List<String> whitelist = List.of();

    public WordFilter(ConfigManager configManager) {
        this.configManager = configManager;
        this.normalizer = new TextNormalizer(configManager);
        this.matcher = new SimilarityMatcher(configManager);
        reload();
    }

    public void reload() {
        List<Entry> loaded = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            for (String word : configManager.words(level)) {
                String normalized = normalizer.normalizeWord(word);
                if (!normalized.isBlank()) {
                    loaded.add(new Entry(level, word, normalized));
                }
            }
        }
        loaded.sort(Comparator.comparingInt(Entry::level).reversed().thenComparing(Comparator.comparingInt((Entry entry) -> entry.normalized().length()).reversed()));
        entries = List.copyOf(loaded);
        whitelist = configManager.whitelist().stream().map(normalizer::normalizeWord).filter(value -> !value.isBlank()).toList();
    }

    public FilterResult check(String message) {
        WordMatchResult result = match(message);
        if (!result.detected()) {
            return FilterResult.clean();
        }
        return new FilterResult(true, result.severityLevel(), result.bannedWord(), result.normalizedMessage(), result.similarityScore(), result.exactMatch());
    }

    public WordMatchResult match(String message) {
        TextNormalizer.NormalizedText normalized = normalizer.normalizeMessage(message);
        if (normalized.value().isBlank()) {
            return WordMatchResult.clean(message, normalized.value());
        }
        for (Entry entry : entries) {
            SimilarityMatcher.Match exact = matcher.exact(normalized, entry.normalized());
            if (exact.found() && !whitelisted(normalized, exact)) {
                return new WordMatchResult(true, message, normalized.value(), entry.word(), 1D, entry.level(), true);
            }
            SimilarityMatcher.Match similar = matcher.similar(normalized, entry.normalized());
            if (similar.found() && !whitelisted(normalized, similar)) {
                return new WordMatchResult(true, message, normalized.value(), entry.word(), similar.score(), entry.level(), false);
            }
        }
        return WordMatchResult.clean(message, normalized.value());
    }

    private boolean whitelisted(TextNormalizer.NormalizedText text, SimilarityMatcher.Match bannedMatch) {
        for (String allowed : whitelist) {
            SimilarityMatcher.Match allowedMatch = matcher.exact(text, allowed);
            if (allowedMatch.found() && allowedMatch.start() <= bannedMatch.start() && allowedMatch.end() >= bannedMatch.end()) {
                return true;
            }
        }
        return false;
    }

    private record Entry(int level, String word, String normalized) {
    }
}
