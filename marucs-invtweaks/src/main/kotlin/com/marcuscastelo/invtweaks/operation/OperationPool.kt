package com.marcuscastelo.invtweaks.operation

object OperationPool {
    private val operations = mutableListOf<Operation<*>>()

    fun addOperation(operation: Operation<*>) {
        operations.add(operation)
    }

    fun executeNextOperation(): OperationResult {
        if (operations.isEmpty()) {
            return OperationResult.PASS
        }

        val operation = operations.removeFirst()
        return operation.execute()
    }
}