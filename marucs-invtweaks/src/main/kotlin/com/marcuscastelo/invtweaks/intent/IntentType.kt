package com.marcuscastelo.invtweaks.intent

import com.marcuscastelo.invtweaks.behavior.IInvTweaksBehavior
import com.marcuscastelo.invtweaks.operation.OperationExecutor
import com.marcuscastelo.invtweaks.operation.OperationResult


enum class IntentType(val nature: IntentNature, val modifier: IntentModifier) {
    IGNORE(IntentNature.IGNORE, IntentModifier.NORMAL),

    MOVE_ONE(IntentNature.MOVE, IntentModifier.ONE),
    MOVE_ALL(IntentNature.MOVE, IntentModifier.ALL),
    MOVE_ALL_SAME_TYPE(IntentNature.MOVE, IntentModifier.ALL_SAME_TYPE),
    MOVE_STACK(IntentNature.MOVE, IntentModifier.STACK),
    MOVE_NORMAL(IntentNature.MOVE, IntentModifier.NORMAL),

    DROP_ONE(IntentNature.DROP, IntentModifier.ONE),
    DROP_ALL(IntentNature.DROP, IntentModifier.ALL),
    DROP_ALL_SAME_TYPE(IntentNature.DROP, IntentModifier.ALL_SAME_TYPE),
    DROP_STACK(IntentNature.DROP, IntentModifier.STACK),
    DROP_NORMAL(IntentNature.DROP, IntentModifier.NORMAL),

    SORT_ONE(IntentNature.SORT, IntentModifier.ONE),
    SORT_ALL(IntentNature.SORT, IntentModifier.ALL),
    SORT_ALL_SAME_TYPE(IntentNature.SORT, IntentModifier.ALL_SAME_TYPE),
    SORT_STACK(IntentNature.SORT, IntentModifier.STACK),
    SORT_NORMAL(IntentNature.SORT, IntentModifier.NORMAL),

    CRAFT_ONE(IntentNature.CRAFT, IntentModifier.ONE),
    CRAFT_ALL(IntentNature.CRAFT, IntentModifier.ALL),
    CRAFT_ALL_SAME_TYPE(IntentNature.CRAFT, IntentModifier.ALL_SAME_TYPE),
    CRAFT_STACK(IntentNature.CRAFT, IntentModifier.STACK),
    CRAFT_NORMAL(IntentNature.CRAFT, IntentModifier.NORMAL),
    ;

    companion object {
        @JvmStatic
        fun fromPair(nature: IntentNature, modifier: IntentModifier): IntentType? {
            return values().firstOrNull { it.nature == nature && it.modifier == modifier }
        }
    }

    fun isIgnore() = nature == IntentNature.IGNORE
    fun isMove() = nature == IntentNature.MOVE
    fun isDrop() = nature == IntentNature.DROP
    fun isSort() = nature == IntentNature.SORT
    fun isAll() = modifier == IntentModifier.ALL
    fun isAllSameType() = modifier == IntentModifier.ALL_SAME_TYPE

    fun asOperationExecutor(behavior: IInvTweaksBehavior): OperationExecutor? {
        //TODO: remove this function and make a registry for OperationExecutors?
        fun cast(function: (Intent) -> OperationResult): OperationExecutor =
                OperationExecutor { function(it) }

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