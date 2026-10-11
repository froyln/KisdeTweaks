package xyz.kisde.kisdetweaks.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/** Page-local editing state; rejected edits never change the page or selection. */
public final class BookEditor {
    public static final class Line {
        public final int start;
        public final int end;
        public Line(int start, int end) { this.start = start; this.end = end; }
    }

    public String text = "";
    public int cursor;
    public int anchor;
    private int preferredX = -1;

    public void reset(String value) {
        text = value;
        cursor = anchor = value.length();
        preferredX = -1;
    }

    public int start() { return Math.min(cursor, anchor); }
    public int end() { return Math.max(cursor, anchor); }
    public String selected() { return text.substring(start(), end()); }

    public void move(int position, boolean select) {
        cursor = Math.max(0, Math.min(text.length(), position));
        if (!select) anchor = cursor;
        preferredX = -1;
    }

    public void horizontal(int direction, boolean select, boolean word) {
        int position = cursor;
        if (!select && cursor != anchor) position = direction < 0 ? start() : end();
        else if (word) {
            if (direction < 0) {
                while (position > 0 && Character.isWhitespace(text.charAt(position - 1))) position--;
                while (position > 0 && !Character.isWhitespace(text.charAt(position - 1))) position--;
            } else {
                while (position < text.length() && !Character.isWhitespace(text.charAt(position))) position++;
                while (position < text.length() && Character.isWhitespace(text.charAt(position))) position++;
            }
        } else if (direction < 0 && position > 0) position = text.offsetByCodePoints(position, -1);
        else if (direction > 0 && position < text.length()) position = text.offsetByCodePoints(position, 1);
        move(position, select);
    }

    public boolean replace(String value, Predicate<String> allowed) {
        String result = text.substring(0, start()) + value + text.substring(end());
        if (!allowed.test(result)) return false;
        int position = start() + value.length();
        text = result;
        move(position, false);
        return true;
    }

    public void delete(int direction, boolean word, Predicate<String> allowed) {
        if (cursor == anchor) horizontal(direction, true, word);
        replace("", allowed);
    }

    /** Start/end offsets preserve whitespace omitted at visual wrap boundaries. */
    public List<Line> lines(ToIntFunction<String> width) {
        List<Line> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = start;
            int space = -1;
            while (end < text.length() && text.charAt(end) != '\n'
                    && width.applyAsInt(text.substring(start, end + 1)) <= 116) {
                if (text.charAt(end) == ' ') space = end;
                end++;
            }
            if (end < text.length() && text.charAt(end) != '\n' && space > start) end = space;
            if (end == start && text.charAt(end) != '\n') end++;
            result.add(new Line(start, end));
            start = end;
            if (start < text.length() && (text.charAt(start) == ' ' || text.charAt(start) == '\n')) start++;
        }
        if (result.isEmpty() || text.endsWith("\n")) result.add(new Line(text.length(), text.length()));
        return result;
    }

    public int line(List<Line> lines) {
        int row = 0;
        while (row + 1 < lines.size() && cursor >= lines.get(row + 1).start) row++;
        return row;
    }

    public int atX(Line line, int x, ToIntFunction<String> width) {
        int position = line.start;
        while (position < line.end) {
            int next = text.offsetByCodePoints(position, 1);
            int left = width.applyAsInt(text.substring(line.start, position));
            int right = width.applyAsInt(text.substring(line.start, next));
            if (2 * x < left + right) break;
            position = next;
        }
        return position;
    }

    public void vertical(int direction, boolean select, ToIntFunction<String> width) {
        List<Line> lines = lines(width);
        int row = line(lines);
        Line current = lines.get(row);
        int x = preferredX >= 0 ? preferredX : width.applyAsInt(text.substring(current.start, Math.min(cursor, current.end)));
        int target = Math.max(0, Math.min(lines.size() - 1, row + direction));
        move(atX(lines.get(target), x, width), select);
        preferredX = x;
    }
}
