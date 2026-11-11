package walksy.visualinputs.hud;

import main.walksy.lib.core.hud.HudEditScreen;
import main.walksy.lib.core.hud.HudSpace;
import main.walksy.lib.core.platform.WLClient;
import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import walksy.visualinputs.config.VisualInputsConfig;

import java.util.Comparator;
import java.util.List;

public final class HudLayout {
    public static final float LEFT_MARGIN = 9f;
    private static final float STACK_GAP = 4f;
    private static final float STACK_SLACK = 24f;
    private static final float TOLERANCE = 0.75f;

    private @Nullable HudSpace timeline;
    private @Nullable HudSpace flush;
    private @Nullable Placement lastFlush;
    private @Nullable Rect previousTimeline;
    private @Nullable Rect previousFlush;
    private @Nullable Safe safeTimeline;
    private @Nullable Safe safeFlush;

    private record Placement(float anchorX, float anchorY, float offsetX, float offsetY) {
        static Placement of(final HudSpace space) {
            return new Placement(space.anchorX(), space.anchorY(), space.offsetX(), space.offsetY());
        }

        boolean matches(final HudSpace space) {
            return space.anchorX() == this.anchorX && space.anchorY() == this.anchorY && Math.abs(space.offsetX() - this.offsetX) < TOLERANCE && Math.abs(space.offsetY() - this.offsetY) < TOLERANCE;
        }
    }

    private record Safe(float x, float y, float scale) {
        static Safe of(final HudSpace space) {
            return new Safe(space.screenX(), space.screenY(), space.scale());
        }

        void restore(final HudSpace space) {
            space.resize(this.scale);
            space.setScreenPosition(this.x, this.y);
        }
    }

    private record Rect(float x, float y, float w, float h) {
        static Rect of(final HudSpace space) {
            return new Rect(space.screenX(), space.screenY(), space.width(), space.height());
        }

        float right() {
            return this.x + this.w;
        }

        float bottom() {
            return this.y + this.h;
        }

        Rect at(final float x, final float y) {
            return new Rect(x, y, this.w, this.h);
        }

        Rect clampedTo(final float screenWidth, final float screenHeight) {
            return this.at(Math.max(0f, Math.min(screenWidth - this.w, this.x)), Math.max(0f, Math.min(screenHeight - this.h, this.y)));
        }

        boolean sameAs(final Rect other) {
            return Math.abs(this.x - other.x) < TOLERANCE && Math.abs(this.y - other.y) < TOLERANCE && Math.abs(this.w - other.w) < TOLERANCE && Math.abs(this.h - other.h) < TOLERANCE;
        }

        boolean overlaps(final Rect other) {
            return this.x < other.right() - TOLERANCE && other.x < this.right() - TOLERANCE && this.y < other.bottom() - TOLERANCE && other.y < this.bottom() - TOLERANCE;
        }

        float overlapArea(final Rect other) {
            final float width = Math.min(this.right(), other.right()) - Math.max(this.x, other.x);
            final float height = Math.min(this.bottom(), other.bottom()) - Math.max(this.y, other.y);
            return width > 0f && height > 0f ? width * height : 0f;
        }

        float distanceTo(final Rect other) {
            return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
        }
    }

    public boolean visible() {
        if (!VisualInputsConfig.modEnabled) {
            return false;
        }
        final Screen screen = WLClient.screen();
        return screen == null || screen instanceof ChatScreen || HudEditScreen.isOpen();
    }

    public void bind(final HudSpace timelineSpace, final HudSpace flushSpace) {
        this.timeline = timelineSpace;
        this.flush = flushSpace;
        this.lastFlush = null;
        this.previousTimeline = null;
        this.previousFlush = null;
        this.safeTimeline = null;
        this.safeFlush = null;
    }

    public void arrange(final WLGraphics2D graphics) {
        if (this.timeline == null || this.flush == null || !this.timeline.visible()) {
            return;
        }
        final boolean following = this.flush.isDefault() || (!HudEditScreen.isOpen() && this.followsTimeline());
        if (following) {
            this.stackOnTimeline();
        }
        this.separate(graphics.guiWidth(), graphics.guiHeight());
        if (following) {
            this.lastFlush = Placement.of(this.flush);
        }
        this.remember();
    }

    private void remember() {
        final Rect fixed = Rect.of(this.timeline);
        final Rect moving = Rect.of(this.flush);
        this.previousTimeline = fixed;
        this.previousFlush = moving;
        if (!fixed.overlaps(moving)) {
            this.safeTimeline = Safe.of(this.timeline);
            this.safeFlush = Safe.of(this.flush);
        }
    }

    @Deprecated
    private boolean revertTimeline(final Rect fixed, final Rect moving) {
        final boolean timelineMoved = this.previousTimeline != null && !this.previousTimeline.sameAs(fixed);
        final boolean flushMoved = this.previousFlush != null && !this.previousFlush.sameAs(moving);
        if (!timelineMoved || flushMoved || this.safeTimeline == null) {
            return false;
        }
        this.safeTimeline.restore(this.timeline);
        return true;
    }

    private void stackOnTimeline() {
        final float x = this.timeline.screenX();
        final float above = this.timeline.screenY() - STACK_GAP - this.flush.height();
        final float y = above >= 0f ? above : this.timeline.screenY() + this.timeline.height() + STACK_GAP;
        if (Math.abs(this.flush.screenX() - x) >= TOLERANCE || Math.abs(this.flush.screenY() - y) >= TOLERANCE) {
            this.flush.setScreenPosition(x, y);
        }
    }

    @Deprecated
    private void separate(final float screenWidth, final float screenHeight) {
        final Rect fixed = Rect.of(this.timeline);
        final Rect moving = Rect.of(this.flush);
        if (!moving.overlaps(fixed)) {
            return;
        }
        if (HudEditScreen.isOpen() && this.revertTimeline(fixed, moving)) {
            return;
        }
        final List<Rect> candidates = List.of(
            moving.at(moving.x(), fixed.y() - STACK_GAP - moving.h()),
            moving.at(moving.x(), fixed.bottom() + STACK_GAP),
            moving.at(fixed.x() - STACK_GAP - moving.w(), moving.y()),
            moving.at(fixed.right() + STACK_GAP, moving.y())
        );
        final Rect best = candidates.stream()
            .map(candidate -> candidate.clampedTo(screenWidth, screenHeight))
            .min(Comparator.<Rect>comparingLong(candidate -> Math.round(candidate.overlapArea(fixed))).thenComparingDouble(candidate -> candidate.distanceTo(moving)))
            .orElse(moving);
        this.flush.setScreenPosition(best.x(), best.y());
    }

    private boolean followsTimeline() {
        return (this.lastFlush != null && this.lastFlush.matches(this.flush)) || this.stackedOnTimeline();
    }

    private boolean stackedOnTimeline() {
        if (Math.abs(this.flush.screenX() - this.timeline.screenX()) >= TOLERANCE) {
            return false;
        }
        final float gapAbove = this.timeline.screenY() - (this.flush.screenY() + this.flush.height());
        final float gapBelow = this.flush.screenY() - (this.timeline.screenY() + this.timeline.height());
        return withinStack(gapAbove) || withinStack(gapBelow);
    }

    private static boolean withinStack(final float gap) {
        return gap >= 0f && gap <= STACK_GAP + STACK_SLACK;
    }
}
