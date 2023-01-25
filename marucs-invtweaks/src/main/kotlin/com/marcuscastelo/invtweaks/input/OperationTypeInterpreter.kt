package com.marcuscastelo.invtweaks.input

import com.marcuscastelo.invtweaks.operation.OperationModifier
import com.marcuscastelo.invtweaks.operation.OperationNature
import com.marcuscastelo.invtweaks.operation.OperationType
import com.marcuscastelo.invtweaks.util.Assure
import com.marcuscastelo.invtweaks.util.ChatUtils
import com.marcuscastelo.invtweaks.util.KeyUtils
import org.lwjgl.glfw.GLFW

object OperationTypeInterpreter {
    fun interpret(inputProvider: IInputProvider): OperationType? {
        val nature = interpretOperationNature(inputProvider)
        val target = interpretOperationModifier(inputProvider)
        return OperationType.fromPair(nature, target)
    }

    private fun interpretOperationNature(inputProvider: IInputProvider): OperationNature {
        val drop: Boolean = inputProvider.isDropOperation()
        return when (inputProvider.getPressedButton()) {
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> OperationNature.SORT
            GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT ->
                    if (drop) OperationNature.DROP else OperationNature.MOVE
            else -> OperationNature.IGNORE
        }
    }

    private fun interpretOperationModifier(inputProvider: IInputProvider): OperationModifier {
        val appliesToOne = OperationModifier.ONE.applies() // TODO: use IInputProvider inside applies()
        val appliesToSameType = OperationModifier.ALL_SAME_TYPE.applies()
        val appliesToStack = OperationModifier.STACK.applies()
        val appliesToAll = OperationModifier.ALL.applies()

        if (!Assure.onlyOneTrue(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            ChatUtils.warnPlayer("Bug found! combination pressed: applyToOne=$appliesToOne, applyToSameType=$appliesToSameType, applyToStack=$appliesToStack, applyToAll=$appliesToAll")
            return OperationModifier.IMPOSSIBLE
        }

        if (appliesToOne) return OperationModifier.ONE
        if (appliesToSameType) return OperationModifier.ALL_SAME_TYPE
        if (appliesToStack) return OperationModifier.STACK
        if (appliesToAll) return OperationModifier.ALL

        val drop: Boolean = inputProvider.isDropOperation()
        val isMoveUpOrDown = KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W) || KeyUtils.isKeyPressed(GLFW.GLFW_KEY_S)
        val stackIsTheNormal = drop || isMoveUpOrDown

        return if (stackIsTheNormal) OperationModifier.STACK else OperationModifier.NORMAL
    }
}