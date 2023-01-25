package com.marcuscastelo.invtweaks.crafting

import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.intent.IntentContext
import com.marcuscastelo.invtweaks.intent.IntentType

class MassCrafter(
        private val recipe: Recipe,
        private val resultSlotId: Int,
        private val recipeScreenInventory: ScreenInventory,
        private val resourcesScreenInventory: ScreenInventory) {

//    fun craftAll(otherInventories: ScreenInventories): List<Intent> {
//        val operations = mutableListOf<Intent>()
//
//        val sampleOperation = Intent(
//                IntentType.MOVE_ONE,
//                context = IntentContext(
//                        screenha
//                        clickedSlot = recipeScreenInventory.screenHandler.slots[1],
//                        clickedSI = recipeScreenInventory,
//                        targetSI = resourcesScreenInventory,
//                        otherInventories = otherInventories
//                )
//        )
//
//        operations.add(sampleOperation)
//
//        return operations
//    }
}