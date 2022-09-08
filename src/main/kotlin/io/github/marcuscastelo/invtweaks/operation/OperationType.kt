package io.github.marcuscastelo.invtweaks.operation

import io.github.marcuscastelo.invtweaks.behavior.IInvTweaksBehavior
import java.util.Optional;


enum class OperationType(val nature: OperationNature, val modifier: OperationModifier) {
    IGNORE(OperationNature.IGNORE, OperationModifier.NORMAL),

    MOVE_ONE(OperationNature.MOVE, OperationModifier.ONE),
    MOVE_ALL(OperationNature.MOVE, OperationModifier.ALL),
    MOVE_ALL_SAME_TYPE(OperationNature.MOVE, OperationModifier.ALL_SAME_TYPE),
    MOVE_STACK(OperationNature.MOVE, OperationModifier.STACK),
    MOVE_NORMAL(OperationNature.MOVE, OperationModifier.NORMAL),

    DROP_ONE(OperationNature.DROP, OperationModifier.ONE),
    DROP_ALL(OperationNature.DROP, OperationModifier.ALL),
    DROP_ALL_SAME_TYPE(OperationNature.DROP, OperationModifier.ALL_SAME_TYPE),
    DROP_STACK(OperationNature.DROP, OperationModifier.STACK),
    DROP_NORMAL(OperationNature.DROP, OperationModifier.NORMAL),

    SORT_ONE(OperationNature.SORT, OperationModifier.ONE),
    SORT_ALL(OperationNature.SORT, OperationModifier.ALL),
    SORT_ALL_SAME_TYPE(OperationNature.SORT, OperationModifier.ALL_SAME_TYPE),
    SORT_STACK(OperationNature.SORT, OperationModifier.STACK),
    SORT_NORMAL(OperationNature.SORT, OperationModifier.NORMAL),

    CRAFT_ONE(OperationNature.CRAFT, OperationModifier.ONE),
    CRAFT_ALL(OperationNature.CRAFT, OperationModifier.ALL),
    CRAFT_ALL_SAME_TYPE(OperationNature.CRAFT, OperationModifier.ALL_SAME_TYPE),
    CRAFT_STACK(OperationNature.CRAFT, OperationModifier.STACK),
    CRAFT_NORMAL(OperationNature.CRAFT, OperationModifier.NORMAL),
    ;

    companion object {
        @JvmStatic
        fun fromPair(nature: OperationNature, modifier: OperationModifier): Optional<OperationType> {
            return values().firstOrNull { it.nature == nature && it.modifier == modifier }?.let { Optional.of(it) } ?: Optional.empty()
        }
    }


    fun isIgnore() = nature == OperationNature.IGNORE
    fun isMove() = nature == OperationNature.MOVE
    fun isDrop() = nature == OperationNature.DROP
    fun isSort() = nature == OperationNature.SORT
    fun isAll() = modifier == OperationModifier.ALL
    fun isAllSameType() = modifier == OperationModifier.ALL_SAME_TYPE

    fun asOperationExecutor(behavior: IInvTweaksBehavior): Optional<OperationExecutor> {
        fun cast(function: (OperationInfo) -> OperationResult): Optional<OperationExecutor> =
                Optional.of(OperationExecutor { function(it) })

        return when (this.nature) {
            OperationNature.IGNORE -> Optional.empty()
            OperationNature.SORT -> when (this.modifier) {
                OperationModifier.NORMAL -> cast(behavior::sort) // TODO: sort
                OperationModifier.ONE -> cast(behavior::sort) // TODO: sort one
                OperationModifier.STACK -> cast(behavior::sort) // TODO: sort stack
                OperationModifier.ALL -> cast(behavior::sort) // TODO: sort all
                OperationModifier.ALL_SAME_TYPE -> cast(behavior::sort) // TODO: sort all same type
            }
            OperationNature.DROP -> when (this.modifier) {
                OperationModifier.NORMAL -> Optional.empty()
                OperationModifier.ALL -> cast(behavior::dropAll)
                OperationModifier.ONE -> cast(behavior::dropOne)
                OperationModifier.STACK -> cast(behavior::dropStack)
                OperationModifier.ALL_SAME_TYPE -> cast(behavior::dropAllSameType)
            }
            OperationNature.MOVE -> when (this.modifier) {
                OperationModifier.NORMAL -> Optional.empty()
                OperationModifier.ALL -> cast(behavior::moveAll)
                OperationModifier.ONE -> cast(behavior::moveOne)
                OperationModifier.STACK -> cast(behavior::moveStack)
                OperationModifier.ALL_SAME_TYPE -> cast(behavior::moveAllSameType)
            }
            OperationNature.CRAFT -> when (this.modifier) {
                OperationModifier.NORMAL -> Optional.empty()
                OperationModifier.ALL -> cast(behavior::craftAll)
                OperationModifier.ONE -> cast(behavior::craftOne)
                OperationModifier.STACK -> cast(behavior::craftStack)
                OperationModifier.ALL_SAME_TYPE -> cast(behavior::craftAllSameType)
            }
        }
    }
}