package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlindnessFogEnvironment.class)
public class BlindnessFogEnvironmentMixin {
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private void night$setupFog(FogData arg0, Camera arg1, ClientLevel arg2, float arg3, DeltaTracker arg4, CallbackInfo arg5) {
        Object var7 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var7.isToggled() && var7.blindness.getValue()) {
            arg5.cancel();
        }
    }

}
