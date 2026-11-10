package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void night$resetItemCounter(CallbackInfo arg0) {
        Object var2 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var2 != null) {
            var2.resetItemCounter();
        }
    }

}
