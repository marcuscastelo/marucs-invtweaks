package io.github.marcuscastelo.invtweaks.input

import io.github.marcuscastelo.invtweaks.operation.OperationModifier
import io.github.marcuscastelo.invtweaks.operation.OperationNature
import io.github.marcuscastelo.invtweaks.operation.OperationType
import io.github.marcuscastelo.invtweaks.util.Assure
import io.github.marcuscastelo.invtweaks.util.ChatUtils
import io.github.marcuscastelo.invtweaks.util.KeyUtils
import org.lwjgl.glfw.GLFW
import java.util.*

object OperationTypeInterpreter {
    fun interpret(inputProvider: IInputProvider): Optional<OperationType> {
        val nature = interpretOperationNature(inputProvider).orElse(null)
        val target = interpretOperationModifier(inputProvider).orElse(null)
        return OperationType.fromPair(nature, target)
    }

    private fun interpretOperationNature(inputProvider: IInputProvider): Optional<OperationNature> {
        val drop: Boolean = inputProvider.isDropOperation()
        return when (inputProvider.getPressedButton()) {
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Optional.of(OperationNature.SORT)
            GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Optional.of(
                    if (drop) OperationNature.DROP else OperationNature.MOVE
            )
            else -> Optional.of(OperationNature.IGNORE)
        }
    }

    private fun interpretOperationModifier(inputProvider: IInputProvider): Optional<OperationModifier> {
        val appliesToOne = OperationModifier.ONE.applies()
        val appliesToSameType = OperationModifier.ALL_SAME_TYPE.applies()
        val appliesToStack = OperationModifier.STACK.applies()
        val appliesToAll = OperationModifier.ALL.applies()
        if (!Assure.onlyOneTrue(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            ChatUtils.warnPlayer("Unknown combination pressed: applyToOne=$appliesToOne, applyToSameType=$appliesToSameType, applyToStack=$appliesToStack, applyToAll=$appliesToAll")
            return Optional.empty()
        }
        if (appliesToOne) return Optional.of(OperationModifier.ONE)
        if (appliesToSameType) return Optional.of(OperationModifier.ALL_SAME_TYPE)
        if (appliesToStack) return Optional.of(OperationModifier.STACK)
        if (appliesToAll) return Optional.of(OperationModifier.ALL)
        val drop: Boolean = inputProvider.isDropOperation()
        val isMoveUpOrDown = KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W) || KeyUtils.isKeyPressed(GLFW.GLFW_KEY_S)
        val stackIsTheNormal = drop || isMoveUpOrDown
        return Optional.of(if (stackIsTheNormal) OperationModifier.STACK else OperationModifier.NORMAL)
    }
}