package walksy.visualinputs.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.config.Config;
import main.walksy.lib.api.config.PathUtils;
import main.walksy.lib.api.config.option.AvailabilityMode;
import main.walksy.lib.api.config.option.Category;
import main.walksy.lib.api.config.option.Option;
import main.walksy.lib.api.config.option.OptionGroup;
import main.walksy.lib.api.config.option.Options;
import main.walksy.lib.api.config.value.WLColor;
import main.walksy.lib.api.hud.HudElements;
import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.hud.HudSpace;
import main.walksy.lib.core.hud.HudSpaces;
import walksy.visualinputs.VisualInputs;
import walksy.visualinputs.hud.HudLayout;
import walksy.visualinputs.hud.element.FlushHud;
import walksy.visualinputs.hud.element.TimelineHud;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VisualInputsConfig implements WalksyLibApi {
    private static final WLColor DEFAULT_LEFT_CLICK = new WLColor(255, 159, 10);
    private static final WLColor DEFAULT_RIGHT_CLICK = new WLColor(100, 180, 255);
    private static final WLColor DEFAULT_OTHER_MOUSE = new WLColor(255, 214, 10);
    private static final WLColor DEFAULT_KEYBOARD = new WLColor(191, 90, 242);

    public static boolean modEnabled = true;
    public static WidgetStyle style = WidgetStyle.GLASS;
    public static int glassBlur = 2;
    public static int tickRows = 6;
    public static boolean showFlushes = false;
    public static int flushRows = 3;
    public static boolean holdLastInputs = true;
    public static boolean showReleases = false;
    public static boolean leftClickEnabled = true;
    public static WLColor leftClickColor = DEFAULT_LEFT_CLICK.copy();
    public static boolean rightClickEnabled = true;
    public static WLColor rightClickColor = DEFAULT_RIGHT_CLICK.copy();
    public static boolean otherMouseEnabled = false;
    public static WLColor otherMouseColor = DEFAULT_OTHER_MOUSE.copy();
    public static boolean keyboardEnabled = true;
    public static WLColor keyboardColor = DEFAULT_KEYBOARD.copy();

    private static Option<Boolean> toggle(final String name, final String saveKey, final Supplier<Boolean> getter, final boolean defaultValue, final Consumer<Boolean> setter, final String description) {
        return Options.ofBoolean(name, getter, defaultValue, setter).saveKey(saveKey).description(description).build();
    }

    private static Option<WLColor> color(final String name, final String saveKey, final Supplier<WLColor> getter, final WLColor defaultValue, final Consumer<WLColor> setter, final Supplier<Boolean> available) {
        return Options.ofColor(name, getter, defaultValue, setter).saveKey(saveKey).availability(available, AvailabilityMode.HIDE).build();
    }

    @Override
    public Config define() {
        final HudSpaces hudSpaces = WalksyLib.getInstance().getHudSpaces();
        final HudSpace timelineSpace = hudSpaces.create("Visual Inputs", new TimelineHud(VisualInputs.TIMELINE));
        final HudSpace flushSpace = hudSpaces.create("Flushed Inputs", HudElements.when(() -> showFlushes, new FlushHud(VisualInputs.TIMELINE)));
        HudLayout.bind(timelineSpace, flushSpace);
        return Config.builder()
            .path(PathUtils.ofConfigDir(VisualInputs.MOD_ID))
            .category(Category.builder("General")
                .group(OptionGroup.builder("General")
                    .option(toggle("Mod Enabled", "Mod Enabled", () -> modEnabled, true, value -> modEnabled = value, null))
                    .option(toggle("Hold Last Inputs", "Hold Last Inputs", () -> holdLastInputs, true, value -> holdLastInputs = value, "Stop scrolling shortly after your last input so it stays readable"))
                    .option(toggle("Show Releases", "Show Releases", () -> showReleases, false, value -> showReleases = value, "Also mark when a button or key is let go, drawn fainter than presses"))
                    .build())
                .group(OptionGroup.builder("Appearance")
                    .option(Options.ofEnum("Style", () -> style, WidgetStyle.GLASS, value -> style = value, WidgetStyle.class)
                        .saveKey("Style")
                        .description("Glass matches WalksyLib's notifications. Minecraft uses a flat dark panel and the Minecraft font")
                        .build())
                    .option(Options.ofNumber("Glass Blur", () -> glassBlur, 2, value -> glassBlur = value)
                        .range(0, 48, 1)
                        .saveKey("Glass Blur")
                        .availability(() -> style == WidgetStyle.GLASS, AvailabilityMode.HIDE)
                        .description("How much the glass blurs what's behind it")
                        .build())
                    .build())
                .group(OptionGroup.builder("Timeline")
                    .option(Options.ofHudSpace(timelineSpace)
                        .description("Move and resize the timeline")
                        .build())
                    .option(Options.ofNumber("Tick Rows", () -> tickRows, 6, value -> tickRows = value)
                        .range(2, 20, 1)
                        .saveKey("Tick Rows")
                        .description("How many ticks are listed, one row each")
                        .build())
                    .build())
                .group(OptionGroup.builder("Flushed Inputs")
                    .option(toggle("Show Flushed Inputs", "Show Flushed Inputs", () -> showFlushes, false, value -> showFlushes = value, "A second widget above the timeline listing which clicks each tick actually handled, in the order the game handled them. Keys that are only held, like movement, aren't flushed and don't appear here"))
                    .option(Options.ofHudSpace(flushSpace)
                        .availability(() -> showFlushes, AvailabilityMode.HIDE)
                        .description("Move and resize the flushed inputs widget")
                        .build())
                    .option(Options.ofNumber("Flush Rows", () -> flushRows, 3, value -> flushRows = value)
                        .range(1, 10, 1)
                        .saveKey("Flush Rows")
                        .availability(() -> showFlushes, AvailabilityMode.HIDE)
                        .description("How many of the latest ticks that flushed something are listed")
                        .build())
                    .build())
                .group(OptionGroup.builder("Left Click")
                    .option(toggle("Left Click", "Left Click Enabled", () -> leftClickEnabled, true, value -> leftClickEnabled = value, "Show left clicks"))
                    .option(color("Color", "Left Click Color", () -> leftClickColor, DEFAULT_LEFT_CLICK, value -> leftClickColor = value, () -> leftClickEnabled))
                    .build())
                .group(OptionGroup.builder("Right Click")
                    .option(toggle("Right Click", "Right Click Enabled", () -> rightClickEnabled, true, value -> rightClickEnabled = value, "Show right clicks"))
                    .option(color("Color", "Right Click Color", () -> rightClickColor, DEFAULT_RIGHT_CLICK, value -> rightClickColor = value, () -> rightClickEnabled))
                    .build())
                .group(OptionGroup.builder("Other Mouse Buttons")
                    .option(toggle("Other Mouse Buttons", "Other Mouse Enabled", () -> otherMouseEnabled, false, value -> otherMouseEnabled = value, "Show the middle and side mouse buttons"))
                    .option(color("Color", "Other Mouse Color", () -> otherMouseColor, DEFAULT_OTHER_MOUSE, value -> otherMouseColor = value, () -> otherMouseEnabled))
                    .build())
                .group(OptionGroup.builder("Keyboard")
                    .option(toggle("Keyboard", "Keyboard Enabled", () -> keyboardEnabled, true, value -> keyboardEnabled = value, "Show key presses"))
                    .option(color("Color", "Keyboard Color", () -> keyboardColor, DEFAULT_KEYBOARD, value -> keyboardColor = value, () -> keyboardEnabled))
                    .build())
                .build())
            .build();
    }
}
