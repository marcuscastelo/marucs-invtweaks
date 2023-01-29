package com.marcuscastelo.invtweaks.behavior

import com.marcuscastelo.invtweaks.crafting.InventoryAnalyzer
import com.marcuscastelo.invtweaks.crafting.Recipe
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.intent.IntentContext
import com.marcuscastelo.invtweaks.intent.IntentType
import com.marcuscastelo.invtweaks.operation.*
import com.marcuscastelo.invtweaks.util.ChatUtils
import com.marcuscastelo.invtweaks.util.ScreenController
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.SlotActionType

object CraftHelper {
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

    fun countItemsOnRecipeStacks(recipeStacks: Array<ItemStack?>): Map<Item, com.marcuscastelo.invtweaks.behavior.CraftHelper.RecipeStackItemInfo> {
        val itemCounts = HashMap<Item, com.marcuscastelo.invtweaks.behavior.CraftHelper.RecipeStackItemInfo>()
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
                val info = com.marcuscastelo.invtweaks.behavior.CraftHelper.RecipeStackItemInfo(item, stack.count, 1)
                itemCounts[item] = info
            }
        }
        return itemCounts
    }

    fun calcSpreadItemCountPerSlot(countInfo: Map<Item, com.marcuscastelo.invtweaks.behavior.CraftHelper.RecipeStackItemInfo>): Map<Item, Int> {
        val itemCountsPerSlot = HashMap<Item, Int>()
        for ((key, info) in countInfo) {
            val countPerSlot = info.totalCount / info.slotCount
            itemCountsPerSlot[key] = countPerSlot
        }
        return itemCountsPerSlot
    }

    fun createTargetSpreadRecipeStacks(recipeStacks: Array<ItemStack?>, countInfo: Map<Item, com.marcuscastelo.invtweaks.behavior.CraftHelper.RecipeStackItemInfo>, itemCountsPerSlot: Map<Item, Int>): Array<ItemStack?> {
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

    fun spreadItemsInPlace(gridSI: ScreenInventory): OperationResult {
        val handler = gridSI.screenHandler
        val screenController = ScreenController(handler)
        val currentRecipeStacks = com.marcuscastelo.invtweaks.behavior.CraftHelper.getCurrentRecipeStacks(gridSI) // TODO: use Recipe class
        val itemCountInfo = com.marcuscastelo.invtweaks.behavior.CraftHelper.countItemsOnRecipeStacks(currentRecipeStacks)
        val itemCountsPerSlot = com.marcuscastelo.invtweaks.behavior.CraftHelper.calcSpreadItemCountPerSlot(itemCountInfo)
        val targetRecipeStacks = com.marcuscastelo.invtweaks.behavior.CraftHelper.createTargetSpreadRecipeStacks(currentRecipeStacks, itemCountInfo, itemCountsPerSlot)
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
                    val countToMoveToDest = countToMove.coerceAtMost(targetForDest.count - destStack.count)
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

        return OperationResult.success("Spread items in place: no problems found")
    }

    fun searchForItem(stacks: ScreenInventory, item: Item): Int {
        val screenController = ScreenController(stacks.screenHandler)
        for (slotId in stacks.start..stacks.end) {
            val stack = screenController.getStack(slotId)
            if (stack.isOf(item)) {
                return slotId
            }
        }
        return -1
    }

    enum class ReplenishResult {
        SUCCESS,
        NO_MATERIALS,
    }

    fun replenishRecipe(gridSI: ScreenInventory, resourcesSI: ScreenInventory, recipe: Recipe): ReplenishResult {
        val handler = gridSI.screenHandler
        val screenController = ScreenController(handler)
        val gridStart = gridSI.start
        var ranOutOfMaterials = true // Assume we ran out of materials (until proven otherwise)

        val recipeItemCount = recipe.itemCount
        val resourceItemCount = InventoryAnalyzer.calculateAmountPerItem(resourcesSI.stacks)

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
                val resourceSlot = com.marcuscastelo.invtweaks.behavior.CraftHelper.searchForItem(resourcesSI, recipeStack.item)
                val foundResource = resourceSlot != -1

                if (!foundResource) {
                    ChatUtils.warnPlayer("Cannot find ${recipeStack.item.name.string} in ${resourcesSI.start}..${resourcesSI.end}")
                    break
                }

                replenishedAtLeastOnce = true

                if (screenController.heldStack.isEmpty.not()) {
                    screenController.dropHeldStack()
                }

                val recipeCount = recipeItemCount[recipeStack.item]
                        ?: run { ChatUtils.warnPlayer("Cannot find ${recipeStack.item.name.string} in recipe"); 999 }
                val currentStackCount = screenController.getStack(resourceSlot).count
                screenController.pickStack(resourceSlot)
//                repeat(1) {
//                    screenController.placeStack(targetSlotID)
//                    screenController.pickStack(targetSlotID)
//                }
                screenController.placeSome(targetSlotID, 1)
//                screenController.placeStack(targetSlotID)


                if (!screenController.heldStack.isEmpty) {
                    screenController.placeStack(resourceSlot)
                } else {
                    //                LOGGER.info("There is nothing to place anymore");
                    handler.slots[resourceSlot].stack = ItemStack.EMPTY
                }
            } while (false)

            if (!replenishedAtLeastOnce) {
                val message = "Ran out of materials for crafting ${recipeStack.item.name.string}"
                ChatUtils.warnPlayer(message)
                com.marcuscastelo.invtweaks.InvTweaksMod.LOGGER.info(message)
                //We can't break or return because maybe we can spread the items in place from previously replenished slots
            } else {
                ranOutOfMaterials = false
            }
        }

        return if (ranOutOfMaterials) {
            ReplenishResult.NO_MATERIALS
        } else {
            ReplenishResult.SUCCESS
        }
    }

    fun massCraft(resultSlot: CraftingResultSlot, intent: Intent): OperationResult {
        val craftingSI = intent.context.otherInventories.craftingSI.orElse(null)
                ?: return OperationResult.failure("No crafting inventory found")
        val currentRecipe = intent.massCraftRecipe ?: Recipe(craftingSI.stacks, resultSlot.stack.copy())
        val craftingResultSI = intent.context.otherInventories.craftingResultSI.orElse(null)
                ?: return OperationResult.failure("No crafting result inventory found")
        val resourcesSI = intent.context.otherInventories.playerCombinedSI

        intent.massCraftRecipe = currentRecipe

        val replenishOperation = ReplenishRecipeOperation(
                ReplenishData(
                        originalIntent = intent,
                        recipe = currentRecipe,
                )
        )


        var intentType = IntentType.MOVE_STACK
        var slotActionType = SlotActionType.QUICK_MOVE
        val canResourcesFit = InventoryAnalyzer.canFit(resourcesSI.stacks, currentRecipe.output)
        if (!canResourcesFit) {
//            return OperationResult.failure("Not enough space in resources inventory")
            intentType = IntentType.DROP_STACK
            slotActionType = SlotActionType.THROW
        }
        val craftIntent = Intent(
                type = intentType,
                context = IntentContext(
                        screenHandler = craftingResultSI.screenHandler,
                        clickedSlot = resultSlot,
                        otherInventories = intent.context.otherInventories,
                        targetSI = resourcesSI,
                        clickedSI = craftingResultSI,
                        slotActionType = slotActionType,
                        button = 0,
                )
        )

        val craftOperation = CraftOperation(
                CraftData(
                        originalIntent = intent,
                        craftIntent = craftIntent,
                        recipe = currentRecipe,
                )
        )

        val operations = listOf(
                replenishOperation,
                DelayOperation(
                        AndOperation(
                                craftOperation,
                                DelayOperation(replenishOperation, 3u),
                                DelayOperation(IntentedOperation(intent), 7u),
                        ),
                        2u)
        )
        return OperationResult(
                success = OperationResult.SuccessType.SUCCESS,
                message = "Requesting replenish operation",
                nextOperations = operations
        )
    }
}