package io.github.marcuscastelo.invtweaks.mixin

import com.google.common.collect.Streams
import io.github.marcuscastelo.invtweaks.InvTweaksMod
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig.OverflowMode
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory
import io.github.marcuscastelo.invtweaks.operation.OperationInfo
import io.github.marcuscastelo.invtweaks.operation.OperationModifier
import io.github.marcuscastelo.invtweaks.operation.OperationNature
import io.github.marcuscastelo.invtweaks.operation.OperationType
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry.executeOperation
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry.isScreenSupported
import io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer
import io.github.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*
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

    private fun isScreenSupported() = isScreenSupported(handler.javaClass)

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

        if (slot == null) return
        if (pressedButton != 0 && pressedButton != 1 && pressedButton != 2) return  //Only left, right and middle clicks are handled
        //Bypass the middle click filter, so that we can handle the middle click
        if (isBypassActive()) {
            pressedButton = MIDDLE_CLICK
            actionType = SlotActionType.CLONE
        }

        //We do not handle pickup all, so we can just call the original method
        if (!isSlotActionTypeSupported(actionType!!)) return
        if (!isScreenSupported()) {
            warnPlayer("This screen is not supported by Marucs' InvTweaks")
            warnPlayer("This screen is not supported by Marucs' InvTweaks")
            warnPlayer("This screen is not supported by Marucs' InvTweaks")
            warnPlayer("This screen is not supported by Marucs' InvTweaks")
            while (--pressedButton >= 0) {
                warnPlayer("This screen is not supported by Marucs' InvTweaks")
            }
            return
        }
        val screenInvs = ScreenInventories(handler)
        val clickedSI = screenInvs.getClickedInventory(slot.id)
        val targetSI = getTargetInventory(clickedSI, screenInvs, isOverflowAllowed(pressedButton))
        if (isKeyPressed(GLFW.GLFW_KEY_F1)) {
            debugPrintScreenHandlerInfo(screenInvs)
        } else if (isKeyPressed(GLFW.GLFW_KEY_F2)) {
            warnPlayer("Current slot = " + slot + ", id = " + slot.id)
            warnPlayer("Clicked SI = $clickedSI")
            warnPlayer("Target SI = $targetSI")
        }
        val operationType_: Optional<OperationType> = getOperationType(pressedButton)
        if (operationType_.map { obj: OperationType -> obj.isIgnore() }.orElse(false)) return
        val operationType = operationType_.orElseThrow()
        val operationInfo = OperationInfo(operationType, slot, clickedSI, targetSI!!, screenInvs)
        try {
            val result = executeOperation(handler.javaClass, operationInfo)
            result.nextOperations.forEach(Consumer { e: OperationInfo? -> queuedOperations.add(e!!) })
            if (result.success()) {
                ci.cancel()
            }
        } catch (e: IllegalArgumentException) {
            warnPlayer("Operation not supported: " + e.message)
            warnPlayer("Operation info: $operationInfo")
            warnPlayer("Operation type: $operationType")
            warnPlayer("Clicked slot: $slot")
            warnPlayer("Clicked inventory: $clickedSI")
            warnPlayer("Other inventory: $targetSI")
            warnPlayer("Handler: $handler")
        }
    }

    private fun assertOnlyOneBool(vararg booleans: Boolean): Boolean {
        var count = 0
        for (b in booleans) {
            if (b) count++
            if (count > 1) return false
        }
        return true
    }

    //TODO: Make this a config option
    private fun getOperationType(pressedButton: Int): Optional<OperationType> {
        val nature = getOperationNature(pressedButton).orElse(null)
        val target = getOperationModifier(pressedButton).orElse(null)
        return OperationType.fromPair(nature, target)
    }

    private fun isDropOperation(): Boolean {
        return Screen.hasAltDown()
    }

    private fun getOperationNature(pressedButton: Int): Optional<OperationNature> {
        val operationNature = OperationNature.IGNORE
        val drop: Boolean = isDropOperation()
        return when (pressedButton) {
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Optional.of(OperationNature.SORT)
            GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Optional.of(
                    if (drop) OperationNature.DROP else OperationNature.MOVE
            )
            else -> Optional.of(OperationNature.IGNORE)
        }
    }

    private fun getOperationModifier(pressedButton: Int): Optional<OperationModifier> {
        val appliesToOne = OperationModifier.ONE.applies()
        val appliesToSameType = OperationModifier.ALL_SAME_TYPE.applies()
        val appliesToStack = OperationModifier.STACK.applies()
        val appliesToAll = OperationModifier.ALL.applies()
        if (!assertOnlyOneBool(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            warnPlayer("Unknown combination pressed: applyToOne=$appliesToOne, applyToSameType=$appliesToSameType, applyToStack=$appliesToStack, applyToAll=$appliesToAll")
            return Optional.empty()
        }
        if (appliesToOne) return Optional.of(OperationModifier.ONE)
        if (appliesToSameType) return Optional.of(OperationModifier.ALL_SAME_TYPE)
        if (appliesToStack) return Optional.of(OperationModifier.STACK)
        if (appliesToAll) return Optional.of(OperationModifier.ALL)
        val drop: Boolean = isDropOperation()
        val isMoveUpOrDown = isKeyPressed(GLFW.GLFW_KEY_W) || isKeyPressed(GLFW.GLFW_KEY_S)
        val stackIsTheNormal = drop || isMoveUpOrDown
        return Optional.of(if (stackIsTheNormal) OperationModifier.STACK else OperationModifier.NORMAL)
    }

    @Inject(at = [At("HEAD")], method = ["tick"])
    open fun tick(ci: CallbackInfo?) {
        if (queuedOperations.isEmpty()) return
        val operationInfo = queuedOperations.removeAt(0)
        InvTweaksMod.LOGGER.info("Executing queued operation: $operationInfo")
        try {
            val result = executeOperation(handler.javaClass, operationInfo)
            result.nextOperations.forEach(Consumer { e: OperationInfo? -> queuedOperations.add(e!!) })
        } catch (ignored: IllegalArgumentException) {
        }
    }
}
