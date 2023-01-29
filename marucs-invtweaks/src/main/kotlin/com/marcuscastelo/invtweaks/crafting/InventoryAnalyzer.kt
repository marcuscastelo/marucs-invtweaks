package com.marcuscastelo.invtweaks.crafting

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object InventoryAnalyzer {
    fun calculateAmountPerItem(invStacks: Iterable<ItemStack>): Map<Item, Int> {
        val itemCounts = mutableMapOf<Item, Int>()
        for (stack in invStacks) {
            if (stack.isEmpty) continue
            val item = stack.item

            val count = itemCounts[item] ?: 0
            itemCounts[item] = count + stack.count
        }
        return itemCounts
    }

    fun calculateSlotsUsedPerItem(invStacks: Iterable<ItemStack>): Map<Item, Int> {
        val itemCounts = mutableMapOf<Item, Int>()
        for (stack in invStacks) {
            if (stack.isEmpty) continue
            val item = stack.item

            val count = itemCounts[item] ?: 0
            itemCounts[item] = count + 1
        }
        return itemCounts
    }

    fun searchRecipeItems(resourceStacks: Iterable<ItemStack>, recipe: Recipe): Map<Item, Iterable<Int>> {
        val entries = resourceStacks.mapIndexed(::Pair).filter { it.second.item in recipe.materials }
        return entries.groupBy({ it.second.item }, { it.first })
    }

    fun canFit(invStacks: Iterable<ItemStack>, stackToInsert: ItemStack): Boolean {
        val invStacksList = invStacks.toList()
        val slotsPerItem = calculateSlotsUsedPerItem(invStacksList)
        val amountPerItem = calculateAmountPerItem(invStacksList)

        val itemToInsert = stackToInsert.item

        val totalSlotCount = invStacksList.size
        val remainingSlotCount = totalSlotCount - slotsPerItem.values.sum()

        if (remainingSlotCount > 0) {
            return true
        }

        val itemSlotCount = slotsPerItem[itemToInsert] ?: 0
        val itemAmount = amountPerItem[itemToInsert] ?: 0

        return itemSlotCount > 0 && itemAmount < itemSlotCount * itemToInsert.maxCount
    }
}