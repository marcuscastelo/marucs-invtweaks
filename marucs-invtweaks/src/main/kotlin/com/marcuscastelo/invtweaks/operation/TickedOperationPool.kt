package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.InvTweaksMod

object TickedOperationPool {
    private val operationLists = mutableListOf(mutableListOf<Operation<*>>())

    var currentTick: UInt = 0u
        private set

    fun clear() {
        operationLists.clear()
        operationLists.add(mutableListOf())
    }

    fun addOperation(operation: Operation<*>) {
        val nextOperationList = operationLists.last()
        InvTweaksMod.LOGGER.info("[OperationPool] Adding operation: $operation")
        nextOperationList.add(operation)
    }

    fun tick() = sequence {
        currentTick++

        if (operationLists.isEmpty()) {
            yield(OperationResult.PASS)
            return@sequence
        }

        val operations = operationLists.removeFirst()
        operationLists.add(mutableListOf()) // Add a new tick operation list

        if (operations.isEmpty()) {
            yield(OperationResult.PASS)
            return@sequence
        }

        InvTweaksMod.LOGGER.info("[OperationPool #$currentTick] Executing next operation list: $operations")

        operations.forEach { operation ->
            InvTweaksMod.LOGGER.info("[OperationPool #$currentTick] Executing next operation: $operation")
            yieldAll(operation.execute())
        }

        InvTweaksMod.LOGGER.info("[OperationPool #$currentTick] All operations executed, exiting tick")

        operations.clear()

        return@sequence
    }
}