package lightning.mixins;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRenderDispatcherMixin {
    @Inject(method = "renderAllFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain()V"))
    private void night$executeOutline(SubmitNodeStorage arg0, CallbackInfo arg1, FeatureRenderDispatcher.PreparedFrame arg2) {
        arg2.executeOutline();
    }

}
