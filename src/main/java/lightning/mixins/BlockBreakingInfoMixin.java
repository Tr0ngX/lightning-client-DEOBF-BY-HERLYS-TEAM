package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.PlayerMineEvent;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockDestructionProgress.class)
public class BlockBreakingInfoMixin {
    @Inject(method = "setProgress", at = @At("HEAD"))
    private void setProgress(int arg0, CallbackInfo arg1) {
        Object var3 = ((BlockDestructionProgress) this);
        if (arg0 >= 0 && arg0 <= 9) {
            LightningClient.EVENT_HANDLER.post(new PlayerMineEvent(var3.getId(), var3.getPos()));
        }
    }

}
