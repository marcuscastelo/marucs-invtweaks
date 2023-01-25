package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import com.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import org.lwjgl.glfw.GLFW

class InvTweaksVanillaPlayerBehaviour : InvTweaksVanillaGenericBehavior() {
    private fun isArmorSlot(slotId: Int): Boolean {
        return slotId in 5..8
    }

    override fun sort(intent: Intent): OperationResult {
        //Do not sort armor
        return if (isArmorSlot(intent.context.clickedSlot.id)) FAILURE else super.sort(intent)
    }

    override fun moveAllSameType(intent: Intent): OperationResult {
        return super.moveAllSameType(intent)
    }

    fun isMoveableToArmorSlot(intent: Intent, itemStack: ItemStack?): Boolean {
        val screenHandler = intent.context.clickedSI.screenHandler
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

    override fun moveStack(intent: Intent): OperationResult {
        val itemStack = intent.context.clickedSlot.stack
        val screenHandler = intent.context.clickedSI.screenHandler
        assert(screenHandler is PlayerScreenHandler)
        //Keep the same behavior for armor
        val isTrendedMovement = isKeyPressed(GLFW.GLFW_KEY_S) || isKeyPressed(GLFW.GLFW_KEY_W)
        val isClickInArmorOrCraft = intent.context.clickedSI.start <= 8
        if (!isTrendedMovement && isMoveableToArmorSlot(intent, itemStack) && !isClickInArmorOrCraft) {
            return pass("Using vanilla behavior for armor")
        }

//            int clickedSlotId = operationInfo.clickedSlot().id;
//            MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, clickedSlotId, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
//            return; //Minecraft default behavior
        return super.moveStack(intent)
    }
}