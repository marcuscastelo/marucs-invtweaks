package io.github.marcuscastelo.invtweaks.util

import net.minecraft.item.ItemStack

object ItemStackUtils {
    @JvmStatic
    fun canStacksMerge(itemStack1: ItemStack, itemStack2: ItemStack): Boolean {
        val mergeType = getMergeType(itemStack1, itemStack2)
        return mergeType == MergeType.MERGE_NO_OVERFLOW || mergeType == MergeType.MERGE_OVERFLOW
    }

    fun canStacksMergeNoOverflow(itemStack1: ItemStack, itemStack2: ItemStack): Boolean {
        return getMergeType(itemStack1, itemStack2) == MergeType.MERGE_NO_OVERFLOW
    }

    fun getMergeType(itemStack1: ItemStack, itemStack2: ItemStack): MergeType {
        val oneEmpty = itemStack1.isEmpty || itemStack2.isEmpty
        val sameItem = itemStack1.item === itemStack2.item
        val stackable = itemStack1.isStackable && itemStack2.isStackable
        val mergeDoesNotOverflow = itemStack1.count + itemStack2.count < itemStack2.maxCount
        if (oneEmpty) return MergeType.MERGE_NO_OVERFLOW
        if (!stackable || !sameItem) return MergeType.NO_MERGE
        return if (!mergeDoesNotOverflow) MergeType.MERGE_OVERFLOW else MergeType.MERGE_NO_OVERFLOW
    }

    @JvmStatic
    fun canStackAddMore(existingStack: ItemStack, stack: ItemStack?): Boolean {
        return existingStack.isEmpty || ItemStack.areItemsEqual(existingStack, stack) && existingStack.isStackable && existingStack.count < existingStack.maxCount && existingStack.count < 64
    }

    enum class MergeType {
        NO_MERGE, MERGE_NO_OVERFLOW, MERGE_OVERFLOW
    }
}