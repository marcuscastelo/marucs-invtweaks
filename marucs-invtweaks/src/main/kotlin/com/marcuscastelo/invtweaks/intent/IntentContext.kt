package com.marcuscastelo.invtweaks.intent

import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

data class IntentContext(
        val screenHandler: ScreenHandler,
        val clickedSlot: Slot,
        val clickedSI: ScreenInventory,
        val targetSI: ScreenInventory,
        val slotActionType: SlotActionType,
        val button: Int,
        val otherInventories: ScreenInventories,
)