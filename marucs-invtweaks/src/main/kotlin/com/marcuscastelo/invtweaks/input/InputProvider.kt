package com.marcuscastelo.invtweaks.input

import net.minecraft.client.gui.screen.Screen

data class InputProvider(
        val _pressedButton: Int
): IInputProvider {
    override fun isDropOperation() = Screen.hasAltDown()
    override fun getPressedButton() = _pressedButton
}