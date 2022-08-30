import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PigEntity
import org.spongepowered.asm.mixin.Mixin

@Mixin(PigEntity::class)
abstract class PigEntityMixin : AnimalEntity {
    override fun getMaxSpawnedInChunk(): Int {
        return 1
        val maxSpawnedInChunk = super.getMaxSpawnedInChunk()
        maxSpawnedInChunk = 3
    }
}