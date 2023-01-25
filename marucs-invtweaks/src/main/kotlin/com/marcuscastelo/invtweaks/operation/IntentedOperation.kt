package com.marcuscastelo.invtweaks.operation

import com.marcuscastelo.invtweaks.behavior.IInvTweaksBehavior
import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.intent.IntentModifier
import com.marcuscastelo.invtweaks.intent.IntentNature
import com.marcuscastelo.invtweaks.intent.IntentType
import com.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry
import com.marcuscastelo.invtweaks.util.ChatUtils

class IntentedOperation(intent: Intent) : Operation<Intent>() {
    override val operationData = intent

    override fun execute(): OperationResult {
        ChatUtils.warnPlayer("Executing operation ${operationData.type} on ${operationData.context.screenHandler.javaClass}", true)

        val intent = operationData

        val behavior = InvTweaksBehaviorRegistry.behaviors[intent.context.screenHandler.javaClass] ?: run {
            ChatUtils.warnPlayer("Screen ${intent.context.screenHandler.javaClass} doesn't have a behavior");
            InvTweaksBehaviorRegistry.DEFAULT_BEHAVIOR
        }

        val executor = intent.type.asOperationExecutor(behavior) ?: { intentToExecute: Intent ->
            com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.warn("<InvTweaksBehaviorRegistry> Operation " + intentToExecute.type + " is not supported by " + behavior.javaClass)
            OperationResult.pass("Operation " + intentToExecute.type + " is not supported by " + behavior.javaClass)
        }

        return executor(intent)
    }

    fun IntentType.asOperationExecutor(behavior: IInvTweaksBehavior): ((Intent) -> OperationResult)? {
        //TODO: remove this function and make a registry for OperationExecutors?
        fun cast(function: (Intent) -> OperationResult) = function

        return when (this.nature) {
            IntentNature.IGNORE -> null
            IntentNature.SORT -> when (this.modifier) {
                IntentModifier.NORMAL -> cast(behavior::sort) // TODO: sort
                IntentModifier.ONE -> cast(behavior::sort) // TODO: sort one
                IntentModifier.STACK -> cast(behavior::sort) // TODO: sort stack
                IntentModifier.ALL -> cast(behavior::sort) // TODO: sort all
                IntentModifier.ALL_SAME_TYPE -> cast(behavior::sort) // TODO: sort all same type
                IntentModifier.IMPOSSIBLE -> null
            }

            IntentNature.DROP -> when (this.modifier) {
                IntentModifier.NORMAL -> null
                IntentModifier.ALL -> cast(behavior::dropAll)
                IntentModifier.ONE -> cast(behavior::dropOne)
                IntentModifier.STACK -> cast(behavior::dropStack)
                IntentModifier.ALL_SAME_TYPE -> cast(behavior::dropAllSameType)
                IntentModifier.IMPOSSIBLE -> null
            }

            IntentNature.MOVE -> when (this.modifier) {
                IntentModifier.NORMAL -> cast(behavior::pickupStack)
                IntentModifier.ALL -> cast(behavior::moveAll)
                IntentModifier.ONE -> cast(behavior::moveOne)
                IntentModifier.STACK -> cast(behavior::moveStack)
                IntentModifier.ALL_SAME_TYPE -> cast(behavior::moveAllSameType)
                IntentModifier.IMPOSSIBLE -> null
            }

            IntentNature.CRAFT -> when (this.modifier) {
                IntentModifier.NORMAL -> null
                IntentModifier.ALL -> cast(behavior::craftAll)
                IntentModifier.ONE -> cast(behavior::craftOne)
                IntentModifier.STACK -> cast(behavior::craftStack)
                IntentModifier.ALL_SAME_TYPE -> cast(behavior::craftAllSameType)
                IntentModifier.IMPOSSIBLE -> null
            }
        }
    }
}