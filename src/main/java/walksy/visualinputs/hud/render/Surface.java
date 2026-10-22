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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.joml.Matrix3x2fStack;
import walksy.visualinputs.config.VisualInputsConfig;
import walksy.visualinputs.config.WidgetStyle;

public final class Surface {
    private static final int GLASS_LAYER = 2;
    private static final float CORNER_RADIUS = 12f;
    private static final float CARD_TINT_ALPHA = 0.20f;
    private static final float CARD_LENS_THICKNESS = 6f;
    private static final float CARD_LENS_REACH = 0.34f;
    private static final float CAP_HEIGHT_RATIO = 0.59043f;
    private static final float VANILLA_CAP_HEIGHT = 7f;
    private static final String ELLIPSIS = "...";
    private static final int MIN_ROUNDED_PIXELS = 2;
    private static final int PANEL_COLOR = 0xD0141414;
    private static final int PANEL_EDGE_COLOR = 0xFF2A2A2A;

    private Surface() {
    }

    public static void panel(final WLGraphics2D graphics, final float x, final float y, final float w, final float h, final float s) {
        if (minecraft()) {
            final float edge = 1f / DevicePixels.perLogicalY();
            graphics.fill(x, y, x + w, y + h, PANEL_EDGE_COLOR, null);
            graphics.fill(x + edge, y + edge, x + w - edge, y + h - edge, PANEL_COLOR, null);
            return;
        }
        graphics.glass()
            .dimensions(x, y, w, h)
            .cornerRadius(Math.min(CORNER_RADIUS * s, 0.5f * Math.min(w, h)))
            .style(glassStyle(s))
            .extract();
    }

    public static void bar(final WLGraphics2D graphics, final float x0, final float y0, final float x1, final float y1, final int color) {
        final float left = DevicePixels.snapX(x0, graphics.pose());
        final float right = Math.max(left + 1f / DevicePixels.perLogicalX(), DevicePixels.snapX(x1, graphics.pose()));
        final float top = DevicePixels.snapY(y0, graphics.pose());
        final float bottom = top + devicePixels(y1 - y0, DevicePixels.perLogicalY(), 1) / DevicePixels.perLogicalY();
        rounded(graphics, left, top, right, bottom, color);
    }

    public static void marker(final WLGraphics2D graphics, final float centerX, final float centerY, final float width, final float height, final int color) {
        final int minimum = minecraft() ? 1 : MIN_ROUNDED_PIXELS;
        final float w = devicePixels(width, DevicePixels.perLogicalX(), minimum) / DevicePixels.perLogicalX();
        final float h = devicePixels(height, DevicePixels.perLogicalY(), minimum) / DevicePixels.perLogicalY();
        final float left = DevicePixels.snapX(centerX - w / 2f, graphics.pose());
        final float top = DevicePixels.snapY(centerY - h / 2f, graphics.pose());
        rounded(graphics, left, top, left + w, top + h, color);
    }

    public static void divider(final WLGraphics2D graphics, final float x0, final float x1, final float centerY, final float s) {
        final float pixel = 1f / DevicePixels.perLogicalX();
        final float thickness = minecraft() ? Math.max(pixel, Math.round(s * DevicePixels.perLogicalX()) * pixel) : Math.max(s, pixel);
        final float top = centerY - thickness / 2f;
        graphics.fill(x0, top, x1, top + thickness, trackColor(), null);
    }

    public static float width(final String text, final float size, final boolean bold) {
        if (minecraft()) {
            return Minecraft.getInstance().font.width(text) * vanillaScale(size);
        }
        final MsdfFont font = msdf(bold);
        return font == null ? 0f : font.width(text, size);
    }

    public static String truncate(final String text, final float size, final float room, final boolean bold) {
        if (!minecraft()) {
            final MsdfFont font = msdf(bold);
            return font == null ? text : font.truncate(text, size, room);
        }
        if (width(text, size, bold) <= room) {
            return text;
        }
        String cut = text;
        while (!cut.isEmpty() && width(cut + ELLIPSIS, size, bold) > room) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.isEmpty() ? "" : cut + ELLIPSIS;
    }

    public static void text(final WLGraphics2D graphics, final String text, final float x, final float lineTop, final float lineHeight, final float size, final int color, final boolean bold) {
        if (text.isEmpty()) {
            return;
        }
        if (minecraft()) {
            final Font font = Minecraft.getInstance().font;
            final float scale = vanillaScale(size);
            final Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            try {
                pose.translate(x, lineTop + (lineHeight - VANILLA_CAP_HEIGHT * scale) / 2f);
                pose.scale(scale, scale);
                graphics.text(font, text, 0, 0, color, true);
            } finally {
                pose.popMatrix();
            }
            return;
        }
        final MsdfFont font = msdf(bold);
        if (font == null) {
            return;
        }
        final float baseline = lineTop + lineHeight / 2f + size * CAP_HEIGHT_RATIO * 0.5f;
        font.extract(graphics, text, x, font.topForBaseline(baseline, size), size, color);
    }

    public static int secondaryColor() {
        return minecraft() ? 0xFF7A7A7A : 0x80FFFFFF;
    }

    public static int tickColor() {
        return minecraft() ? 0xFFAAAAAA : 0x99FFFFFF;
    }

    public static int liveTickColor() {
        return minecraft() ? 0xFF55FF55 : 0xFF30D158;
    }

    public static int playheadColor() {
        return minecraft() ? 0xFFFF5555 : 0xFFFF453A;
    }

    public static int trackColor() {
        return minecraft() ? 0xFF333333 : 0x26FFFFFF;
    }

    private static boolean minecraft() {
        return VisualInputsConfig.style == WidgetStyle.MINECRAFT;
    }

    private static void rounded(final WLGraphics2D graphics, final float x0, final float y0, final float x1, final float y1, final int color) {
        final float thinnest = Math.min((x1 - x0) * DevicePixels.perLogicalX(), (y1 - y0) * DevicePixels.perLogicalY());
        if (minecraft() || thinnest < MIN_ROUNDED_PIXELS) {
            graphics.fill(x0, y0, x1, y1, color, null);
            return;
        }
        graphics.fillRoundedGradient(x0, y0, x1, y1, color, color, color, color, 0.5f * Math.min(x1 - x0, y1 - y0));
    }

    private static int devicePixels(final float logical, final float perLogical, final int minimum) {
        return Math.max(minimum, Math.round(logical * perLogical));
    }

    private static float vanillaScale(final float size) {
        final float device = DevicePixels.perLogicalY();
        final float wanted = size * CAP_HEIGHT_RATIO / VANILLA_CAP_HEIGHT;
        return Math.max(1f, Math.round(wanted * device)) / device;
    }

    private static MsdfFont msdf(final boolean bold) {
        return bold ? Type.CAPTION1.emphasized() : Type.CAPTION1.regular();
    }

    private static GlassStyle glassStyle(final float scale) {
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
}
