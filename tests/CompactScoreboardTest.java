import xyz.kisde.kisdetweaks.util.CompactScoreboard;

public class CompactScoreboardTest {
    public static void main(String[] args) {
        int[] scores = {0, 999, 1000, 1250, 1000000, 1500050, 1555555,
                1000000000, -1250, Integer.MIN_VALUE, Integer.MAX_VALUE, 1001, 1010, 999999};
        String[] expected = {"0", "999", "1K", "1.25K", "1M", "1.5M", "1.55M",
                "1B", "-1.25K", "-2.14B", "2.14B", "1K", "1.01K", "999.99K"};
        for (int i = 0; i < scores.length; i++) {
            String actual = CompactScoreboard.format(scores[i]);
            if (!actual.equals(expected[i])) throw new AssertionError(scores[i] + ": " + actual);
        }
        System.out.println("CompactScoreboard: 14 cases passed");
    }
}
