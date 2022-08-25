package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class InvTweaksVanillaPlayerBehaviour extends InvTweaksVanillaGenericBehavior {
    @Override
    protected int moveToSlot(ScreenHandler handler, int maxSlot, int fromSlotId, int toSlotId, int quantity, boolean sorting) {
        return super.moveToSlot(handler, maxSlot, fromSlotId, toSlotId, quantity, sorting);
    }

    @Override
    protected int moveToInventory(ScreenHandler handler, int fromSlot, ScreenInventory destinationBoundInfo, int quantity, boolean sorting) {
        return super.moveToInventory(handler, fromSlot, destinationBoundInfo, quantity, sorting);
    }

    @Override
    public void sort(InvTweaksOperationInfo operationInfo) {
        super.sort(operationInfo);
    }

    @Override
    public void moveAll(InvTweaksOperationInfo operationInfo) {
        super.moveAll(operationInfo);
    }

    @Override
    public void dropAll(InvTweaksOperationInfo operationInfo) {
        super.dropAll(operationInfo);
    }

    @Override
    public void moveAllSameType(InvTweaksOperationInfo operationInfo) {
        super.moveAllSameType(operationInfo);
    }

    @Override
    public void dropAllSameType(InvTweaksOperationInfo operationInfo) {
        super.dropAllSameType(operationInfo);
    }

    @Override
    public void moveOne(InvTweaksOperationInfo operationInfo) {
        super.moveOne(operationInfo);
    }

    @Override
    public void dropOne(InvTweaksOperationInfo operationInfo) {
        super.dropOne(operationInfo);
    }

    @Override
    public void dropStack(InvTweaksOperationInfo operationInfo) {
        super.dropStack(operationInfo);
    }

    boolean testIfArmorPiece(InvTweaksOperationInfo operationInfo, ItemStack itemStack) {
        ScreenHandler screenHandler = operationInfo.clickedSI().screenHandler();
        if (!(screenHandler instanceof PlayerScreenHandler)) return false;

        ScreenInventory armorInv = new ScreenInventory(screenHandler, 5, 8);

        boolean moveableToArmorInv = false;
        for (int slotId = armorInv.start(); slotId <= armorInv.end(); slotId++) {
            Slot slot = screenHandler.getSlot(slotId);
            if (slot.canInsert(itemStack))
            {
                moveableToArmorInv = true;
                break;
            }
        }

        return moveableToArmorInv;
    }

    @Override
    public void moveStack(InvTweaksOperationInfo operationInfo) {
        ItemStack itemStack = operationInfo.clickedSlot().getStack();

        ScreenHandler screenHandler = operationInfo.clickedSI().screenHandler();
        //Keep the same behavior for armor
        if (testIfArmorPiece(operationInfo, itemStack)) {
            int clickedSlotId = operationInfo.clickedSlot().id;
            MinecraftClient.getInstance().interactionManager.clickSlot(screenHandler.syncId, clickedSlotId, 0, SlotActionType.QUICK_MOVE, MinecraftClient.getInstance().player);
            return; //Minecraft default behavior
        }
        else {
            super.moveStack(operationInfo);
        }
    }
}
