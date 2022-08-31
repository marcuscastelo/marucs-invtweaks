package io.github.marcuscastelo.invtweaks

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.screen.slot.Slot

data class InvTweaksOperationInfo(
        val type: InvTweaksOperationType,
        val clickedSlot: Slot,
        val clickedSI: ScreenInventory,
        val targetSI: ScreenInventory,
        val otherInventories: ScreenInventories)
{
    //TODO: refactor those accessors
    fun type() = type
    fun clickedSlot() = clickedSlot
    fun clickedSI() = clickedSI
    fun targetSI() = targetSI
    fun otherInventories() = otherInventories
}
