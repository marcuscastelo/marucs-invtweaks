package io.github.marcuscastelo.invtweaks.behavior

import io.github.marcuscastelo.invtweaks.InvTweaksMod.Companion.LOGGER
import io.github.marcuscastelo.invtweaks.crafting.InventoryAnalyzer
import io.github.marcuscastelo.invtweaks.crafting.Recipe
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.failure


import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.SUCCESS
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.success
import io.github.marcuscastelo.invtweaks.operation.OperationType
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import io.github.marcuscastelo.invtweaks.util.InventoryUtils
import io.github.marcuscastelo.invtweaks.util.InvtweaksScreenController
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.CraftingScreenHandler

class InvTweaksVanillaCraftingBehavior : InvTweaksVanillaGenericBehavior() {
    fun getCurrentRecipeStacks(gridSI: ScreenInventory): Array<ItemStack?> {
        val recipe = arrayOfNulls<ItemStack>(gridSI.size)
        var recipeIndex = 0
        var slot = gridSI.start
        while (slot <= gridSI.end) {
            recipe[recipeIndex] = gridSI.screenHandler.slots[slot].stack
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
        val handler = gridSI.screenHandler
        val screenController = InvtweaksScreenController(handler)
        val currentRecipeStacks = getCurrentRecipeStacks(gridSI) // TODO: use Recipe class
        val itemCountInfo = countItemsOnRecipeStacks(currentRecipeStacks)
        val itemCountsPerSlot = calcSpreadItemCountPerSlot(itemCountInfo)
        val targetRecipeStacks = createTargetSpreadRecipeStacks(currentRecipeStacks, itemCountInfo, itemCountsPerSlot)
        val gridStart = gridSI.start
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
                for (destSlot in gridSI.start..gridSI.end) {
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
        val subScreenInvs = operationInfo.otherInventories
        val craftingGridSI: ScreenInventory = subScreenInvs.craftingSI.orElse(null) ?: return failure("No crafting grid found")

        if (operationInfo.clickedSlot.id in craftingGridSI.start..craftingGridSI.end) {
            warnPlayer("AAA ${operationInfo.clickedSlot.id} ${craftingGridSI.start} ${craftingGridSI.end}")
            spreadItemsInPlace(craftingGridSI)
            return success("Spread items in place")
        }
        return super.sort(operationInfo)
    }

    override fun moveAll(operationInfo: OperationInfo): OperationResult {
        return super.moveAll(operationInfo)

    }

    override fun dropAll(operationInfo: OperationInfo): OperationResult {
        return super.dropAll(operationInfo)
    }

    private fun searchForItem(stacks: ScreenInventory, item: Item): Int {
        val screenController = InvtweaksScreenController(stacks.screenHandler)
        LOGGER.info("Searching for item $item in slots ${stacks.start} to ${stacks.end}")
        for (slotId in stacks.start..stacks.end) {
            val stack = screenController.getStack(slotId)
            if (stack.isOf(item)) {
                return slotId
            }
        }
        return -1
    }

    private fun replenishRecipe(gridSI: ScreenInventory, resourcesSI: ScreenInventory, recipe: Recipe, hackAlreadyCrafted: Int = 0): Boolean {
        val handler = gridSI.screenHandler
        val screenController = InvtweaksScreenController(handler)
        val gridStart = gridSI.start
        var ranOutOfMaterials = true // Assume we ran out of materials (until proven otherwise)

        val recipeItemCount = recipe.itemCount
        val resourceItemCount = InventoryAnalyzer.countItems(resourcesSI.stacks)

        for (i in recipe.stacks.indices) {
            val recipeStack = recipe.stacks[i]
            if (recipeStack.isEmpty) {
                continue
            } else {
//                LOGGER.info("Recipe stack for slot " + (i+1) + " is: " + recipeStack);
            }
            val targetSlotID = gridStart + i

            var replenishedAtLeastOnce = false;

            do {
                val resourceSlot = searchForItem(resourcesSI, recipeStack.item)
                val foundResource = resourceSlot != -1

                if (!foundResource) {
                    warnPlayer("Cannot find ${recipeStack.item.name.string} in ${resourcesSI.start}..${resourcesSI.end}")
                    break
                }

                replenishedAtLeastOnce = true

                if (screenController.heldStack.isEmpty.not()) {
                    screenController.dropHeldStack()
                }

                val recipeCount = recipeItemCount[recipeStack.item] ?: run { warnPlayer("Cannot find ${recipeStack.item.name.string} in recipe"); 999 }
                val currentStackCount = screenController.getStack(resourceSlot).count
                screenController.pickStack(resourceSlot)
                screenController.placeSome(targetSlotID, 1)


                if (!screenController.heldStack.isEmpty) {
                    screenController.placeStack(resourceSlot)
                } else {
    //                LOGGER.info("There is nothing to place anymore");
                    handler.slots[resourceSlot].stack = ItemStack.EMPTY
                }
            } while (false)

            if (! replenishedAtLeastOnce) {
                val message = "Ran out of materials for crafting ${recipeStack.item.name.string}"
                warnPlayer(message)
                LOGGER.info(message)
                //We can't break or return because maybe we can spread the items in place from previously replenished slots
            } else {
                ranOutOfMaterials = false
            }
        }

        LOGGER.info("ranOutOfMaterials: $ranOutOfMaterials")
        return ranOutOfMaterials
    }

    private fun massCraft(operationInfo: OperationInfo): OperationResult {
        val inventories = operationInfo.otherInventories
        val craftingSI = inventories.craftingSI.orElse(null) ?: return failure("No crafting inventory found")
        val resourcesSI = inventories.playerCombinedSI

        val handler = craftingSI.screenHandler

        if (handler.slots[RESULT_SLOT].stack.isEmpty) {
            warnPlayer("Nothing to craft")
            return SUCCESS
        }

        val originalCraftingResult = handler.slots[RESULT_SLOT].stack.item

        val screenController = InvtweaksScreenController(handler)

        val recipe = Recipe(craftingSI.stacks)
        val resources = InventoryAnalyzer.searchRecipeItems(resourcesSI.stacks, recipe)

        val recipeItemCounts = InventoryAnalyzer.countItems(recipe.stacks)
        val resourceItemCounts = InventoryAnalyzer.countItems(resourcesSI.stacks)

        fun craft() {
            val currentItem = handler.slots[RESULT_SLOT].stack.item
            if (originalCraftingResult != currentItem) {
                warnPlayer("Item in crafting table is not the original one, not crafting anymore! ($originalCraftingResult -> $currentItem)")
                return
            }

            fun isInventoryFull() = resourcesSI.stacks.all { it.isEmpty.not() }
            if (isInventoryFull())
                screenController.dropOne(RESULT_SLOT)

            screenController.craftAll(RESULT_SLOT)
        }


        val missingItems = recipeItemCounts.filter { (item, count) -> (resourceItemCounts[item] ?: 0) < count }
        if (missingItems.isNotEmpty()) {
            // If we're missing items, we can't replenish the recipe,
            // but there may be some items in the crafting grid that we can still craft
            spreadItemsInPlace(craftingSI)
            repeat(64) { craft() }

            warnPlayer("Missing items: $missingItems")
            return failure("Missing items: $missingItems")
        }

//        operationInfo.clickedSI = operationInfo.otherInventories.craftingResultSI.orElse(null)!!
//        moveAll(operationInfo)
//        moveStack(operationInfo)

        repeat(1) {
            replenishRecipe(craftingSI, resourcesSI, recipe)

            craft()
            replenishRecipe(craftingSI, resourcesSI, recipe)
            spreadItemsInPlace(craftingSI)
        }

        val spreadOperation = OperationInfo(
                type = OperationType.SORT_NORMAL,
                clickedSI = craftingSI,
                clickedSlot = craftingSI.slots[1],
                otherInventories = operationInfo.otherInventories,
                targetSI = craftingSI,
        )

        val ignoreOperation = OperationInfo(
                type = OperationType.IGNORE,
                clickedSI = craftingSI,
                clickedSlot = craftingSI.slots[1],
                otherInventories = operationInfo.otherInventories,
                targetSI = craftingSI,
        )

        return OperationResult(
                success = OperationResult.SuccessType.SUCCESS,
                message = "Crafted ${handler.slots[RESULT_SLOT].stack.item.name.string}",
                nextOperations = listOf(operationInfo)
        )
    }

    override fun moveAllSameType(operationInfo: OperationInfo): OperationResult {
        // If the result slot is an output slot, we're mass crafting
        if (InventoryUtils.isCraftingOutputSlot(operationInfo.clickedSlot)) {
            RESULT_SLOT = operationInfo.clickedSlot.id // TODO: remove RESULT_SLOT completely
            return massCraft(operationInfo)
        }

        return super.moveAllSameType(operationInfo)
    }

    override fun dropAllSameType(operationInfo: OperationInfo): OperationResult {
//        val RESULT_SLOT = 0
//        if (operationInfo.clickedSlot.id != RESULT_SLOT) {
//            super.dropAllSameType(operationInfo)
//        }
        return pass("Not implemented")
    }

    override fun moveOne(operationInfo: OperationInfo): OperationResult {
        return super.moveOne(operationInfo)
    }

    override fun dropOne(operationInfo: OperationInfo): OperationResult {
        return super.dropOne(operationInfo)
    }

    override fun moveStack(operationInfo: OperationInfo): OperationResult {
//        if (operationInfo.clickedSlot.id == RESULT_SLOT) {
//            val screenController = InvtweaksScreenController(operationInfo.clickedSI.screenHandler)
//            screenController.craftAll(RESULT_SLOT)
//            return SUCCESS
//        }
        return super.moveStack(operationInfo)
    }

    override fun dropStack(operationInfo: OperationInfo): OperationResult {
        return super.dropStack(operationInfo)
    }

    companion object {
        private var RESULT_SLOT = 0
    }
}