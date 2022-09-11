package io.github.marcuscastelo.invtweaks.behavior

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.SUCCESS
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.failure
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import io.github.marcuscastelo.invtweaks.util.InventoryUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper

open class InvTweaksVanillaGenericBehavior : IInvTweaksBehavior {
    val MOVE_RESULT_FULL = -3
    protected open fun moveToSlot(handler: ScreenHandler, maxSlot: Int, fromSlotId: Int, toSlotId: Int, quantity: Int, sorting: Boolean): Int {
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

    protected open fun moveToInventory(handler: ScreenHandler, fromSlot: Int, destinationBoundInfo: ScreenInventory, quantity: Int, sorting: Boolean): Int {
        val destinationStart = destinationBoundInfo.start
        val inventorySize = destinationBoundInfo.size
        return moveToSlot(handler, destinationStart + inventorySize - 1, fromSlot, destinationStart, quantity, sorting)
    }

    protected val sortingComparator: Comparator<Item>
        protected get() = Comparator.comparing { item: Item ->
            val sb = StringBuilder()
            sb.append(if (item.group != null) item.group!!.name else "")
            sb.append(item.name)
            sb.append(item.maxCount)
            sb.toString()
        }

    override fun sort(operationInfo: OperationInfo): OperationResult {
        val handler = operationInfo.clickedSI.screenHandler
        val startSlot = operationInfo.clickedSI.start
        val endSlot = operationInfo.clickedSI.end
        val items: MutableSet<Item> = HashSet()
        val quantityPerItem = HashMap<Item, Int>()
        for (slot in startSlot..endSlot) {
            val stack = handler.slots[slot].stack
            val item = stack.item
            if (item === Items.AIR) continue
            quantityPerItem[item] = quantityPerItem.getOrDefault(item, 0) + stack.count
            items.add(item)
        }
        val itemsArray: List<Item> = ArrayList(items).sortedWith(sortingComparator)
        var destinationSlot = startSlot
        for (item in itemsArray) {
            if (item === Items.AIR) continue
            var movedItems = 0
            val totalItems = quantityPerItem[item]!!
            for (fromSlot in startSlot..endSlot) {
                if (movedItems >= totalItems) break
                val stack = handler.slots[fromSlot].stack
                if (stack.item !== item) continue
                val placedAt = moveToSlot(handler, endSlot, fromSlot, destinationSlot, stack.count, true)
                destinationSlot = Math.max(destinationSlot, placedAt)
                if (placedAt > 0) {
                    movedItems += stack.count
                    if (handler.slots[placedAt].stack.item !== item) destinationSlot++ //If different, no merging attempt is needed TODO: check if best approach
                }
            }
            while (handler.slots[destinationSlot].stack.item === item) destinationSlot++
        }
        return SUCCESS
    }

    override fun moveAll(operationInfo: OperationInfo): OperationResult {
        for (slotId in operationInfo.clickedSI.start..operationInfo.clickedSI.end) {
            val stack = operationInfo.clickedSI.screenHandler.getSlot(slotId).stack
            if (stack.item === Items.AIR) continue
            val result = moveToInventory(operationInfo.clickedSI.screenHandler, slotId, operationInfo.targetSI, stack.count, false)
            if (result == MOVE_RESULT_FULL) break
        }
        return SUCCESS
    }

    override fun dropAll(operationInfo: OperationInfo): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        for (slotId in operationInfo.clickedSI.start..operationInfo.clickedSI.end) {
            MinecraftClient.getInstance().interactionManager!!.clickSlot(operationInfo.clickedSI.screenHandler.syncId, slotId, 1, SlotActionType.THROW, playerEntity)
        }
        return SUCCESS
    }

    override fun moveAllSameType(operationInfo: OperationInfo): OperationResult {
        val itemType = operationInfo.clickedSlot.stack.item
        for (slot in operationInfo.clickedSI.start..operationInfo.clickedSI.end) {
            val stack = operationInfo.clickedSI.screenHandler.slots[slot].stack
            if (stack.item !== itemType) continue
            val result = moveToInventory(operationInfo.clickedSI.screenHandler, slot, operationInfo.targetSI, stack.count, false)
            if (result == MOVE_RESULT_FULL) break
        }
        return SUCCESS
    }

    override fun dropAllSameType(operationInfo: OperationInfo): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        val itemType = operationInfo.clickedSlot.stack.item
        for (slot in operationInfo.clickedSI.start..operationInfo.clickedSI.end) {
            val stack = operationInfo.clickedSI.screenHandler.slots[slot].stack
            if (stack.item !== itemType) continue
            MinecraftClient.getInstance().interactionManager!!.clickSlot(operationInfo.clickedSI.screenHandler.syncId, slot, 1, SlotActionType.THROW, playerEntity)
        }
        return SUCCESS
    }

    override fun moveOne(operationInfo: OperationInfo): OperationResult {
        val handler = operationInfo.clickedSI.screenHandler
        val from = operationInfo.clickedSlot.id
        val result = moveToInventory(handler, from, operationInfo.targetSI, 1, false)
        return SUCCESS
    }

    override fun dropOne(operationInfo: OperationInfo): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        MinecraftClient.getInstance().interactionManager!!.clickSlot(operationInfo.clickedSI.screenHandler.syncId, operationInfo.clickedSlot.id, 0, SlotActionType.THROW, playerEntity)
        return SUCCESS
    }

    override fun moveStack(operationInfo: OperationInfo): OperationResult {


        InvTweaksMod.LOGGER.info("moveStack in ${operationInfo.clickedSI.screenHandler}")
        InvTweaksMod.LOGGER.info("clickedSlot: ${operationInfo.clickedSlot}; class: ${operationInfo.clickedSlot.javaClass}")
        if (InventoryUtils.isCraftingOutputSlot(operationInfo.clickedSlot)) {
            return pass("Use vanilla crafting for moveStack in crafting output slot")
        }

        val handler = operationInfo.clickedSI.screenHandler
        val from = operationInfo.clickedSlot.id
        val stack = operationInfo.clickedSlot.stack
        moveToInventory(handler, from, operationInfo.targetSI, stack.count, false)
        return SUCCESS
    }

    override fun dropStack(operationInfo: OperationInfo): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        MinecraftClient.getInstance().interactionManager!!.clickSlot(operationInfo.clickedSI.screenHandler.syncId, operationInfo.clickedSlot.id, 1, SlotActionType.THROW, playerEntity)
        return SUCCESS
    }

    override fun craftOne(operationInfo: OperationInfo?): OperationResult {
        return FAILURE
    }

    override fun craftStack(operationInfo: OperationInfo?): OperationResult {
        return FAILURE
    }

    override fun craftAll(operationInfo: OperationInfo?): OperationResult {
        return FAILURE
    }

    override fun craftAllSameType(operationInfo: OperationInfo?): OperationResult {
        return FAILURE
    }
}