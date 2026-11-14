package xyz.kisde.kisdetweaks.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Tolerant SNBT completion parser for the pre-Brigadier 1.12 command grammar. */
public final class NbtSuggestions {
    public static final class Tag {
        public final String type;
        final Map<String, Tag> children = new LinkedHashMap<>();
        Tag element;
        Tag(String type) { this.type = type; }
        Tag fields(String type, String... names) {
            for (String name : names) children.put(name, new Tag(type));
            return this;
        }
        Tag put(String name, Tag tag) { children.put(name, tag); return this; }
    }

    public static final class Result {
        public final int start;
        public final int end;
        public final String token;
        public final Map<String, String> candidates = new LinkedHashMap<>();
        Result(int start, int end, String token) {
            this.start = start;
            this.end = end;
            this.token = token;
        }
    }

    private final String text;
    private final int limit;
    private int pos;
    private Result result;

    private NbtSuggestions(String text, int cursor, int start) {
        this.text = text;
        this.limit = cursor;
        this.pos = start;
    }

    public static Result complete(String text, int cursor) {
        int start = text.indexOf('{');
        if (start < 0 || start >= cursor) return null;
        String[] args = text.substring(0, start).trim().split("\\s+");
        Tag schema = commandSchema(args, 0);
        if (schema == null) return null;
        NbtSuggestions parser = new NbtSuggestions(text, cursor, start);
        parser.value(schema);
        // A recognized NBT context with no key suggestions still owns completion.
        return parser.result != null ? parser.result : new Result(cursor, cursor, "");
    }

    private static Tag commandSchema(String[] args, int offset) {
        if (offset >= args.length) return null;
        String cmd = args[offset].replaceFirst("^/", "").replaceFirst("^minecraft:", "");
        int n = args.length - offset;
        if (cmd.equals("execute") && n >= 6) {
            int nested = offset + (args[offset + 5].equals("detect") ? 11 : 5);
            return commandSchema(args, nested);
        }
        switch (cmd) {
            case "give": case "clear": return n == 5 ? NbtSchemas.item(args[offset + 2]) : null;
            case "replaceitem":
                if (n == 7 && args[offset + 1].equals("entity")) return NbtSchemas.item(args[offset + 4]);
                if (n == 9 && args[offset + 1].equals("block")) return NbtSchemas.item(args[offset + 6]);
                return null;
            case "summon": return n == 5 ? NbtSchemas.entity(args[offset + 1]) : null;
            case "entitydata": case "testfor": return n == 2 ? NbtSchemas.entity(selectorType(args[offset + 1])) : null;
            case "blockdata": return n == 4 ? NbtSchemas.block("") : null;
            case "setblock": return n == 7 ? NbtSchemas.block(args[offset + 4]) : null;
            case "fill": return n == 10 ? NbtSchemas.block(args[offset + 7]) : null;
            case "testforblock": return n == 6 ? NbtSchemas.block(args[offset + 4]) : null;
            default: return null;
        }
    }

    private static String selectorType(String selector) {
        int start = selector.indexOf("type=");
        if (start < 0) return "";
        int end = start + 5;
        while (end < selector.length() && selector.charAt(end) != ',' && selector.charAt(end) != ']') end++;
        String type = selector.substring(start + 5, end);
        return type.startsWith("!") ? "" : type;
    }

    private void whitespace() {
        while (pos < limit && Character.isWhitespace(text.charAt(pos))) pos++;
    }

    private boolean value(Tag tag) {
        whitespace();
        if (pos >= limit) {
            result = new Result(pos, pos, "");
            int next = pos;
            while (next < text.length() && Character.isWhitespace(text.charAt(next))) next++;
            boolean empty = next == text.length() || text.charAt(next) == '}' || text.charAt(next) == ']'
                    || text.charAt(next) == ',';
            if (tag != null && empty) {
                String[] values;
                switch (tag.type) {
                    case "compound": values = new String[] {"{"}; break;
                    case "list": values = new String[] {"["}; break;
                    case "byte": values = new String[] {"0b", "1b"}; break;
                    case "short": values = new String[] {"0s"}; break;
                    case "long": values = new String[] {"0L"}; break;
                    case "float": values = new String[] {"0.0f"}; break;
                    case "double": values = new String[] {"0.0d"}; break;
                    case "string": values = new String[] {"\"\""}; break;
                    case "int array": values = new String[] {"[I;"}; break;
                    case "byte array": values = new String[] {"[B;"}; break;
                    case "long array": values = new String[] {"[L;"}; break;
                    default: values = new String[] {"0"};
                }
                for (String v : values) result.candidates.put(v, tag.type);
            }
            return false;
        }
        char c = text.charAt(pos);
        if (c == '{') return compound(tag);
        if (c == '[') return parseList(tag);
        if (c == '"') return quoted() != null;
        int start = pos;
        while (pos < limit && text.charAt(pos) != ',' && text.charAt(pos) != '}'
                && text.charAt(pos) != ']' && !Character.isWhitespace(text.charAt(pos))) pos++;
        return pos > start && pos < limit;
    }

    private boolean compound(Tag schema) {
        pos++;
        Set<String> used = new LinkedHashSet<>();
        while (true) {
            whitespace();
            int start = pos;
            if (pos >= limit) { keys(schema, used, start, "", '\0'); return false; }
            if (text.charAt(pos) == '}') { pos++; return true; }
            char quote = text.charAt(pos) == '"' ? '"' : '\0';
            String key;
            if (quote != '\0') {
                key = quoted();
                if (key == null) { keys(schema, used, start, unescape(text.substring(start + 1, limit)), quote); return false; }
            } else {
                while (pos < limit && keyChar(text.charAt(pos))) pos++;
                key = text.substring(start, pos);
            }
            whitespace();
            if (pos >= limit) { keys(schema, used, start, key, quote); return false; }
            if (key.isEmpty() || text.charAt(pos++) != ':') return false;
            used.add(key);
            Tag child = schema == null ? null : schema.children.get(key);
            if (!value(child)) return false;
            whitespace();
            if (pos >= limit) return false;
            char next = text.charAt(pos++);
            if (next == '}') return true;
            if (next != ',') return false;
        }
    }

    private boolean parseList(Tag schema) {
        pos++;
        // Typed arrays contain numbers, never compound keys.
        if (pos + 1 < limit && text.charAt(pos + 1) == ';') {
            pos += 2;
            schema = null;
        }
        while (true) {
            whitespace();
            if (pos < limit && text.charAt(pos) == ']') { pos++; return true; }
            if (!value(schema == null ? null : schema.element)) return false;
            whitespace();
            if (pos >= limit) return false;
            char next = text.charAt(pos++);
            if (next == ']') return true;
            if (next != ',') return false;
        }
    }

    private String quoted() {
        char quote = text.charAt(pos++);
        StringBuilder value = new StringBuilder();
        while (pos < limit) {
            char c = text.charAt(pos++);
            if (c == quote) return value.toString();
            if (c == '\\') {
                if (pos >= limit) return null;
                c = text.charAt(pos++);
            }
            value.append(c);
        }
        return null;
    }

    private void keys(Tag schema, Set<String> used, int start, String token, char quote) {
        int end = limit;
        if (quote != '\0') {
            // Only consume the rest of this key, preserving the following value and braces.
            while (end < text.length() && text.charAt(end) != quote
                    && text.charAt(end) != ':' && text.charAt(end) != '}' && text.charAt(end) != ',') end++;
            if (end < text.length() && text.charAt(end) == quote) end++;
        } else {
            while (end < text.length() && keyChar(text.charAt(end))) end++;
        }
        int colon = end;
        while (colon < text.length() && Character.isWhitespace(text.charAt(colon))) colon++;
        if (colon < text.length() && text.charAt(colon) == ':') end = colon + 1;
        result = new Result(start, end, token);
        if (schema == null) return;
        for (Map.Entry<String, Tag> entry : schema.children.entrySet()) {
            if (used.contains(entry.getKey())) continue;
            String key = entry.getKey();
            if (quote != '\0') key = quote + key + quote;
            result.candidates.put(key + ":", entry.getValue().type);
        }
    }

    private static boolean keyChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == '+';
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\'", "'").replace("\\\\", "\\");
    }

    static Tag compound() { return new Tag("compound"); }
    static Tag list(Tag entry) { Tag tag = new Tag("list"); tag.element = entry; return tag; }
}
