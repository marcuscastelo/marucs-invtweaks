package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
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
    public static InvTweaksVanillaGenericBehavior INSTANCE = new InvTweaksVanillaGenericBehavior();

    protected int moveToSlot(ScreenHandler handler, int maxSlot, int fromSlotId, int toSlotId, int quantity, boolean sorting) {
        int from = fromSlotId;
        ItemStack initialStack = handler.getSlot(from).getStack().copy();
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
        interactionManager.clickSlot(handler.syncId, from, 0, SlotActionType.PICKUP, player);
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

                //If slot is occuppied
                if (sorting) {
                    int dumpWrongStackSlot = candidateDestination + 1;
                    while (handler.slots.get(dumpWrongStackSlot).getStack().getItem() != Items.AIR && dumpWrongStackSlot <= maxSlot) dumpWrongStackSlot++;

                    if (dumpWrongStackSlot > maxSlot) { //If there's no more room
                        //Returns remaining
                        interactionManager.clickSlot(handler.syncId, from, 0, SlotActionType.PICKUP, player);
                        currentHeldStack = handler.getCursorStack();
                        return -3;
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

        //Place remaining back
        if (initialCount - quantity > 0)
            interactionManager.clickSlot(handler.syncId, from, 0, SlotActionType.PICKUP, player);

        if (candidateDestination > toSlotId) candidateDestination--;
        return candidateDestination;
    }

    protected int moveToInventory(ScreenHandler handler, int fromSlot, InventoryContainerBoundInfo destinationBoundInfo, int quantity, boolean sorting) {
        int destinationStart = destinationBoundInfo.start;
        int inventorySize = destinationBoundInfo.getSize();
        return moveToSlot(handler, destinationStart+inventorySize-1, fromSlot, destinationStart, quantity, sorting);
    }

    @Override
    public void sort(InvTweaksOperationInfo operationInfo) {
        System.out.println("SORT IS BEING CALLED !! :)");
        ScreenHandler handler = operationInfo.clickedInventoryBoundInfo.screenHandler;
        int startSlot = operationInfo.clickedInventoryBoundInfo.start;
        int endSlot = operationInfo.clickedInventoryBoundInfo.end;

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
        itemsArray.sort(Comparator.comparing(item -> item.getName().toString()));

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
        for (int slotId = operationInfo.clickedInventoryBoundInfo.start; slotId <= operationInfo.clickedInventoryBoundInfo.end; slotId++) {
            ItemStack stack = operationInfo.clickedInventoryBoundInfo.screenHandler.getSlot(slotId).getStack();
            if (stack.getItem() == Items.AIR) continue;
            moveToInventory(operationInfo.clickedInventoryBoundInfo.screenHandler, slotId, operationInfo.otherInventoryBoundInfo, stack.getCount(), false);
        }
    }

    @Override
    public void dropAll(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        for (int slotId = operationInfo.clickedInventoryBoundInfo.start; slotId <= operationInfo.clickedInventoryBoundInfo.end; slotId++) {
            MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedInventoryBoundInfo.screenHandler.syncId, slotId, 1, SlotActionType.THROW, playerEntity);
        }
    }

    @Override
    public void moveAllSameType(InvTweaksOperationInfo operationInfo) {
        Item itemType = operationInfo.clickedSlot.getStack().getItem();
        for (int slot = operationInfo.clickedInventoryBoundInfo.start; slot <= operationInfo.clickedInventoryBoundInfo.end; slot++) {
            ItemStack stack = operationInfo.clickedInventoryBoundInfo.screenHandler.slots.get(slot).getStack();
            if (stack.getItem() != itemType) continue;

            int placedAt = moveToInventory(operationInfo.clickedInventoryBoundInfo.screenHandler, slot, operationInfo.otherInventoryBoundInfo, stack.getCount(), false);
//            if (placedAt+1 > container.slots.size()) {
            //If destination inventory is full
//                MinecraftClient.getInstance().interactionManager.clickSlot(container.syncId,slot,0,SlotActionType.PICKUP,MinecraftClient.getInstance().player);
//                return;
//            }
        }
    }

    @Override
    public void dropAllSameType(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        Item itemType = operationInfo.clickedSlot.getStack().getItem();
        for (int slot = operationInfo.clickedInventoryBoundInfo.start; slot <= operationInfo.clickedInventoryBoundInfo.end; slot++) {
            ItemStack stack = operationInfo.clickedInventoryBoundInfo.screenHandler.slots.get(slot).getStack();
            if (stack.getItem() != itemType) continue;
            MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedInventoryBoundInfo.screenHandler.syncId, slot, 1, SlotActionType.THROW, playerEntity);
        }
    }

    @Override
    public void moveOne(InvTweaksOperationInfo operationInfo) {
        ScreenHandler handler = operationInfo.clickedInventoryBoundInfo.screenHandler;
        int from = operationInfo.clickedSlot.id;
        moveToInventory(handler,from, operationInfo.otherInventoryBoundInfo, 1, false);
    }

    @Override
    public void dropOne(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedInventoryBoundInfo.screenHandler.syncId, operationInfo.clickedSlot.id, 0, SlotActionType.THROW, playerEntity);
    }

    @Override
    public void dropStack(InvTweaksOperationInfo operationInfo) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        MinecraftClient.getInstance().interactionManager.clickSlot(operationInfo.clickedInventoryBoundInfo.screenHandler.syncId, operationInfo.clickedSlot.id, 1, SlotActionType.THROW, playerEntity);
    }
}