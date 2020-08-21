package io.github.marcuscastelo.invtweaks.mixin;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
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

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> {
    @Shadow @Final protected T handler;

    @Shadow @Final protected PlayerInventory playerInventory;

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    protected void onMouseClicked(Slot slot, int invSlot, int button, SlotActionType actionType, CallbackInfo ci) {
        if (actionType == SlotActionType.PICKUP_ALL) return;

        if (slot == null || handler == null) return;

        int totalSize = handler.slots.size();
        int playerInvSize = 27;
        int containerInvSize = totalSize - playerInvSize - 9;

        InventoryContainerBoundInfo containerBoundInfo = new InventoryContainerBoundInfo(handler, 0, containerInvSize-1);
        InventoryContainerBoundInfo playerBoundInfo = new InventoryContainerBoundInfo(handler, containerInvSize, containerInvSize+playerInvSize-1);
        InventoryContainerBoundInfo hotbarBoundInfo = new InventoryContainerBoundInfo(handler, containerInvSize+playerInvSize, containerInvSize+playerInvSize+9-1);

//        for (int i = playerBoundInfo.start; i <= playerBoundInfo.end; i++) handler.slots.get(i).setStack(new ItemStack(Items.GOLDEN_APPLE));
//        for (int i = containerBoundInfo.start; i <= containerBoundInfo.end; i++) handler.slots.get(i).setStack(new ItemStack(Items.COOKED_BEEF));

        InvTweaksOperationInfo operationInfo;

        InventoryContainerBoundInfo clickedInventoryBoundInfo;
        InventoryContainerBoundInfo otherInventoryBoundInfo;

        if (slot.id < containerInvSize) {
            clickedInventoryBoundInfo = containerBoundInfo;
            otherInventoryBoundInfo = playerBoundInfo;
        } else if (slot.id < containerInvSize+playerInvSize){
            clickedInventoryBoundInfo = playerBoundInfo;
            otherInventoryBoundInfo = containerBoundInfo;
        } else {
            clickedInventoryBoundInfo = hotbarBoundInfo;
            otherInventoryBoundInfo = containerBoundInfo;
        }

        if (actionType == SlotActionType.CLONE && button == 2) { //Sorting functionality
            //Allow clone operation in creative mode
            if (slot.getStack().getItem() != Items.AIR && playerInventory.player.abilities.creativeMode) return;

            operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.SORT, slot, clickedInventoryBoundInfo);
        }

        else if (button == 0) {

            //"All" operations
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE)){
                if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
                else operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
            }
            //"AllSameType" operations
            else if (Screen.hasControlDown() && Screen.hasShiftDown() && Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo);
            else if (Screen.hasControlDown() && Screen.hasShiftDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
                //"One" operations
            else if (Screen.hasControlDown() && Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ONE, slot, clickedInventoryBoundInfo);
            else if (Screen.hasControlDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ONE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
                //"Stack" operations
            else if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_STACK, slot, clickedInventoryBoundInfo);
                //If none, follow Minecraft's default logic
            else return;

        } else return;

        try {
            InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
        } catch (IllegalArgumentException e) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
        }
        ci.cancel();
    }

}
