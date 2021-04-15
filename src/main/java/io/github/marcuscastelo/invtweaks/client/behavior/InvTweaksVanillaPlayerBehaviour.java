package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
import net.minecraft.screen.ScreenHandler;

public class InvTweaksVanillaPlayerBehaviour extends InvTweaksVanillaGenericBehavior {
    @Override
    protected int moveToSlot(ScreenHandler handler, int maxSlot, int fromSlotId, int toSlotId, int quantity, boolean sorting) {
        return super.moveToSlot(handler, maxSlot, fromSlotId, toSlotId, quantity, sorting);
    }

    @Override
    protected int moveToInventory(ScreenHandler handler, int fromSlot, InventoryContainerBoundInfo destinationBoundInfo, int quantity, boolean sorting) {
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
}
