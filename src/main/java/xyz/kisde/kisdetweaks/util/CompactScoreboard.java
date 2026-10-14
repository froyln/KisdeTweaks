package xyz.kisde.kisdetweaks.util;

public final class CompactScoreboard {
    private CompactScoreboard() {}

    public static String format(int score) {
        long magnitude = Math.abs((long) score);
        long divisor;
        String suffix;
        if (magnitude >= 1_000_000_000L) {
            divisor = 1_000_000_000L;
            suffix = "B";
        } else if (magnitude >= 1_000_000L) {
            divisor = 1_000_000L;
            suffix = "M";
        } else if (magnitude >= 1_000L) {
            divisor = 1_000L;
            suffix = "K";
        } else {
            return Integer.toString(score);
        }
        long hundredths = magnitude * 100 / divisor;
        long fraction = hundredths % 100;
        String decimals = fraction == 0 ? "" : fraction % 10 == 0 ? "." + fraction / 10
                : "." + (fraction < 10 ? "0" : "") + fraction;
        return (score < 0 ? "-" : "") + hundredths / 100 + decimals + suffix;
    }
}
