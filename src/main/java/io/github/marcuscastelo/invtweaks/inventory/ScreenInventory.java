package io.github.marcuscastelo.invtweaks.inventory;

import net.minecraft.screen.ScreenHandler;

public class ScreenInventory {
    public final ScreenHandler screenHandler;
    public final int start, end;

    public ScreenInventory(ScreenHandler screenHandler, int start, int end) {
        this.screenHandler = screenHandler;
        this.start = start;
        this.end = end;
    }

    public int getSize() {
        return end-start+1;
    }
}
