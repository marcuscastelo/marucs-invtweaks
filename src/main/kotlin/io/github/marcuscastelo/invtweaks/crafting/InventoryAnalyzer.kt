package io.github.marcuscastelo.invtweaks.crafting

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object InventoryAnalyzer {
    fun countItems(stacks: Iterable<ItemStack>): Map<Item, Int> {
        val itemCounts = mutableMapOf<Item, Int>()
        for (stack in stacks) {
            if (stack.isEmpty) continue
            val item = stack.item

            val count = itemCounts[item] ?: 0
            itemCounts[item] = count + stack.count
        }
        return itemCounts
    }

    fun countSlots(stacks: Iterable<ItemStack>): Map<Item, Int> {
        val slotCounts = mutableMapOf<Item, Int>()
        for (stack in stacks) {
            if (stack.isEmpty) continue
            val item = stack.item

            val count = slotCounts[item] ?: 0
            slotCounts[item] = count + 1
        }
        return slotCounts
    }

    fun searchRecipeItems(resourceStacks: Iterable<ItemStack>, recipe: Recipe): Map<Item, Iterable<Int>> {
        val entries = resourceStacks.mapIndexed(::Pair).filter { it.second.item in recipe.materials }
        return entries.groupBy({ it.second.item }, { it.first })
    }
}