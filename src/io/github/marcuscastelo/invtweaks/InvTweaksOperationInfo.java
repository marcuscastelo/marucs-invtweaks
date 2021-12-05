package io.github.marcuscastelo.invtweaks;

import net.minecraft.screen.slot.Slot;

public class InvTweaksOperationInfo {
    public final InvTweaksOperationType type;

    public final Slot clickedSlot;

    public final InventoryContainerBoundInfo clickedInventoryBoundInfo;

    public final InventoryContainerBoundInfo otherInventoryBoundInfo;

    public InvTweaksOperationInfo(InvTweaksOperationType type, Slot clickedSlot, InventoryContainerBoundInfo clickedInventoryBoundInfo, InventoryContainerBoundInfo otherInventoryBoundInfo) {
        this.type = type;
        this.clickedSlot = clickedSlot;
        this.clickedInventoryBoundInfo = clickedInventoryBoundInfo;
        this.otherInventoryBoundInfo = otherInventoryBoundInfo;
    }

    public InvTweaksOperationInfo(InvTweaksOperationType type, Slot clickedSlot, InventoryContainerBoundInfo clickedInventory) {
        this(type, clickedSlot, clickedInventory, null);
    }
}
