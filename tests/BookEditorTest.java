import xyz.kisde.kisdetweaks.util.BookEditor;

public class BookEditorTest {
    public static void main(String[] args) {
        BookEditor e = new BookEditor();
        e.reset("one two\nthree");
        e.move(7, false);
        e.horizontal(-1, true, true);
        assert e.selected().equals("two");
        assert e.replace("TWO", s -> s.length() < 256);
        assert e.text.equals("one TWO\nthree") && e.cursor == 7;
        e.move(0, false);
        e.vertical(1, false, String::length);
        assert e.cursor == 8;
        e.horizontal(1, true, false);
        assert e.selected().equals("t");
        assert !e.replace("too long", s -> false);
        assert e.selected().equals("t") && e.text.equals("one TWO\nthree");
        e.delete(1, false, s -> true);
        assert e.text.equals("one TWO\nhree");
        e.reset("A\uD83D\uDE00B");
        e.horizontal(-1, false, false);
        e.delete(-1, false, s -> true);
        assert e.text.equals("AB");
        e.reset("abcd\nx\nabcd");
        e.move(3, false);
        e.vertical(1, false, String::length);
        assert e.cursor == 6;
        e.vertical(1, true, String::length);
        assert e.cursor == 10;
        e.reset("");
        e.vertical(-1, true, String::length);
        assert e.cursor == 0;
        e.reset("a\n");
        assert e.lines(String::length).size() == 2;
        e.reset("abcdefghijklmnopqrstuvwxyz");
        assert e.lines(s -> s.length() * 6).size() == 2;
        System.out.println("BookEditor checks passed");
    }
}
