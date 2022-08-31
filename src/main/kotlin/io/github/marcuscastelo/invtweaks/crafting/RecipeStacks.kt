package io.github.marcuscastelo.invtweaks.crafting

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class RecipeStacks(initialStacks: List<ItemStack>) {
    var recipeStacks: List<ItemStack> = initialStacks
        private set
        get() {
            return field.map { it.copy() }
        }

    val recipeItems: List<Item>
        get() = recipeStacks.map { it.item }

    val materialList: List<Item>
        get() = recipeStacks.filter { it.count > 0 }.map { it.item }.filter { it != Items.AIR }.distinct()

    init {
        recipeStacks = initialStacks.map { it.copy() }
        recipeStacks.forEach { it.count = 1 } //TODO: support for crafting multiple items (modded recipes)
    }
}