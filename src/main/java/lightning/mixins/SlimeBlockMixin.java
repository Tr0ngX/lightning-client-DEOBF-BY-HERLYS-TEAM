package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public class SlimeBlockMixin implements IMinecraft {
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(Level arg0, BlockPos arg1, BlockState arg2, Entity arg3, CallbackInfo arg4) {
        if (arg3 == mc.player && ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).slimeBlocks.getValue()) {
            arg4.cancel();
        }
    }

}
