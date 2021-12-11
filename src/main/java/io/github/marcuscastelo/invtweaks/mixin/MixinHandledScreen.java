package io.github.marcuscastelo.invtweaks.mixin;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.config.InvtweaksConfig;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        boolean isCloneBtn = MinecraftClient.getInstance().options.keyPickItem.matchesMouse(button);
        boolean isInCreative = MinecraftClient.getInstance().interactionManager.hasCreativeInventory();

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
        System.out.println("Mouse clicked: " + button);
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

    private void warnPlayer(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        player.sendMessage(new LiteralText(message), false);
    }

    private boolean isOverflowAllowed(int button) {
        return switch (InvtweaksConfig.getOverflowMode()) {
            case ALWAYS -> true;
            case NEVER -> false;
            case ON_RIGHT_CLICK -> button == 1;
        };
    }

    private ScreenInventory getTargetInventory(ScreenInventory clickedSI, ScreenInventories screenInventories, boolean allowOverflow) {
        int verticalTrend = isKeyPressed(GLFW.GLFW_KEY_W) ? 1 : isKeyPressed(GLFW.GLFW_KEY_S) ? -1 : 0;

        return switch (verticalTrend) {
            case 0 -> screenInventories.getOppositeInventory(clickedSI, allowOverflow);
            case 1 -> screenInventories.getInventoryUpwards(clickedSI, allowOverflow);
            case -1 -> screenInventories.getInventoryDownwards(clickedSI, allowOverflow);
            default -> throw new IllegalStateException("Unexpected value: " + verticalTrend);
        };
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

        System.out.println("Is cursor dragging: " + this.cursorDragging);

        //We do not handle pickup all, so we can just call the original method
        if (!isSlotActionTypeSupported(actionType)) return;

        if (slot instanceof CraftingResultSlot)
        {
            if (!(this.handler instanceof CraftingScreenHandler))
            {
                warnPlayer("Please tell Marucs that there are CraftingResultSlot in a non-crafting screen handler");
                warnPlayer("Current handler: " + this.handler.getClass().getName());
            }
        }

        if (!isScreenSupported()) {
            warnPlayer("This screen is not supported by Marucs' InvTweaks");
            return;
        }

        ScreenInventories screenInvs = new ScreenInventories(this.handler);

        //TODO: make this generic instead of hardcoded:
        if (handler.getClass().equals(PlayerScreenHandler.class)) {
            if (invSlot >= 5 && invSlot < 9) return;
        }

        ScreenInventory clickedSI = screenInvs.getClickedInventory(slot.id);
        ScreenInventory targetSI = getTargetInventory(clickedSI, screenInvs, isOverflowAllowed(pressedButton));

        InvTweaksOperationType operationType = getOperationType(pressedButton);

        if (operationType == InvTweaksOperationType.NONE)
            return; //Use vanilla behavior for these operations

        InvTweaksOperationInfo operationInfo = new InvTweaksOperationInfo(operationType, slot, clickedSI, targetSI);

        try {
            InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
        } catch (IllegalArgumentException e) {
            warnPlayer("Operation not supported: " + e.getMessage());
            warnPlayer("Operation info: " + operationInfo);
            warnPlayer("Operation type: " + operationType);
            warnPlayer("Clicked slot: " + slot);
            warnPlayer("Clicked inventory: " + clickedSI);
            warnPlayer("Other inventory: " + targetSI);
            warnPlayer("Handler: " + handler);
        }

        ci.cancel();
    }

    private boolean isKeyPressed(int key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key);
    }

    private boolean assertOnlyOneBool(boolean... booleans) {
        int count = 0;
        for (boolean b : booleans) {
            if (b) count++;
            if (count > 1) return false;
        }
        return true;
    }

    //TODO: Make this a config option
    private InvTweaksOperationType getOperationType(int pressedButton) {
        InvTweaksOperationType operationType;

        if (pressedButton == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            operationType = InvTweaksOperationType.SORT;
        }
        else if (pressedButton == 0 || pressedButton == 1) {
            operationType = getSubOperationType(pressedButton);
        }
        else {
            warnPlayer("Unknown button pressed: " + pressedButton);
            operationType = InvTweaksOperationType.NONE;
        }
        return operationType;
    }

    private InvTweaksOperationType getSubOperationType(int pressedButton) {
        boolean appliesToOne = Screen.hasControlDown() && !Screen.hasShiftDown();
        boolean appliesToSameType = Screen.hasControlDown() && Screen.hasShiftDown();
        boolean appliesToStack = !Screen.hasControlDown() && Screen.hasShiftDown();
        boolean appliesToAll = !Screen.hasControlDown() && !Screen.hasShiftDown() && isKeyPressed(GLFW.GLFW_KEY_SPACE);
        boolean drop = Screen.hasAltDown();

        if (!assertOnlyOneBool(appliesToOne, appliesToSameType, appliesToStack, appliesToAll)) {
            warnPlayer("Unknown combination pressed: applyToOne=" + appliesToOne + ", applyToSameType=" + appliesToSameType + ", applyToStack=" + appliesToStack + ", applyToAll=" + appliesToAll);
            return InvTweaksOperationType.NONE;
        }

        InvTweaksOperationType operationType;
        if (appliesToOne) {
            if (drop) operationType = InvTweaksOperationType.DROP_ONE;
            else operationType = InvTweaksOperationType.MOVE_ONE;
        }
        else if (appliesToSameType) {
            if (drop) operationType = InvTweaksOperationType.DROP_ALL_SAME_TYPE;
            else operationType = InvTweaksOperationType.MOVE_ALL_SAME_TYPE;
        }
        else if (appliesToStack) {
            if (drop) operationType = InvTweaksOperationType.DROP_STACK;
            else operationType = InvTweaksOperationType.MOVE_STACK;
        }
        else if (appliesToAll) {
            if (drop) operationType = InvTweaksOperationType.DROP_ALL;
            else operationType = InvTweaksOperationType.MOVE_ALL;
        }
        else {
            if (drop) operationType = InvTweaksOperationType.DROP_STACK;
            else {
                if (isKeyPressed(GLFW.GLFW_KEY_W) || isKeyPressed(GLFW.GLFW_KEY_S)) {
                    operationType = InvTweaksOperationType.MOVE_STACK;
                } else{
                    operationType = InvTweaksOperationType.NONE;
                }
            }
        }

        return operationType;
    }
}
