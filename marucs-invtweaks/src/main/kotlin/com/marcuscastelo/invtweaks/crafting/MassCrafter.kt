package com.marcuscastelo.invtweaks.crafting

import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import com.marcuscastelo.invtweaks.operation.OperationInfo
import com.marcuscastelo.invtweaks.operation.OperationType

class MassCrafter(
        private val recipe: Recipe,
        private val resultSlotId: Int,
        private val recipeScreenInventory: ScreenInventory,
        private val resourcesScreenInventory: ScreenInventory) {

    fun craftAll(otherInventories: ScreenInventories): List<OperationInfo> {
        val operations = mutableListOf<OperationInfo>()

        val sampleOperation = OperationInfo(
                OperationType.MOVE_ONE,
                clickedSlot = recipeScreenInventory.screenHandler.slots[1],
                clickedSI = recipeScreenInventory,
                targetSI = resourcesScreenInventory,
                otherInventories = otherInventories
        )

        operations.add(sampleOperation)

        return operations
    }
}