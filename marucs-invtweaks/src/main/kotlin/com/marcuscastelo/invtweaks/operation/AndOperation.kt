package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.InvTweaksMod

class AndOperation(val operations: List<Operation<*>>) : Operation<Unit>() {
    override val operationData: Unit = Unit

    override fun execute() = sequence {
        operations.forEach { operation ->
            InvTweaksMod.LOGGER.info("[AndOperation] Executing inner operation: $operation")

            for (result in operation.execute()) {
                if (result.success == OperationResult.SuccessType.FAILURE) {
                    InvTweaksMod.LOGGER.info("[AndOperation] Inner operation failed, aborting")
                    InvTweaksMod.LOGGER.info("[AndOperation] Inner Operation result: $result")
                    yield(result)
                    return@sequence
                }
                yield(result)
            }

            InvTweaksMod.LOGGER.info("[AndOperation] All inner operations executed successfully")
            yield(OperationResult.SUCCESS)
        }
    }
}