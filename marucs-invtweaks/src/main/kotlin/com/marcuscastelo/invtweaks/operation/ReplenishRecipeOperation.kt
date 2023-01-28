package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.behavior.CraftHelper
import com.marcuscastelo.invtweaks.crafting.Recipe
import com.marcuscastelo.invtweaks.intent.Intent
import kotlin.jvm.optionals.getOrNull

data class ReplenishData(val originalIntent: Intent, val recipe: Recipe)

class ReplenishRecipeOperation(val replenishData: ReplenishData) : Operation<ReplenishData>() {
    override val operationData: ReplenishData
        get() = replenishData

    override fun execute() = sequence {
        val originalIntent = replenishData.originalIntent

        val screenHandler = originalIntent.context.screenHandler
        val recipe = replenishData.recipe

        val inventories = originalIntent.context.otherInventories
        val craftingSI = inventories.craftingSI.getOrNull() ?: let {
            yield(OperationResult.failure("Crafting inventory not found"))
            return@sequence
        }
        val resourcesSI = inventories.playerCombinedSI

        val replenishedResult = CraftHelper.replenishRecipe(
                gridSI = craftingSI,
                resourcesSI = resourcesSI,
                recipe = recipe,
        )

        if (replenishedResult == CraftHelper.ReplenishResult.SUCCESS) {
            yield(OperationResult.SUCCESS)
        } else {
            yield(OperationResult.failure("Error: Could not replenish recipe: ${replenishedResult.name}"))
        }

        return@sequence
    }
}
