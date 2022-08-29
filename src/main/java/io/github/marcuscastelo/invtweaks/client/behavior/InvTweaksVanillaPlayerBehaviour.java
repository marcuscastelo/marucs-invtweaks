package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.OperationResult;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

import static io.github.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed;

public class InvTweaksVanillaPlayerBehaviour extends InvTweaksVanillaGenericBehavior {
    private boolean isArmorSlot(int slotId) { return slotId >= 5 && slotId <= 8; }

    @Override
    protected int moveToSlot(ScreenHandler handler, int maxSlot, int fromSlotId, int toSlotId, int quantity, boolean sorting) {
        return super.moveToSlot(handler, maxSlot, fromSlotId, toSlotId, quantity, sorting);
    }

    @Override
    protected int moveToInventory(ScreenHandler handler, int fromSlot, ScreenInventory destinationBoundInfo, int quantity, boolean sorting) {
        return super.moveToInventory(handler, fromSlot, destinationBoundInfo, quantity, sorting);
    }

    @Override
    public OperationResult sort(InvTweaksOperationInfo operationInfo) {
        //Do not sort armor
        if (isArmorSlot(operationInfo.clickedSlot().id))
            return new OperationResult(false);

        return super.sort(operationInfo);
    }

    @Override
    public OperationResult moveAll(InvTweaksOperationInfo operationInfo) {
        return super.moveAll(operationInfo);
    }

    @Override
    public OperationResult dropAll(InvTweaksOperationInfo operationInfo) {
        return super.dropAll(operationInfo);
    }

    @Override
    public OperationResult moveAllSameType(InvTweaksOperationInfo operationInfo) {
        return super.moveAllSameType(operationInfo);
    }

    @Override
    public OperationResult dropAllSameType(InvTweaksOperationInfo operationInfo) {
        return super.dropAllSameType(operationInfo);
    }

    @Override
    public OperationResult moveOne(InvTweaksOperationInfo operationInfo) {
        return super.moveOne(operationInfo);
    }

    @Override
    public OperationResult dropOne(InvTweaksOperationInfo operationInfo) {
        return super.dropOne(operationInfo);
    }

    @Override
    public OperationResult dropStack(InvTweaksOperationInfo operationInfo) {
        return super.dropStack(operationInfo);
    }

    boolean isMoveableToArmorSlot(InvTweaksOperationInfo operationInfo, ItemStack itemStack) {
        ScreenHandler screenHandler = operationInfo.clickedSI().screenHandler();
        if (!(screenHandler instanceof PlayerScreenHandler)) return false;

        ScreenInventory armorInv = new ScreenInventory(screenHandler, 5, 8);

        boolean moveableToArmorInv = false;
        for (int slotId = armorInv.start(); slotId <= armorInv.end(); slotId++) {
            Slot slot = screenHandler.getSlot(slotId);
            if (slot.getStack().isEmpty() && slot.canInsert(itemStack))
            {
                moveableToArmorInv = true;
                break;
            }
        }

        return moveableToArmorInv;
    }

    @Override
    public OperationResult moveStack(InvTweaksOperationInfo operationInfo) {
        ItemStack itemStack = operationInfo.clickedSlot().getStack();

        ScreenHandler screenHandler = operationInfo.clickedSI().screenHandler();
        assert screenHandler instanceof PlayerScreenHandler;
        //Keep the same behavior for armor
        boolean isDownwardsMovement = isKeyPressed(GLFW.GLFW_KEY_S);
        boolean isClickInArmorOrCraft = operationInfo.clickedSI().start() <= 8;
        if (!isDownwardsMovement && (isMoveableToArmorSlot(operationInfo, itemStack) && !isClickInArmorOrCraft)) {
            ScreenInventory armorInv = new ScreenInventory(screenHandler, 5, 8);
            operationInfo = new InvTweaksOperationInfo(operationInfo.type(), operationInfo.clickedSlot(), operationInfo.clickedSI(), armorInv, operationInfo.otherInventories());
        }

//            int clickedSlotId = operationInfo.clickedSlot().id;
//            MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, clickedSlotId, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
//            return; //Minecraft default behavior

        return super.moveStack(operationInfo);
    }
}
