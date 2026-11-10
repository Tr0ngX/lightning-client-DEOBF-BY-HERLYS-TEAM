package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void renderArmor(PoseStack arg0, SubmitNodeCollector arg1, int arg2, HumanoidRenderState arg3, float arg4, float arg5, CallbackInfo arg6) {
        Object var8 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var8.isToggled() && var8.armor.getValue()) {
            arg6.cancel();
        }
    }

}
