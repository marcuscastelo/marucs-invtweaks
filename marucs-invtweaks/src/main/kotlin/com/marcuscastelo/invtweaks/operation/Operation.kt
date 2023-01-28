package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.intent.Intent

abstract class Operation<T> {
    abstract val operationData: T
    abstract fun execute(): Sequence<OperationResult>
}