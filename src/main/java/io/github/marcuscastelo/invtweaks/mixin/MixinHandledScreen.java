package io.github.marcuscastelo.invtweaks.mixin;

import com.google.common.collect.Streams;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.OperationResult;
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import io.github.marcuscastelo.invtweaks.util.KeyUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static io.github.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer;
import static io.github.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler>{
    private final int MIDDLE_CLICK = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

    @Shadow
    @Final
    protected T handler;

    @Shadow public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    @Shadow protected boolean cursorDragging;
    private boolean _middleClickBypass = false;
    private boolean isBypassActive() { return _middleClickBypass; }

    private boolean isTryingToCloneItem(int button) {
        boolean isCloneBtn = MinecraftClient.getInstance().options.pickItemKey.matchesMouse(button);
        boolean isInCreative = MinecraftClient.getInstance().interactionManager.hasCreativeInventory();

        int a = 2;

        return isCloneBtn && isInCreative;
    }

    private void runMiddleClickAsLeftClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        //Informs this class that the middle click is going to be handled
        _middleClickBypass = true;

        //Simulates left click (which will be treated as middle click by us because of the hack)
        boolean returnValue;
        returnValue = this.mouseClicked(mouseX, mouseY, 0);

        //Informs this class that the middle click was successfully handled
        _middleClickBypass = false;

        //Do not execute the original code for the initial click
        cir.cancel();
        cir.setReturnValue(returnValue);
    }

    /**
     * Minecraft's original code filters out middle clicks, but we want to handle them.
     * This method bypasses the filter.
     */
    private void bypassMiddleClickBarrier(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {

        //Do not handle middle click if the player is trying to clone an item
        if (isTryingToCloneItem(button)) return;

        //Other buttons that are not a middle click are handled by the original method
        if (button != MIDDLE_CLICK) return;

        //Here, we handle the middle click
        runMiddleClickAsLeftClick(mouseX, mouseY, button, cir);
    }

    @Inject(method = "mouseClicked", at=@At("HEAD"), cancellable = true)
    protected void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {
        //mouseClicked is called before onMouseClick
        //we use this to bypass the middle click filter
        bypassMiddleClickBarrier(mouseX, mouseY, button, cir);
    }

    private boolean isSlotActionTypeSupported(SlotActionType type) {
        return type == SlotActionType.CLONE || type == SlotActionType.PICKUP || type == SlotActionType.QUICK_MOVE;
    }

    private boolean isScreenSupported() {
        return InvTweaksBehaviorRegistry.isScreenSupported(this.handler.getClass());
    }



    private boolean isOverflowAllowed(int button) {
        return switch (InvtweaksConfig.getOverflowMode()) {
            case ALWAYS -> true;
            case NEVER -> false;
            case ON_RIGHT_CLICK -> button == 1;
        };
    }

    private static int getVerticalTrend() {
        return isKeyPressed(GLFW.GLFW_KEY_W) ? 1 : isKeyPressed(GLFW.GLFW_KEY_S) ? -1 : 0;
    }

    private ScreenInventory getTargetInventory(ScreenInventory clickedSI, ScreenInventories screenInventories, boolean allowOverflow) {
        int verticalTrend = getVerticalTrend();

        return switch (verticalTrend) {
            case 0 -> screenInventories.getOppositeInventory(clickedSI, allowOverflow);
            case 1 -> screenInventories.getInventoryUpwards(clickedSI, allowOverflow);
            case -1 -> screenInventories.getInventoryDownwards(clickedSI, allowOverflow);
            default -> throw new IllegalStateException("Unexpected value: " + verticalTrend);
        };
    }

    private void debugPrintScreenHandlerInfo(ScreenInventories invs) {
        warnPlayer(handler.getClass().getName());
        warnPlayer("Inventories:");
        warnPlayer("\tHotbar: "+invs.playerHotbarSI);
        warnPlayer("\tMain: "+invs.playerMainSI);
        warnPlayer("\tCrafting: "+invs.craftingSI);
        warnPlayer("\tExternal: "+invs.storageSI);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void onMouseClick(Slot slot, int invSlot, int pressedButton, SlotActionType actionType, CallbackInfo ci) {
        //In case of clicking outside of inventory, just ignore
        if (slot == null) return;
        if (pressedButton != 0 && pressedButton != 1 && pressedButton != 2) return; //Only left, right and middle clicks are handled

        //Bypass the middle click filter, so that we can handle the middle click
        if (isBypassActive()) {
            pressedButton = MIDDLE_CLICK;
            actionType = SlotActionType.CLONE;
        }

        //We do not handle pickup all, so we can just call the original method
        if (!isSlotActionTypeSupported(actionType)) return;

        if (!isScreenSupported()) {
            warnPlayer("This screen is not supported by Marucs' InvTweaks");
            return;
        }

        ScreenInventories screenInvs = new ScreenInventories(this.handler);


        ScreenInventory clickedSI = screenInvs.getClickedInventory(slot.id);
        ScreenInventory targetSI = getTargetInventory(clickedSI, screenInvs, isOverflowAllowed(pressedButton));

        if (isKeyPressed(GLFW.GLFW_KEY_F1)) {
            debugPrintScreenHandlerInfo(screenInvs);
        } else if (isKeyPressed(GLFW.GLFW_KEY_F2)) {
            warnPlayer("Current slot = " + slot + ", id = " + slot.id);
            warnPlayer("Clicked SI = " + clickedSI);
            warnPlayer("Target SI = " + targetSI);
        }

        Optional<InvTweaksOperationType> operationType_ = getOperationType(pressedButton);

        if (operationType_.map(InvTweaksOperationType::isIgnore).orElse(false)) return;

        InvTweaksOperationType operationType = operationType_.orElseThrow();
        InvTweaksOperationInfo operationInfo = new InvTweaksOperationInfo(operationType, slot, clickedSI, targetSI, screenInvs);

        try {
            OperationResult result = InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
            if (result.success()) {
                ci.cancel();
            }
        } catch (IllegalArgumentException e) {
            warnPlayer("Operation not supported: " + e.getMessage());
            warnPlayer("Operation info: " + operationInfo);
            warnPlayer("Operation type: " + operationType);
            warnPlayer("Clicked slot: " + slot);
            warnPlayer("Clicked inventory: " + clickedSI);
            warnPlayer("Other inventory: " + targetSI);
            warnPlayer("Handler: " + handler);
        }

    }


    private static boolean assertOnlyOneBool(boolean... booleans) {
        int count = 0;
        for (boolean b : booleans) {
            if (b) count++;
            if (count > 1) return false;
        }
        return true;
    }

    //TODO: Make this a config option
    private Optional<InvTweaksOperationType> getOperationType(int pressedButton) {
        Optional<InvTweaksOperationType.Nature> nature = getOperationNature(pressedButton);
        Optional<InvTweaksOperationType.Modifier> target = getOperationModifier(pressedButton);

        return Streams.zip(nature.stream(), target.stream(), InvTweaksOperationType::new).findFirst();
    }

    private static boolean isDropOperation() { return Screen.hasAltDown(); }

    private static Optional<InvTweaksOperationType.Nature> getOperationNature(int pressedButton) {
        InvTweaksOperationType.Nature operationNature = InvTweaksOperationType.Nature.IGNORE;
        boolean drop = isDropOperation();

        return switch (pressedButton) {
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE ->
                    Optional.of(InvTweaksOperationType.Nature.SORT);
            case GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT ->
                    Optional.of(
                            drop ?
                                    InvTweaksOperationType.Nature.DROP :
                                    InvTweaksOperationType.Nature.MOVE
                    );
            default -> Optional.of(InvTweaksOperationType.Nature.IGNORE);
        };
    }

    private static Optional<InvTweaksOperationType.Modifier> getOperationModifier(int pressedButton) {
        boolean appliesToOne = Screen.hasControlDown() && !Screen.hasShiftDown();
        boolean appliesToSameType = Screen.hasControlDown() && Screen.hasShiftDown();
        boolean appliesToStack = !Screen.hasControlDown() && Screen.hasShiftDown();
        boolean appliesToAll = !Screen.hasControlDown() && !Screen.hasShiftDown() && isKeyPressed(GLFW.GLFW_KEY_SPACE);

        if (!assertOnlyOneBool(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            warnPlayer("Unknown combination pressed: applyToOne=" + appliesToOne + ", applyToSameType=" + appliesToSameType + ", applyToStack=" + appliesToStack + ", applyToAll=" + appliesToAll);
            return Optional.empty();
        }

        if (appliesToOne) return Optional.of(InvTweaksOperationType.Modifier.ONE);
        if (appliesToSameType) return Optional.of(InvTweaksOperationType.Modifier.ALL_SAME_TYPE);
        if (appliesToStack) return Optional.of(InvTweaksOperationType.Modifier.STACK);
        if (appliesToAll) return Optional.of(InvTweaksOperationType.Modifier.ALL);

        boolean drop = isDropOperation();
        boolean isMoveUpOrDown = KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W) || KeyUtils.isKeyPressed(GLFW.GLFW_KEY_S);

        boolean stackIsTheNormal = drop || isMoveUpOrDown;

        return Optional.of(stackIsTheNormal ? InvTweaksOperationType.Modifier.STACK : InvTweaksOperationType.Modifier.NORMAL);
    }
}
