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

    fun searchRecipeItems(resourceStacks: Iterable<ItemStack>, recipe: Recipe): List<ItemStack> {
        return resourceStacks.filter { it.item in recipe.materials }
    }
}