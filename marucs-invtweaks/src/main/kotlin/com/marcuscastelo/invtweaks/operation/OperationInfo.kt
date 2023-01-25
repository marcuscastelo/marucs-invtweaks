package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.crafting.Recipe
import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.screen.slot.Slot

data class OperationInfo(
        val type: OperationType,
        val clickedSlot: Slot,
        val clickedSI: ScreenInventory,
        val targetSI: ScreenInventory,
        val otherInventories: ScreenInventories,
        var massCraftRecipe: Recipe? = null, // TODO: Refactor to use a better way to handle mass crafting instead of this
)