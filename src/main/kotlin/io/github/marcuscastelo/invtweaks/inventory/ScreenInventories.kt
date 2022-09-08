package io.github.marcuscastelo.invtweaks.inventory

import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import net.minecraft.screen.CraftingScreenHandler
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

class ScreenInventories(handler: ScreenHandler) {
    val playerCombinedSI: ScreenInventory
    val playerMainSI: ScreenInventory
    val playerHotbarSI: ScreenInventory

    //TODO: support more than one external inventory
    var storageSI: Optional<ScreenInventory> = Optional.empty()
    var craftingSI: Optional<ScreenInventory> = Optional.empty()
    var craftingResultSI: Optional<ScreenInventory> = Optional.empty()
    var armorSI: Optional<ScreenInventory> = Optional.empty()
    fun extraInvs(): Stream<ScreenInventory> {
        val ao = arrayOf(storageSI, craftingSI, craftingResultSI, armorSI)
        val aos: Stream<Optional<ScreenInventory>> = Arrays.stream(ao)
        return aos.filter { obj: Optional<ScreenInventory> -> obj.isPresent }.map { obj: Optional<ScreenInventory> -> obj.get() }
    }

    fun allInvs(): Stream<ScreenInventory> {
        return Stream.concat(Stream.of(playerCombinedSI, playerMainSI, playerHotbarSI), extraInvs())
    }

    private fun isExtraInv(si: ScreenInventory): Boolean {
        return extraInvs().anyMatch { extraInv: ScreenInventory -> extraInv === si }
    }

    init {
        val screenSpecification = InvTweaksBehaviorRegistry.getScreenSpecs(handler.javaClass)
        val screenSlotCount = handler.slots.size

        //FIXME: this is a hack to get the player inventory size
        val playerMainInvSize = screenSpecification.inventoriesSpecification.playerMainInvSize
        val playerHotbarSize = screenSpecification.inventoriesSpecification.playerHotbarSize
        val playerCombinedInvSize = playerHotbarSize + playerMainInvSize
        var storageInventorySize = screenSlotCount - playerCombinedInvSize

        //TODO: make this generic instead of hardcoded:
        if (handler.javaClass == PlayerScreenHandler::class.java) {
            storageInventorySize = 0
            craftingSI = Optional.of(ScreenInventory(handler, 1, 4))
            craftingResultSI = Optional.of(ScreenInventory(handler, 0, 0))
            armorSI = Optional.of(ScreenInventory(handler, 5, 8))
        } else if (handler.javaClass == CraftingScreenHandler::class.java) {
            storageInventorySize = 0
            craftingSI = Optional.of(ScreenInventory(handler, 1, 9))
            craftingResultSI = Optional.of(ScreenInventory(handler, 0, 0))
            armorSI = Optional.empty()
        } else {
            craftingSI = Optional.empty()
            craftingResultSI = Optional.empty()
            armorSI = Optional.empty()
        }
        storageSI = if (storageInventorySize > 0) Optional.of(ScreenInventory(handler, 0, storageInventorySize - 1)) else Optional.empty()
        val extraInvsStream = extraInvs()
        val extraInvsSize = extraInvsStream.map(ScreenInventory::size).reduce { a: Int, b: Int -> Integer.sum(a, b) }.orElse(0)
        playerCombinedSI = ScreenInventory(handler, extraInvsSize, extraInvsSize + playerCombinedInvSize - 1)
        playerMainSI = ScreenInventory(handler, extraInvsSize, extraInvsSize + playerMainInvSize - 1)
        playerHotbarSI = ScreenInventory(handler, extraInvsSize + playerMainInvSize, extraInvsSize + playerMainInvSize + playerHotbarSize - 1)
    }

    fun getClickedInventory(slotId: Int): ScreenInventory {
        val clickedInventory: ScreenInventory
        val results = extraInvs().filter { (_, start, end): ScreenInventory -> slotId in start..end }.toList()
        val resultCount = results.size.toLong()
        if (resultCount > 1) {
            warnPlayer("Please tell Marucs there is a bug on the code!!")
            warnPlayer("Couldn't determine which inventory was clicked (more than one have slotId = $slotId)")
            warnPlayer("Results: " + results.stream().map(Function<ScreenInventory, String> { it.toString()}).reduce { a: String, s: String -> "$a, $s" })
        }
        clickedInventory = if (resultCount > 0) {
            results[0]
        } else if (slotId <= playerMainSI.end()) {
            playerMainSI
        } else if (slotId <= playerHotbarSI.end()) {
            playerHotbarSI
        } else {
            warnPlayer("Hmm... This is a bug! Couldn't determine which inventory has slotId = $slotId")
            playerHotbarSI //Just to avoid crash
        }
        return clickedInventory
    }

    fun getOppositeInventory(initialSI: ScreenInventory, allowCombined: Boolean): ScreenInventory {
        val playerScreen = isPlayerScreen(initialSI)
        val main = if (allowCombined) playerCombinedSI else playerMainSI
//        val (screenHandler, start, end) = if (allowCombined) playerCombinedSI else playerHotbarSI
        return if (initialSI === playerMainSI) if (playerScreen) playerHotbarSI else extraInvs().findAny().orElse(playerHotbarSI) else if (initialSI === playerHotbarSI) if (playerScreen) playerMainSI else extraInvs().findAny().orElse(playerMainSI) else if (isExtraInv(initialSI)) main else {
            warnPlayer("getOppositeInventory() - Unkown ScreenInventory: $initialSI")
            initialSI
        }
    }

    fun getInventoryUpwards(initialSI: ScreenInventory, allowCombined: Boolean): ScreenInventory {
        val playerScreen = isPlayerScreen(initialSI)
//        val (screenHandler, start, end) = if (allowCombined) playerCombinedSI else playerMainSI
        val hotbar = if (allowCombined) playerCombinedSI else playerHotbarSI
        return if (initialSI === playerMainSI) extraInvs().findAny().orElse(playerHotbarSI) else if (initialSI === playerHotbarSI) playerMainSI else if (isExtraInv(initialSI)) hotbar else {
            warnPlayer("getInventoryUpwards() - Unkown ScreenInventory: $initialSI")
            initialSI
        }
    }

    fun getInventoryDownwards(initialSI: ScreenInventory, allowCombined: Boolean): ScreenInventory {
//        val playerScreen = isPlayerScreen(initialSI)
        val main = if (allowCombined) playerCombinedSI else playerMainSI
//        val (screenHandler, start, end) = if (allowCombined) playerCombinedSI else playerHotbarSI
        return if (initialSI === playerMainSI) playerHotbarSI else if (initialSI === playerHotbarSI) extraInvs().findAny().orElse(playerMainSI) else if (isExtraInv(initialSI)) main else {
            warnPlayer("getInventoryDownwards() - Unkown ScreenInventory: $initialSI")
            initialSI
        }
    }

    companion object {
        private fun isPlayerScreen(si: ScreenInventory): Boolean {
            return si.screenHandler() is PlayerScreenHandler
        }
    }
}