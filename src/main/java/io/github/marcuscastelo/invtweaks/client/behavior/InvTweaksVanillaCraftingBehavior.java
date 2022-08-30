package io.github.marcuscastelo.invtweaks.client.behavior;

import io.github.marcuscastelo.invtweaks.InvTweaksMod;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.OperationResult;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.marcuscastelo.invtweaks.InvTweaksMod.LOGGER;

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
    public OperationResult sort(InvTweaksOperationInfo operationInfo) {
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

        return new OperationResult(true);
    }

    @Override
    public OperationResult moveAll(InvTweaksOperationInfo operationInfo) {
        return super.moveAll(operationInfo);
    }

    @Override
    public OperationResult dropAll(InvTweaksOperationInfo operationInfo) {
        return super.dropAll(operationInfo);
    }

    private void warnPlayer(String message) {
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message), false);
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

    private boolean replenishRecipe(ScreenInventory gridSI, ScreenInventory resourcesSI, List<ItemStack> recipeStacks) {
        ScreenHandler handler = gridSI.screenHandler();
        InvtweaksScreenController screenController = new InvtweaksScreenController(handler);

        int gridStart = gridSI.start();

        boolean ranOutOfMaterials = false;

        LOGGER.info("Replenishing recipe");
        for (int i = 0; i < recipeStacks.size(); i++) {
            ItemStack recipeStack = recipeStacks.get(i);
            if (recipeStack.isEmpty()) {
                LOGGER.info("Recipe stack is empty for slot " + (i+1) + " of stack: " + recipeStack);
                continue;
            }
            else {
                LOGGER.info("Recipe stack for slot " + (i+1) + " is: " + recipeStack);
            }

            int currentSlot = gridStart + i;
            ItemStack currentStack = screenController.getStack(currentSlot);
            if (currentStack.getCount() >= currentStack.getMaxCount()) {
                LOGGER.info("Current stack is full for slot " + (i+1) + " of stack: " + currentStack);
//                continue; // Already full
            }

            int playerMainSlot = searchForItem(resourcesSI, recipeStack.getItem());
            if (playerMainSlot == -1) {
                warnPlayer("Could not find item " + recipeStack.getItem().getName().getString());
                LOGGER.info("Could not find item " + recipeStack.getItem().getName().getString());
                return true;
            }

            LOGGER.info("Replenishing item " + recipeStack.getItem().getName().getString() + " from slot " + playerMainSlot + " to slot " + currentSlot);

            LOGGER.info("Pick item from slot " + playerMainSlot);
            screenController.pickStack(playerMainSlot);
            LOGGER.info("Held stack: " + screenController.getHeldStack());
            LOGGER.info("Place item to slot " + currentSlot);
            screenController.placeStack(currentSlot);
            LOGGER.info("Check if there is still something to place");
            if (!screenController.getHeldStack().isEmpty()){
                LOGGER.info("There is still something to place, place it to slot " + playerMainSlot);
                screenController.placeStack(playerMainSlot);
            } else {
                LOGGER.info("There is nothing to place anymore");
            }
        }

        LOGGER.info("ranOutOfMaterials: " + ranOutOfMaterials);
        return ranOutOfMaterials;
    }

    @Override
    public OperationResult moveAllSameType(InvTweaksOperationInfo operationInfo) {
        //TODO: implement this
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            return super.moveAllSameType(operationInfo);
        }

        ItemStack resultStack = operationInfo.clickedSI().screenHandler().slots.get(RESULT_SLOT).getStack();
        if (resultStack.isEmpty()) {
            return new OperationResult(true);
        }

        ScreenInventory craftingSI = operationInfo.otherInventories().craftingSI.orElse(null);
        if (craftingSI == null) {
            LOGGER.error("Could not find crafting inventory in " + operationInfo.clickedSI().screenHandler().toString());
            return new OperationResult(false);
        }
        CraftingSubScreenInvs subScreenInvs = getCraftingSubScreenInvs(craftingSI);

        ScreenInventory gridSI = subScreenInvs.gridSI;
        ScreenInventory playerCombinedSI = operationInfo.otherInventories().playerCombinedSI;
        //TODO: refactor hardcoded values 1 and 10
        List<ItemStack> currentRecipeStacks = craftingSI.screenHandler().getStacks().subList(1, 10).stream().map(ItemStack::copy).toList();


        InvtweaksScreenController screenController = new InvtweaksScreenController(gridSI.screenHandler());

        int maxIters = 64 * 4 * 9;

        gridSI.screenHandler().disableSyncing();

        boolean ranOutOfMaterials;
        do {
            spreadItemsInPlace(gridSI);
            screenController.dropOne(RESULT_SLOT);
//            screenController.dropAll(RESULT_SLOT);
//            screenController.craftAll(RESULT_SLOT);
            warnPlayer("Replenishing recipe: " + resultStack.getItem().getName().getString());
            ranOutOfMaterials = replenishRecipe(gridSI, playerCombinedSI, currentRecipeStacks);
            ranOutOfMaterials = false;
        } while (!ranOutOfMaterials && --maxIters > 0);

        gridSI.screenHandler().enableSyncing();
        gridSI.screenHandler().sendContentUpdates();
        if (maxIters == 0) {
            warnPlayer("Could not replenish recipe");
        }

        return new OperationResult(true);
    }

    @Override
    public OperationResult dropAllSameType(InvTweaksOperationInfo operationInfo) {
        final int RESULT_SLOT = 0;
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            super.dropAllSameType(operationInfo);
        }
        return new OperationResult(true);
    }

    @Override
    public OperationResult moveOne(InvTweaksOperationInfo operationInfo) {
        return new OperationResult(false);
    }

    @Override
    public OperationResult dropOne(InvTweaksOperationInfo operationInfo) {
        return super.dropOne(operationInfo);
    }

    @Override
    public OperationResult moveStack(InvTweaksOperationInfo operationInfo) {
        if (operationInfo.clickedSlot().id == RESULT_SLOT) {
            InvtweaksScreenController screenController = new InvtweaksScreenController(operationInfo.clickedSI().screenHandler());
            screenController.craftAll(RESULT_SLOT);
            return new OperationResult(true);
        }

        return super.moveStack(operationInfo);
    }

    @Override
    public OperationResult dropStack(InvTweaksOperationInfo operationInfo) {
        return super.dropStack(operationInfo);
    }
}
