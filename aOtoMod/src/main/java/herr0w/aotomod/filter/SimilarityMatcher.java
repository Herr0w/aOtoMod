package herr0w.aotomod.filter;

import herr0w.aotomod.config.ConfigManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SimilarityMatcher {
    private final ConfigManager configManager;

    public SimilarityMatcher(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public Match exact(TextNormalizer.NormalizedText text, String word) {
        String message = text.value();
        int from = 0;
        while (from <= message.length() - word.length()) {
            int index = message.indexOf(word, from);
            if (index < 0) {
                return Match.none();
            }
            int after = index + word.length();
            if (boundary(text, index, after)) {
                return new Match(true, index, after, 1D, true);
            }
            from = index + 1;
        }
        return Match.none();
    }

    public Match similar(TextNormalizer.NormalizedText text, String word) {
        if (!configManager.similarityEnabled() || word.length() < 2) {
            return Match.none();
        }
        String message = text.value();
        List<Match> matches = new ArrayList<>();
        int min = Math.max(1, word.length() - 2);
        int max = word.length() + 2;
        for (int start = 0; start < message.length(); start++) {
            for (int length = min; length <= max && start + length <= message.length(); length++) {
                int end = start + length;
                if (!boundary(text, start, end)) {
                    continue;
                }
                String candidate = message.substring(start, end);
                double score = score(candidate, word);
                if (score >= configManager.similarityThreshold()) {
                    matches.add(new Match(true, start, end, score, false));
                }
            }
        }
        return matches.stream().max(Comparator.comparingDouble(Match::score)).orElse(Match.none());
    }

    private boolean boundary(TextNormalizer.NormalizedText text, int start, int end) {
        return text.boundaryBefore().get(start) && (end >= text.value().length() || text.boundaryBefore().get(end));
    }

    private double score(String left, String right) {
        double best = 0D;
        if (configManager.useLevenshtein()) {
            int distance = levenshtein(left, right);
            best = Math.max(best, 1D - ((double) distance / Math.max(left.length(), right.length())));
        }
        if (configManager.useJaroWinkler()) {
            best = Math.max(best, jaroWinkler(left, right));
        }
        return best;
    }

    private int levenshtein(String left, String right) {
        int[] costs = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            costs[0] = i;
            int northwest = i - 1;
            for (int j = 1; j <= right.length(); j++) {
                int current = costs[j];
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                costs[j] = Math.min(Math.min(costs[j] + 1, costs[j - 1] + 1), northwest + cost);
                northwest = current;
            }
        }
        return costs[right.length()];
    }

    private double jaroWinkler(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1D;
        }
        int maxDistance = Math.max(s1.length(), s2.length()) / 2 - 1;
        boolean[] s1Matches = new boolean[s1.length()];
        boolean[] s2Matches = new boolean[s2.length()];
        int matches = 0;
        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - maxDistance);
            int end = Math.min(i + maxDistance + 1, s2.length());
            for (int j = start; j < end; j++) {
                if (!s2Matches[j] && s1.charAt(i) == s2.charAt(j)) {
                    s1Matches[i] = true;
                    s2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }
        if (matches == 0) {
            return 0D;
        }
        double transpositions = 0D;
        int k = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        double jaro = ((matches / (double) s1.length()) + (matches / (double) s2.length()) + ((matches - transpositions / 2D) / matches)) / 3D;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(s1.length(), s2.length())); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }
        return jaro + prefix * 0.1D * (1D - jaro);
    }

    public record Match(boolean found, int start, int end, double score, boolean exact) {
        public static Match none() {
            return new Match(false, -1, -1, 0D, false);
        }
    }
}
