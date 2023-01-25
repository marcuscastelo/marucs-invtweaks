package com.marcuscastelo.invtweaks.intent

import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

data class IntentContext(
    val screenHandler: ScreenHandler,
    val clickedSlot: Slot,
    val clickedSI: ScreenInventory,
    val targetSI: ScreenInventory,
    val otherInventories: ScreenInventories,
)