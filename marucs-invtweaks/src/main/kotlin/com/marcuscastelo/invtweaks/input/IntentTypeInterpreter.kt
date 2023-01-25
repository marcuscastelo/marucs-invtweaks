package com.marcuscastelo.invtweaks.input

import com.marcuscastelo.invtweaks.intent.IntentModifier
import com.marcuscastelo.invtweaks.intent.IntentNature
import com.marcuscastelo.invtweaks.intent.IntentType
import com.marcuscastelo.invtweaks.util.Assure
import com.marcuscastelo.invtweaks.util.ChatUtils
import com.marcuscastelo.invtweaks.util.KeyUtils
import org.lwjgl.glfw.GLFW

object IntentTypeInterpreter {
    fun interpret(inputProvider: IInputProvider): IntentType? {
        val nature = interpretOperationNature(inputProvider)
        val target = interpretOperationModifier(inputProvider)
        return IntentType.fromPair(nature, target)
    }

    private fun interpretOperationNature(inputProvider: IInputProvider): IntentNature {
        val drop: Boolean = inputProvider.isDropOperation()
        return when (inputProvider.getPressedButton()) {
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> IntentNature.SORT
            GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT ->
                    if (drop) IntentNature.DROP else IntentNature.MOVE
            else -> IntentNature.IGNORE
        }
    }

    private fun interpretOperationModifier(inputProvider: IInputProvider): IntentModifier {
        val appliesToOne = IntentModifier.ONE.applies() // TODO: use IInputProvider inside applies()
        val appliesToSameType = IntentModifier.ALL_SAME_TYPE.applies()
        val appliesToStack = IntentModifier.STACK.applies()
        val appliesToAll = IntentModifier.ALL.applies()

        if (!Assure.onlyOneTrue(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            ChatUtils.warnPlayer("Bug found! combination pressed: applyToOne=$appliesToOne, applyToSameType=$appliesToSameType, applyToStack=$appliesToStack, applyToAll=$appliesToAll")
            return IntentModifier.IMPOSSIBLE
        }

        if (appliesToOne) return IntentModifier.ONE
        if (appliesToSameType) return IntentModifier.ALL_SAME_TYPE
        if (appliesToStack) return IntentModifier.STACK
        if (appliesToAll) return IntentModifier.ALL

        val drop: Boolean = inputProvider.isDropOperation()
        val isMoveUpOrDown = KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W) || KeyUtils.isKeyPressed(GLFW.GLFW_KEY_S)
        val stackIsTheNormal = drop || isMoveUpOrDown

        return if (stackIsTheNormal) IntentModifier.STACK else IntentModifier.NORMAL
    }
}