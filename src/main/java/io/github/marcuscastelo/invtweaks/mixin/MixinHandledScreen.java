package io.github.marcuscastelo.invtweaks.mixin;

import io.github.marcuscastelo.invtweaks.InvTweaksHandledScreen;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventories;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import io.github.marcuscastelo.invtweaks.tests.ITScreenControllerTest;
import io.github.marcuscastelo.invtweaks.util.InvTweaksScreenController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> implements InvTweaksHandledScreen {
    @Shadow @Final protected T handler;

    @Shadow @Final protected PlayerInventory playerInventory;

    protected InvTweaksScreenController controller;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(T handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        controller = new InvTweaksScreenController(handler);
    }

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    protected void onMouseClicked(Slot slot, int invSlot, int button, SlotActionType actionType, CallbackInfo ci) {
        if (actionType == SlotActionType.PICKUP_ALL) return;
        if (!InvTweaksBehaviorRegistry.isScreenSupported(handler.getClass())) {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
            return;
        }

        //In case of clicking outside of inventory, just ignore
        if (slot == null) return;

        ScreenSpecification screenSpecification = InvTweaksBehaviorRegistry.getScreenInfo(handler.getClass());
        ScreenInventories screenInventories = new ScreenInventories(handler);

        InvTweaksOperationInfo operationInfo;

        ScreenInventory clickedInventoryBoundInfo;
        ScreenInventory otherInventoryBoundInfo;

        boolean clickedInventoryIsPlayer;
        //Depending on the index of the clicked slot, determine the clicked inventory
        if (slot.id < containerInvSize) {
            clickedInventoryBoundInfo = containerBoundInfo;
            otherInventoryBoundInfo = playerMainBoundInfo;
            clickedInventoryIsPlayer = false;
        } else if (slot.id < containerInvSize+playerInvSize){
            clickedInventoryBoundInfo = playerMainBoundInfo;
            otherInventoryBoundInfo = containerBoundInfo;
            clickedInventoryIsPlayer = true;
        } else {
            clickedInventoryBoundInfo = hotbarBoundInfo;
            otherInventoryBoundInfo = containerBoundInfo;
            clickedInventoryIsPlayer = true;
        }

        if (actionType == SlotActionType.CLONE && button == 2) { //Sorting functionality
            //Allow clone operation in creative mode
            if (slot.getStack().getItem() != Items.AIR && playerInventory.player.abilities.creativeMode) return;

            operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.SORT, slot, clickedInventoryBoundInfo);
        }

        else if (button == 0) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z) &&
                    InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X)) {

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z)) {

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X)) {

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C)) {
                return;
            }

            //"All" operations
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE)){
                if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
                else operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
            }

            //"AllSameType" operations
            else if (Screen.hasControlDown() && Screen.hasShiftDown() && Screen.hasAltDown()) {
                if (clickedInventoryIsPlayer) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL_SAME_TYPE, slot, playerFullBoundInfo);
                else operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo);
            }
            else if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                if (clickedInventoryIsPlayer) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL_SAME_TYPE, slot, playerFullBoundInfo, otherInventoryBoundInfo);
                else operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
            }

            //"One" operations
            else if (Screen.hasControlDown() && Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ONE, slot, clickedInventoryBoundInfo);
            else if (Screen.hasControlDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ONE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);

            //"Stack" operations
            else if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_STACK, slot, clickedInventoryBoundInfo);

            //If none, follow Minecraft's default logic
            else return;

        } else return;

        try {
            ITScreenControllerTest test = new ITScreenControllerTest(controller, screenSpecification);
            test.testSort(clickedInventoryBoundInfo);
            //InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
        } catch (IllegalArgumentException e) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
        }
        ci.cancel();
    }

}
