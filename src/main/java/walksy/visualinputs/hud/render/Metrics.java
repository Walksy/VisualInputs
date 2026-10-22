package walksy.visualinputs.hud.render;

public final class Metrics {
    public static final float PAD_X = 9f;
    public static final float PAD_Y = 6f;
    public static final float HEADER_SIZE = 6f;
    public static final float HEADER_HEIGHT = 8f;
    public static final float HEADER_GAP = 1f;
    public static final float TICK_SIZE = 7f;
    public static final float ROW_HEIGHT = 9f;
    public static final float ROW_GAP = 2f;
    public static final float LABEL_GAP = 6f;
    public static final float BAR_WIDTH = 150f;
    private static final float FALLBACK_LABEL_WIDTH = 24f;
    private static final long TICK_WRAP = 1000L;
    private static final String[] WIDEST_LABELS = {"T000", "T888", "T999"};

    private Metrics() {
    }

    public static String tickLabel(final long tick) {
        return "T" + Math.floorMod(tick, TICK_WRAP);
    }

    public static float width(final float s) {
        return (PAD_X * 2f + LABEL_GAP + BAR_WIDTH) * s + labelWidth(s);
    }

    public static float frameHeight(final float s) {
        return (PAD_Y * 2f + HEADER_HEIGHT + HEADER_GAP) * s;
    }

    public static float labelLeft(final float x, final float s) {
        return x + PAD_X * s;
    }

    public static float barLeft(final float x, final float s) {
        return labelLeft(x, s) + labelWidth(s) + LABEL_GAP * s;
    }

    public static float contentTop(final float y, final float s) {
        return y + (PAD_Y + HEADER_HEIGHT + HEADER_GAP) * s;
    }

    private static float labelWidth(final float s) {
        float width = 0f;
        for (final String sample : WIDEST_LABELS) {
            width = Math.max(width, Surface.width(sample, TICK_SIZE * s, true));
        }
        return width > 0f ? width : FALLBACK_LABEL_WIDTH * s;
    }
}
