package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry
import com.marcuscastelo.invtweaks.util.ChatUtils

data class SimpleOperationData(val intent: Intent)

class SimpleOperation(intent: Intent) : Operation<SimpleOperationData>() {
    override val operationData = SimpleOperationData(intent)

    override fun execute(): OperationResult {
        ChatUtils.warnPlayer("Executing operation ${operationData.intent.type} on ${operationData.intent.context.screenHandler.javaClass}", true)
        return InvTweaksBehaviorRegistry.executeOperation(operationData.intent.context.screenHandler.javaClass, operationData.intent).also {
            ChatUtils.warnPlayer("Operation result: $it", true)
        }
    }
}