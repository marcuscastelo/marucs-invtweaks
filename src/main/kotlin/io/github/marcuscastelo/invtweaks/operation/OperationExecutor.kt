package io.github.marcuscastelo.invtweaks.operation

fun interface OperationExecutor {
    fun execute(operationInfo: OperationInfo): OperationResult
}