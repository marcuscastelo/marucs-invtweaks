package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.InvTweaksMod

object TickedOperationPool {
    private val operationLists = mutableListOf(mutableListOf<Operation<*>>())

    fun addOperation(operation: Operation<*>) {
        val nextOperationList = operationLists.last()
        InvTweaksMod.LOGGER.info("[OperationPool] Adding operation: $operation")
        nextOperationList.add(operation)
    }

    fun tick() = sequence {
        if (operationLists.isEmpty()) {
            yield(OperationResult.PASS)
            return@sequence
        }

        operationLists.add(mutableListOf()) // Add a new tick operation list

        val operations = operationLists.removeFirst()
        InvTweaksMod.LOGGER.info("[OperationPool] Executing next operation list: $operations")

        operations.forEach { operation ->
            InvTweaksMod.LOGGER.info("[OperationPool] Executing next operation: $operation")
            yieldAll(operation.execute())
        }

        InvTweaksMod.LOGGER.info("[OperationPool] All operations executed, exiting tick")

        operations.clear()

        return@sequence
    }
}