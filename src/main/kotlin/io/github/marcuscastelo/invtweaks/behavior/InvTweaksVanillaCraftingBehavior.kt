package io.github.marcuscastelo.invtweaks.behavior

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.crafting.RecipeStacks
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.SUCCESS
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.CraftingScreenHandler

class InvTweaksVanillaCraftingBehavior : InvTweaksVanillaGenericBehavior() {
    private data class CraftingSubScreenInvs(val resultSI: ScreenInventory, val gridSI: ScreenInventory)

    private fun isCraftingInv(screenInventory: ScreenInventory): Boolean {
        return screenInventory.start() == 0 && screenInventory.screenHandler() is CraftingScreenHandler
    }

    private fun getCraftingSubScreenInvs(craftingSI: ScreenInventory): CraftingSubScreenInvs {
        val resultSI = ScreenInventory(craftingSI.screenHandler(), 0, 0)
        val gridSI = ScreenInventory(craftingSI.screenHandler(), 1, craftingSI.end())
        return CraftingSubScreenInvs(resultSI, gridSI)
    }

    fun getCurrentRecipeStacks(gridSI: ScreenInventory): Array<ItemStack?> {
        val recipe = arrayOfNulls<ItemStack>(gridSI.size)
        var recipeIndex = 0
        var slot = gridSI.start()
        while (slot <= gridSI.end()) {
            recipe[recipeIndex] = gridSI.screenHandler().slots[slot].stack
            slot++
            recipeIndex++
        }
        return recipe
    }

    class RecipeStackItemInfo internal constructor(var item: Item, var totalCount: Int, var slotCount: Int)

    fun countItemsOnRecipeStacks(recipeStacks: Array<ItemStack?>): Map<Item, RecipeStackItemInfo> {
        val itemCounts = HashMap<Item, RecipeStackItemInfo>()
        for (stack in recipeStacks) {
            if (stack!!.isEmpty) {
                continue
            }
            val item = stack.item
            if (itemCounts.containsKey(item)) {
                val info = itemCounts[item]
                info!!.totalCount += stack.count
                info!!.slotCount++
            } else {
                val info = RecipeStackItemInfo(item, stack.count, 1)
                itemCounts[item] = info
            }
        }
        return itemCounts
    }

    private fun calcSpreadItemCountPerSlot(countInfo: Map<Item, RecipeStackItemInfo>): Map<Item, Int> {
        val itemCountsPerSlot = HashMap<Item, Int>()
        for ((key, info) in countInfo) {
            val countPerSlot = info.totalCount / info.slotCount
            itemCountsPerSlot[key] = countPerSlot
        }
        return itemCountsPerSlot
    }

    private fun createTargetSpreadRecipeStacks(recipeStacks: Array<ItemStack?>, countInfo: Map<Item, RecipeStackItemInfo>, itemCountsPerSlot: Map<Item, Int>): Array<ItemStack?> {
        val targetRecipeStacks = arrayOfNulls<ItemStack>(recipeStacks.size)
        val isItemInRecipe: MutableMap<Item, Boolean> = HashMap()
        for (i in recipeStacks.indices) {
            val stack = recipeStacks[i]
            if (stack!!.isEmpty) {
                targetRecipeStacks[i] = stack
                continue
            }
            val item = stack.item
            var countPerSlot = itemCountsPerSlot[item]!!
            val totalCount = countInfo[item]!!.totalCount
            val calcTotalCount = countPerSlot * countInfo[item]!!.slotCount
            if (totalCount != calcTotalCount) {
                if (!isItemInRecipe.containsKey(item)) {
                    isItemInRecipe[item] = true
                    val remainder = totalCount - calcTotalCount
                    countPerSlot += remainder
                }
            }
            val targetCount = countPerSlot
            targetRecipeStacks[i] = ItemStack(item, targetCount)
        }
        return targetRecipeStacks
    }

    private fun spreadItemsInPlace(gridSI: ScreenInventory) {
        val handler = gridSI.screenHandler()
        val screenController = InvtweaksScreenController(handler)
        val currentRecipeStacks = getCurrentRecipeStacks(gridSI)
        val itemCountInfo = countItemsOnRecipeStacks(currentRecipeStacks)
        val itemCountsPerSlot = calcSpreadItemCountPerSlot(itemCountInfo)
        val targetRecipeStacks = createTargetSpreadRecipeStacks(currentRecipeStacks, itemCountInfo, itemCountsPerSlot)
        val gridStart = gridSI.start()
        for (i in targetRecipeStacks.indices) {
            val targetForCurrent = targetRecipeStacks[i]
            val currentSlot = gridStart + i
            val currentStack = screenController.getStack(currentSlot)
            if (currentStack.count <= targetForCurrent!!.count) {
                continue  // Already in place
            } else {
                var countToMove = currentStack.count - targetForCurrent.count
                if (countToMove == 0) continue
                screenController.pickStack(currentSlot)
                for (destSlot in gridSI.start()..gridSI.end()) {
                    if (countToMove <= 0) break
                    val destStack = screenController.getStack(destSlot)
                    val targetForDest = targetRecipeStacks[destSlot - gridStart]
                    if (destStack.item !== targetForCurrent.item) continue
                    if (destStack.count >= targetForDest!!.count) continue
                    val countToMoveToDest = Math.min(countToMove, targetForDest.count - destStack.count)
                    if (countToMoveToDest <= 0) continue
                    screenController.placeSome(destSlot, countToMoveToDest)
                    countToMove -= countToMoveToDest
                }
                screenController.placeStack(currentSlot)
                assert(countToMove == 0)
                assert(screenController.getStack(currentSlot).count == targetForCurrent.count)
                assert(screenController.getStack(currentSlot).item === targetForCurrent.item)
                assert(screenController.heldStack.isEmpty)
            }
        }
    }

    override fun sort(operationInfo: OperationInfo): OperationResult {
//        if (!isCraftingInv(operationInfo.clickedSI())) {
//            super.sort(operationInfo);
//            return;
//        }
//
        val craftingSI = operationInfo.clickedSI()
        val subScreenInvs = getCraftingSubScreenInvs(craftingSI)
        val craftingGridSI: ScreenInventory = subScreenInvs.gridSI
        //
        spreadItemsInPlace(craftingGridSI)
        if (operationInfo.clickedSI() === operationInfo.otherInventories().craftingSI.orElse(null)) {
            warnPlayer("Toppp")
        }
        return SUCCESS
    }

    override fun moveAll(operationInfo: OperationInfo): OperationResult {
        return super.moveAll(operationInfo)
    }

    override fun dropAll(operationInfo: OperationInfo): OperationResult {
        return super.dropAll(operationInfo)
    }

    private fun searchForItem(inventory: ScreenInventory, item: Item): Int {
        val screenController = InvtweaksScreenController(inventory.screenHandler())
        for (slotId in inventory.start()..inventory.end()) {
            val stack = screenController.getStack(slotId)
            if (stack.isOf(item)) {
                return slotId
            }
        }
        return -1
    }

    private fun replenishRecipe(gridSI: ScreenInventory, resourcesSI: ScreenInventory, recipeStacks: RecipeStacks): Boolean {
        val handler = gridSI.screenHandler()
        val screenController = InvtweaksScreenController(handler)
        val gridStart = gridSI.start()
        val ranOutOfMaterials = false
        InvTweaksMod.LOGGER.info("Replenishing recipe")
        for (i in recipeStacks.recipeStacks.indices) {
            val recipeStack = recipeStacks.recipeStacks[i]
            if (recipeStack.isEmpty) {
//                LOGGER.info("Recipe stack is empty for slot " + (i+1) + " of stack: " + recipeStack);
                continue
            } else {
//                LOGGER.info("Recipe stack for slot " + (i+1) + " is: " + recipeStack);
            }
            val currentSlot = gridStart + i
            val currentStack = screenController.getStack(currentSlot)
            if (currentStack.count >= currentStack.maxCount) {
//                LOGGER.info("Current stack is full for slot " + (i+1) + " of stack: " + currentStack);
//                continue; // Already full
            }
            val playerMainSlot = searchForItem(resourcesSI, recipeStack.item)
            if (playerMainSlot == -1) {
                warnPlayer("Could not find item " + recipeStack.item.name.string)
                InvTweaksMod.LOGGER.info("Could not find item " + recipeStack.item.name.string)
                return true
            }
            screenController.pickStack(playerMainSlot)
            screenController.placeStack(currentSlot)
            if (!screenController.heldStack.isEmpty) {
                screenController.placeStack(playerMainSlot)
            } else {
//                LOGGER.info("There is nothing to place anymore");
            }
        }

//        LOGGER.info("ranOutOfMaterials: " + ranOutOfMaterials);
        return ranOutOfMaterials
    }

    override fun moveAllSameType(operationInfo: OperationInfo): OperationResult {
        //TODO: implement this
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            return super.moveAllSameType(operationInfo)
        }
        val resultStack = operationInfo.clickedSI().screenHandler().slots[RESULT_SLOT].stack
        if (resultStack.isEmpty) {
            return SUCCESS
        }
        val craftingSI = operationInfo.otherInventories().craftingSI.orElse(null)
        if (craftingSI == null) {
            InvTweaksMod.LOGGER.error("Could not find crafting inventory in " + operationInfo.clickedSI().screenHandler().toString())
            return FAILURE
        }
        val subScreenInvs = getCraftingSubScreenInvs(craftingSI)
        val gridSI: ScreenInventory = subScreenInvs.gridSI
        val playerCombinedSI = operationInfo.otherInventories().playerCombinedSI
        val resourceStackList = playerCombinedSI.screenHandler().stacks.subList(playerCombinedSI.start(), playerCombinedSI.end() + 1).stream().map { obj: ItemStack -> obj.copy() }.toList()
        var recipeStacks: RecipeStacks
        run {

            //TODO: refactor hardcoded values 1 and 10
            val recipeStackList = craftingSI.screenHandler().stacks.subList(1, 10).stream().map { obj: ItemStack -> obj.item }.map { obj: Item -> obj.defaultStack }.toList()
            recipeStacks = RecipeStacks(recipeStackList)
        }
        val screenController = InvtweaksScreenController(gridSI.screenHandler())
        val itemToSlotMap: MutableMap<Item, MutableList<Int>> = HashMap()
        for (item in recipeStacks.materialList) {
            itemToSlotMap[item] = ArrayList()
        }
        for (slotId in gridSI.start()..gridSI.end()) {
            val stack = screenController.getStack(slotId)
            if (stack.isEmpty) {
                continue
            }
            val item = stack.item
            if (!itemToSlotMap.containsKey(item)) {
                continue
            }
            itemToSlotMap[item]!!.add(slotId)
        }
        val recipeItemCounts: MutableMap<Item, Int> = HashMap()
        val resourceItemCounts: MutableMap<Item, Int> = HashMap()
        for (item in recipeStacks.materialList) {
            recipeItemCounts[item] = 0
        }
        for (recipeStack in recipeStacks.recipeStacks) {
            assert(recipeStack.count == 1)
            val item = recipeStack.item
            val newCount = recipeItemCounts.getOrDefault(item, 0) + recipeStack.count
            recipeItemCounts[item] = newCount
        }
        for (resourceStack in resourceStackList) {
            val item = resourceStack.item
            val newCount = resourceItemCounts.getOrDefault(item, 0) + resourceStack.count
            resourceItemCounts[item] = newCount
        }
        val craftCountByItem: MutableMap<Item, Int> = HashMap()
        for (item in recipeStacks.materialList) {
            if (recipeItemCounts[item] == 0) {
                warnPlayer("Bug in recipeItemCounts.get(item) == 0, " + this.javaClass.name)
                continue
            }
            val resourceCount = resourceItemCounts.getOrDefault(item, 0)
            val recipeCount = recipeItemCounts.getOrDefault(item, -1)
            if (resourceCount == 0) {
                InvTweaksMod.LOGGER.info("No resources for item " + item.name.string)
                warnPlayer("No resources for item " + item.name.string)
                break
            } else if (recipeCount == -1) {
                InvTweaksMod.LOGGER.error("Bug in recipeItemCounts.get(item) == -1, " + this.javaClass.name)
                warnPlayer("Bug in recipeItemCounts.get(item) == -1, " + this.javaClass.name)
                break
            }
            val craftCount = resourceCount / recipeCount
            craftCountByItem[item] = craftCount
        }
        val maxCraftCount = craftCountByItem.values.stream().mapToInt { obj: Int -> obj }.min().orElse(0)
        InvTweaksMod.LOGGER.info("maxCraftCount: $maxCraftCount")

        // Craft all items
        for (i in 0 until maxCraftCount) {
            val ranOutOfMaterials = replenishRecipe(gridSI, playerCombinedSI, recipeStacks)
            if (ranOutOfMaterials) {
                InvTweaksMod.LOGGER.warn("Ran out of materials earlier than expected")
                warnPlayer("Ran out of materials earlier than expected")
                return SUCCESS
            }

//            screenController.
        }
        return SUCCESS
    }

    override fun dropAllSameType(operationInfo: OperationInfo): OperationResult {
        val RESULT_SLOT = 0
        if (operationInfo.clickedSlot().id != RESULT_SLOT) {
            super.dropAllSameType(operationInfo)
        }
        return SUCCESS
    }

    override fun moveOne(operationInfo: OperationInfo): OperationResult {
        return FAILURE
    }

    override fun dropOne(operationInfo: OperationInfo): OperationResult {
        return super.dropOne(operationInfo)
    }

    override fun moveStack(operationInfo: OperationInfo): OperationResult {
        if (operationInfo.clickedSlot().id == RESULT_SLOT) {
            val screenController = InvtweaksScreenController(operationInfo.clickedSI().screenHandler())
            screenController.craftAll(RESULT_SLOT)
            return SUCCESS
        }
        return super.moveStack(operationInfo)
    }

    override fun dropStack(operationInfo: OperationInfo): OperationResult {
        return super.dropStack(operationInfo)
    }

    companion object {
        private const val RESULT_SLOT = 0
    }
}