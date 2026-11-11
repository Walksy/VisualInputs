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
import walksy.visualinputs.hud.element.FlushHud;
import walksy.visualinputs.hud.element.TimelineHud;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class VisualInputsConfig implements WalksyLibApi {
    private static final WLColor DEFAULT_LEFT_CLICK = new WLColor(255, 255, 255);
    private static final WLColor DEFAULT_RIGHT_CLICK = new WLColor(255, 255, 255);
    private static final WLColor DEFAULT_OTHER_MOUSE = new WLColor(255, 255, 255);
    private static final WLColor DEFAULT_KEYBOARD = new WLColor(255, 255, 255);
    private static final List<String> LANE_COLORS = List.of("Left Click Color", "Right Click Color", "Other Mouse Color", "Keyboard Color");
    private static final List<String> MINECRAFT_LOOK = List.of("Minecraft Panel Color", "Minecraft Show Border", "Minecraft Border Color", "Minecraft Text Color", "Minecraft Header Color", "Minecraft Track Color", "Minecraft Playhead Color", "Minecraft Live Tick Color", "Minecraft Text Shadow");
    public static final List<String> TIMELINE_LOOK = look(List.of("Tick Rows", "Show Releases"));
    public static final List<String> FLUSH_LOOK = look(List.of("Flush Rows"));

    public static boolean modEnabled = true;
    public static WidgetStyle style = WidgetStyle.GLASS;
    public static final MinecraftTheme minecraft = new MinecraftTheme();
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
    public static boolean otherMouseEnabled = true;
    public static WLColor otherMouseColor = DEFAULT_OTHER_MOUSE.copy();
    public static boolean keyboardEnabled = true;
    public static WLColor keyboardColor = DEFAULT_KEYBOARD.copy();

    private static List<String> look(final List<String> own) {
        return Stream.of(List.of("Style", "Glass Blur"), MINECRAFT_LOOK, own, LANE_COLORS).flatMap(List::stream).toList();
    }

    private static boolean minecraftStyle() {
        return style == WidgetStyle.MINECRAFT;
    }

    private static Option<Boolean> toggle(final String name, final String saveKey, final Supplier<Boolean> getter, final boolean defaultValue, final Consumer<Boolean> setter) {
        return Options.ofBoolean(name, getter, defaultValue, setter).saveKey(saveKey).build();
    }

    private static Option<WLColor> color(final String name, final String saveKey, final Supplier<WLColor> getter, final WLColor defaultValue, final Consumer<WLColor> setter, final Supplier<Boolean> available) {
        return Options.ofColor(name, getter, defaultValue, setter).saveKey(saveKey).availability(available, AvailabilityMode.HIDE).build();
    }

    private static Option<Boolean> minecraftToggle(final String name, final String saveKey, final Supplier<Boolean> getter, final boolean defaultValue, final Consumer<Boolean> setter) {
        return Options.ofBoolean(name, getter, defaultValue, setter).saveKey(saveKey).availability(VisualInputsConfig::minecraftStyle, AvailabilityMode.HIDE).build();
    }

    private static Option<WLColor> minecraftColor(final String name, final String saveKey, final Supplier<WLColor> getter, final WLColor defaultValue, final Consumer<WLColor> setter) {
        return color(name, saveKey, getter, defaultValue, setter, VisualInputsConfig::minecraftStyle);
    }

    private static Option<Integer> number(final String name, final Supplier<Integer> getter, final int defaultValue, final Consumer<Integer> setter, final int min, final int max, final Supplier<Boolean> available) {
        return Options.ofNumber(name, getter, defaultValue, setter).range(min, max, 1).saveKey(name).availability(available, AvailabilityMode.HIDE).build();
    }

    @Override
    public Config define() {
        final HudSpaces hudSpaces = WalksyLib.getInstance().getHudSpaces();
        final HudSpace timelineSpace = hudSpaces.create("Visual Inputs", new TimelineHud(VisualInputs.TIMELINE, VisualInputs.LAYOUT));
        final HudSpace flushSpace = hudSpaces.create("Flushed Inputs", HudElements.when(() -> showFlushes, new FlushHud(VisualInputs.TIMELINE, VisualInputs.LAYOUT)));
        VisualInputs.LAYOUT.bind(timelineSpace, flushSpace);
        return Config.builder()
            .path(PathUtils.ofConfigDir(VisualInputs.MOD_ID))
            .category(Category.builder("General")
                .group(this.generalGroup())
                .group(this.appearanceGroup())
                .group(this.timelineGroup(timelineSpace))
                .group(this.flushGroup(flushSpace))
                .build())
            .category(this.inputsCategory())
            .build();
    }

    private OptionGroup generalGroup() {
        return OptionGroup.builder("General")
            .option(toggle("Mod Enabled", "Mod Enabled", () -> modEnabled, true, value -> modEnabled = value))
            .option(toggle("Hold Last Inputs", "Hold Last Inputs", () -> holdLastInputs, true, value -> holdLastInputs = value))
            .option(toggle("Show Releases", "Show Releases", () -> showReleases, false, value -> showReleases = value))
            .build();
    }

    private OptionGroup appearanceGroup() {
        return OptionGroup.builder("Appearance")
            .option(Options.ofEnum("Style", () -> style, WidgetStyle.GLASS, value -> style = value, WidgetStyle.class).saveKey("Style").build())
            .option(minecraftColor("Panel Color", "Minecraft Panel Color", () -> minecraft.panel, MinecraftTheme.DEFAULT_PANEL, value -> minecraft.panel = value))
            .option(minecraftToggle("Show Border", "Minecraft Show Border", () -> minecraft.border, true, value -> minecraft.border = value))
            .option(color("Border Color", "Minecraft Border Color", () -> minecraft.borderColor, MinecraftTheme.DEFAULT_BORDER, value -> minecraft.borderColor = value, () -> minecraftStyle() && minecraft.border))
            .option(minecraftColor("Text Color", "Minecraft Text Color", () -> minecraft.text, MinecraftTheme.DEFAULT_TEXT, value -> minecraft.text = value))
            .option(minecraftColor("Header Color", "Minecraft Header Color", () -> minecraft.header, MinecraftTheme.DEFAULT_HEADER, value -> minecraft.header = value))
            .option(minecraftColor("Track Color", "Minecraft Track Color", () -> minecraft.track, MinecraftTheme.DEFAULT_TRACK, value -> minecraft.track = value))
            .option(minecraftColor("Playhead Color", "Minecraft Playhead Color", () -> minecraft.playhead, MinecraftTheme.DEFAULT_PLAYHEAD, value -> minecraft.playhead = value))
            .option(minecraftColor("Live Tick Color", "Minecraft Live Tick Color", () -> minecraft.live, MinecraftTheme.DEFAULT_LIVE, value -> minecraft.live = value))
            .option(minecraftToggle("Text Shadow", "Minecraft Text Shadow", () -> minecraft.textShadow, true, value -> minecraft.textShadow = value))
            .option(number("Glass Blur", () -> glassBlur, 2, value -> glassBlur = value, 0, 48, () -> style == WidgetStyle.GLASS))
            .build();
    }

    private OptionGroup timelineGroup(final HudSpace space) {
        return OptionGroup.builder("Timeline")
            .option(Options.ofHudSpace(space).build())
            .option(number("Tick Rows", () -> tickRows, 6, value -> tickRows = value, 2, 20, () -> true))
            .build();
    }

    private OptionGroup flushGroup(final HudSpace space) {
        return OptionGroup.builder("Flushed Inputs")
            .option(toggle("Show Flushed Inputs", "Show Flushed Inputs", () -> showFlushes, false, value -> showFlushes = value))
            .option(Options.ofHudSpace(space).availability(() -> showFlushes, AvailabilityMode.HIDE).build())
            .option(number("Flush Rows", () -> flushRows, 3, value -> flushRows = value, 1, 10, () -> showFlushes))
            .build();
    }

    private Category inputsCategory() {
        return Category.builder("Inputs")
            .group(OptionGroup.builder("Left Click")
                .option(toggle("Left Click", "Left Click Enabled", () -> leftClickEnabled, true, value -> leftClickEnabled = value))
                .option(color("Color", "Left Click Color", () -> leftClickColor, DEFAULT_LEFT_CLICK, value -> leftClickColor = value, () -> leftClickEnabled))
                .build())
            .group(OptionGroup.builder("Right Click")
                .option(toggle("Right Click", "Right Click Enabled", () -> rightClickEnabled, true, value -> rightClickEnabled = value))
                .option(color("Color", "Right Click Color", () -> rightClickColor, DEFAULT_RIGHT_CLICK, value -> rightClickColor = value, () -> rightClickEnabled))
                .build())
            .group(OptionGroup.builder("Other Mouse Buttons")
                .option(toggle("Other Mouse Buttons", "Other Mouse Enabled", () -> otherMouseEnabled, false, value -> otherMouseEnabled = value))
                .option(color("Color", "Other Mouse Color", () -> otherMouseColor, DEFAULT_OTHER_MOUSE, value -> otherMouseColor = value, () -> otherMouseEnabled))
                .build())
            .group(OptionGroup.builder("Keyboard")
                .option(toggle("Keyboard", "Keyboard Enabled", () -> keyboardEnabled, true, value -> keyboardEnabled = value))
                .option(color("Color", "Keyboard Color", () -> keyboardColor, DEFAULT_KEYBOARD, value -> keyboardColor = value, () -> keyboardEnabled))
                .build())
            .build();
    }
}
