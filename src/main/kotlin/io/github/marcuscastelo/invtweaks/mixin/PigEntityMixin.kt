package io.github.marcuscastelo.invtweaks.mixin;

import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject

@Mixin(PigEntity::class)
abstract class PigEntityMixin(entityType: EntityType<out AnimalEntity>?, world: World?) : AnimalEntity(entityType, world) {
}