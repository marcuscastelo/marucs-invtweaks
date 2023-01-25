package com.marcuscastelo.invtweaks.intent

import com.marcuscastelo.invtweaks.crafting.Recipe
import com.marcuscastelo.invtweaks.inventory.ScreenInventories
import com.marcuscastelo.invtweaks.inventory.ScreenInventory
import net.minecraft.screen.slot.Slot

data class Intent(
        val type: IntentType,
        val context: IntentContext,
        var massCraftRecipe: Recipe? = null, // TODO: Refactor to use a better way to handle mass crafting instead of this
)