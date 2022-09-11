package io.github.marcuscastelo.invtweaks.registry

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.api.ScreenInventoriesSpecification
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification
import io.github.marcuscastelo.invtweaks.behavior.InvTweaksVanillaCraftingBehavior
import io.github.marcuscastelo.invtweaks.behavior.InvTweaksVanillaGenericBehavior
import io.github.marcuscastelo.invtweaks.behavior.InvTweaksVanillaMerchantBehavior
import io.github.marcuscastelo.invtweaks.behavior.InvTweaksVanillaPlayerBehaviour
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.FAILURE
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.failure
import net.minecraft.screen.*

object InvTweaksBehaviorRegistry {
    var screenBehaviorMap = HashMap<Class<out ScreenHandler?>, ScreenSpecification>()
    fun register(screenSpecification: ScreenSpecification): ScreenSpecification {
        require(!screenBehaviorMap.containsKey(screenSpecification.handlerClass)) { "Screen " + screenSpecification.handlerClass + " is already registered" }
        screenBehaviorMap[screenSpecification.handlerClass] = screenSpecification
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
            failure("Operation " + operationInfo.type + " is not supported by " + behavior.javaClass)
        }
    }

    @JvmStatic
    fun isScreenSupported(screenHandlerClass: Class<out ScreenHandler?>): Boolean {
        return screenBehaviorMap.containsKey(screenHandlerClass)
    }

    fun createSpecsBuilder(handlerClass: Class<out ScreenHandler?>?): ScreenSpecification.Builder {
        return ScreenSpecification.Builder(handlerClass)
    }

    fun createVanillaGenericSpecsBuilder(handlerClass: Class<out ScreenHandler?>?): ScreenSpecification.Builder {
        return createSpecsBuilder(handlerClass).withBehavior(InvTweaksVanillaGenericBehavior())
    }

    fun buildVanillaGeneric(handlerClass: Class<out ScreenHandler?>?): ScreenSpecification {
        return createVanillaGenericSpecsBuilder(handlerClass).build()
    }

    init {
        //Default behaviour
        register(buildVanillaGeneric(GenericContainerScreenHandler::class.java))
        register(buildVanillaGeneric(ShulkerBoxScreenHandler::class.java))
        register(buildVanillaGeneric(BrewingStandScreenHandler::class.java))
        register(buildVanillaGeneric(BeaconScreenHandler::class.java))
        register(buildVanillaGeneric(HopperScreenHandler::class.java))
        register(buildVanillaGeneric(Generic3x3ContainerScreenHandler::class.java))
        register(buildVanillaGeneric(FurnaceScreenHandler::class.java))
        register(buildVanillaGeneric(SmokerScreenHandler::class.java))
        register(buildVanillaGeneric(BlastFurnaceScreenHandler::class.java))
        register(buildVanillaGeneric(EnchantmentScreenHandler::class.java))
        register(buildVanillaGeneric(AnvilScreenHandler::class.java))
        register(buildVanillaGeneric(CartographyTableScreenHandler::class.java))
        register(buildVanillaGeneric(LoomScreenHandler::class.java))
        register(buildVanillaGeneric(SmithingScreenHandler::class.java))
        register(buildVanillaGeneric(GrindstoneScreenHandler::class.java))
        register(buildVanillaGeneric(HorseScreenHandler::class.java))
        val playerScreenInvSpecs = ScreenInventoriesSpecification(true, 27, 9)
        register(
                createSpecsBuilder(PlayerScreenHandler::class.java)
                        .withBehavior(InvTweaksVanillaPlayerBehaviour())
                        .withInventoriesSpecification(playerScreenInvSpecs)
                        .build()
        )


        //Merchant behaviour
        register(createSpecsBuilder(MerchantScreenHandler::class.java).withBehavior(InvTweaksVanillaMerchantBehavior()).build())


        //Crafting behaviour
        register(createSpecsBuilder(CraftingScreenHandler::class.java).withBehavior(InvTweaksVanillaCraftingBehavior()).build())
        register(createSpecsBuilder(StonecutterScreenHandler::class.java).withBehavior(InvTweaksVanillaCraftingBehavior()).build())
    }
}