package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.operation.OperationInfo
import com.marcuscastelo.invtweaks.operation.OperationResult
import net.minecraft.item.Items

class EnchantingTableBehavior : InvTweaksVanillaGenericBehavior() {
    override fun moveStack(operationInfo: OperationInfo): OperationResult {
        if (operationInfo.clickedSlot.stack.isOf(Items.LAPIS_LAZULI))
            return OperationResult.pass("Using vanilla Lapis lazuli behavior for enchanting table")
        
        return super.moveStack(operationInfo)
    }
}