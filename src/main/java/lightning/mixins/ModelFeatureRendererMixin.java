package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lightning.utils.graphics.ChamsVertexConsumer;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
    @Redirect(method = "prepareModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void night$captureChams(Model arg0, PoseStack arg1, VertexConsumer arg2, int arg3, int arg4, int arg5, ModelFeatureRenderer.Submit arg6) {
        Object var9 = arg6.state();
        if (!((var9 instanceof IChamsCapture))) break label_2;
        var tmp0 = ((IChamsCapture) var9);
        Object var8 = tmp0;
        if (!(tmp0.night$chamsFill()) && !(var8.night$chamsOutline()) && !(var8.night$chamsGlint())) {
            if (var8.night$chamsRealAlpha() >= 1.0f) break label_2;
        }
        int var10 = arg5;
        float var11 = var8.night$chamsRealAlpha();
        if (var11 < 1.0f) {
            int var12 = ((arg5 >>> 24) & 255);
            int var13 = Math.round((((float) var12) * var11));
            var10 = ((arg5 & 16777215) | (var13 << 24));
        }
        arg0.renderToBuffer(arg1, new ChamsVertexConsumer(arg2, var8.night$chamsFill(), var8.night$chamsFillColor(), var8.night$chamsOutline(), var8.night$chamsOutlineColor(), var8.night$chamsShine(), var8.night$chamsGlint(), var8.night$chamsGlintColor(), var8.night$chamsGlintParams(), var8.night$chamsSuppressReal(), var11), arg3, arg4, var10);
        break label_3;
        label_2:
        arg0.renderToBuffer(arg1, arg2, arg3, arg4, arg5);
        label_3:
    }

}
