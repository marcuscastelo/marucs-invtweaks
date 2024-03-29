package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import com.marcuscastelo.invtweaks.operation.OperationInfo
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.failure
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import com.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import org.lwjgl.glfw.GLFW

class InvTweaksVanillaPlayerBehaviour : InvTweaksVanillaGenericBehavior() {
    private fun isArmorSlot(slotId: Int): Boolean {
        return slotId in 5..8
    }

    override fun sort(operationInfo: OperationInfo): OperationResult {
        //Do not sort armor
        return if (isArmorSlot(operationInfo.clickedSlot.id)) FAILURE else super.sort(operationInfo)
    }

    override fun moveAllSameType(operationInfo: OperationInfo): OperationResult {
        return super.moveAllSameType(operationInfo)
    }

    fun isMoveableToArmorSlot(operationInfo: OperationInfo, itemStack: ItemStack?): Boolean {
        val screenHandler = operationInfo.clickedSI.screenHandler
        val armorInv = ScreenInventory(screenHandler, 5, 8)
        var moveableToArmorInv = false
        for (slotId in armorInv.slotRange) {
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
        val isTrendedMovement = isKeyPressed(GLFW.GLFW_KEY_S) || isKeyPressed(GLFW.GLFW_KEY_W)
        val isClickInArmorOrCraft = operationInfo.clickedSI.start <= 8
        if (!isTrendedMovement && isMoveableToArmorSlot(operationInfo, itemStack) && !isClickInArmorOrCraft) {
            return pass("Using vanilla behavior for armor")
        }

//            int clickedSlotId = operationInfo.clickedSlot().id;
//            MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, clickedSlotId, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
//            return; //Minecraft default behavior
        return super.moveStack(operationInfo)
    }
}