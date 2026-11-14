package xyz.kisde.kisdetweaks.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Filtering and ranking applied to candidates actually supplied by the server or local schemas. */
public final class SuggestionMatching {
    public enum Mode { STRICT, SLIGHTLY_LOOSE, LOOSE, VERY_LOOSE }
    private SuggestionMatching() {}

    public static List<String> filter(Collection<String> values, String token, Mode mode,
                                      boolean caseSensitive, String hiddenPrefix) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (!hidden(value, hiddenPrefix) && rank(value, token, caseSensitive) <= mode.ordinal()) {
                result.add(value);
            }
        }
        result.sort(Comparator.comparingInt((String value) -> rank(value, token, caseSensitive))
                .thenComparing(caseSensitive ? Comparator.naturalOrder() : String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Comparator.naturalOrder()));
        return result;
    }

    public static int rank(String value, String token, boolean caseSensitive) {
        if (!caseSensitive) {
            value = value.toLowerCase(Locale.ROOT);
            token = token.toLowerCase(Locale.ROOT);
        }
        if (value.startsWith("\"") || value.startsWith("'")) value = value.substring(1);
        if (value.startsWith("/")) value = value.substring(1);
        if (token.startsWith("/")) token = token.substring(1);
        String path = value.substring(value.indexOf(':') + 1);
        if (value.startsWith(token) || path.startsWith(token)
                || value.contains("_" + token) || value.contains("." + token)) return 0;
        if (value.contains(token)) return 1;
        int position = 0;
        boolean words = true;
        for (String word : token.split("_", -1)) {
            int next = value.indexOf(word, position);
            if (next < 0) { words = false; break; }
            position = next + word.length();
        }
        if (words) return 2;
        position = 0;
        for (int i = 0; i < token.length(); i++) {
            int next = value.indexOf(token.charAt(i), position);
            if (next < 0) return Integer.MAX_VALUE;
            position = next + 1;
        }
        return 3;
    }

    private static boolean hidden(String value, String prefix) {
        if (prefix.isEmpty()) return false;
        for (String component : value.split("[:/]")) {
            if (component.startsWith(prefix)) return true;
        }
        return false;
    }
}
