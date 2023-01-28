package com.marcuscastelo.invtweaks.operation

class DelayOperation(val operation: Operation<*>, val delay: UInt = 1u) : Operation<Unit>() {
    override val operationData: Unit
        get() = Unit

    override fun execute() = sequence {
        if (delay == 0u) {
            yieldAll(operation.execute())
        }

        var nextOperation = operation
        if (delay > 1u) {
            nextOperation = DelayOperation(operation, delay - 1u)
        }

        yield(OperationResult(
                success = OperationResult.SuccessType.PASS,
                message = "Delaying operation $nextOperation",
                nextOperations = listOf(nextOperation)
        ))

        return@sequence
    }
}
