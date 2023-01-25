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
}