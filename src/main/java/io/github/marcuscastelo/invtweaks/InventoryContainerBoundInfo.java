package io.github.marcuscastelo.invtweaks;

import net.minecraft.screen.ScreenHandler;

public class InventoryContainerBoundInfo {
    public final ScreenHandler screenHandler;
    public final int start, end;

    public InventoryContainerBoundInfo(ScreenHandler screenHandler, int start, int end) {
        this.screenHandler = screenHandler;
        this.start = start;
        this.end = end;
    }

    public int getSize() {
        return end-start+1;
    }
}
