package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;

import java.util.HashMap;
import java.util.Map;

public class InvTweaksVanillaCraftingBehavior extends InvTweaksVanillaGenericBehavior{
    private static final int RESULT_SLOT = 0;

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
        super.dropAll(operationInfo);
    }

    private void warnPlayer(String message) {
        MinecraftClient.getInstance().player.sendMessage(new LiteralText(message), false);
    }

    private int searchForItem(ScreenInventory inventory, Item item) {
        InvtweaksScreenController screenController = new InvtweaksScreenController(inventory.screenHandler());
        for (int slotId = inventory.start(); slotId <= inventory.end(); slotId++) {
            ItemStack stack = screenController.getStack(slotId);
            if (stack.getItem() == item) {
                return slotId;
            }
        }
        return -1;
    }

    private boolean replenishRecipe(ScreenInventory gridSI, ScreenInventory playerMainSI, ItemStack[] recipeStacks) {
        ScreenHandler handler = gridSI.screenHandler();
        InvtweaksScreenController screenController = new InvtweaksScreenController(handler);

        int gridStart = gridSI.start();

        boolean replenishedAtLeastOne = false;

        System.out.println("Replenishing recipe");
        for (int i = 0; i < recipeStacks.length; i++) {
            ItemStack recipeStack = recipeStacks[i];
            int currentSlot = gridStart + i;
            ItemStack currentStack = screenController.getStack(currentSlot);
            if (currentStack.getCount() < currentStack.getMaxCount()) {
                int playerMainSlot = searchForItem(playerMainSI, recipeStack.getItem());
                if (playerMainSlot == -1) {
                    warnPlayer("Could not find item " + recipeStack.getItem().getName().getString());
                    System.out.println("Could not find item " + recipeStack.getItem().getName().getString());
                    return false;
                }

                screenController.pickStack(playerMainSlot);
                screenController.placeOne(currentSlot);
                screenController.placeStack(playerMainSlot);
                replenishedAtLeastOne = true;
            }
        }

        System.out.println("Replenished at least one: " + replenishedAtLeastOne);
        return replenishedAtLeastOne;
    }

    @Override
    public void moveAllSameType(InvTweaksOperationInfo operationInfo) {
        //TODO: implement this
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            super.moveAllSameType(operationInfo);
            return;
        }

        ItemStack resultStack = operationInfo.clickedSI().screenHandler().slots.get(RESULT_SLOT).getStack();
        if (resultStack.isEmpty()) {
            return;
        }

        ScreenInventory craftingSI = operationInfo.clickedSI();
        CraftingSubScreenInvs subScreenInvs = getCraftingSubScreenInvs(craftingSI);

        ScreenInventory gridSI = subScreenInvs.gridSI;
        ScreenInventory playerMainSI = operationInfo.otherSI();
        ItemStack[] currentRecipeStacks = getCurrentRecipeStacks(gridSI);

        InvtweaksScreenController screenController = new InvtweaksScreenController(gridSI.screenHandler());

        int maxReplenish = 100;

        boolean replenished;
        do {
            spreadItemsInPlace(gridSI);
            screenController.dropOne(RESULT_SLOT);
//            screenController.craftAll(RESULT_SLOT);
            replenished = replenishRecipe(gridSI, playerMainSI, currentRecipeStacks);
        } while (replenished && --maxReplenish > 0);
    }

    @Override
    public void dropAllSameType(InvTweaksOperationInfo operationInfo) {
        final int RESULT_SLOT = 0;
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            super.moveAllSameType(operationInfo);
            return;
        }
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
    public void moveStack(InvTweaksOperationInfo operationInfo) {
        if (operationInfo.clickedSlot().id == RESULT_SLOT) {
            InvtweaksScreenController screenController = new InvtweaksScreenController(operationInfo.clickedSI().screenHandler());
            screenController.craftAll(RESULT_SLOT);
            return;
        }
        super.moveStack(operationInfo);
    }

    @Override
    public void dropStack(InvTweaksOperationInfo operationInfo) {
        super.dropStack(operationInfo);
    }
}
