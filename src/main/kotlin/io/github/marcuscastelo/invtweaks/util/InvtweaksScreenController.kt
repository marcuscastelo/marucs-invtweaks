package io.github.marcuscastelo.invtweaks.util

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.InvTweaksMod.Companion.LOGGER
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType

class InvtweaksScreenController(private val handler: ScreenHandler) {
    private val interaction: ClientPlayerInteractionManager = MinecraftClient.getInstance().interactionManager?: throw AssertionError("InteractionManager is null")
    private val player: ClientPlayerEntity = MinecraftClient.getInstance().player?: throw AssertionError("Player is null")

    val isHoldingStack: Boolean get() = !heldStack.isEmpty
    val heldStack: ItemStack get() = handler.cursorStack.copy()

    fun pickStack(slot: Int) {
        if (!heldStack.isEmpty) {
            LOGGER.error("<ScreenController> Cannot pick stack, there is already a stack in hand!")
            return
        }
        leftClick(slot, SlotActionType.PICKUP)
    }

    fun placeStack(slot: Int) {
        if (heldStack.isEmpty) {
            LOGGER.error("<ScreenController> Cannot place stack, there is no stack in hand!")
            return
        }
        leftClick(slot, SlotActionType.PICKUP)
    }

    fun quickMove(slot: Int) {
        click(slot, 0, SlotActionType.QUICK_MOVE)
    }

    fun placeSome(slot: Int, quantity: Int) {
        //TODO: if quantity == getHeldStack().getCount() then we can just place the stack
        for (i in 0 until quantity) {
            placeOne(slot)
        }
    }

    fun placeOne(slot: Int) {
        if (heldStack.isEmpty) {
            LOGGER.error("<ScreenController> Cannot place stack, there is no stack in hand!")
            return
        }
        val heldBefore = heldStack.copy()
        rightClick(slot, SlotActionType.PICKUP)
        val heldAfter = heldStack.copy()
        if (heldBefore == heldAfter) {
            LOGGER.error("<ScreenController> Big problem! The stack in hand didn't change after placing one item!")
            ChatUtils.warnPlayer("There was a problem placing one item! Please report this to the mod author!")
            return
        }
    }

    fun move(from: Int, to: Int) {
        if (isEmpty(from)) return
        if (isEmpty(to)) {
            pickStack(from)
            placeStack(to)
        }
    }

    fun move(from: Int, to: Int, quantity: Int) {
        if (isEmpty(from)) return
        val fromCount = getStack(from).count
        if (quantity > fromCount) return
        val fromStack = getStack(from)
        val toStack = getStack(to)
        if (!ItemStackUtils.canStacksMergeNoOverflow(fromStack, toStack)) return
        pickStack(from)
        placeSome(to, quantity)
        if (!heldStack.isEmpty) placeStack(from)
    }

    fun dropOne(slot: Int) {
        //TODO: check if held stack is empty?
        if (isEmpty(slot)) {
            InvTweaksMod.LOGGER.warn("<ScreenController> Cannot dropOne stack, slot is empty!")
            //            return;
        }
        leftClick(slot, SlotActionType.THROW)
    }

    fun dropAll(slot: Int) {
        //TODO: check if held stack is empty?
        if (isEmpty(slot)) {
            InvTweaksMod.LOGGER.warn("<ScreenController> Cannot dropAll stack, slot is empty!")
            //            return;
        }
        rightClick(slot, SlotActionType.THROW)
    }

    fun isEmpty(slot: Int): Boolean {
        return getStack(slot).isEmpty
    }

    fun getItem(slot: Int): Item {
        return getStack(slot).item
    }

    fun getStack(slot: Int): ItemStack {
        return handler.stacks[slot].copy()
//                return handler.getSlot(slot).getStack().copy();
    }

    fun dropHeldStack() {
        if (isHoldingStack) {
            leftClick(-999, SlotActionType.PICKUP)
        }
    }

    private fun leftClick(slot: Int, actionType: SlotActionType) {
        click(slot, 0, actionType)
    }

    private fun rightClick(slot: Int, actionType: SlotActionType) {
        click(slot, 1, actionType)
    }

    private fun click(slot: Int, mouseButton: Int, actionType: SlotActionType) {
        interaction.clickSlot(handler.syncId, slot, mouseButton, actionType, player)
    }
}