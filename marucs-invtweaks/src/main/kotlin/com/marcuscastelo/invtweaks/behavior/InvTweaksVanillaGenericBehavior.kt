package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.InvTweaksMod
import com.marcuscastelo.invtweaks.behavior.Jesus
import com.marcuscastelo.invtweaks.operation.OperationInfo
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.SUCCESS
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import com.marcuscastelo.invtweaks.util.InventoryUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.SlotActionType

open class InvTweaksVanillaGenericBehavior : IInvTweaksBehavior {

    protected val sortingComparator: Comparator<Item>
        protected get() = Comparator.comparing { item: Item ->
            val sb = StringBuilder()
            sb.append(if (item.group != null) item.group!!.name else "")
            sb.append(item.name)
            sb.append(item.maxCount)
            sb.toString()
        }

    override fun sort(operationInfo: OperationInfo): OperationResult {
        if (operationInfo.clickedSlot.inventory is CraftingInventory)
            return com.marcuscastelo.invtweaks.behavior.CraftHelper.spreadItemsInPlace(operationInfo.clickedSI)

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
                val placedAt = Jesus.moveToSlot(handler, endSlot, fromSlot, destinationSlot, stack.count, true)
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
            val result = Jesus.moveToInventory(operationInfo.clickedSI.screenHandler, slotId, operationInfo.targetSI, stack.count, false)
            if (result == Jesus.MOVE_RESULT_FULL) break
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
        if (operationInfo.clickedSlot is CraftingResultSlot)
            return com.marcuscastelo.invtweaks.behavior.CraftHelper.massCraft(operationInfo.clickedSlot, operationInfo)

        val itemType = operationInfo.clickedSlot.stack.item
        for (slot in operationInfo.clickedSI.start..operationInfo.clickedSI.end) {
            val stack = operationInfo.clickedSI.screenHandler.slots[slot].stack
            if (stack.item !== itemType) continue
            val result = Jesus.moveToInventory(operationInfo.clickedSI.screenHandler, slot, operationInfo.targetSI, stack.count, false)
            if (result == Jesus.MOVE_RESULT_FULL) break
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
        val result = Jesus.moveToInventory(handler, from, operationInfo.targetSI, 1, false)
        return SUCCESS
    }

    override fun dropOne(operationInfo: OperationInfo): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        MinecraftClient.getInstance().interactionManager!!.clickSlot(operationInfo.clickedSI.screenHandler.syncId, operationInfo.clickedSlot.id, 0, SlotActionType.THROW, playerEntity)
        return SUCCESS
    }

    override fun moveStack(operationInfo: OperationInfo): OperationResult {


        com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info("moveStack in ${operationInfo.clickedSI.screenHandler}")
        com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info("clickedSlot: ${operationInfo.clickedSlot}; class: ${operationInfo.clickedSlot.javaClass}")
        if (operationInfo.clickedSlot is CraftingResultSlot) {
            return pass("Use vanilla crafting for moveStack in crafting output slot")
        }

        val handler = operationInfo.clickedSI.screenHandler
        val from = operationInfo.clickedSlot.id
        val stack = operationInfo.clickedSlot.stack
        Jesus.moveToInventory(handler, from, operationInfo.targetSI, stack.count, false)
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