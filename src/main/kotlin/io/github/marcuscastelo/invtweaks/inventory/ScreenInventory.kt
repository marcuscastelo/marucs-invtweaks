package io.github.marcuscastelo.invtweaks.inventory

import net.minecraft.screen.ScreenHandler

data class ScreenInventory(val screenHandler: ScreenHandler, val start: Int, val end: Int) {
    val size = end - start + 1

    val slotRange = start..end
    val slots get() = screenHandler.slots.subList(start, end + 1)
    val stacks get() = screenHandler.stacks.subList(start, end + 1)
}
