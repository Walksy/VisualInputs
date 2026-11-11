package walksy.visualinputs.config;

import main.walksy.lib.api.config.value.WLColor;

public final class MinecraftTheme {
    public static final WLColor DEFAULT_PANEL = new WLColor(20, 20, 20, 208);
    public static final WLColor DEFAULT_BORDER = new WLColor(42, 42, 42);
    public static final WLColor DEFAULT_TEXT = new WLColor(170, 170, 170);
    public static final WLColor DEFAULT_HEADER = new WLColor(122, 122, 122);
    public static final WLColor DEFAULT_TRACK = new WLColor(51, 51, 51);
    public static final WLColor DEFAULT_PLAYHEAD = new WLColor(255, 85, 85);
    public static final WLColor DEFAULT_LIVE = new WLColor(85, 255, 85);

    public WLColor panel = DEFAULT_PANEL.copy();
    public boolean border = true;
    public WLColor borderColor = DEFAULT_BORDER.copy();
    public WLColor text = DEFAULT_TEXT.copy();
    public WLColor header = DEFAULT_HEADER.copy();
    public WLColor track = DEFAULT_TRACK.copy();
    public WLColor playhead = DEFAULT_PLAYHEAD.copy();
    public WLColor live = DEFAULT_LIVE.copy();
    public boolean textShadow = true;
}
