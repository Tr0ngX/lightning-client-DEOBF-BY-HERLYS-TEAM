package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @ModifyExpressionValue(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState night$predictPlacedCollision(BlockState arg0) {
        if (!(arg0.isAir())) return arg0;
        Object var2 = WorldUtils.getPredictedCollisionState(this.pos);
        return (var2 != null ? var2 : arg0);
    }

}
