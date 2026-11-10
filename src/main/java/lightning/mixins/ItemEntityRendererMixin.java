package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void night$limitItems(CallbackInfo arg0) {
        Object var2 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if ((var2 == null) || (!(var2.isToggled()))) {
            return;
        }
        if (!(var2.shouldRenderItem())) {
            arg0.cancel();
        }
    }

}
