package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtmosphericFogEnvironment.class)
public class BackgroundRendererMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void night$setupFog(FogData arg0, Camera arg1, ClientLevel arg2, float arg3, DeltaTracker arg4, CallbackInfo arg5) {
        Object var7 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        Object var8 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if (var7.isToggled() && var7.fog.getValue()) {
            arg0.environmentalStart = (arg3 * 4.0f);
            arg0.environmentalEnd = (arg3 * 4.25f);
            break label_1;
        }
        if (((var8 == null) || (!(var8.isToggled()))) || (!(var8.modifyFog.getValue()))) break label_1;
        arg0.environmentalStart = var8.fogStart.getValue().floatValue();
        arg0.environmentalEnd = var8.fogEnd.getValue().floatValue();
        label_1:
    }

    @Inject(method = "getBaseColor", at = @At("TAIL"), cancellable = true)
    private void night$getBaseColor(ClientLevel arg0, Camera arg1, int arg2, float arg3, CallbackInfoReturnable arg4) {
        Object var6 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if (var6 != null && var6.isToggled() && var6.modifyFog.getValue()) {
            Object var7 = var6.fogColor.getColor();
            arg4.setReturnValue(ARGB.color(255, var7.getRed(), var7.getGreen(), var7.getBlue()));
        }
    }

}
