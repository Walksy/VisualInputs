package walksy.visualinputs.hud.render;

import main.walksy.lib.core.hud.HudEditScreen;
import main.walksy.lib.core.render.DevicePixels;
import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.joml.Matrix3x2fStack;
import walksy.visualinputs.config.MinecraftTheme;
import walksy.visualinputs.config.VisualInputsConfig;

final class MinecraftSurface extends Surface {
    private static final float VANILLA_CAP_HEIGHT = 7f;
    private static final String ELLIPSIS = "...";

    private final GlassSurface glass;

    MinecraftSurface(final GlassSurface glass) {
        this.glass = glass;
    }

    @Override
    public void panel(final WLGraphics2D graphics, final float x, final float y, final float w, final float h, final float s) {
        final MinecraftTheme theme = VisualInputsConfig.minecraft;
        this.keepTextAbovePanel(graphics, x, y, s);
        if (!theme.border) {
            graphics.fill(x, y, x + w, y + h, theme.panel.getRGB(), null);
            return;
        }
        final float edge = 1f / DevicePixels.perLogicalY();
        final int border = theme.borderColor.getRGB();
        graphics.fill(x, y, x + w, y + edge, border, null);
        graphics.fill(x, y + h - edge, x + w, y + h, border, null);
        graphics.fill(x, y + edge, x + edge, y + h - edge, border, null);
        graphics.fill(x + w - edge, y + edge, x + w, y + h - edge, border, null);
        graphics.fill(x + edge, y + edge, x + w - edge, y + h - edge, theme.panel.getRGB(), null);
    }

    @Override
    public float width(final String text, final float size, final boolean bold) {
        return Minecraft.getInstance().font.width(text) * this.scale(size);
    }

    @Override
    public String truncate(final String text, final float size, final float room, final boolean bold) {
        if (this.width(text, size, bold) <= room) {
            return text;
        }
        String cut = text;
        while (!cut.isEmpty() && this.width(cut + ELLIPSIS, size, bold) > room) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.isEmpty() ? "" : cut + ELLIPSIS;
    }

    @Override
    public void text(final WLGraphics2D graphics, final String text, final float x, final float lineTop, final float lineHeight, final float size, final int color, final boolean bold) {
        if (text.isEmpty()) {
            return;
        }
        this.drawVanilla(graphics, text, x, lineTop, lineHeight, size, color);
    }

    @Deprecated
    private void drawVanilla(final WLGraphics2D graphics, final String text, final float x, final float lineTop, final float lineHeight, final float size, final int color) {
        final Font font = Minecraft.getInstance().font;
        final float scale = this.scale(size);
        final Matrix3x2fStack pose = graphics.pose();
        final float left = DevicePixels.snapX(x, pose);
        final float top = DevicePixels.snapY(lineTop + (lineHeight - VANILLA_CAP_HEIGHT * scale) / 2f, pose);
        pose.pushMatrix();
        try {
            pose.translate(left, top);
            pose.scale(scale, scale);
            graphics.text(font, text, 0, 0, color, VisualInputsConfig.minecraft.textShadow);
        } finally {
            pose.popMatrix();
        }
    }

    @Override
    public int secondaryColor() {
        return VisualInputsConfig.minecraft.header.getRGB();
    }

    @Override
    public int tickColor() {
        return VisualInputsConfig.minecraft.text.getRGB();
    }

    @Override
    public int liveTickColor() {
        return VisualInputsConfig.minecraft.live.getRGB();
    }

    @Override
    public int playheadColor() {
        return VisualInputsConfig.minecraft.playhead.getRGB();
    }

    @Override
    public int trackColor() {
        return VisualInputsConfig.minecraft.track.getRGB();
    }

    @Override
    protected int minimumMarkerPixels() {
        return 1;
    }

    @Override
    protected float dividerThickness(final float s) {
        final float pixel = 1f / DevicePixels.perLogicalX();
        return Math.max(pixel, Math.round(s * DevicePixels.perLogicalX()) * pixel);
    }

    @Override
    protected void shape(final WLGraphics2D graphics, final float x0, final float y0, final float x1, final float y1, final int color) {
        graphics.fill(x0, y0, x1, y1, color, null);
    }

    @Deprecated
    private void keepTextAbovePanel(final WLGraphics2D graphics, final float x, final float y, final float s) {
        if (!HudEditScreen.isOpen()) {
            return;
        }
        final float pixel = 1f / DevicePixels.perLogicalX();
        graphics.glass()
            .dimensions(x, y, pixel, pixel)
            .cornerRadius(0f)
            .style(this.glass.glassStyle(s).opacity(0f))
            .extract();
    }

    private float scale(final float size) {
        final float device = DevicePixels.perLogicalY();
        final float wanted = size * CAP_HEIGHT_RATIO / VANILLA_CAP_HEIGHT;
        return Math.max(1f, Math.round(wanted * device)) / device;
    }
}
