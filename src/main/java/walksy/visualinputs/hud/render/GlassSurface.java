package walksy.visualinputs.hud.render;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.config.internal.AppearanceConfig;
import main.walksy.lib.core.gui.style.Theme;
import main.walksy.lib.core.gui.style.Type;
import main.walksy.lib.core.gui.widget.options.control.LiquidKnob;
import main.walksy.lib.core.render.DevicePixels;
import main.walksy.lib.core.render.WLGraphics2D;
import main.walksy.lib.core.render.font.msdf.MsdfFont;
import main.walksy.lib.core.render.glass.api.GlassStyle;
import main.walksy.lib.core.render.glass.api.LiquidGlassConstants;
import walksy.visualinputs.config.VisualInputsConfig;

final class GlassSurface extends Surface {
    private static final int GLASS_LAYER = 2;
    private static final float CORNER_RADIUS = 12f;
    private static final float CARD_TINT_ALPHA = 0.20f;
    private static final float CARD_LENS_THICKNESS = 6f;
    private static final float CARD_LENS_REACH = 0.34f;
    private static final int MIN_ROUNDED_PIXELS = 2;
    private static final int SECONDARY_COLOR = 0x80FFFFFF;
    private static final int TICK_COLOR = 0x99FFFFFF;
    private static final int LIVE_TICK_COLOR = 0xFF30D158;
    private static final int PLAYHEAD_COLOR = 0xFFFF453A;
    private static final int TRACK_COLOR = 0x26FFFFFF;

    @Override
    public void panel(final WLGraphics2D graphics, final float x, final float y, final float w, final float h, final float s) {
        graphics.glass()
            .dimensions(x, y, w, h)
            .cornerRadius(Math.min(CORNER_RADIUS * s, 0.5f * Math.min(w, h)))
            .style(this.glassStyle(s))
            .extract();
    }

    @Override
    public float width(final String text, final float size, final boolean bold) {
        final MsdfFont font = font(bold);
        return font == null ? 0f : font.width(text, size);
    }

    @Override
    public String truncate(final String text, final float size, final float room, final boolean bold) {
        final MsdfFont font = font(bold);
        return font == null ? text : font.truncate(text, size, room);
    }

    @Override
    public void text(final WLGraphics2D graphics, final String text, final float x, final float lineTop, final float lineHeight, final float size, final int color, final boolean bold) {
        final MsdfFont font = font(bold);
        if (text.isEmpty() || font == null) {
            return;
        }
        final float baseline = lineTop + lineHeight / 2f + size * CAP_HEIGHT_RATIO * 0.5f;
        font.extract(graphics, text, x, font.topForBaseline(baseline, size), size, color);
    }

    @Override
    public int secondaryColor() {
        return SECONDARY_COLOR;
    }

    @Override
    public int tickColor() {
        return TICK_COLOR;
    }

    @Override
    public int liveTickColor() {
        return LIVE_TICK_COLOR;
    }

    @Override
    public int playheadColor() {
        return PLAYHEAD_COLOR;
    }

    @Override
    public int trackColor() {
        return TRACK_COLOR;
    }

    @Override
    protected int minimumMarkerPixels() {
        return MIN_ROUNDED_PIXELS;
    }

    @Override
    protected float dividerThickness(final float s) {
        return Math.max(s, 1f / DevicePixels.perLogicalX());
    }

    @Override
    protected void shape(final WLGraphics2D graphics, final float x0, final float y0, final float x1, final float y1, final int color) {
        final float thinnest = Math.min((x1 - x0) * DevicePixels.perLogicalX(), (y1 - y0) * DevicePixels.perLogicalY());
        if (thinnest < MIN_ROUNDED_PIXELS) {
            graphics.fill(x0, y0, x1, y1, color, null);
            return;
        }
        graphics.fillRoundedGradient(x0, y0, x1, y1, color, color, color, color, 0.5f * Math.min(x1 - x0, y1 - y0));
    }

    GlassStyle glassStyle(final float scale) {
        final AppearanceConfig appearance = WalksyLib.getInstance().getAppearancePage();
        return LiquidKnob.bevelKnobStyle(GLASS_LAYER)
            .blurRadius(Math.max(0, VisualInputsConfig.glassBlur))
            .tint(appearance.toastTint(), CARD_TINT_ALPHA * appearance.toastOpacityScale())
            .refractionThickness(CARD_LENS_THICKNESS)
            .refractionReach(CARD_LENS_REACH)
            .refractionDispersion(0f)
            .toneRange(LiquidGlassConstants.OVER_WORLD_TONE_FLOOR, LiquidGlassConstants.OVER_WORLD_TONE_CEILING)
            .chroma(Theme.get().chroma())
            .scaled(scale);
    }

    private static MsdfFont font(final boolean bold) {
        return bold ? Type.CAPTION1.emphasized() : Type.CAPTION1.regular();
    }
}
