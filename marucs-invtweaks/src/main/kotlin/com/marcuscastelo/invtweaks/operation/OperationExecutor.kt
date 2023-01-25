package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.intent.Intent

fun interface OperationExecutor {
    fun execute(intent: Intent): OperationResult
}