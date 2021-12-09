package io.github.marcuscastelo.invtweaks.mixin;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
import io.github.marcuscastelo.invtweaks.api.ScreenSpecification;
import io.github.marcuscastelo.invtweaks.inventory.ScreenInventory;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler>{
    @Shadow
    @Final
    protected T handler;

    @Shadow public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    private boolean hack__middle_click = false;

    void deleteme_test(ScreenInventory si) {
        try {
            si.screenHandler.slots.get(si.start).setStack(new ItemStack(Blocks.GRASS));
            si.screenHandler.slots.get(si.end).setStack(new ItemStack(Blocks.END_STONE));
            for (int i = si.start + 1; i < si.end; i++) {
                si.screenHandler.slots.get(i).setStack(new ItemStack(Blocks.BEDROCK, i));
            }
        } catch (Exception e) {
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(new LiteralText(e.getMessage()), false);
        }
    }

    @Inject(method = "mouseClicked", at=@At("HEAD"), cancellable = true)
    protected void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {
        boolean isBtnPickItem = MinecraftClient.getInstance().options.keyPickItem.matchesMouse(button);
        boolean isClientCreative = MinecraftClient.getInstance().interactionManager.hasCreativeInventory();
        boolean isCreativeCloneOperation = isBtnPickItem && isClientCreative;

        if (isCreativeCloneOperation) return; //Unchanged clone item in creative mode

        if (button != 2) return; //Unchanged: onMouseClick function below already works

        //Here button == 2, and we are not cloning.

        hack__middle_click = true;

        //Simulates left click
        boolean returnValue;
        returnValue = this.mouseClicked(mouseX, mouseY, 0);

        hack__middle_click = false;

        cir.setReturnValue(returnValue);
        cir.cancel();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void onMouseClick(Slot slot, int invSlot, int button, SlotActionType actionType, CallbackInfo ci) {
        if (hack__middle_click) {
            button = 2;
            actionType = SlotActionType.CLONE;
        }

        if (actionType == SlotActionType.PICKUP_ALL) return;
        if (!InvTweaksBehaviorRegistry.isScreenSupported(handler.getClass())) {
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
            return;
        }

        //In case of clicking outside of inventory, just ignore
        if (slot == null) return;

        ScreenSpecification screenSpecs = InvTweaksBehaviorRegistry.getScreenSpecs(handler.getClass());

        System.out.println("ScreenSpecs = " + screenSpecs.getHandlerClass().descriptorString() );



        int totalSize = handler.slots.size();
        int playerInvSize = screenSpecs.getInventoriesSpecification().playerMainInvSize;
        int playerHotbarSize = screenSpecs.getInventoriesSpecification().playerHotbarSize;
        int containerInvSize = totalSize - (playerInvSize + playerHotbarSize);

        //TODO: make this generic instead of hardcoded:
        if (handler.getClass().equals(PlayerScreenHandler.class)) {
            containerInvSize = 9;
        }

        System.out.println("Total = " + totalSize );
        System.out.println("PlayerInvSize = " + playerInvSize );
        System.out.println("PlayerHotbarSize = " + playerHotbarSize );
        System.out.println("ContainerInvSize = " + containerInvSize );
        
        ScreenInventory containerBoundInfo = new ScreenInventory(handler, 0, containerInvSize-1);
        ScreenInventory playerMainBoundInfo = new ScreenInventory(handler, containerInvSize, containerInvSize+playerInvSize-1);
        ScreenInventory hotbarBoundInfo = new ScreenInventory(handler, containerInvSize+playerInvSize, containerInvSize+playerInvSize+playerHotbarSize-1);
        ScreenInventory playerFullBoundInfo = new ScreenInventory(handler, containerInvSize, containerInvSize+playerInvSize+playerHotbarSize-1);

        InvTweaksOperationInfo operationInfo;

        ScreenInventory clickedInventoryBoundInfo;
        ScreenInventory otherInventoryBoundInfo;

//        for (int i = playerMainBoundInfo.start; i <= playerMainBoundInfo.end; i++) {
//            handler.slots.get(i).setStack(new ItemStack(Items.BEDROCK, i + 1));
//        }
//        if (1<2)return;

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
            if (slot.getStack().getItem() != Items.AIR && MinecraftClient.getInstance().player.isCreative()) return;

            operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.SORT, slot, clickedInventoryBoundInfo);
        }

        else if (button == 0) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z) &&
                    InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X)) {
                deleteme_test(playerFullBoundInfo);                return;

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_Z)) {
                deleteme_test(hotbarBoundInfo);                return;

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_X)) {
                deleteme_test(playerMainBoundInfo);                return;

            }
            else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_C)) {
                deleteme_test(containerBoundInfo);
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
            InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
        } catch (IllegalArgumentException e) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
        }
        ci.cancel();
    }

}
