package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.SUCCESS
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import net.minecraft.client.MinecraftClient
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.SlotActionType

open class InvTweaksVanillaGenericBehavior : IInvTweaksBehavior {

    private val sortingComparator: Comparator<Item>
        get() = Comparator.comparing { item: Item ->
            val sb = StringBuilder()
            sb.append(if (item.group != null) item.group!!.name else "")
            sb.append(item.name)
            sb.append(item.maxCount)
            sb.toString()
        }

    override fun sort(intent: Intent): OperationResult {
        if (intent.context.clickedSlot.inventory is CraftingInventory)
            return com.marcuscastelo.invtweaks.behavior.CraftHelper.spreadItemsInPlace(intent.context.clickedSI)

        val handler = intent.context.clickedSI.screenHandler
        val startSlot = intent.context.clickedSI.start
        val endSlot = intent.context.clickedSI.end
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
            while (destinationSlot < endSlot && handler.slots[destinationSlot].stack.item === item) destinationSlot++
        }
        return SUCCESS
    }

    override fun moveAll(intent: Intent): OperationResult {
        for (slotId in intent.context.clickedSI.start..intent.context.clickedSI.end) {
            val stack = intent.context.clickedSI.screenHandler.getSlot(slotId).stack
            if (stack.item === Items.AIR) continue
            val result = Jesus.moveToInventory(intent.context.clickedSI.screenHandler, slotId, intent.context.targetSI, stack.count, false)
            if (result == Jesus.MOVE_RESULT_FULL) break
        }
        return SUCCESS
    }

    override fun dropAll(intent: Intent): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        for (slotId in intent.context.clickedSI.start..intent.context.clickedSI.end) {
            MinecraftClient.getInstance().interactionManager!!.clickSlot(intent.context.clickedSI.screenHandler.syncId, slotId, 1, SlotActionType.THROW, playerEntity)
        }
        return SUCCESS
    }

    override fun moveAllSameType(intent: Intent): OperationResult {
        if (intent.context.clickedSlot is CraftingResultSlot)
            return com.marcuscastelo.invtweaks.behavior.CraftHelper.massCraft(intent.context.clickedSlot, intent)

        val itemType = intent.context.clickedSlot.stack.item
        for (slot in intent.context.clickedSI.start..intent.context.clickedSI.end) {
            val stack = intent.context.clickedSI.screenHandler.slots[slot].stack
            if (stack.item !== itemType) continue
            val result = Jesus.moveToInventory(intent.context.clickedSI.screenHandler, slot, intent.context.targetSI, stack.count, false)
            if (result == Jesus.MOVE_RESULT_FULL) break
        }
        return SUCCESS
    }

    override fun dropAllSameType(intent: Intent): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        val itemType = intent.context.clickedSlot.stack.item
        for (slot in intent.context.clickedSI.start..intent.context.clickedSI.end) {
            val stack = intent.context.clickedSI.screenHandler.slots[slot].stack
            if (stack.item !== itemType) continue
            MinecraftClient.getInstance().interactionManager!!.clickSlot(intent.context.clickedSI.screenHandler.syncId, slot, 1, SlotActionType.THROW, playerEntity)
        }
        return SUCCESS
    }

    override fun moveOne(intent: Intent): OperationResult {
        val handler = intent.context.clickedSI.screenHandler
        val from = intent.context.clickedSlot.id
        val result = Jesus.moveToInventory(handler, from, intent.context.targetSI, 1, false)
        return SUCCESS
    }

    override fun dropOne(intent: Intent): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        MinecraftClient.getInstance().interactionManager!!.clickSlot(intent.context.clickedSI.screenHandler.syncId, intent.context.clickedSlot.id, 0, SlotActionType.THROW, playerEntity)
        return SUCCESS
    }

    override fun moveStack(intent: Intent): OperationResult {
        com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info("moveStack in ${intent.context.clickedSI.screenHandler}")
        com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info("clickedSlot: ${intent.context.clickedSlot}; class: ${intent.context.clickedSlot.javaClass}")
        if (intent.context.clickedSlot is CraftingResultSlot) {
            return pass("Use vanilla crafting for moveStack in crafting output slot")
        }

        val handler = intent.context.clickedSI.screenHandler
        val from = intent.context.clickedSlot.id
        val stack = intent.context.clickedSlot.stack
        Jesus.moveToInventory(handler, from, intent.context.targetSI, stack.count, false)
        return SUCCESS
    }

    override fun dropStack(intent: Intent): OperationResult {
        val playerEntity = MinecraftClient.getInstance().player
        MinecraftClient.getInstance().interactionManager!!.clickSlot(intent.context.clickedSI.screenHandler.syncId, intent.context.clickedSlot.id, 1, SlotActionType.THROW, playerEntity)
        return SUCCESS
    }

    override fun craftOne(intent: Intent?): OperationResult {
        return FAILURE
    }

    override fun craftStack(intent: Intent?): OperationResult {
        return FAILURE
    }

    override fun craftAll(intent: Intent?): OperationResult {
        return FAILURE
    }

    override fun craftAllSameType(intent: Intent?): OperationResult {
        return FAILURE
    }
}