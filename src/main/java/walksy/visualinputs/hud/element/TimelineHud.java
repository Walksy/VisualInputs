package walksy.visualinputs.hud.element;

import main.walksy.lib.api.hud.HudElement;
import main.walksy.lib.core.hud.HudEditScreen;
import main.walksy.lib.core.render.WLGraphics2D;
import main.walksy.lib.core.utils.ColorUtils;
import walksy.visualinputs.InputTimeline;
import walksy.visualinputs.config.VisualInputsConfig;
import walksy.visualinputs.hud.HudLayout;
import walksy.visualinputs.hud.render.Metrics;
import walksy.visualinputs.hud.render.Surface;

import java.util.ArrayList;
import java.util.List;

public final class TimelineHud implements HudElement {
    private static final float TRACK_HEIGHT = 3f;
    private static final float MARKER_WIDTH = 1.5f;
    private static final float MARKER_HEIGHT = 7f;
    private static final float PLAYHEAD_HEIGHT = 9f;
    private static final float INPUT_SIZE = 6.5f;
    private static final float INPUT_LINE_HEIGHT = 8f;
    private static final float INPUT_SPACING = 7f;
    private static final float RELEASE_ALPHA = 0.45f;

    private final InputTimeline timeline;
    private final HudLayout layout;

    private record Placed(InputTimeline.Input input, String text, int line, float x) {
    }

    public TimelineHud(final InputTimeline timeline, final HudLayout layout) {
        this.timeline = timeline;
        this.layout = layout;
    }

    @Override
    public boolean visible() {
        return this.layout.visible();
    }

    @Override
    public List<String> hudOptions() {
        return VisualInputsConfig.TIMELINE_LOOK;
    }

    public boolean allowsOverlap() {
        return false;
    }

    @Override
    public float defaultAnchorX() {
        return 0f;
    }

    @Override
    public float defaultAnchorY() {
        return 0.5f;
    }

    @Override
    public float defaultOffsetX() {
        return HudLayout.LEFT_MARGIN;
    }

    @Override
    public float width(final float scale) {
        return new Metrics(Surface.current()).width(scale);
    }

    @Override
    public float height(final float scale) {
        return height(this.place(Surface.current(), this.view(), scale), scale);
    }

    @Override
    public void render(final WLGraphics2D graphics, final float x, final float y, final float s, final boolean editing) {
        if (editing) {
            this.layout.arrange(graphics);
        }
        final Surface surface = Surface.current();
        final Metrics metrics = new Metrics(surface);
        final InputTimeline.View view = this.view();
        final List<List<Placed>> rows = this.place(surface, view, s);
        surface.panel(graphics, x, y, metrics.width(s), height(rows, s), s);

        final float labelLeft = Metrics.labelLeft(x, s);
        final float barLeft = metrics.barLeft(x, s);
        final float barWidth = Metrics.BAR_WIDTH * s;
        final float barRight = barLeft + barWidth;
        final float headerSize = Metrics.HEADER_SIZE * s;
        final float headerTop = y + Metrics.PAD_Y * s;
        final float headerHeight = Metrics.HEADER_HEIGHT * s;
        final String end = Math.round(InputTimeline.tickMillis()) + "ms";
        surface.text(graphics, "0", barLeft, headerTop, headerHeight, headerSize, surface.secondaryColor(), false);
        surface.text(graphics, end, barRight - surface.width(end, headerSize, false), headerTop, headerHeight, headerSize, surface.secondaryColor(), false);

        final float rowHeight = Metrics.ROW_HEIGHT * s;
        final float trackHeight = TRACK_HEIGHT * s;
        final float markerWidth = MARKER_WIDTH * s;
        final float lineHeight = INPUT_LINE_HEIGHT * s;
        float top = Metrics.contentTop(y, s);
        for (int index = 0; index < rows.size(); index++) {
            final long tick = view.head() - (rows.size() - 1 - index);
            final boolean live = view.live() && tick == view.head();
            final List<Placed> row = rows.get(index);
            final float center = top + rowHeight / 2f;
            surface.text(graphics, Metrics.tickLabel(tick), labelLeft, top, rowHeight, Metrics.TICK_SIZE * s, live ? surface.liveTickColor() : surface.tickColor(), true);
            surface.bar(graphics, barLeft, center - trackHeight / 2f, barRight, center + trackHeight / 2f, surface.trackColor());
            for (final Placed placed : row) {
                surface.marker(graphics, barLeft + placed.input().position() * barWidth, center, markerWidth, MARKER_HEIGHT * s, inputColor(placed.input()));
            }
            if (live) {
                surface.marker(graphics, barLeft + view.progress() * barWidth, center, markerWidth, PLAYHEAD_HEIGHT * s, surface.playheadColor());
            }
            top += rowHeight;
            for (final Placed placed : row) {
                surface.text(graphics, placed.text(), barLeft + placed.x(), top + placed.line() * lineHeight, lineHeight, INPUT_SIZE * s, inputColor(placed.input()), true);
            }
            top += lines(row) * lineHeight + Metrics.ROW_GAP * s;
        }
    }

    private InputTimeline.View view() {
        return this.timeline.view(HudEditScreen.isOpen());
    }

    private static float height(final List<List<Placed>> rows, final float s) {
        float height = Metrics.frameHeight(s) + (rows.size() * Metrics.ROW_HEIGHT + (rows.size() - 1) * Metrics.ROW_GAP) * s;
        for (final List<Placed> row : rows) {
            height += lines(row) * INPUT_LINE_HEIGHT * s;
        }
        return height;
    }

    private List<List<Placed>> place(final Surface surface, final InputTimeline.View view, final float s) {
        final int count = Math.max(1, VisualInputsConfig.tickRows);
        final List<List<InputTimeline.Input>> byRow = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            byRow.add(new ArrayList<>());
        }
        for (final InputTimeline.Input input : view.newestFirst()) {
            final long back = view.head() - input.tick();
            if (back >= count) {
                break;
            }
            if (back >= 0 && input.shown()) {
                byRow.get(count - 1 - (int) back).addFirst(input);
            }
        }
        final float size = INPUT_SIZE * s;
        final float spacing = INPUT_SPACING * s;
        final float room = Metrics.BAR_WIDTH * s;
        final List<List<Placed>> rows = new ArrayList<>(count);
        for (final List<InputTimeline.Input> inputs : byRow) {
            final List<Placed> row = new ArrayList<>(inputs.size());
            int line = 0;
            float cursor = 0f;
            for (final InputTimeline.Input input : inputs) {
                final String text = "#" + input.sequence() + " " + input.action() + (input.press() ? "" : " RELEASED") + " " + Math.round(input.millis()) + "ms";
                final float textWidth = Math.min(surface.width(text, size, true), room);
                if (cursor > 0f && cursor + textWidth > room) {
                    line++;
                    cursor = 0f;
                }
                row.add(new Placed(input, surface.truncate(text, size, room, true), line, cursor));
                cursor += textWidth + spacing;
            }
            rows.add(row);
        }
        return rows;
    }

    private static int lines(final List<Placed> row) {
        return row.isEmpty() ? 0 : row.getLast().line() + 1;
    }

    private static int inputColor(final InputTimeline.Input input) {
        return ColorUtils.withAlpha(input.lane().color(), input.press() ? 1f : RELEASE_ALPHA);
    }
}
