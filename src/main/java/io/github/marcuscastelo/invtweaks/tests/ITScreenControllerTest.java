package io.github.marcuscastelo.invtweaks.tests;

import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController;
import io.github.marcuscastelo.invtweaks.util.ItemStackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class ITScreenControllerTest {
    private final InvtweaksScreenController controller;
    private final ScreenSpecification screenSpecification;
    public ITScreenControllerTest(InvtweaksScreenController controller, ScreenSpecification screenSpecification) {
        this.controller = controller;
        this.screenSpecification = screenSpecification;
    }

    public void testSpreadStack(ScreenInventory boundInfo) {
        controller.pickStack(boundInfo.start);
        for (int i = boundInfo.start; i <= boundInfo.end; i++)
            controller.placeOne(i);
    }

    public void shiftStacksRight(ScreenInventory boundInfo) {
        controller.pickStack(boundInfo.start);
        for (int i = boundInfo.start + 1; i <= boundInfo.end; i++) {
            if (controller.isHoldingStack())
                controller.placeStack(i);
            else
                controller.pickStack(i);
        }

        if (controller.isHoldingStack())
            controller.placeStack(boundInfo.start);
    }


    static class StacksSorter {
        static class StackGroup {
            private final List<ItemStack> stacks = new LinkedList<>();

            public void addStack(ItemStack stack) {
                int itemCountToAdd = stack.getCount();
                for (int i = 0; i < stacks.size() && itemCountToAdd > 0; i++) {
                    ItemStack otherStack = stacks.get(i);
                    if (ItemStackUtils.canStacksMerge(stack, otherStack)) {
                        if (otherStack.getCount() > otherStack.getMaxCount()) continue; //Does not mess with glitched items
                        int newOtherStackSize = Math.min(otherStack.getCount() + itemCountToAdd, otherStack.getMaxCount());
                        itemCountToAdd -= newOtherStackSize - otherStack.getCount();
                        stacks.get(i).setCount(newOtherStackSize);
                    }
                }

                //If not possible to merge with some other stack, just add a new stack
                if (itemCountToAdd > 0) {
                    stacks.add(stack);
                }
            }

            private int getSize() { return stacks.size(); }
        }

        static class StackGroupSortInfo {
            public final StackGroup stackGroup;
            public final int startSlot, endSlot;
            public int nextSlotToPlace;

            public StackGroupSortInfo(StackGroup group, int start, int end) {
                stackGroup = group;
                startSlot = start;
                endSlot = end;
                nextSlotToPlace = start;
            }

            //Places stack held by the controller (may end up having another stack being held).
            //Returns last slot
            public int placeHeldStack(InvtweaksScreenController controller) {
                assert controller.isHoldingStack();
                int targetSlot;
                boolean dbg_other_type = false;

                for (targetSlot = startSlot; targetSlot <= endSlot; targetSlot++) {
                    if (!controller.isHoldingStack()) break;
                    ItemStack heldStack = controller.getHeldStack();

                    ItemStack targetStack = controller.getStack(targetSlot);

                    if (heldStack.getItem() == Items.STONE)
                        System.out.println("Moving stone to " + targetSlot);

                    if (ItemStackUtils.canStackAddMore(targetStack, heldStack)) {
                        //Merge stacks and the remaining amount is placed in the next iteration
                        // (or break if nothing remaining)
                        controller.placeStack(targetSlot);
                    }
                    else if (!ItemStack.areItemsEqual(targetStack, heldStack)) {
                        //If just swapped with a random item, the placement of the stack is completed
                        controller.placeStack(targetSlot);
                        dbg_other_type = true;
                        break;
                    }
                }

                //Returns last slot
                return targetSlot;
            }

        }

        private final Map<Item, StackGroup> stackGroups = new HashMap<>();
        private final List<Item> itemList;

        private final ScreenInventory boundInfo;

        public StacksSorter(ScreenInventory boundInfo) {
            this.boundInfo = boundInfo;
            this.itemList = new ArrayList<>();
        }

        public void addStack(ItemStack stack) {
            Item item = stack.getItem();

            StackGroup stackGroup;
            if (!stackGroups.containsKey(item)) {
                stackGroup = new StackGroup();
                stackGroups.put(item, stackGroup);
                itemList.add(item);
            } else stackGroup = stackGroups.get(item);

            stackGroup.addStack(stack);
        }

        public void sort() {
            itemList.sort(Comparator.comparing(Item::getRawId));
        }

        public void shuffle() {
            Collections.shuffle(itemList);
        }

        public Map<Item, StackGroupSortInfo> getStackGroupsSortingInfo() {
            Map<Item, StackGroupSortInfo> stackGroupSortInfo = new HashMap<>();

            int currentSlot = boundInfo.start;
            for (Item item : itemList) {
                StackGroup group = stackGroups.get(item);
                int start = currentSlot;
                int end = group.getSize() + start - 1;
                if (end < start) end = start;

                StackGroupSortInfo sortInfo = new StackGroupSortInfo(group, start, end);
                stackGroupSortInfo.put(item, sortInfo);

                currentSlot = end + 1;
                assert currentSlot <= boundInfo.end;
            }

            return stackGroupSortInfo;
        }

    }

    public void testSort(ScreenInventory boundInfo) {
        StacksSorter stacksSorter = new StacksSorter(boundInfo);

        for (int slot = boundInfo.start; slot <= boundInfo.end; slot++) {
            ItemStack slotStack = controller.getStack(slot);
            if (slotStack.isEmpty()) continue;
            stacksSorter.addStack(slotStack.copy());
        }

        stacksSorter.sort();
        Map<Item, StacksSorter.StackGroupSortInfo> stackGroupsInfo = stacksSorter.getStackGroupsSortingInfo();
        boolean[] slotIsSorted = new boolean[boundInfo.end + 1];

        //Keeps track of the slots ordered without swapping logic
        for (int slot = boundInfo.start; slot <= boundInfo.end; slot++) {
            if (controller.getStack(slot).isEmpty()) continue;
            if (slotIsSorted[slot]) continue;

            if (!controller.isHoldingStack()) {
                controller.pickStack(slot);
            }

            int currentSlot = slot;
            while (controller.isHoldingStack()) {
                if (slotIsSorted[currentSlot]) break;
                slotIsSorted[currentSlot] = true;

                ItemStack heldStack = controller.getHeldStack();
                StacksSorter.StackGroupSortInfo groupInfo = stackGroupsInfo.get(heldStack.getItem());

                String dbg_targetGroupBound = groupInfo.startSlot + " - " + groupInfo.endSlot;
                System.out.println("Placing " + heldStack + " from " + currentSlot + " to " + dbg_targetGroupBound);

                currentSlot = groupInfo.placeHeldStack(controller);
            }

            slotIsSorted[slot] = true;
        }

        //TODO: sort inventory using hop logic
    }
}
