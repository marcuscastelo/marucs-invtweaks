package io.github.marcuscastelo.invtweaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class InvtweaksScreenController {
    private final ScreenHandler handler;
    private final ClientPlayerInteractionManager interaction;
    private final ClientPlayerEntity player;

    public InvtweaksScreenController(ScreenHandler handler) {
        this.handler = handler;
        this.interaction = MinecraftClient.getInstance().interactionManager;
        if (this.interaction == null) throw new RuntimeException("MinecraftClient.getInstance().interactionManager is null!");
        this.player = MinecraftClient.getInstance().player;
        if (this.player == null) throw new RuntimeException("MinecraftClient.getInstance().interactionManager is null!");
    }

    public boolean isHoldingStack() {
        return !getHeldStack().isEmpty();
    }

    public ItemStack getHeldStack() {
        return handler.getCursorStack();
    }

    public void pickStack(int slot) {
        if (!getHeldStack().isEmpty()) {
            System.err.println("[InvTweaks] Cannot pick stack, there is already a stack in hand!");
            return;
        }
        leftClick(slot, SlotActionType.PICKUP);
    }

    public void placeStack(int slot) {
        if (getHeldStack().isEmpty()) {
            System.err.println("[InvTweaks] Cannot place stack, there is no stack in hand!");
            return;
        }
        leftClick(slot, SlotActionType.PICKUP);
    }

    public void craftAll(int resultSlot) {
        click(resultSlot, 0, SlotActionType.QUICK_MOVE);
    }

    public void placeSome(int slot, int quantity) {
        //TODO: if quantity == getHeldStack().getCount() then we can just place the stack
        for (int i = 0; i < quantity; i ++) {
            placeOne(slot);
        }
    }

    public void placeOne(int slot) {
        if (getHeldStack().isEmpty()) {
            System.err.println("[InvTweaks] Cannot place stack, there is no stack in hand!");
            return;
        }
        rightClick(slot, SlotActionType.PICKUP);
    }

    public void move(int from, int to) {
        if (isEmpty(from)) return;

        if (isEmpty(to)) {
            pickStack(from);
            placeStack(to);
        }
    }

    public void move(int from, int to, int quantity) {
        if (isEmpty(from))
            return;

        int fromCount = getStack(from).getCount();
        if (quantity > fromCount)
            return;

        ItemStack fromStack, toStack;
        fromStack = getStack(from);
        toStack = getStack(to);

        if (!ItemStackUtils.canStacksMergeNoOverflow(fromStack, toStack))
            return;

        pickStack(from);
        placeSome(to, quantity);

        if (!getHeldStack().isEmpty())
            placeStack(from);
    }

    public void dropOne(int slot) {
        if (isEmpty(slot)) return;
        rightClick(slot, SlotActionType.THROW);
    }

    public void dropAll(int slot) {
        if (isEmpty(slot)) return;
        leftClick(slot, SlotActionType.THROW);
    }

    public boolean isEmpty(int slot) {
        return getStack(slot).isEmpty();
    }

    public Item getItem(int slot) {
        return getStack(slot).getItem();
    }

    public ItemStack getStack(int slot) {
        return handler.getSlot(slot).getStack().copy();
    }

    private void leftClick(int slot, SlotActionType actionType) {
        click(slot, 0, actionType);
    }

    private void rightClick(int slot, SlotActionType actionType) {
        click(slot, 1, actionType);
    }

    private void click(int slot, int mouseButton, SlotActionType actionType) {
        interaction.clickSlot(handler.syncId, slot, mouseButton, actionType, player);
        handler.onSlotClick(slot, mouseButton, actionType, player);
    }

}
