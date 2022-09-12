package io.github.marcuscastelo.invtweaks.behavior

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.MathHelper

object Jesus {
    const val MOVE_RESULT_FULL = -3

    fun moveToSlot(handler: ScreenHandler, maxSlot: Int, fromSlotId: Int, toSlotId: Int, quantity: Int, sorting: Boolean): Int {
        val initialStack = handler.getSlot(fromSlotId).stack.copy()
        val initialCount = initialStack.count
        if (quantity > initialCount) {
            println("Trying to move more than we have InvTweaksVanillaBehavior@moveToSlot")
            return -1
        }
    val interactionManager = MinecraftClient.getInstance().interactionManager
        val player = MinecraftClient.getInstance().player
        if (interactionManager == null || player == null) {
            println("nullptr in InvTweaksVanillaBehavior@moveSome")
            return -2
        }

        //Item in hand
        interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player)
        var currentHeldStack = handler.cursorStack
        var remainingTotalClicks = quantity
        var candidateDestination = toSlotId
        while (remainingTotalClicks > 0 && candidateDestination <= maxSlot) {
            if (!handler.canInsertIntoSlot(currentHeldStack, handler.slots[candidateDestination])) {
                candidateDestination++
                continue
            }
            val candidateDstStack = handler.slots[candidateDestination].stack
            if (candidateDstStack.item !== initialStack.item) {
                return if (candidateDstStack.item === Items.AIR) {
                    //If air, just put
                    val rightClicks = MathHelper.clamp(remainingTotalClicks, 0, initialStack.maxCount)
                    if (rightClicks == initialStack.maxCount) {
                        interactionManager.clickSlot(handler.syncId, candidateDestination, 0, SlotActionType.PICKUP, player)
                    } else {
                        //TODO: send one packet, instead of a flood
                        for (i in 0 until rightClicks) {
                            interactionManager.clickSlot(handler.syncId, candidateDestination, 1, SlotActionType.PICKUP, player)
                        }
                    }
                    remainingTotalClicks -= rightClicks
                    candidateDestination++
                    continue
                } else {
                    //If slot is occupied
                    if (sorting) {
                        var dumpWrongStackSlot = candidateDestination + 1
                        while (handler.slots[dumpWrongStackSlot].stack.item !== Items.AIR && dumpWrongStackSlot <= maxSlot) dumpWrongStackSlot++
                        if (dumpWrongStackSlot > maxSlot) { //If there's no more room
                            //Returns remaining
                            interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player)
                            currentHeldStack = handler.cursorStack
                            return MOVE_RESULT_FULL
                        }

                        //Swap right and wrong
                        interactionManager.clickSlot(handler.syncId, candidateDestination, 0, SlotActionType.PICKUP, player)
                        currentHeldStack = handler.cursorStack

                        //Dump wrong
                        interactionManager.clickSlot(handler.syncId, dumpWrongStackSlot, 0, SlotActionType.PICKUP, player)
                        currentHeldStack = handler.cursorStack
                        candidateDestination
                    } else {
                        candidateDestination++
                        continue
                    }
                }
            }

            //If same type:
            val clicksToCompleteStack = candidateDstStack.maxCount - candidateDstStack.count
            val rightClicks = MathHelper.clamp(remainingTotalClicks, 0, clicksToCompleteStack)
            if (rightClicks > 0 && remainingTotalClicks >= clicksToCompleteStack) {
                interactionManager.clickSlot(handler.syncId, candidateDestination, 0, SlotActionType.PICKUP, player)
                currentHeldStack = handler.cursorStack
            } else for (i in 0 until rightClicks) {
                interactionManager.clickSlot(handler.syncId, candidateDestination, 1, SlotActionType.PICKUP, player)
                currentHeldStack = handler.cursorStack
            }
            remainingTotalClicks -= rightClicks
            candidateDestination++
        }
        if (remainingTotalClicks > 0) interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player)
        currentHeldStack = handler.cursorStack
        if (!currentHeldStack.isEmpty) interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player)
        if (candidateDestination > toSlotId) candidateDestination--
        return candidateDestination
    }

    fun moveToInventory(handler: ScreenHandler, fromSlot: Int, destinationBoundInfo: ScreenInventory, quantity: Int, sorting: Boolean): Int {
        val destinationStart = destinationBoundInfo.start
        val inventorySize = destinationBoundInfo.size
        return moveToSlot(handler, destinationStart + inventorySize - 1, fromSlot, destinationStart, quantity, sorting)
    }
}