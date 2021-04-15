package io.github.marcuscastelo.invtweaks.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackUtils {
    public enum MergeType {
        NO_MERGE,
        MERGE_NO_OVERFLOW,
        MERGE_OVERFLOW
    };

    public static boolean canStacksMerge(ItemStack itemStack1, ItemStack itemStack2) {
        MergeType mergeType = getMergeType(itemStack1, itemStack2);
        return mergeType == MergeType.MERGE_NO_OVERFLOW || mergeType == MergeType.MERGE_OVERFLOW;
    }

    public static boolean canStacksMergeNoOverflow(ItemStack itemStack1, ItemStack itemStack2) {
        return getMergeType(itemStack1, itemStack2) == MergeType.MERGE_NO_OVERFLOW;
    }

    public static MergeType getMergeType(ItemStack itemStack1, ItemStack itemStack2) {
        boolean oneEmpty = itemStack1.isEmpty() || itemStack2.isEmpty();
        boolean sameItem = itemStack1.getItem() == itemStack2.getItem();
        boolean tagsEqual = ItemStack.areTagsEqual(itemStack1, itemStack2);
        boolean stackable = itemStack1.isStackable() && itemStack2.isStackable();
        boolean mergeDoesNotOverflow = itemStack1.getCount() + itemStack2.getCount() < itemStack2.getMaxCount();

        if (oneEmpty) return MergeType.MERGE_NO_OVERFLOW;
        if (!stackable || !sameItem || !tagsEqual) return MergeType.NO_MERGE;
        if (!mergeDoesNotOverflow) return MergeType.MERGE_OVERFLOW;
        return  MergeType.MERGE_NO_OVERFLOW;
    }

    public static boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return existingStack.isEmpty() || ItemStack.areItemsEqual(existingStack, stack) && ItemStack.areTagsEqual(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < 64;
    }

}
