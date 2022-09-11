package io.github.marcuscastelo.invtweaks.util

import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot

object InventoryUtils {
    fun isCraftingOutputSlot(slot: Slot): Boolean {
        return slot is CraftingResultSlot || slot.inventory is CraftingResultInventory
    }
}