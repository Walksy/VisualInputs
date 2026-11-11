package walksy.visualinputs.hud.element;

import main.walksy.lib.api.hud.HudElement;
import main.walksy.lib.core.hud.HudEditScreen;
import main.walksy.lib.core.render.WLGraphics2D;
import walksy.visualinputs.InputTimeline;
import walksy.visualinputs.config.VisualInputsConfig;
import walksy.visualinputs.hud.HudLayout;
import walksy.visualinputs.hud.render.Metrics;
import walksy.visualinputs.hud.render.Surface;

import java.util.ArrayList;
import java.util.List;

public final class FlushHud implements HudElement {
    private static final float ENTRY_SIZE = 6.5f;
    private static final float LINE_GAP = 1f;
    private static final float ROW_GAP = 5f;
    private static final float SEPARATOR_SPACING = 3f;
    private static final String TITLE = "Flushed";
    private static final String SEPARATOR = ">";
    private static final String EMPTY = "Nothing flushed yet";

    private final InputTimeline timeline;
    private final HudLayout layout;

    private record Entry(InputTimeline.Flush flush, String label, int line, float x, boolean separated) {
    }

    private record Row(long tick, List<Entry> entries, int lines) {
        float height() {
            return this.lines * Metrics.ROW_HEIGHT + (this.lines - 1) * LINE_GAP;
        }
    }

    public FlushHud(final InputTimeline timeline, final HudLayout layout) {
        this.timeline = timeline;
        this.layout = layout;
    }

    @Override
    public boolean visible() {
        return this.layout.visible();
    }

    @Override
    public List<String> hudOptions() {
        return VisualInputsConfig.FLUSH_LOOK;
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
        return 0f;
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
        return height(this.layout(Surface.current(), scale), scale);
    }

    @Override
    public void render(final WLGraphics2D graphics, final float x, final float y, final float s, final boolean editing) {
        final Surface surface = Surface.current();
        final Metrics metrics = new Metrics(surface);
        final List<Row> rows = this.layout(surface, s);
        final float w = metrics.width(s);
        surface.panel(graphics, x, y, w, height(rows, s), s);

        final float labelLeft = Metrics.labelLeft(x, s);
        final float barLeft = metrics.barLeft(x, s);
        surface.text(graphics, TITLE, labelLeft, y + Metrics.PAD_Y * s, Metrics.HEADER_HEIGHT * s, Metrics.HEADER_SIZE * s, surface.secondaryColor(), false);

        final float lineHeight = Metrics.ROW_HEIGHT * s;
        final float linePitch = (Metrics.ROW_HEIGHT + LINE_GAP) * s;
        final float size = ENTRY_SIZE * s;
        float top = Metrics.contentTop(y, s);
        if (rows.isEmpty()) {
            surface.text(graphics, EMPTY, barLeft, top, lineHeight, size, surface.secondaryColor(), false);
            return;
        }
        final float spacing = SEPARATOR_SPACING * s;
        final float separatorWidth = surface.width(SEPARATOR, size, false);
        final float right = x + w - Metrics.PAD_X * s;
        for (int index = 0; index < rows.size(); index++) {
            final Row row = rows.get(index);
            if (index > 0) {
                surface.divider(graphics, labelLeft, right, top - ROW_GAP * s / 2f, s);
            }
            surface.text(graphics, Metrics.tickLabel(row.tick()), labelLeft, top, lineHeight, Metrics.TICK_SIZE * s, surface.tickColor(), true);
            for (final Entry entry : row.entries()) {
                final float lineTop = top + entry.line() * linePitch;
                float cursor = barLeft + entry.x();
                if (entry.separated()) {
                    surface.text(graphics, SEPARATOR, cursor, lineTop, lineHeight, size, surface.secondaryColor(), false);
                    cursor += separatorWidth + spacing;
                }
                surface.text(graphics, entry.label(), cursor, lineTop, lineHeight, size, entry.flush().lane().color(), true);
            }
            top += (row.height() + ROW_GAP) * s;
        }
    }

    private static float height(final List<Row> rows, final float s) {
        if (rows.isEmpty()) {
            return Metrics.frameHeight(s) + Metrics.ROW_HEIGHT * s;
        }
        float content = (rows.size() - 1) * ROW_GAP;
        for (final Row row : rows) {
            content += row.height();
        }
        return Metrics.frameHeight(s) + content * s;
    }

    private List<Row> layout(final Surface surface, final float s) {
        final int count = Math.max(1, VisualInputsConfig.flushRows);
        final List<Long> ticks = new ArrayList<>(count);
        final List<List<InputTimeline.Flush>> grouped = new ArrayList<>(count);
        for (final InputTimeline.Flush flush : this.timeline.flushesNewestFirst(HudEditScreen.isOpen())) {
            if (!flush.lane().shown()) {
                continue;
            }
            if (ticks.isEmpty() || ticks.getFirst() != flush.tick()) {
                if (ticks.size() >= count) {
                    break;
                }
                ticks.addFirst(flush.tick());
                grouped.addFirst(new ArrayList<>());
            }
            grouped.getFirst().addFirst(flush);
        }
        final float size = ENTRY_SIZE * s;
        final float room = Metrics.BAR_WIDTH * s;
        final float spacing = SEPARATOR_SPACING * s;
        final float separatorWidth = spacing + surface.width(SEPARATOR, size, false) + spacing;
        final List<Row> rows = new ArrayList<>(ticks.size());
        for (int index = 0; index < ticks.size(); index++) {
            final List<Entry> entries = new ArrayList<>();
            int line = 0;
            float cursor = 0f;
            for (final InputTimeline.Flush flush : grouped.get(index)) {
                final String label = surface.truncate(flush.label(), size, room, true);
                final float entryWidth = surface.width(label, size, true);
                boolean separated = cursor > 0f;
                if (separated && cursor + separatorWidth + entryWidth > room) {
                    line++;
                    cursor = 0f;
                    separated = false;
                }
                final float x = separated ? cursor + spacing : cursor;
                entries.add(new Entry(flush, label, line, x, separated));
                cursor = x + (separated ? separatorWidth - spacing : 0f) + entryWidth;
            }
            rows.add(new Row(ticks.get(index), entries, line + 1));
        }
        return rows;
    }
}
