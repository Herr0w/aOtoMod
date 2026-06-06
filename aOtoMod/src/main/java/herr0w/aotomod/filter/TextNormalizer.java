package herr0w.aotomod.filter;

import herr0w.aotomod.config.ConfigManager;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TextNormalizer {
    private final ConfigManager configManager;

    public TextNormalizer(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public NormalizedText normalizeMessage(String input) {
        if (input == null) {
            return new NormalizedText("", List.of());
        }
        String lower = input.toLowerCase(Locale.forLanguageTag("tr-TR"));
        String ascii = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder builder = new StringBuilder();
        List<Boolean> boundaries = new ArrayList<>();
        boolean boundaryBeforeNext = true;
        char previous = 0;
        for (int i = 0; i < ascii.length(); i++) {
            char c = normalizeChar(ascii.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                if (!configManager.compressRepeatedLetters() || builder.isEmpty() || c != previous) {
                    boundaries.add(boundaryBeforeNext);
                    builder.append(c);
                    previous = c;
                }
                boundaryBeforeNext = false;
            } else {
                boundaryBeforeNext = true;
                previous = 0;
            }
        }
        boundaries.add(true);
        return new NormalizedText(builder.toString(), List.copyOf(boundaries));
    }

    public String normalizeWord(String input) {
        return normalizeMessage(input).value();
    }

    private char normalizeChar(char c) {
        c = switch (c) {
            case 'ı' -> 'i';
            case 'ç' -> 'c';
            case 'ğ' -> 'g';
            case 'ö' -> 'o';
            case 'ş' -> 's';
            case 'ü' -> 'u';
            default -> c;
        };
        Map<String, Object> map = configManager.normalizationMap();
        Object replacement = map.get(String.valueOf(c));
        if (replacement != null && !replacement.toString().isBlank()) {
            c = replacement.toString().charAt(0);
        } else if (configManager.leetNormalization()) {
            c = switch (c) {
                case '@', '4' -> 'a';
                case '3' -> 'e';
                case '1', '!', '|', 'í', 'ì', 'î', 'ï' -> 'i';
                case '0' -> 'o';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> c;
            };
        }
        return c;
    }

    public record NormalizedText(String value, List<Boolean> boundaryBefore) {
    }
}
