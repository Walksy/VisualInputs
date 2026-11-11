package walksy.visualinputs;

import main.walksy.lib.api.event.WalksyLibEvents;
import net.fabricmc.api.ClientModInitializer;
import walksy.visualinputs.hud.HudLayout;

public final class VisualInputs implements ClientModInitializer {
    public static final String MOD_ID = "visualinputs";
    public static final InputTimeline TIMELINE = new InputTimeline();
    public static final HudLayout LAYOUT = new HudLayout();

    @Override
    public void onInitializeClient() {
        WalksyLibEvents.START_CLIENT_TICK.register(minecraft -> TIMELINE.onTick());
        WalksyLibEvents.MOUSE_BUTTON_INPUT.register((button, action, modifiers) -> TIMELINE.onMouseButton(button, action));
        WalksyLibEvents.KEY_INPUT.register((code, action, scancode, modifiers) -> TIMELINE.onKey(code, action));
        WalksyLibEvents.RENDER_HUD.register((graphics, deltaTracker) -> LAYOUT.arrange(graphics));
    }
}
