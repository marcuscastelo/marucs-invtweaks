package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.crafting.Recipe
import com.marcuscastelo.invtweaks.intent.Intent
import kotlinx.coroutines.yield
import kotlin.jvm.optionals.getOrNull

data class CraftData(val originalIntent: Intent, val craftIntent: Intent, val recipe: Recipe)

class CraftOperation(val craftData: CraftData) : Operation<CraftData>() {
    override val operationData: CraftData
        get() = craftData

    override fun execute() = sequence {
        val originalIntent = craftData.originalIntent

        val screenHandler = originalIntent.context.screenHandler
        val recipe = craftData.recipe
        val inventories = originalIntent.context.otherInventories
        val craftingResultSI = inventories.craftingResultSI.getOrNull()
                ?: let {
                    yield(OperationResult.failure("Crafting result inventory not found"))
                    return@sequence
                }

        val expectedOutput = recipe.output
        val currentOutput = craftingResultSI.slots[0].stack.copy()

        if (expectedOutput.item != currentOutput.item) {
            yield(OperationResult.failure("Warning: Expected output item ${expectedOutput.name} but got ${currentOutput.name}")
                    .also { com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.warn(it.message) })
            return@sequence
        }

        val intentedOperation = IntentedOperation(craftData.craftIntent)
        yieldAll(intentedOperation.execute())
    }
}