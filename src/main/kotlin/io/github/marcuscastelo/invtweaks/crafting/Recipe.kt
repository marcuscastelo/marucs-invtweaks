package io.github.marcuscastelo.invtweaks.crafting

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class Recipe//TODO: support for crafting multiple items (modded recipes)
(stacks: Iterable<ItemStack>) {
    private val _stacks: List<ItemStack> =
            stacks.map { it.copy() }.map { it.count = 1; it }.toList()

    val items: List<Item> = stacks.map { it.item }
    val materials: List<Item> = stacks.filter { it.count > 0 }.map { it.item }.filter { it != Items.AIR }.distinct()

    val stacks get() = _stacks.map { it.copy() }
}