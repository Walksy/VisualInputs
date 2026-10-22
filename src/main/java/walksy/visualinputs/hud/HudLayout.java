package walksy.visualinputs.hud;

import main.walksy.lib.core.hud.HudEditScreen;
import main.walksy.lib.core.hud.HudSpace;
import main.walksy.lib.core.platform.WLClient;
import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import walksy.visualinputs.config.VisualInputsConfig;

public final class HudLayout {
    private static final float LEFT_MARGIN = 9f;
    private static final float STACK_GAP = 4f;
    private static final float STACK_SLACK = 24f;
    private static final float TOLERANCE = 0.75f;
    private static @Nullable HudSpace timeline;
    private static @Nullable HudSpace flush;
    private static @Nullable Placement lastFlush;

    private record Placement(float anchorX, float anchorY, float offsetX, float offsetY) {
        static Placement of(final HudSpace space) {
            return new Placement(space.anchorX(), space.anchorY(), space.offsetX(), space.offsetY());
        }

        boolean matches(final HudSpace space) {
            return space.anchorX() == this.anchorX && space.anchorY() == this.anchorY && Math.abs(space.offsetX() - this.offsetX) < TOLERANCE && Math.abs(space.offsetY() - this.offsetY) < TOLERANCE;
        }
    }

    private HudLayout() {
    }

    public static boolean visible() {
        if (!VisualInputsConfig.modEnabled) {
            return false;
        }
        final Screen screen = WLClient.screen();
        return screen == null || screen instanceof ChatScreen || HudEditScreen.isOpen();
    }

    public static void bind(final HudSpace timelineSpace, final HudSpace flushSpace) {
        timeline = timelineSpace;
        flush = flushSpace;
        lastFlush = null;
    }

    public static void arrange(final WLGraphics2D graphics) {
        if (timeline == null || flush == null || HudEditScreen.isOpen()) {
            return;
        }
        if (timeline.isDefault()) {
            timeline.setScreenPosition(LEFT_MARGIN, (graphics.guiHeight() - timeline.height()) / 2f);
        }
        if (!flush.visible() || !followsTimeline(flush)) {
            return;
        }
        final float x = timeline.screenX();
        final float above = timeline.screenY() - STACK_GAP - flush.height();
        final float y = above >= 0f ? above : timeline.screenY() + timeline.height() + STACK_GAP;
        if (Math.abs(flush.screenX() - x) >= TOLERANCE || Math.abs(flush.screenY() - y) >= TOLERANCE) {
            flush.setScreenPosition(x, y);
        }
        lastFlush = Placement.of(flush);
    }

    private static boolean followsTimeline(final HudSpace space) {
        return space.isDefault() || (lastFlush != null && lastFlush.matches(space)) || stackedOnTimeline(space);
    }

    private static boolean stackedOnTimeline(final HudSpace space) {
        if (timeline == null || Math.abs(space.screenX() - timeline.screenX()) >= TOLERANCE) {
            return false;
        }
        final float gapAbove = timeline.screenY() - (space.screenY() + space.height());
        final float gapBelow = space.screenY() - (timeline.screenY() + timeline.height());
        return withinStack(gapAbove) || withinStack(gapBelow);
    }

    private static boolean withinStack(final float gap) {
        return gap >= 0f && gap <= STACK_GAP + STACK_SLACK;
    }
}
