package io.github.marcuscastelo.invtweaks.inventory;

import net.minecraft.screen.ScreenHandler;

public record ScreenInventory(ScreenHandler screenHandler, int start, int end) {
    public int getSize() {
        return end - start + 1;
    }
}
