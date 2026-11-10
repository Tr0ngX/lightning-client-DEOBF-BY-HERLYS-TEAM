package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.NoSlowModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "getFriction", at = @At("HEAD"), cancellable = true)
    private void getSlipperiness(CallbackInfoReturnable arg0) {
        if (this == Blocks.SLIME_BLOCK && ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).slimeBlocks.getValue()) {
            arg0.setReturnValue(0.6f);
        }
    }

}
