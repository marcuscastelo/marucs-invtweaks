package com.marcuscastelo.invtweaks.operation

import kotlinx.coroutines.yield

object PassOperation : Operation<Unit>() {
    override val operationData: Unit = Unit

    override fun execute() = sequence {
        yield(OperationResult.SUCCESS)
    }
}