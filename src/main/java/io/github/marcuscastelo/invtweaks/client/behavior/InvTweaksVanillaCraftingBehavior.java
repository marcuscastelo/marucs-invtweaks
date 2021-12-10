package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import java.util.HashMap;
import java.util.Map;

public class InvTweaksVanillaCraftingBehavior extends InvTweaksVanillaGenericBehavior{

    private record CraftingSubScreenInvs(ScreenInventory resultSI, ScreenInventory gridSI) {
    }

    private boolean isCraftingInv(ScreenInventory screenInventory) {
        return screenInventory.start() == 0 && screenInventory.screenHandler() instanceof net.minecraft.screen.CraftingScreenHandler;
    }

    private CraftingSubScreenInvs getCraftingSubScreenInvs(ScreenInventory craftingSI) {
        ScreenInventory resultSI = new ScreenInventory(craftingSI.screenHandler(), 0, 0);
        ScreenInventory gridSI = new ScreenInventory(craftingSI.screenHandler(), 1, craftingSI.end());
        return new CraftingSubScreenInvs(resultSI, gridSI);
    }

    ItemStack[] getCurrentRecipeStacks(ScreenInventory gridSI) {
        var recipe = new ItemStack[gridSI.getSize()];
        int recipeIndex = 0;
        for (int slot = gridSI.start(); slot <= gridSI.end(); slot++, recipeIndex++) {
            recipe[recipeIndex] = gridSI.screenHandler().getSlot(slot).getStack();
        }
        return recipe;
    }

    private static class RecipeStackItemInfo{
        public Item item;
        public int totalCount;
        public int slotCount;

        RecipeStackItemInfo(Item item, int totalCount, int slotCount) {
            this.item = item;
            this.totalCount = totalCount;
            this.slotCount = slotCount;
        }

    }

    Map<Item, RecipeStackItemInfo> countItemsOnRecipeStacks(ItemStack[] recipeStacks) {
        HashMap<Item, RecipeStackItemInfo> itemCounts = new HashMap<>();
        for (ItemStack stack : recipeStacks) {
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            if (itemCounts.containsKey(item)) {
                RecipeStackItemInfo info = itemCounts.get(item);
                info.totalCount += stack.getCount();
                info.slotCount++;
            }
            else {
                RecipeStackItemInfo info = new RecipeStackItemInfo(item, stack.getCount(), 1);
                itemCounts.put(item, info);
            }
        }
        return itemCounts;
    }

    private Map<Item, Integer> calcSpreadItemCountPerSlot(Map<Item, RecipeStackItemInfo> countInfo) {
        HashMap<Item, Integer> itemCountsPerSlot = new HashMap<>();
        for (Map.Entry<Item, RecipeStackItemInfo> entry : countInfo.entrySet()) {
            RecipeStackItemInfo info = entry.getValue();
            int countPerSlot = info.totalCount / info.slotCount;
            itemCountsPerSlot.put(entry.getKey(), countPerSlot);
        }
        return itemCountsPerSlot;
    }

    private ItemStack[] createTargetSpreadRecipeStacks(ItemStack[] recipeStacks, Map<Item, RecipeStackItemInfo> countInfo, Map<Item, Integer> itemCountsPerSlot) {
        ItemStack[] targetRecipeStacks = new ItemStack[recipeStacks.length];
        Map<Item, Boolean> isItemInRecipe = new HashMap<>();
        for (int i = 0; i < recipeStacks.length; i++) {
            ItemStack stack = recipeStacks[i];
            if (stack.isEmpty()) {
                targetRecipeStacks[i] = stack;
                continue;
            }

            Item item = stack.getItem();
            int countPerSlot = itemCountsPerSlot.get(item);
            int totalCount = countInfo.get(item).totalCount;
            int calcTotalCount = countPerSlot * countInfo.get(item).slotCount;
            if (totalCount != calcTotalCount) {
                if (!isItemInRecipe.containsKey(item)) {
                    isItemInRecipe.put(item, true);

                    int remainder = totalCount - calcTotalCount;
                    countPerSlot += remainder;
                }
            }

            int targetCount = countPerSlot;
            targetRecipeStacks[i] = new ItemStack(item, targetCount);
        }
        return targetRecipeStacks;
    }

    private void spreadItemsInPlace(ScreenInventory gridSI) {
        ScreenHandler handler = gridSI.screenHandler();
        InvtweaksScreenController screenController = new InvtweaksScreenController(handler);

        ItemStack[] currentRecipeStacks = getCurrentRecipeStacks(gridSI);
        Map<Item, RecipeStackItemInfo> itemCountInfo = countItemsOnRecipeStacks(currentRecipeStacks);
        Map<Item, Integer> itemCountsPerSlot = calcSpreadItemCountPerSlot(itemCountInfo);
        ItemStack[] targetRecipeStacks = createTargetSpreadRecipeStacks(currentRecipeStacks, itemCountInfo, itemCountsPerSlot);

        int gridStart = gridSI.start();
        for (int i = 0; i < targetRecipeStacks.length; i++) {
            ItemStack targetForCurrent = targetRecipeStacks[i];
            int currentSlot = gridStart + i;
            ItemStack currentStack = screenController.getStack(currentSlot);

            if (currentStack.getCount() <= targetForCurrent.getCount()) {
                continue; // Already in place
            }
            else {
                int countToMove = currentStack.getCount() - targetForCurrent.getCount();
                if (countToMove == 0) continue;

                screenController.pickStack(currentSlot);

                for (int destSlot = gridSI.start(); destSlot <= gridSI.end(); destSlot++) {
                    if (countToMove <= 0) break;
                    ItemStack destStack = screenController.getStack(destSlot);
                    ItemStack targetForDest = targetRecipeStacks[destSlot - gridStart];
                    if (destStack.getItem() != targetForCurrent.getItem()) continue;
                    if (destStack.getCount() >= targetForDest.getCount()) continue;

                    int countToMoveToDest = Math.min(countToMove, targetForDest.getCount() - destStack.getCount());
                    if (countToMoveToDest <= 0) continue;

                    screenController.placeSome(destSlot, countToMoveToDest);
                    countToMove -= countToMoveToDest;
                }

                screenController.placeStack(currentSlot);

                //TODO: move to unit test code
                assert countToMove == 0;
                assert screenController.getStack(currentSlot).getCount() == targetForCurrent.getCount();
                assert screenController.getStack(currentSlot).getItem() == targetForCurrent.getItem();
                assert screenController.getHeldStack().isEmpty();
            }

        }
    }

    @Override
    public void sort(InvTweaksOperationInfo operationInfo) {
        if (!isCraftingInv(operationInfo.clickedSI())) {
            super.sort(operationInfo);
            return;
        }

        ScreenInventory craftingSI = operationInfo.clickedSI();
        CraftingSubScreenInvs subScreenInvs = getCraftingSubScreenInvs(craftingSI);
        ScreenInventory craftingGridSI = subScreenInvs.gridSI;

        spreadItemsInPlace(craftingGridSI);
    }

    @Override
    public void moveAll(InvTweaksOperationInfo operationInfo) {
        super.moveAll(operationInfo);
    }

    @Override
    public void dropAll(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void moveAllSameType(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void dropAllSameType(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void moveOne(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void dropOne(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void moveStack(InvTweaksOperationInfo operationInfo) {

    }

    @Override
    public void dropStack(InvTweaksOperationInfo operationInfo) {

    }
}
