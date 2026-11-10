package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.multiplayer.ClientExplosionTracker;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientExplosionTracker.class)
public class ClientExplosionTrackerMixin {
    @Inject(method = "track", at = @At("HEAD"), cancellable = true)
    private void night$cancelDebris(Vec3 arg0, float arg1, int arg2, WeightedList arg3, CallbackInfo arg4) {
        Object var6 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var6.isToggled() && var6.explosions.getValue()) {
            arg4.cancel();
        }
    }

}
