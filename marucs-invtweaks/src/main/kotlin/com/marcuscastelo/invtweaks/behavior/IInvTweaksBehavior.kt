package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.util.ScreenController
import net.minecraft.screen.slot.SlotActionType

interface IInvTweaksBehavior {
    fun sort(intent: Intent): OperationResult
    fun pickupStack(intent: Intent): OperationResult { //TODO: standardize this function
        ScreenController(intent.context.screenHandler).click(intent.context.clickedSlot.id, intent.context.button, SlotActionType.PICKUP)
        return OperationResult.SUCCESS
    }

    fun moveAll(intent: Intent): OperationResult
    fun dropAll(intent: Intent): OperationResult
    fun moveAllSameType(intent: Intent): OperationResult
    fun dropAllSameType(intent: Intent): OperationResult
    fun moveOne(intent: Intent): OperationResult
    fun dropOne(intent: Intent): OperationResult
    fun moveStack(intent: Intent): OperationResult
    fun dropStack(intent: Intent): OperationResult
    fun craftOne(intent: Intent?): OperationResult
    fun craftStack(intent: Intent?): OperationResult
    fun craftAll(intent: Intent?): OperationResult
    fun craftAllSameType(intent: Intent?): OperationResult
}