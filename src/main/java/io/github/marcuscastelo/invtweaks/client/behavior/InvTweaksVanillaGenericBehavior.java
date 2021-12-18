package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class InvTweaksVanillaGenericBehavior implements IInvTweaksBehavior {
    public final int MOVERESULT_FULL = -3;


    protected int moveToSlot(ScreenHandler handler, int maxSlot, int fromSlotId, int toSlotId, int quantity, boolean sorting) {
        ItemStack initialStack = handler.getSlot(fromSlotId).getStack().copy();
        int initialCount = initialStack.getCount();
        if (quantity > initialCount) {
            System.out.println("Trying to move more than we have InvTweaksVanillaBehavior@moveToSlot");
            return -1;
        }

        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (interactionManager == null || player == null) {
            System.out.println("nullptr in InvTweaksVanillaBehavior@moveSome");
            return -2;
        }

        //Item in hand
        interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player);
        ItemStack currentHeldStack = handler.getCursorStack();

        int remainingTotalClicks = quantity;
        int candidateDestination = toSlotId;
        for (;remainingTotalClicks > 0 && candidateDestination <= maxSlot; candidateDestination++) {

            if (!handler.canInsertIntoSlot(currentHeldStack, handler.slots.get(candidateDestination))) continue;

            ItemStack candidateDstStack = handler.slots.get(candidateDestination).getStack();

            if (candidateDstStack.getItem() != initialStack.getItem()) {
                if (candidateDstStack.getItem() == Items.AIR) {
                    //If air, just put
                    int rightClicks = MathHelper.clamp(remainingTotalClicks, 0, initialStack.getMaxCount());
                    if (rightClicks == initialStack.getMaxCount()) {
                        interactionManager.clickSlot(handler.syncId,candidateDestination,0,SlotActionType.PICKUP,player);
                    }
                    else {
                        //TODO: send one packet, instead of a flood
                        for (int i = 0; i < rightClicks; i++) {
                            interactionManager.clickSlot(handler.syncId, candidateDestination, 1, SlotActionType.PICKUP, player);
                        }
                    }
                    remainingTotalClicks -= rightClicks;
                    continue;
                }
                else {
                    //If slot is occuppied
                    if (sorting) {
                        int dumpWrongStackSlot = candidateDestination + 1;
                        while (handler.slots.get(dumpWrongStackSlot).getStack().getItem() != Items.AIR && dumpWrongStackSlot <= maxSlot) dumpWrongStackSlot++;

                        if (dumpWrongStackSlot > maxSlot) { //If there's no more room
                            //Returns remaining
                            interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player);
                            currentHeldStack = handler.getCursorStack();
                            return MOVERESULT_FULL;
                        }

                        //Swap right and wrong
                        interactionManager.clickSlot(handler.syncId, candidateDestination, 0, SlotActionType.PICKUP, player);
                        currentHeldStack = handler.getCursorStack();

                        //Dump wrong
                        interactionManager.clickSlot(handler.syncId, dumpWrongStackSlot, 0, SlotActionType.PICKUP, player);
                        currentHeldStack = handler.getCursorStack();

                        return candidateDestination;
                    }
                    else continue;
                }
            }

            //If same type:

            int clicksToCompleteStack = candidateDstStack.getMaxCount() - candidateDstStack.getCount();
            int rightClicks = MathHelper.clamp(remainingTotalClicks, 0, clicksToCompleteStack);

            if (rightClicks > 0 && remainingTotalClicks >= clicksToCompleteStack) {
                interactionManager.clickSlot(handler.syncId, candidateDestination, 0, SlotActionType.PICKUP, player);
                currentHeldStack = handler.getCursorStack();
            }
            else for (int i = 0; i < rightClicks; i++) {
                interactionManager.clickSlot(handler.syncId, candidateDestination, 1, SlotActionType.PICKUP, player);
                currentHeldStack = handler.getCursorStack();
            }

            remainingTotalClicks -= rightClicks;
        }

        if (remainingTotalClicks > 0)
            interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player);

        currentHeldStack = handler.getCursorStack();
        if (!currentHeldStack.isEmpty())
            interactionManager.clickSlot(handler.syncId, fromSlotId, 0, SlotActionType.PICKUP, player);

        if (candidateDestination > toSlotId) candidateDestination--;
        return candidateDestination;
    }

    protected int moveToInventory(ScreenHandler handler, int fromSlot, ScreenInventory destinationBoundInfo, int quantity, boolean sorting) {
        int destinationStart = destinationBoundInfo.start();

        int inventorySize = destinationBoundInfo.getSize();
        return moveToSlot(handler, destinationStart+inventorySize-1, fromSlot, destinationStart, quantity, sorting);
    }

    protected Comparator<Item> getSortingComparator() {
        return Comparator.comparing(
                item -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(item.getGroup() != null ? item.getGroup().getName() : "");
                    sb.append(item.getName());
                    sb.append(item.getMaxCount());
                    return sb.toString();
                }
        );
    }

    @Override
    public void sort(InvTweaksOperationInfo operationInfo) {
        ScreenHandler handler = operationInfo.clickedSI().screenHandler();
        int startSlot = operationInfo.clickedSI().start();
        int endSlot = operationInfo.clickedSI().end();

        Set<Item> items = new HashSet<>();
        HashMap<Item, Integer> quantityPerItem = new HashMap<>();
        for (int slot = startSlot; slot <= endSlot; slot++) {
            ItemStack stack = handler.slots.get(slot).getStack();
            Item item = stack.getItem();
            if (item == Items.AIR) continue;
            quantityPerItem.put(item, quantityPerItem.getOrDefault(item, 0) + stack.getCount());
            items.add(item);
        }
        List<Item> itemsArray = new ArrayList<>(items);
        itemsArray.sort(getSortingComparator());

        int destinationSlot = startSlot;
        for (Item item : itemsArray) {
            if (item == Items.AIR) continue;
            int movedItems = 0;
            int totalItems = quantityPerItem.get(item);
            for (int fromSlot = startSlot; fromSlot <= endSlot; fromSlot++) {
                if (movedItems >= totalItems) break;
                ItemStack stack = handler.slots.get(fromSlot).getStack();

                if (stack.getItem() != item) continue;
                int placedAt = moveToSlot(handler, endSlot, fromSlot, destinationSlot, stack.getCount(), true);
                destinationSlot = Math.max(destinationSlot, placedAt);
                if (placedAt > 0) {
                    movedItems += stack.getCount();
                    if (handler.slots.get(placedAt).getStack().getItem() != item)
                        destinationSlot++; //If diferent, no merging attempt is needed TODO: check if best approach
                }
            }
            while (handler.slots.get(destinationSlot).getStack().getItem() == item) destinationSlot++;
        }
    }

    @Override
    public void moveAll(InvTweaksOperationInfo operationInfo) {
        for (int slotId = operationInfo.clickedSI().start(); slotId <= operationInfo.clickedSI().end(); slotId++) {
            ItemStack stack = operationInfo.clickedSI().screenHandler().getSlot(slotId).getStack();
            if (stack.getItem() == Items.AIR) continue;
            int result = moveToInventory(operationInfo.clickedSI().screenHandler(), slotId, operationInfo.otherSI(), stack.getCount(), false);
            if (result == MOVERESULT_FULL) break;
        }
    }

    @Override
    public void dropAll(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        for (int slotId = operationInfo.clickedSI().start(); slotId <= operationInfo.clickedSI().end(); slotId++) {
            MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedSI().screenHandler().syncId, slotId, 1, SlotActionType.THROW, playerEntity);
        }
    }

    @Override
    public void moveAllSameType(InvTweaksOperationInfo operationInfo) {
        Item itemType = operationInfo.clickedSlot().getStack().getItem();
        for (int slot = operationInfo.clickedSI().start(); slot <= operationInfo.clickedSI().end(); slot++) {
            ItemStack stack = operationInfo.clickedSI().screenHandler().slots.get(slot).getStack();
            if (stack.getItem() != itemType) continue;

            int result = moveToInventory(operationInfo.clickedSI().screenHandler(), slot, operationInfo.otherSI(), stack.getCount(), false);
            if (result == MOVERESULT_FULL) break;
        }
    }

    @Override
    public void dropAllSameType(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        Item itemType = operationInfo.clickedSlot().getStack().getItem();
        for (int slot = operationInfo.clickedSI().start(); slot <= operationInfo.clickedSI().end(); slot++) {
            ItemStack stack = operationInfo.clickedSI().screenHandler().slots.get(slot).getStack();
            if (stack.getItem() != itemType) continue;
            MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedSI().screenHandler().syncId, slot, 1, SlotActionType.THROW, playerEntity);
        }
    }

    @Override
    public void moveOne(InvTweaksOperationInfo operationInfo) {
        ScreenHandler handler = operationInfo.clickedSI().screenHandler();
        int from = operationInfo.clickedSlot().id;
        int result = moveToInventory(handler,from, operationInfo.otherSI(), 1, false);
    }

    @Override
    public void dropOne(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedSI().screenHandler().syncId, operationInfo.clickedSlot().id, 0, SlotActionType.THROW, playerEntity);
    }

    @Override
    public void moveStack(InvTweaksOperationInfo operationInfo) {
        ScreenHandler handler = operationInfo.clickedSI().screenHandler();
        int from = operationInfo.clickedSlot().id;
        ItemStack stack = operationInfo.clickedSlot().getStack();
        moveToInventory(handler,from, operationInfo.otherSI(), stack.getCount(), false);

    }

    @Override
    public void dropStack(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedSI().screenHandler().syncId, operationInfo.clickedSlot().id, 1, SlotActionType.THROW, playerEntity);
    }
}