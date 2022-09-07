package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.operation.OperationInfo;
import io.github.marcuscastelo.invtweaks.crafting.RecipeStacks;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.operation.OperationResult;
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.marcuscastelo.invtweaks.InvTweaksMod.LOGGER;
import static io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer;

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
            recipe[recipeIndex] = gridSI.screenHandler().slots.get(slot).getStack();
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
    public OperationResult sort(OperationInfo operationInfo) {
//        if (!isCraftingInv(operationInfo.clickedSI())) {
//            super.sort(operationInfo);
//            return;
//        }
//
        ScreenInventory craftingSI = operationInfo.clickedSI();
        CraftingSubScreenInvs subScreenInvs = getCraftingSubScreenInvs(craftingSI);
        ScreenInventory craftingGridSI = subScreenInvs.gridSI;
//
        spreadItemsInPlace(craftingGridSI);

        if (operationInfo.clickedSI() == operationInfo.otherInventories().craftingSI.orElse(null)) {
            warnPlayer("Toppp");
        }

        return OperationResult.Companion.getSUCCESS();
    }

    @Override
    public OperationResult moveAll(OperationInfo operationInfo) {
        return super.moveAll(operationInfo);
    }

    @Override
    public OperationResult dropAll(OperationInfo operationInfo) {
        return super.dropAll(operationInfo);
    }

    private int searchForItem(ScreenInventory inventory, Item item) {
        InvtweaksScreenController screenController = new InvtweaksScreenController(inventory.screenHandler());
        for (int slotId = inventory.start(); slotId <= inventory.end(); slotId++) {
            ItemStack stack = screenController.getStack(slotId);
            if (stack.isOf(item)) {
                return slotId;
            }
        }
        return -1;
    }

    private boolean replenishRecipe(ScreenInventory gridSI, ScreenInventory resourcesSI, RecipeStacks recipeStacks) {
        ScreenHandler handler = gridSI.screenHandler();
        InvtweaksScreenController screenController = new InvtweaksScreenController(handler);

        int gridStart = gridSI.start();

        boolean ranOutOfMaterials = false;

        LOGGER.info("Replenishing recipe");
        for (int i = 0; i < recipeStacks.getRecipeStacks().size(); i++) {
            ItemStack recipeStack = recipeStacks.getRecipeStacks().get(i);
            if (recipeStack.isEmpty()) {
//                LOGGER.info("Recipe stack is empty for slot " + (i+1) + " of stack: " + recipeStack);
                continue;
            }
            else {
//                LOGGER.info("Recipe stack for slot " + (i+1) + " is: " + recipeStack);
            }

            int currentSlot = gridStart + i;
            ItemStack currentStack = screenController.getStack(currentSlot);
            if (currentStack.getCount() >= currentStack.getMaxCount()) {
//                LOGGER.info("Current stack is full for slot " + (i+1) + " of stack: " + currentStack);
//                continue; // Already full
            }

            int playerMainSlot = searchForItem(resourcesSI, recipeStack.getItem());
            if (playerMainSlot == -1) {
                warnPlayer("Could not find item " + recipeStack.getItem().getName().getString());
                LOGGER.info("Could not find item " + recipeStack.getItem().getName().getString());
                return true;
            }

            screenController.pickStack(playerMainSlot);
            screenController.placeStack(currentSlot);
            if (!screenController.getHeldStack().isEmpty()){
                screenController.placeStack(playerMainSlot);
            } else {
//                LOGGER.info("There is nothing to place anymore");
            }
        }

//        LOGGER.info("ranOutOfMaterials: " + ranOutOfMaterials);
        return ranOutOfMaterials;
    }

    @Override
    public OperationResult moveAllSameType(OperationInfo operationInfo) {
        //TODO: implement this
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            return super.moveAllSameType(operationInfo);
        }

        ItemStack resultStack = operationInfo.clickedSI().screenHandler().slots.get(RESULT_SLOT).getStack();
        if (resultStack.isEmpty()) {
            return OperationResult.Companion.getSUCCESS();
        }

        ScreenInventory craftingSI = operationInfo.otherInventories().craftingSI.orElse(null);
        if (craftingSI == null) {
            LOGGER.error("Could not find crafting inventory in " + operationInfo.clickedSI().screenHandler().toString());
            return OperationResult.Companion.getFAILURE();
        }
        CraftingSubScreenInvs subScreenInvs = getCraftingSubScreenInvs(craftingSI);

        ScreenInventory gridSI = subScreenInvs.gridSI;
        ScreenInventory playerCombinedSI = operationInfo.otherInventories().playerCombinedSI;

        List<ItemStack> resourceStackList = playerCombinedSI.screenHandler().getStacks().subList(playerCombinedSI.start(), playerCombinedSI.end()+1).stream().map(ItemStack::copy).toList();

        RecipeStacks recipeStacks;
        {
            //TODO: refactor hardcoded values 1 and 10
            List<ItemStack> recipeStackList = craftingSI.screenHandler().getStacks().subList(1, 10).stream().map(ItemStack::getItem).map(Item::getDefaultStack).toList();
            recipeStacks = new RecipeStacks(recipeStackList);
        }

        InvtweaksScreenController screenController = new InvtweaksScreenController(gridSI.screenHandler());


        Map<Item, List<Integer>> itemToSlotMap = new HashMap<>();
        for (Item item : recipeStacks.getMaterialList()) {
            itemToSlotMap.put(item, new ArrayList<>());
        }

        for (int slotId = gridSI.start(); slotId <= gridSI.end(); slotId++) {
            ItemStack stack = screenController.getStack(slotId);
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();

            if (!itemToSlotMap.containsKey(item)) {
                continue;
            }

            itemToSlotMap.get(item).add(slotId);
        }

        Map<Item, Integer> recipeItemCounts = new HashMap<>();
        Map<Item, Integer> resourceItemCounts = new HashMap<>();

        for (Item item : recipeStacks.getMaterialList()) {
            recipeItemCounts.put(item, 0);
        }

        for (ItemStack recipeStack : recipeStacks.getRecipeStacks()) {
            assert recipeStack.getCount() == 1;
            Item item = recipeStack.getItem();
            int newCount = recipeItemCounts.getOrDefault(item, 0) + recipeStack.getCount();
            recipeItemCounts.put(item, newCount);
        }

        for (ItemStack resourceStack : resourceStackList) {
            Item item = resourceStack.getItem();
            int newCount = resourceItemCounts.getOrDefault(item, 0) + resourceStack.getCount();
            resourceItemCounts.put(item, newCount);
        }

        Map<Item, Integer> craftCountByItem = new HashMap<>();
        for (Item item : recipeStacks.getMaterialList()) {
            if (recipeItemCounts.get(item) == 0) {
                warnPlayer("Bug in recipeItemCounts.get(item) == 0, " + this.getClass().getName());
                continue;
            }
            Integer resourceCount = resourceItemCounts.getOrDefault(item, 0);
            Integer recipeCount = recipeItemCounts.getOrDefault(item, -1);

            if (resourceCount == 0) {
                LOGGER.info("No resources for item " + item.getName().getString());
                warnPlayer("No resources for item " + item.getName().getString());
                break;
            } else if (recipeCount == -1) {
                LOGGER.error("Bug in recipeItemCounts.get(item) == -1, " + this.getClass().getName());
                warnPlayer("Bug in recipeItemCounts.get(item) == -1, " + this.getClass().getName());
                break;
            }

            int craftCount = resourceCount / recipeCount;
            craftCountByItem.put(item, craftCount);
        }

        int maxCraftCount = craftCountByItem.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        LOGGER.info("maxCraftCount: " + maxCraftCount);

        // Craft all items

        for (int i = 0; i < maxCraftCount; i++) {
            boolean ranOutOfMaterials = replenishRecipe(gridSI, playerCombinedSI, recipeStacks);
            if (ranOutOfMaterials) {
                LOGGER.warn("Ran out of materials earlier than expected");
                warnPlayer("Ran out of materials earlier than expected");
                return OperationResult.Companion.getSUCCESS();
            }

//            screenController.
        }

        return OperationResult.Companion.getSUCCESS();
    }

    @Override
    public OperationResult dropAllSameType(OperationInfo operationInfo) {
        final int RESULT_SLOT = 0;
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            super.dropAllSameType(operationInfo);
        }
        return OperationResult.Companion.getSUCCESS();
    }

    @Override
    public OperationResult moveOne(OperationInfo operationInfo) {
        return OperationResult.Companion.getFAILURE();
    }

    @Override
    public OperationResult dropOne(OperationInfo operationInfo) {
        return super.dropOne(operationInfo);
    }

    @Override
    public OperationResult moveStack(OperationInfo operationInfo) {
        if (operationInfo.clickedSlot().id == RESULT_SLOT) {
            InvtweaksScreenController screenController = new InvtweaksScreenController(operationInfo.clickedSI().screenHandler());
            screenController.craftAll(RESULT_SLOT);
            return OperationResult.Companion.getSUCCESS();
        }

        return super.moveStack(operationInfo);
    }

    @Override
    public OperationResult dropStack(OperationInfo operationInfo) {
        return super.dropStack(operationInfo);
    }
}
