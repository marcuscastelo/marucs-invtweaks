package io.github.marcuscastelo.invtweaks.mixin;

import io.github.marcuscastelo.invtweaks.InvTweaksOperationInfo;
import io.github.marcuscastelo.invtweaks.InvTweaksOperationType;
import io.github.marcuscastelo.invtweaks.InventoryContainerBoundInfo;
import io.github.marcuscastelo.invtweaks.registry.InvTweaksBehaviorRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen extends AbstractInventoryScreen<PlayerScreenHandler> {

    public MixinInventoryScreen(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Shadow
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }



    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    protected void onMouseClicked(Slot slot, int invSlot, int button, SlotActionType actionType, CallbackInfo ci) {
//        if (actionType == SlotActionType.PICKUP_ALL) return;
//
//        if (slot == null || handler == null) return;
//
//        int playerInvSize = 27;
//        InventoryContainerBoundInfo mainInvBoundInfo = new InventoryContainerBoundInfo(handler, 9, 9+playerInvSize-1);
//        InventoryContainerBoundInfo hotbarBoundInfo = new InventoryContainerBoundInfo(handler, 9+playerInvSize, 9+playerInvSize+9-1);
//
////        for (int i = hotbarBoundInfo.start; i <= hotbarBoundInfo.end; i++) handler.slots.get(i).setStack(new ItemStack(Items.GOLDEN_APPLE));
//
//
//        InvTweaksOperationInfo operationInfo;
//
//        InventoryContainerBoundInfo clickedInventoryBoundInfo;
//        InventoryContainerBoundInfo otherInventoryBoundInfo;
//        InventoryContainerBoundInfo bothInventoryBoundInfo = new InventoryContainerBoundInfo(handler, 9, 9+playerInvSize+9-1);
//
//        if (slot.id < 9) { //Armor, etc.. ignore
//            return;
//        } else if (slot.id < 9+playerInvSize) {//Inside main inv, not hotbar yet
//            clickedInventoryBoundInfo = mainInvBoundInfo;
//            otherInventoryBoundInfo = hotbarBoundInfo;
//        } else { //hotbar
//            clickedInventoryBoundInfo = hotbarBoundInfo;
//            otherInventoryBoundInfo = mainInvBoundInfo;
//        }
//
//        if (actionType == SlotActionType.CLONE && button == 2) { //Sorting functionality
//            //Allow clone operation in creative mode
//            if (slot.getStack().getItem() != Items.AIR && playerInventory.player.abilities.creativeMode) return;
//
//            operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.SORT, slot, mainInvBoundInfo);
//        }
//
//        else if (button == 0) {
//
//            //"All" operations
//            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE)){
//                if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL, slot, clickedInventoryBoundInfo);
//                else operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
//            }
//            //"AllSameType" operations
//            else if (Screen.hasControlDown() && Screen.hasShiftDown() && Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo);
//            else if (Screen.hasControlDown() && Screen.hasShiftDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ALL_SAME_TYPE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
//                //"One" operations
//            else if (Screen.hasControlDown() && Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_ONE, slot, clickedInventoryBoundInfo);
//            else if (Screen.hasControlDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.MOVE_ONE, slot, clickedInventoryBoundInfo, otherInventoryBoundInfo);
//                //"Stack" operations
//            else if (Screen.hasAltDown()) operationInfo = new InvTweaksOperationInfo(InvTweaksOperationType.DROP_STACK, slot, clickedInventoryBoundInfo);
//                //If none, follow Minecraft's default logic
//            else return;
//
//        } else return;
//
//        try {
//            InvTweaksBehaviorRegistry.executeOperation(handler.getClass(), operationInfo);
//        } catch (IllegalArgumentException e) {
//            MinecraftClient.getInstance().player.sendMessage(new LiteralText("This container is not supported by Marucs' Invtweaks"), false);
//        }
//        ci.cancel();
    }

}
