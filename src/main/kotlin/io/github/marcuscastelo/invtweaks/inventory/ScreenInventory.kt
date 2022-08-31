package io.github.marcuscastelo.invtweaks.inventory

import net.minecraft.screen.ScreenHandler

data class ScreenInventory(val screenHandler: ScreenHandler, val start: Int, val end: Int) {
    val size = end - start + 1

    //TODO: refactor all usages of those methods to use getEnd and getStart instead
    fun end() = end
    fun start() = start
    fun screenHandler() = screenHandler
}
