package walksy.visualinputs;

import walksy.visualinputs.config.VisualInputsConfig;

public enum InputLane {
    LEFT_CLICK,
    RIGHT_CLICK,
    OTHER_MOUSE,
    KEYBOARD;

    public boolean shown() {
        return switch (this) {
            case LEFT_CLICK -> VisualInputsConfig.leftClickEnabled;
            case RIGHT_CLICK -> VisualInputsConfig.rightClickEnabled;
            case OTHER_MOUSE -> VisualInputsConfig.otherMouseEnabled;
            case KEYBOARD -> VisualInputsConfig.keyboardEnabled;
        };
    }

    public int color() {
        return switch (this) {
            case LEFT_CLICK -> VisualInputsConfig.leftClickColor.getRGB();
            case RIGHT_CLICK -> VisualInputsConfig.rightClickColor.getRGB();
            case OTHER_MOUSE -> VisualInputsConfig.otherMouseColor.getRGB();
            case KEYBOARD -> VisualInputsConfig.keyboardColor.getRGB();
        };
    }
}
