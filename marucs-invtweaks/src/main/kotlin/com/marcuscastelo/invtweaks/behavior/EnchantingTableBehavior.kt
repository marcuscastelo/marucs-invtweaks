package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.operation.OperationResult
import net.minecraft.item.Items

class EnchantingTableBehavior : InvTweaksVanillaGenericBehavior() {
    override fun moveStack(intent: Intent): OperationResult {
        if (intent.context.clickedSlot.stack.isOf(Items.LAPIS_LAZULI))
            return OperationResult.pass("Using vanilla Lapis lazuli behavior for enchanting table")
        
        return super.moveStack(intent)
    }
}