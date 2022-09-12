package io.github.marcuscastelo.invtweaks.mixin

import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.InvTweaksMod.Companion.LOGGER
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig.OverflowMode
import io.github.marcuscastelo.invtweaks.input.InputProvider
import io.github.marcuscastelo.invtweaks.input.OperationTypeInterpreter
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationResult
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry.executeOperation
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry.isScreenRegistered
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import io.github.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundEvents
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

@Mixin(HandledScreen::class)
abstract class MixinHandledScreen<T: ScreenHandler> {
    private val MIDDLE_CLICK = GLFW.GLFW_MOUSE_BUTTON_MIDDLE

    @Shadow
    @Final
    protected lateinit var handler: T

    @Shadow
    abstract fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean

    private var _middleClickBypass = false
    private fun isBypassActive() = _middleClickBypass

    private fun isTryingToCloneItem(button: Int): Boolean {
        val isCloneBtn = MinecraftClient.getInstance().options.pickItemKey.matchesMouse(button)
        val isInCreative = MinecraftClient.getInstance().interactionManager!!.hasCreativeInventory()
        return isCloneBtn && isInCreative
    }

    private fun runMiddleClickAsLeftClick(mouseX: Double, mouseY: Double, button: Int, cir: CallbackInfoReturnable<Boolean>) {
        //Informs this class that the middle click is going to be handled
        _middleClickBypass = true

        //Simulates left click (which will be treated as middle click by us because of the hack)
        val returnValue = mouseClicked(mouseX, mouseY, 0)

        //Informs this class that the middle click was successfully handled
        _middleClickBypass = false

        //Do not execute the original code for the initial click
        cir.cancel()
        cir.returnValue = returnValue
    }

    /**
     * Minecraft's original code filters out middle clicks, but we want to handle them.
     * This method bypasses the filter.
     */
    private fun bypassMiddleClickBarrier(mouseX: Double, mouseY: Double, button: Int, cir: CallbackInfoReturnable<Boolean>) {

        //Do not handle middle click if the player is trying to clone an item
        if (isTryingToCloneItem(button)) return

        //Other buttons that are not a middle click are handled by the original method
        if (button != MIDDLE_CLICK) return

        //Here, we handle the middle click
        runMiddleClickAsLeftClick(mouseX, mouseY, button, cir)
    }

    @Inject(method = ["mouseClicked"], at = [At("HEAD")], cancellable = true)
    protected open fun mouseClicked(mouseX: Double, mouseY: Double, button: Int, cir: CallbackInfoReturnable<Boolean>) {
        //mouseClicked is called before onMouseClick
        //we use this to bypass the middle click filter
        bypassMiddleClickBarrier(mouseX, mouseY, button, cir)
    }

    private fun isSlotActionTypeSupported(type: SlotActionType): Boolean {
        return (type == SlotActionType.CLONE) || (type == SlotActionType.PICKUP) || (type == SlotActionType.QUICK_MOVE)
    }

    private fun isScreenRegistered() = isScreenRegistered(handler.javaClass)

    private fun isOverflowAllowed(button: Int): Boolean {
        return when (InvtweaksConfig.getOverflowMode()) {
            OverflowMode.ALWAYS -> true
            OverflowMode.NEVER -> false
            OverflowMode.ON_RIGHT_CLICK -> button == 1
            null -> {
                InvTweaksMod.LOGGER.info("Overflow mode is null! Using default value (ALWAYS)")
                InvtweaksConfig.setOverflowMode(OverflowMode.ALWAYS)
                isOverflowAllowed(button)
            }
        }
    }

    private fun getVerticalTrend(): Int {
        return if (isKeyPressed(GLFW.GLFW_KEY_W)) 1 else if (isKeyPressed(GLFW.GLFW_KEY_S)) -1 else 0
    }

    private fun getTargetInventory(clickedSI: ScreenInventory, screenInventories: ScreenInventories, allowOverflow: Boolean): ScreenInventory? {
        return when (val verticalTrend = getVerticalTrend()) {
            0 -> screenInventories.getOppositeInventory(clickedSI, allowOverflow)
            1 -> screenInventories.getInventoryUpwards(clickedSI, allowOverflow)
            -1 -> screenInventories.getInventoryDownwards(clickedSI, allowOverflow)
            else -> throw IllegalStateException("Unexpected value: $verticalTrend")
        }
    }

    private fun debugPrintScreenHandlerInfo(invs: ScreenInventories) {
        warnPlayer(handler.javaClass.name)
        warnPlayer("Inventories:")
        invs.allInvs().forEach { inv: ScreenInventory -> warnPlayer(inv.javaClass.name) }
    }

    var queuedOperations = mutableListOf<OperationInfo>()

    @Inject(method = ["onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V"], at = [At("HEAD")], cancellable = true)
    protected open fun onMouseClick(slot: Slot?, invSlot: Int, pressedButton: Int, actionType: SlotActionType?, ci: CallbackInfo) {
        //In case of clicking outside of inventory, just ignore
        var pressedButton = pressedButton
        var actionType = actionType

        if (slot == null) return //Ignore clicks outside of inventory
        if (pressedButton !in 0..2) return  //Only left, right and middle clicks are handled

        //Bypass the middle click filter, so that we can handle the middle click
        if (isBypassActive()) {
            pressedButton = MIDDLE_CLICK
            actionType = SlotActionType.CLONE
        }

        //We do not handle pickup all, so we can just call the original method
        if (!isSlotActionTypeSupported(actionType!!)) return warnPlayer("Ignoring unsupported action type: $actionType")
        if (!isScreenRegistered()) return warnPlayer("This screen is not supported by Marucs' InvTweaks")

        val screenInvs = ScreenInventories(handler)
        val clickedSI = screenInvs.getClickedInventory(slot.id)
        val targetSI = getTargetInventory(clickedSI, screenInvs, isOverflowAllowed(pressedButton))

        val inputProvider = InputProvider(pressedButton)
        val operationType =
                OperationTypeInterpreter.interpret(inputProvider) ?:
                return warnPlayer("Operation type is null!")

        val operationInfo = OperationInfo(operationType, slot, clickedSI, targetSI!!, screenInvs)

        val result = executeAndQueueOperation(operationInfo)
        if (result.success == OperationResult.SuccessType.SUCCESS) {
            ci.cancel()
        }
    }

    fun debugHotKeyTick() {
        if (!isKeyPressed(GLFW.GLFW_KEY_G)) return

        warnPlayer("Current handler: " + handler.javaClass.name)

        val uniqueInventories = mutableSetOf<Inventory>()
        for (slot in handler.slots) {
            uniqueInventories.add(slot.inventory)
            if (slot is CraftingResultSlot) {
                slot.stack = Items.BEDROCK.defaultStack
            }
        }
        warnPlayer("Unique inventories: ${uniqueInventories.size}")

        for (inv in uniqueInventories) {
            warnPlayer("Inventory: ${inv.javaClass}, size: ${inv.size()}")
        }
    }

    @Inject(at = [At("HEAD")], method = ["tick"])
    open fun tick(ci: CallbackInfo?) {
        debugHotKeyTick()


        if (queuedOperations.isEmpty()) return
        val operationInfo = queuedOperations.removeAt(0)

        executeAndQueueOperation(operationInfo)
        //FIXME: try-catch here?

    }

    private fun executeAndQueueOperation(operationInfo: OperationInfo): OperationResult {
        LOGGER.info("Executing queued operation: $operationInfo")
        val result = executeOperation(handler.javaClass, operationInfo)
        result.nextOperations.forEach(Consumer { e: OperationInfo? -> queuedOperations.add(e!!) })
        when (result.success) {
            OperationResult.SuccessType.SUCCESS -> MinecraftClient.getInstance().player!!.playSound(SoundEvents.BLOCK_CHAIN_PLACE, 1.8f, 0.8f + MinecraftClient.getInstance().world!!.random.nextFloat() * 0.4f)
            OperationResult.SuccessType.FAILURE -> MinecraftClient.getInstance().player!!.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.8f, 0.8f + MinecraftClient.getInstance().world!!.random.nextFloat() * 0.4f)
            OperationResult.SuccessType.PASS -> {}
        }

        if (result.success != OperationResult.SuccessType.PASS && result.message.isNotEmpty()) {
            warnPlayer("${result.success}: ${result.message}")
        }
        return result
    }
}
