package com.marcuscastelo.invtweaks.crafting

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

//TODO: support for crafting multiple items (modded recipes)
class Recipe
(stacks: Iterable<ItemStack>, output: ItemStack) {
    private val _stacks: List<ItemStack> =
            stacks.map { it.copy() }.map { it.count = 1; it }.toList()

    private val _output: ItemStack = output.copy()

    val items: List<Item> = stacks.map { it.item }
    val materials: List<Item> = stacks.filter { it.count > 0 }.map { it.item }.filter { it != Items.AIR }.distinct()
    val itemCount: Map<Item, Int> = InventoryAnalyzer.calculateAmountPerItem(stacks)
    val stacks get() = _stacks.map { it.copy() }
    val output get() = _output.copy()
}