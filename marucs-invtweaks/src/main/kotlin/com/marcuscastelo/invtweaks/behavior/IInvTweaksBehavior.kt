package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.operation.OperationInfo
import com.marcuscastelo.invtweaks.operation.OperationResult
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot
import kotlin.contracts.contract

interface IInvTweaksBehavior {
    fun sort(operationInfo: OperationInfo): OperationResult
    fun moveAll(operationInfo: OperationInfo): OperationResult
    fun dropAll(operationInfo: OperationInfo): OperationResult
    fun moveAllSameType(operationInfo: OperationInfo): OperationResult
    fun dropAllSameType(operationInfo: OperationInfo): OperationResult
    fun moveOne(operationInfo: OperationInfo): OperationResult
    fun dropOne(operationInfo: OperationInfo): OperationResult
    fun moveStack(operationInfo: OperationInfo): OperationResult
    fun dropStack(operationInfo: OperationInfo): OperationResult
    fun craftOne(operationInfo: OperationInfo?): OperationResult
    fun craftStack(operationInfo: OperationInfo?): OperationResult
    fun craftAll(operationInfo: OperationInfo?): OperationResult
    fun craftAllSameType(operationInfo: OperationInfo?): OperationResult
}