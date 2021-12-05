package io.github.marcuscastelo.invtweaks.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.AreaHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AreaHelper.class)
public class MixinAreaHelper {

    @Shadow private int width;

    @Inject(method = "method_30492", at = @At("RETURN"), cancellable = true)
    void method_30492(BlockPos blockPos, CallbackInfoReturnable<BlockPos> cir) {
        System.out.println(cir.getReturnValue());
        System.out.println(this.width);
    }
}
