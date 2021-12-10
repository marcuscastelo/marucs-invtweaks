package io.github.marcuscastelo.invtweaks;

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import net.minecraft.screen.slot.Slot;

public record InvTweaksOperationInfo(InvTweaksOperationType type,
                                     Slot clickedSlot,
                                     ScreenInventory clickedSI,
                                     ScreenInventory otherSI) {
}
