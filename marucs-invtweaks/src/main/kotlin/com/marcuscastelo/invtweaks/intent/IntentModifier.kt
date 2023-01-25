package com.marcuscastelo.invtweaks.intent

import com.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.client.gui.screen.Screen
import org.lwjgl.glfw.GLFW

enum class IntentModifier(val representation: String, private val checkHotkeyFun: () -> Boolean) {
    ONE("ONE", { Screen.hasControlDown() && !Screen.hasShiftDown() }) ,
    STACK("STACK", { !Screen.hasControlDown() && Screen.hasShiftDown() }),
    ALL("ALL", { !Screen.hasControlDown() && !Screen.hasShiftDown() && isKeyPressed(GLFW.GLFW_KEY_SPACE) }),
    ALL_SAME_TYPE("ALL_SAME_TYPE", { Screen.hasControlDown() && Screen.hasShiftDown() }),
    NORMAL("NORMAL", { !Screen.hasControlDown() && !Screen.hasShiftDown() && !isKeyPressed(GLFW.GLFW_KEY_SPACE) }),
    IMPOSSIBLE("IMPOSSIBLE", { false });
    ;

    fun applies(): Boolean = checkHotkeyFun()
}

