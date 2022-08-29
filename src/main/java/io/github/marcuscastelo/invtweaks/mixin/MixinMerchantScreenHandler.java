package io.github.marcuscastelo.invtweaks.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.util.InputUtil.isKeyPressed;

@Mixin(MerchantScreenHandler.class)
public abstract class MixinMerchantScreenHandler extends ScreenHandler {
    protected MixinMerchantScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "autofill", at = @At("HEAD"), cancellable = true)
    private void autofill(int slot, ItemStack stack, CallbackInfo ci) {
        if (isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE))
            ci.cancel();
    }
}
