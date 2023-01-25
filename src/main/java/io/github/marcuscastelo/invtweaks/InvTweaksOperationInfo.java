package io.github.marcuscastelo.invtweaks;

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import net.minecraft.screen.slot.Slot;

public class InvTweaksOperationInfo {
    public final InvTweaksOperationType type;

    public final Slot clickedSlot;

    public final ScreenInventory clickedInventoryBoundInfo;

    public final ScreenInventory otherInventoryBoundInfo;

    public InvTweaksOperationInfo(InvTweaksOperationType type, Slot clickedSlot, ScreenInventory clickedInventoryBoundInfo, ScreenInventory otherInventoryBoundInfo) {
        this.type = type;
        this.clickedSlot = clickedSlot;
        this.clickedInventoryBoundInfo = clickedInventoryBoundInfo;
        this.otherInventoryBoundInfo = otherInventoryBoundInfo;
    }

    public InvTweaksOperationInfo(InvTweaksOperationType type, Slot clickedSlot, ScreenInventory clickedInventory) {
        this(type, clickedSlot, clickedInventory, null);
    }
}
