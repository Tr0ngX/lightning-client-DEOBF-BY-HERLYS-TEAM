package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PlayerMineEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientWorldMixin {
    @Inject(method = "addEntity", at = @At("HEAD"))
    private void addEntity(Entity arg0, CallbackInfo arg1) {
        LightningClient.EVENT_HANDLER.post(new EntitySpawnEvent(arg0));
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    private void destroyBlockProgress(int arg0, BlockPos arg1, int arg2, CallbackInfo arg3) {
        if (arg2 >= 0 && arg2 <= 9) {
            LightningClient.EVENT_HANDLER.post(new PlayerMineEvent(arg0, arg1));
        }
    }

}
