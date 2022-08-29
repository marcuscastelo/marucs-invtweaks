package io.github.marcuscastelo.invtweaks.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity {


    protected MixinMobEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void refreshPosition() {
        super.refreshPosition();

        Logger logger = LogUtils.getLogger();
        if (!world.isClient) return;
        if (!world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER)) return;

        if (this.getPos().x < 1 || this.getPos().x > 71) return;
        if (this.getPos().y < 71 || this.getPos().y > 117) return;
        if (this.getPos().z < -49 || this.getPos().z > 29) return;

        if (!this.firstUpdate) {
            return;
        }

        Text txt = Text.literal("a " + this.getType().getName().getString() + " has spawned at: " + getBlockPos().toString());
        MinecraftClient.getInstance().player.sendMessage(txt);
        world.setBlockState(getBlockPos().up(2), Blocks.BELL.getDefaultState());

        BlockPos signPos = getBlockPos().up(1);
        world.setBlockState(signPos, Blocks.ACACIA_SIGN.getDefaultState());
        SignBlockEntity sbe = (SignBlockEntity)world.getBlockEntity(signPos);
        sbe.setTextOnRow(1, txt);
        sbe.markDirty();

        world.playSound(MinecraftClient.getInstance().player, new BlockPos(getBlockPos()), SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
    }

}
