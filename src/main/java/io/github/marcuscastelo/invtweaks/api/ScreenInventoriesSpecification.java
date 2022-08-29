package io.github.marcuscastelo.invtweaks.api;

public class ScreenInventoriesSpecification {
    public static ScreenInventoriesSpecification DEFAULT = new ScreenInventoriesSpecification(true, 27, 9);

    public final boolean hasExternalInventory;
    public final int playerMainInvSize;
    public final int playerHotbarSize;

    public ScreenInventoriesSpecification(boolean hasExternalInventory, int playerMainInvSize, int playerHotbarSize) {
        this.hasExternalInventory = hasExternalInventory;
        this.playerMainInvSize = playerMainInvSize;
        this.playerHotbarSize = playerHotbarSize;
    }
}
