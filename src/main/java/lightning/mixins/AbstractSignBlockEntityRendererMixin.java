package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignBlockEntityRendererMixin {
    @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
    private void submitSignText(SignRenderState arg0, PoseStack arg1, SubmitNodeCollector arg2, SignText arg3, CallbackInfo arg4) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).signText.getValue()) {
            arg4.cancel();
        }
    }

}
