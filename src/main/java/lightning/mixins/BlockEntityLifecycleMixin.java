package lightning.mixins;

import lightning.utils.BlockEntityInstanceTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityLifecycleMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void night$trackConstructed(BlockEntityType arg0, BlockPos arg1, BlockState arg2, CallbackInfo arg3) {
        BlockEntityInstanceTracker.constructed(((BlockEntity) this));
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void night$trackRemoved(CallbackInfo arg0) {
        BlockEntityInstanceTracker.removed(((BlockEntity) this));
    }

}
