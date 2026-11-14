package xyz.kisde.kisdetweaks.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Completion state independent of the visible popup size. */
public final class ChatCompletion {
    private final List<String> suggestions = new ArrayList<>();
    private final Map<String, List<String>> cache = new LinkedHashMap<String, List<String>>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<String>> entry) {
            return size() > 64;
        }
    };
    private SuggestionMatching.Mode mode = SuggestionMatching.Mode.STRICT;
    private boolean caseSensitive = true;
    private String hiddenPrefix = "";
    private boolean nbtEnabled;
    private boolean calculator;
    private NbtSuggestions.Result nbt;
    private String input = "";
    private int cursor = -1;
    private String requested;
    private String answered;
    private String cycleInput;
    private int cycleStart;
    private int cycleEnd;
    private int selected;
    private boolean applied;
    private boolean dirty;

    public void configure(SuggestionMatching.Mode matchingMode, boolean sensitive, String hidden, boolean nbtTags) {
        if (mode != matchingMode || caseSensitive != sensitive || !hiddenPrefix.equals(hidden) || nbtEnabled != nbtTags) {
            mode = matchingMode;
            caseSensitive = sensitive;
            hiddenPrefix = hidden;
            nbtEnabled = nbtTags;
            dirty = true;
        }
    }

    public boolean update(String text, int position, boolean calc) {
        if (!dirty && text.equals(input) && position == cursor && calculator == calc) return false;
        dirty = false;
        calculator = calc;
        input = text;
        cursor = position;
        cycleInput = null;
        applied = false;
        selected = 0;
        nbt = nbtEnabled ? NbtSuggestions.complete(text, position) : null;
        rebuild();
        return true;
    }

    private String prefix() { return input.substring(0, cursor); }
    private int tokenStart() { return prefix().lastIndexOf(' ') + 1; }
    private String token() { return prefix().substring(tokenStart()); }

    private String query() {
        if (mode == SuggestionMatching.Mode.STRICT) return prefix();
        // 1.12 filters on the server. Ask for this argument's candidates before filtering locally.
        return tokenStart() == 0 ? "/" : prefix().substring(0, tokenStart());
    }

    public String request() {
        if (cursor < 0 || !input.startsWith("/") || nbt != null || cycleInput != null
                || requested != null || query().equals(answered)) return null;
        requested = query();
        return requested;
    }

    public void receive(String[] values, boolean calc) {
        String responseTo = requested;
        requested = null;
        if (responseTo == null) return;
        Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isEmpty()) unique.add(value);
        }
        cache.put(responseTo, new ArrayList<>(unique));
        calculator = calc;
        if (cycleInput == null) {
            rebuild();
            selected = 0;
        }
    }

    private void rebuild() {
        suggestions.clear();
        answered = null;
        if (nbt != null) {
            suggestions.addAll(SuggestionMatching.filter(nbt.candidates.keySet(), nbt.token,
                    mode, caseSensitive, hiddenPrefix));
            return;
        }
        String query = query();
        List<String> cached = cache.get(query);
        if (cached != null) {
            suggestions.addAll(cached);
            answered = query;
        } else {
            // Show cached matches while requesting authoritative results for a new prefix.
            int start = tokenStart();
            for (int end = prefix().length() - 1; end >= start; end--) {
                cached = cache.get(prefix().substring(0, end));
                if (cached != null) { suggestions.addAll(cached); break; }
            }
        }
        addLocal();
        List<String> filtered = SuggestionMatching.filter(suggestions, token(), mode, caseSensitive, hiddenPrefix);
        suggestions.clear();
        suggestions.addAll(filtered);
    }

    private void addLocal() {
        if (!calculator) return;
        String prefix = prefix();
        if (prefix.startsWith("/") && prefix.indexOf(' ') < 0) {
            add("/calc");
        } else if (prefix.startsWith("/calc ") && prefix.indexOf(' ', 6) < 0) {
            for (String value : new String[] {"variables", "rates", "itemtosb", "sbtoitem",
                    "itemtostack", "stacktoitem", "nether", "overworld", "dist"}) add(value);
        }
    }

    private void add(String value) {
        if (!suggestions.contains(value)) suggestions.add(value);
    }

    public String apply(boolean backwards) {
        if (suggestions.isEmpty()) return null;
        if (cycleInput == null) {
            cycleInput = input;
            cycleStart = nbt == null ? tokenStart() : nbt.start;
            cycleEnd = nbt == null ? cursor : nbt.end;
        }
        if (applied || backwards) selected = Math.floorMod(selected + (backwards ? -1 : 1), suggestions.size());
        applied = true;
        String value = suggestions.get(selected);
        input = cycleInput.substring(0, cycleStart) + value + cycleInput.substring(cycleEnd);
        cursor = cycleStart + value.length();
        if (nbt != null && value.equals("\"\"")) cursor--;
        return input;
    }

    public void select(int index) { selected = index; applied = false; }
    public void move(int direction) {
        if (!suggestions.isEmpty()) selected = Math.floorMod(selected + direction, suggestions.size());
        applied = false;
    }

    public String label(int index) {
        String value = suggestions.get(index);
        return nbt != null && nbt.candidates.containsKey(value) ? value + "  (" + nbt.candidates.get(value) + ")" : value;
    }
    public boolean pending() { return requested != null; }
    public List<String> suggestions() { return suggestions; }
    public int selected() { return selected; }
    public int cursor() { return cursor; }
    public int firstVisible() { return selected / 8 * 8; }
}
