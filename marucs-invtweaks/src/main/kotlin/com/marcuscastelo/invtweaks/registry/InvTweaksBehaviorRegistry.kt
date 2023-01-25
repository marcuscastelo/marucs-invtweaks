package com.marcuscastelo.invtweaks.registry

import com.marcuscastelo.invtweaks.behavior.*
import com.marcuscastelo.invtweaks.intent.Intent
import com.marcuscastelo.invtweaks.operation.OperationResult
import com.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import com.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler
import net.minecraft.screen.*

object InvTweaksBehaviorRegistry {
    private var behaviorMap = mutableMapOf<Class<out ScreenHandler>, IInvTweaksBehavior>()
    val behaviors: Map<Class<out ScreenHandler>, IInvTweaksBehavior> get() = behaviorMap

    val DEFAULT_BEHAVIOR = InvTweaksVanillaGenericBehavior()

    fun register(handlerClass: Class<out ScreenHandler>, behavior: IInvTweaksBehavior = InvTweaksVanillaGenericBehavior()): IInvTweaksBehavior {
        require(!behaviorMap.containsKey(handlerClass)) { "Screen $handlerClass is already registered" }
        behaviorMap[handlerClass] = behavior
        return behavior
    }

    fun isScreenRegistered(screenHandlerClass: Class<out ScreenHandler>): Boolean {
        return behaviorMap.containsKey(screenHandlerClass)
    }

    init {
        register(GenericContainerScreenHandler::class.java)
        register(ShulkerBoxScreenHandler::class.java)
        register(BrewingStandScreenHandler::class.java)
        register(BeaconScreenHandler::class.java)
        register(HopperScreenHandler::class.java)
        register(Generic3x3ContainerScreenHandler::class.java)
        register(FurnaceScreenHandler::class.java)
        register(SmokerScreenHandler::class.java)
        register(BlastFurnaceScreenHandler::class.java)
        register(AnvilScreenHandler::class.java)
        register(CartographyTableScreenHandler::class.java)
        register(LoomScreenHandler::class.java)
        register(SmithingScreenHandler::class.java)
        register(GrindstoneScreenHandler::class.java)
        register(HorseScreenHandler::class.java)
        register(CraftingScreenHandler::class.java)
        register(StonecutterScreenHandler::class.java)
        register(CreativeScreenHandler::class.java)

        //Survival Inventory
        register(PlayerScreenHandler::class.java, InvTweaksVanillaPlayerBehaviour())

        //Merchant behaviour
        register(MerchantScreenHandler::class.java, InvTweaksVanillaMerchantBehavior())

        //Enchantment behaviour
        register(EnchantmentScreenHandler::class.java, EnchantingTableBehavior())

    }
}