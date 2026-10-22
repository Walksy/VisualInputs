package walksy.visualinputs;

import com.mojang.blaze3d.platform.InputConstants;
import main.walksy.lib.core.platform.WLClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import walksy.visualinputs.config.VisualInputsConfig;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class InputTimeline {
    private static final int CAPACITY = 1024;
    private static final int FLUSH_CAPACITY = 256;
    private static final int PENDING_CAPACITY = 32;
    private static final long STALE_TICKS = 100L;
    private static final int HOLD_TICKS = 2;
    private static final float DEFAULT_TICK_MILLIS = 50f;
    private static final float LAST_POSITION = 0.999f;
    private static final long NONE = Long.MIN_VALUE;
    private static final long PREVIEW_HEAD = 969L;
    private static final Map<String, String> ACTIONS = Map.ofEntries(
        Map.entry("key.attack", "ATTACK"),
        Map.entry("key.use", "USE"),
        Map.entry("key.forward", "FORWARD"),
        Map.entry("key.back", "BACK"),
        Map.entry("key.left", "LEFT"),
        Map.entry("key.right", "RIGHT"),
        Map.entry("key.jump", "JUMP"),
        Map.entry("key.sneak", "SNEAK"),
        Map.entry("key.sprint", "SPRINT"),
        Map.entry("key.drop", "DROP"),
        Map.entry("key.inventory", "INVENTORY"),
        Map.entry("key.swapOffhand", "SWAP"),
        Map.entry("key.pickItem", "PICK"),
        Map.entry("key.chat", "CHAT"),
        Map.entry("key.command", "COMMAND"),
        Map.entry("key.playerlist", "PLAYER LIST"),
        Map.entry("key.togglePerspective", "PERSPECTIVE")
    );
    private static final List<Input> PREVIEW = List.of(
        new Input(209, 969L, 0.42f, 21f, InputLane.LEFT_CLICK, "ATTACK", true),
        new Input(208, 967L, 0.68f, 34f, InputLane.RIGHT_CLICK, "USE", true),
        new Input(207, 967L, 0.64f, 32f, InputLane.KEYBOARD, "SLOT 3", true),
        new Input(206, 967L, 0.60f, 30f, InputLane.RIGHT_CLICK, "USE", true),
        new Input(205, 965L, 0.18f, 9f, InputLane.KEYBOARD, "SLOT 2", true)
    );
    private static final List<Flush> PREVIEW_FLUSHES = List.of(
        new Flush(968L, 208, InputLane.RIGHT_CLICK, "USE"),
        new Flush(968L, 206, InputLane.RIGHT_CLICK, "USE"),
        new Flush(968L, 207, InputLane.KEYBOARD, "SLOT 3"),
        new Flush(966L, 205, InputLane.KEYBOARD, "SLOT 2")
    );

    private final ArrayDeque<Input> inputs = new ArrayDeque<>();
    private final ArrayDeque<Flush> flushes = new ArrayDeque<>();
    private final Map<KeyMapping, ArrayDeque<Input>> pending = new IdentityHashMap<>();
    private long tick;
    private long sequence;
    private long tickStartNanos = System.nanoTime();

    public record Input(long sequence, long tick, float position, float millis, InputLane lane, String action, boolean press) {
        public boolean shown() {
            return this.lane.shown() && (this.press || VisualInputsConfig.showReleases);
        }
    }

    public record Flush(long tick, long sequence, InputLane lane, String action) {
        public String label() {
            return this.sequence > 0L ? "#" + this.sequence + " " + this.action : this.action;
        }
    }

    public record View(long head, float progress, boolean live, Iterable<Input> newestFirst) {
    }

    public void onConsume(final KeyMapping mapping) {
        if (!VisualInputsConfig.modEnabled) {
            return;
        }
        final ArrayDeque<Input> queue = this.pending.get(mapping);
        Input input = null;
        while (queue != null && !queue.isEmpty()) {
            final Input next = queue.pollFirst();
            if (this.tick - next.tick() <= STALE_TICKS) {
                input = next;
                break;
            }
        }
        this.flushes.addLast(input == null
            ? new Flush(this.tick, 0L, laneOf(mapping), actionName(mapping.getName()))
            : new Flush(this.tick, input.sequence(), input.lane(), input.action()));
        while (this.flushes.size() > FLUSH_CAPACITY) {
            this.flushes.removeFirst();
        }
    }

    public Iterable<Flush> flushesNewestFirst(final boolean preview) {
        if (preview && this.flushes.isEmpty()) {
            return PREVIEW_FLUSHES;
        }
        return this.flushes::descendingIterator;
    }

    public void onTick() {
        this.tick++;
        this.tickStartNanos = System.nanoTime();
    }

    public void onMouseButton(final int button, final int action) {
        this.record(laneOf(button), InputConstants.Type.MOUSE.getOrCreate(button), action);
    }

    public void onKey(final int code, final int action) {
        this.record(InputLane.KEYBOARD, InputConstants.Type.KEYSYM.getOrCreate(code), action);
    }

    public View view(final boolean preview) {
        final long latest = this.latestShownTick();
        if (preview && latest == NONE) {
            return new View(PREVIEW_HEAD, 1f, false, PREVIEW);
        }
        if (VisualInputsConfig.holdLastInputs && latest != NONE && this.tick - latest >= HOLD_TICKS) {
            return new View(latest + HOLD_TICKS - 1, 1f, false, this.inputs::descendingIterator);
        }
        return new View(this.tick, Math.min(1f, this.elapsedMillis() / tickMillis()), true, this.inputs::descendingIterator);
    }

    private void record(final InputLane lane, final InputConstants.Key key, final int action) {
        if (action != InputConstants.PRESS && action != InputConstants.RELEASE) {
            return;
        }
        if (!VisualInputsConfig.modEnabled || Minecraft.getInstance().level == null || WLClient.screen() != null) {
            return;
        }
        final float tickMillis = tickMillis();
        final float millis = Math.min(this.elapsedMillis(), tickMillis * LAST_POSITION);
        final KeyMapping mapping = mappingOf(key);
        final String name = mapping == null ? key.getDisplayName().getString().toUpperCase(Locale.ROOT) : actionName(mapping.getName());
        final boolean press = action == InputConstants.PRESS;
        final Input input = new Input(++this.sequence, this.tick, millis / tickMillis, millis, lane, name, press);
        this.inputs.addLast(input);
        while (this.inputs.size() > CAPACITY) {
            this.inputs.removeFirst();
        }
        if (press && mapping != null) {
            final ArrayDeque<Input> queue = this.pending.computeIfAbsent(mapping, unused -> new ArrayDeque<>());
            queue.addLast(input);
            while (queue.size() > PENDING_CAPACITY) {
                queue.removeFirst();
            }
        }
    }

    private long latestShownTick() {
        final Iterator<Input> newest = this.inputs.descendingIterator();
        while (newest.hasNext()) {
            final Input input = newest.next();
            if (input.shown()) {
                return input.tick();
            }
        }
        return NONE;
    }

    private float elapsedMillis() {
        return Math.max(0f, (System.nanoTime() - this.tickStartNanos) / 1_000_000f);
    }

    public static float tickMillis() {
        final ClientLevel level = Minecraft.getInstance().level;
        final float millis = level == null ? DEFAULT_TICK_MILLIS : level.tickRateManager().millisecondsPerTick();
        return millis > 0f ? millis : DEFAULT_TICK_MILLIS;
    }

    private static InputLane laneOf(final int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            return InputLane.LEFT_CLICK;
        }
        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            return InputLane.RIGHT_CLICK;
        }
        return InputLane.OTHER_MOUSE;
    }

    private static KeyMapping mappingOf(final InputConstants.Key key) {
        final String saved = key.getName();
        for (final KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            if (saved.equals(mapping.saveString())) {
                return mapping;
            }
        }
        return null;
    }

    private static InputLane laneOf(final KeyMapping mapping) {
        final String saved = mapping.saveString();
        if (saved.equals(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT).getName())) {
            return InputLane.LEFT_CLICK;
        }
        if (saved.equals(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT).getName())) {
            return InputLane.RIGHT_CLICK;
        }
        return saved.startsWith("key.mouse.") ? InputLane.OTHER_MOUSE : InputLane.KEYBOARD;
    }

    private static String actionName(final String translationKey) {
        final String known = ACTIONS.get(translationKey);
        if (known != null) {
            return known;
        }
        if (translationKey.startsWith("key.hotbar.")) {
            return "SLOT " + translationKey.substring("key.hotbar.".length());
        }
        return Component.translatable(translationKey).getString().toUpperCase(Locale.ROOT);
    }
}
