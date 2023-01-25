package com.marcuscastelo.invtweaks.mixin;

import com.google.common.collect.Streams;
import com.marcuscastelo.invtweaks.InvTweaksMod;
import com.marcuscastelo.invtweaks.input.InputProvider;
import com.marcuscastelo.invtweaks.input.OperationTypeInterpreter;
import com.marcuscastelo.invtweaks.operation.*;
import com.marcuscastelo.invtweaks.config.InvtweaksConfig;
import com.marcuscastelo.invtweaks.inventory.ScreenInventories;
import com.marcuscastelo.invtweaks.inventory.ScreenInventory;
import com.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import com.marcuscastelo.invtweaks.util.KeyUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.marcuscastelo.invtweaks.util.ChatUtils.warnPlayer;
import static com.marcuscastelo.invtweaks.util.KeyUtils.isKeyPressed;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> implements ParentElement {
    private final int MIDDLE_CLICK = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

    @Shadow
    @Final
    protected T handler;

    private boolean _middleClickBypass = false;

    private boolean isBypassActive() {
        return _middleClickBypass;
    }

    private boolean isTryingToCloneItem(int button) {
        boolean isCloneBtn = MinecraftClient.getInstance().options.pickItemKey.matchesMouse(button);
        boolean isInCreative = MinecraftClient.getInstance().interactionManager.hasCreativeInventory();
        return isCloneBtn && isInCreative;
    }

    private void runMiddleClickAsLeftClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        //Informs this class that the middle click is going to be handled
        _middleClickBypass = true;

        //Simulates left click (which will be treated as middle click by us because of the hack)
        boolean returnValue = this.mouseClicked(mouseX, mouseY, 0);

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

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    protected void invtweaks$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        //mouseClicked is called before onMouseClick
        //we use this to bypass the middle click filter
        bypassMiddleClickBarrier(mouseX, mouseY, button, cir);
    }

    private boolean isSlotActionTypeSupported(SlotActionType type) {
        return type == SlotActionType.CLONE || type == SlotActionType.PICKUP || type == SlotActionType.QUICK_MOVE;
    }

    private boolean isScreenRegistered() {
        return InvTweaksBehaviorRegistry.INSTANCE.isScreenRegistered(this.handler.getClass());
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
        invs.allInvs().forEach(inv -> warnPlayer(inv.getClass().getName()));
    }

    ArrayList<OperationInfo> queuedOperations = new ArrayList<>();

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void onMouseClick(Slot slot, int invSlot, int pressedButton, SlotActionType actionType, CallbackInfo ci) {
        //In case of clicking outside of inventory, just ignore
        if (slot == null) return;
        if (pressedButton != 0 && pressedButton != 1 && pressedButton != 2)
            return; //Only left, right and middle clicks are handled
        //Bypass the middle click filter, so that we can handle the middle click
        if (isBypassActive()) {
            pressedButton = MIDDLE_CLICK;
            actionType = SlotActionType.CLONE;
        }

        //We do not handle pickup all, so we can just call the original method
        if (!isSlotActionTypeSupported(actionType)) return;
        if (!isScreenRegistered()) {
            warnPlayer("This screen is not supported by Marucs' InvTweaks");
            return;
        }

        ScreenInventories screenInvs = new ScreenInventories(this.handler);
        ScreenInventory clickedSI = screenInvs.getClickedInventory(slot.id);
        ScreenInventory targetSI = getTargetInventory(clickedSI, screenInvs, isOverflowAllowed(pressedButton));

        if (targetSI == null) {
            warnPlayer("Target inventory is null");
            return;
        }

        if (isKeyPressed(GLFW.GLFW_KEY_F1)) {
            debugPrintScreenHandlerInfo(screenInvs);
        } else if (isKeyPressed(GLFW.GLFW_KEY_F2)) {
            warnPlayer("Current slot = " + slot + ", id = " + slot.id);
            warnPlayer("Clicked SI = " + clickedSI);
            warnPlayer("Target SI = " + targetSI);
        }

        InputProvider inputProvider = new InputProvider(pressedButton);
        OperationType operationType = OperationTypeInterpreter.INSTANCE.interpret(inputProvider);
        if (operationType == null) {
            warnPlayer("Operation type is null");
            return;
        }

        OperationInfo operationInfo = new OperationInfo(
                operationType, slot, clickedSI, targetSI, screenInvs, null
        );

        try {
            OperationResult result = executeAndQueueOperation(operationInfo);
            if (result.getSuccess() == OperationResult.SuccessType.SUCCESS) {
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

    public void debugHotKeyTick() {
        if (!isKeyPressed(GLFW.GLFW_KEY_G)) return;

        warnPlayer("Current handler: " + handler.getClass().getName());

        Set<Inventory> uniqueInventories = new HashSet<>();
        for (Slot slot : this.handler.slots) {
            uniqueInventories.add(slot.inventory);
            if (slot instanceof CraftingResultSlot) {
                slot.setStack(Items.BEDROCK.getDefaultStack());
            }
        }
        warnPlayer("Unique inventories: " + uniqueInventories.size());

        for (Inventory inv : uniqueInventories) {
            warnPlayer("Inventory: " + inv.getClass().getName() + ", size: " + inv.size());
        }
    }


    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        debugHotKeyTick();

        if (queuedOperations.isEmpty()) return;
        OperationInfo operationInfo = queuedOperations.remove(0);
        InvTweaksMod.getLOGGER().info("Executing queued operation: " + operationInfo);

        try {
            OperationResult result = executeAndQueueOperation(operationInfo);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private OperationResult executeAndQueueOperation(OperationInfo operationInfo) {
        InvTweaksMod.getLOGGER().info("Executing operation: $operationInfo");
        OperationResult result = InvTweaksBehaviorRegistry.INSTANCE.executeOperation(handler.getClass(), operationInfo);
        result.getNextOperations().forEach(e -> queuedOperations.add(e));
        switch (result.getSuccess()) {
            case SUCCESS ->
                    MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_CHAIN_PLACE, 1.8f, 0.8f + MinecraftClient.getInstance().world.random.nextFloat() * 0.4f);
            case FAILURE ->
                    MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.8f, 0.8f + MinecraftClient.getInstance().world.random.nextFloat() * 0.4f);
            case PASS -> {
            }
        }
        ;

        if (result.getSuccess() != OperationResult.SuccessType.PASS && (!result.getMessage().isEmpty())) {
            warnPlayer("${result.success}: ${result.message}");
        }
        return result;
    }
}