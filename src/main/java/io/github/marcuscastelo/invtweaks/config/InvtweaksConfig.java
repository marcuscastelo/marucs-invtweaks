package io.github.marcuscastelo.invtweaks.config;

public class InvtweaksConfig {
    public enum OverflowMode
    {
        ALWAYS("always"), NEVER("never"), ON_RIGHT_CLICK("if_right_click");

        String s;

        OverflowMode(String s) {
            this.s = s;
        }
    }

    private static OverflowMode overflowMode = OverflowMode.ON_RIGHT_CLICK;

    public static OverflowMode getOverflowMode() {
        return overflowMode;
    }

    public static void setOverflowMode(OverflowMode mode) {
        overflowMode = mode;
    }

}
