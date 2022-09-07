package io.github.marcuscastelo.invtweaks.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

object KeyUtils {
    @JvmStatic
    fun isKeyPressed(key: Int): Boolean {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, key)
    }
}