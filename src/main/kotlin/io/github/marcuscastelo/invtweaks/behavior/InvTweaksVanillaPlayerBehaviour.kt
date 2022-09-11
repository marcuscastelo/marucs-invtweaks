package io.github.marcuscastelo.invtweaks.behavior

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import io.github.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import org.lwjgl.glfw.GLFW

class InvTweaksVanillaPlayerBehaviour : InvTweaksVanillaGenericBehavior() {
    private fun isArmorSlot(slotId: Int): Boolean {
        return slotId in 5..8
    }

    override fun moveToSlot(handler: ScreenHandler, maxSlot: Int, fromSlotId: Int, toSlotId: Int, quantity: Int, sorting: Boolean): Int {
        return super.moveToSlot(handler, maxSlot, fromSlotId, toSlotId, quantity, sorting)
    }

    override fun moveToInventory(handler: ScreenHandler, fromSlot: Int, destinationBoundInfo: ScreenInventory, quantity: Int, sorting: Boolean): Int {
        return super.moveToInventory(handler, fromSlot, destinationBoundInfo, quantity, sorting)
    }

    override fun sort(operationInfo: OperationInfo): OperationResult {
        //Do not sort armor
        return if (isArmorSlot(operationInfo.clickedSlot.id)) FAILURE else super.sort(operationInfo)
    }

    override fun moveAll(operationInfo: OperationInfo): OperationResult {
        return super.moveAll(operationInfo)
    }

    override fun dropAll(operationInfo: OperationInfo): OperationResult {
        return super.dropAll(operationInfo)
    }

    override fun moveAllSameType(operationInfo: OperationInfo): OperationResult {
        return super.moveAllSameType(operationInfo)
    }

    override fun dropAllSameType(operationInfo: OperationInfo): OperationResult {
        return super.dropAllSameType(operationInfo)
    }

    override fun moveOne(operationInfo: OperationInfo): OperationResult {
        return super.moveOne(operationInfo)
    }

    override fun dropOne(operationInfo: OperationInfo): OperationResult {
        return super.dropOne(operationInfo)
    }

    override fun dropStack(operationInfo: OperationInfo): OperationResult {
        return super.dropStack(operationInfo)
    }

    fun isMoveableToArmorSlot(operationInfo: OperationInfo, itemStack: ItemStack?): Boolean {
        val screenHandler = operationInfo.clickedSI.screenHandler as? PlayerScreenHandler ?: return false
        val (_, start, end) = ScreenInventory(screenHandler, 5, 8)
        var moveableToArmorInv = false
        for (slotId in start..end) {
            val slot = screenHandler.getSlot(slotId)
            if (slot.stack.isEmpty && slot.canInsert(itemStack)) {
                moveableToArmorInv = true
                break
            }
        }
        return moveableToArmorInv
    }

    override fun moveStack(operationInfo: OperationInfo): OperationResult {
        var operationInfo = operationInfo
        val itemStack = operationInfo.clickedSlot.stack
        val screenHandler = operationInfo.clickedSI.screenHandler
        assert(screenHandler is PlayerScreenHandler)
        //Keep the same behavior for armor
        val isDownwardsMovement = isKeyPressed(GLFW.GLFW_KEY_S)
        val isClickInArmorOrCraft = operationInfo.clickedSI.start <= 8
        if (!isDownwardsMovement && isMoveableToArmorSlot(operationInfo, itemStack) && !isClickInArmorOrCraft) {
            val armorInv = ScreenInventory(screenHandler, 5, 8)
            operationInfo = OperationInfo(operationInfo.type, operationInfo.clickedSlot, operationInfo.clickedSI, armorInv, operationInfo.otherInventories)
        }

//            int clickedSlotId = operationInfo.clickedSlot().id;
//            MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, clickedSlotId, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
//            return; //Minecraft default behavior
        return super.moveStack(operationInfo)
    }
}