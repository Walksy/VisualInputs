package walksy.visualinputs.hud.render;

import main.walksy.lib.core.render.DevicePixels;
import main.walksy.lib.core.render.WLGraphics2D;
import walksy.visualinputs.config.VisualInputsConfig;
import walksy.visualinputs.config.WidgetStyle;

public abstract class Surface {
    protected static final float CAP_HEIGHT_RATIO = 0.59043f;
    private static final GlassSurface GLASS = new GlassSurface();
    private static final Surface MINECRAFT = new MinecraftSurface(GLASS);

    public static Surface current() {
        return VisualInputsConfig.style == WidgetStyle.MINECRAFT ? MINECRAFT : GLASS;
    }

    public abstract void panel(WLGraphics2D graphics, float x, float y, float w, float h, float s);

    public abstract float width(String text, float size, boolean bold);

    public abstract String truncate(String text, float size, float room, boolean bold);

    public abstract void text(WLGraphics2D graphics, String text, float x, float lineTop, float lineHeight, float size, int color, boolean bold);

    public abstract int secondaryColor();

    public abstract int tickColor();

    public abstract int liveTickColor();

    public abstract int playheadColor();

    public abstract int trackColor();

    protected abstract int minimumMarkerPixels();

    protected abstract float dividerThickness(float s);

    protected abstract void shape(WLGraphics2D graphics, float x0, float y0, float x1, float y1, int color);

    public final void bar(final WLGraphics2D graphics, final float x0, final float y0, final float x1, final float y1, final int color) {
        final float left = DevicePixels.snapX(x0, graphics.pose());
        final float right = Math.max(left + 1f / DevicePixels.perLogicalX(), DevicePixels.snapX(x1, graphics.pose()));
        final float top = DevicePixels.snapY(y0, graphics.pose());
        final float bottom = top + devicePixels(y1 - y0, DevicePixels.perLogicalY(), 1) / DevicePixels.perLogicalY();
        this.shape(graphics, left, top, right, bottom, color);
    }

    public final void marker(final WLGraphics2D graphics, final float centerX, final float centerY, final float width, final float height, final int color) {
        final int minimum = this.minimumMarkerPixels();
        final float w = devicePixels(width, DevicePixels.perLogicalX(), minimum) / DevicePixels.perLogicalX();
        final float h = devicePixels(height, DevicePixels.perLogicalY(), minimum) / DevicePixels.perLogicalY();
        final float left = DevicePixels.snapX(centerX - w / 2f, graphics.pose());
        final float top = DevicePixels.snapY(centerY - h / 2f, graphics.pose());
        this.shape(graphics, left, top, left + w, top + h, color);
    }

    public final void divider(final WLGraphics2D graphics, final float x0, final float x1, final float centerY, final float s) {
        final float thickness = this.dividerThickness(s);
        final float top = centerY - thickness / 2f;
        graphics.fill(x0, top, x1, top + thickness, this.trackColor(), null);
    }

    private static int devicePixels(final float logical, final float perLogical, final int minimum) {
        return Math.max(minimum, Math.round(logical * perLogical));
    }
}
