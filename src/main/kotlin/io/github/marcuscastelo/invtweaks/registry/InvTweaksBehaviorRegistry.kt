package io.github.marcuscastelo.invtweaks.registry

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.api.ScreenInventoriesSpecification
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification
import io.github.marcuscastelo.invtweaks.behavior.*
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import net.minecraft.screen.*

object InvTweaksBehaviorRegistry {
    var screenBehaviorMap = HashMap<Class<out ScreenHandler?>, ScreenSpecification>()
    fun register(handlerClass: Class<out ScreenHandler?>, screenSpecification: ScreenSpecification): ScreenSpecification {
        require(!screenBehaviorMap.containsKey(handlerClass)) { "Screen $handlerClass is already registered" }
        screenBehaviorMap[handlerClass] = screenSpecification
        return screenSpecification
    }

    fun getScreenSpecs(screenHandlerClass: Class<out ScreenHandler?>): ScreenSpecification {
        require(isScreenSupported(screenHandlerClass)) { "Screen $screenHandlerClass is not supported" }
        return screenBehaviorMap[screenHandlerClass] ?: throw IllegalStateException("Screen $screenHandlerClass is not registered")
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun executeOperation(screenHandlerClass: Class<out ScreenHandler?>, operationInfo: OperationInfo): OperationResult {
        require(isScreenSupported(screenHandlerClass)) { "Screen $screenHandlerClass doesn't have a behavior" }
        val behavior = getScreenSpecs(screenHandlerClass).invTweaksBehavior
        val executor = operationInfo.type.asOperationExecutor(behavior)

        return executor?.execute(operationInfo) ?: run {
            InvTweaksMod.LOGGER.warn("<InvTweaksBehaviorRegistry> Operation " + operationInfo.type + " is not supported by " + behavior.javaClass)
            pass("Operation " + operationInfo.type + " is not supported by " + behavior.javaClass)
        }
    }

    @JvmStatic
    fun isScreenSupported(screenHandlerClass: Class<out ScreenHandler?>): Boolean {
        return screenBehaviorMap.containsKey(screenHandlerClass)
    }

    init {
        //Default behaviour

        val generic = ScreenSpecification.Builder().withBehavior(InvTweaksVanillaGenericBehavior()).build()

        register(GenericContainerScreenHandler::class.java, generic)
        register(ShulkerBoxScreenHandler::class.java, generic)
        register(BrewingStandScreenHandler::class.java, generic)
        register(BeaconScreenHandler::class.java, generic)
        register(HopperScreenHandler::class.java, generic)
        register(Generic3x3ContainerScreenHandler::class.java, generic)
        register(FurnaceScreenHandler::class.java, generic)
        register(SmokerScreenHandler::class.java, generic)
        register(BlastFurnaceScreenHandler::class.java, generic)
        register(AnvilScreenHandler::class.java, generic)
        register(CartographyTableScreenHandler::class.java, generic)
        register(LoomScreenHandler::class.java, generic)
        register(SmithingScreenHandler::class.java, generic)
        register(GrindstoneScreenHandler::class.java, generic)
        register(HorseScreenHandler::class.java, generic)
        val playerScreenInvSpecs = ScreenInventoriesSpecification(true, 27, 9)
        register(PlayerScreenHandler::class.java,
                ScreenSpecification.Builder()
                        .withBehavior(InvTweaksVanillaPlayerBehaviour())
                        .withInventoriesSpecification(playerScreenInvSpecs)
                        .build()
        )


        //Merchant behaviour
        register(MerchantScreenHandler::class.java, ScreenSpecification.Builder().withBehavior(InvTweaksVanillaMerchantBehavior()).build())

        //Enchantment behaviour
        register(EnchantmentScreenHandler::class.java, ScreenSpecification.Builder().withBehavior(EnchantingTableBehavior()).build())

        //Crafting behaviour
        register(CraftingScreenHandler::class.java, ScreenSpecification.Builder().withBehavior(InvTweaksVanillaCraftingBehavior()).build())
        register(StonecutterScreenHandler::class.java, ScreenSpecification.Builder().withBehavior(InvTweaksVanillaCraftingBehavior()).build())
    }
}