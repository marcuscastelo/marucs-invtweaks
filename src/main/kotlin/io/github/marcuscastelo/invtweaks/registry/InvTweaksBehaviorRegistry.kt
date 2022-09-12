package io.github.marcuscastelo.invtweaks.registry

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.behavior.*
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.operation.OperationResult.Companion.pass
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import net.minecraft.screen.*

object InvTweaksBehaviorRegistry {
    private var behaviorMap = mutableMapOf<Class<out ScreenHandler>, IInvTweaksBehavior>()

    private val DEFAULT_BEHAVIOR = InvTweaksVanillaGenericBehavior()

    fun register(handlerClass: Class<out ScreenHandler>, behavior: IInvTweaksBehavior): IInvTweaksBehavior {
        require(!behaviorMap.containsKey(handlerClass)) { "Screen $handlerClass is already registered" }
        behaviorMap[handlerClass] = behavior
        return behavior
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun executeOperation(screenHandlerClass: Class<out ScreenHandler>, operationInfo: OperationInfo): OperationResult {
        val behavior = behaviorMap[screenHandlerClass] ?: run {
            warnPlayer("Screen $screenHandlerClass doesn't have a behavior");
            DEFAULT_BEHAVIOR
        }

        val executor = operationInfo.type.asOperationExecutor(behavior)

        return executor?.execute(operationInfo) ?: run {
            InvTweaksMod.LOGGER.warn("<InvTweaksBehaviorRegistry> Operation " + operationInfo.type + " is not supported by " + behavior.javaClass)
            pass("Operation " + operationInfo.type + " is not supported by " + behavior.javaClass)
        }
    }

    @JvmStatic
    fun isScreenRegistered(screenHandlerClass: Class<out ScreenHandler>): Boolean {
        return behaviorMap.containsKey(screenHandlerClass)
    }

    init {
        //TODO: refactor this useless function
        fun makeGeneric(behavior: IInvTweaksBehavior = InvTweaksVanillaGenericBehavior()) = behavior

        register(GenericContainerScreenHandler::class.java, makeGeneric())
        register(ShulkerBoxScreenHandler::class.java, makeGeneric())
        register(BrewingStandScreenHandler::class.java, makeGeneric())
        register(BeaconScreenHandler::class.java, makeGeneric())
        register(HopperScreenHandler::class.java, makeGeneric())
        register(Generic3x3ContainerScreenHandler::class.java, makeGeneric())
        register(FurnaceScreenHandler::class.java, makeGeneric())
        register(SmokerScreenHandler::class.java, makeGeneric())
        register(BlastFurnaceScreenHandler::class.java, makeGeneric())
        register(AnvilScreenHandler::class.java, makeGeneric())
        register(CartographyTableScreenHandler::class.java, makeGeneric())
        register(LoomScreenHandler::class.java, makeGeneric())
        register(SmithingScreenHandler::class.java, makeGeneric())
        register(GrindstoneScreenHandler::class.java, makeGeneric())
        register(HorseScreenHandler::class.java, makeGeneric())
        register(PlayerScreenHandler::class.java,makeGeneric(InvTweaksVanillaPlayerBehaviour()))

        //Merchant behaviour
        register(MerchantScreenHandler::class.java, InvTweaksVanillaMerchantBehavior())

        //Enchantment behaviour
        register(EnchantmentScreenHandler::class.java, EnchantingTableBehavior())

        //Crafting behaviour
        register(CraftingScreenHandler::class.java, InvTweaksVanillaCraftingBehavior())
        register(StonecutterScreenHandler::class.java, InvTweaksVanillaCraftingBehavior())
    }
}