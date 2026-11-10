package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lightning.LightningClient;
import lightning.modules.impl.visuals.ChamsModule;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public class CrystalSizeMixin {
    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), require = 0)
    private void night$crystalSizeIn(PoseStack arg0, VertexConsumer arg1, int arg2, int arg3, int arg4, CallbackInfo arg5) {
        if (!((this instanceof EndCrystalModel))) break label_0;
        Object var7 = ((ChamsModule) LightningClient.MODULE_MANAGER.getModule(ChamsModule.class));
        if (var7 == null) break label_0;
        float var8 = var7.crystalSize.getValue().floatValue();
        if ((var8 == 1.0f) || (var8 <= 0.0f)) break label_0;
        arg0.scale(var8, var8, var8);
        label_0:
    }

    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("RETURN"), require = 0)
    private void night$crystalSizeOut(PoseStack arg0, VertexConsumer arg1, int arg2, int arg3, int arg4, CallbackInfo arg5) {
        if (!((this instanceof EndCrystalModel))) break label_0;
        Object var7 = ((ChamsModule) LightningClient.MODULE_MANAGER.getModule(ChamsModule.class));
        if (var7 == null) break label_0;
        float var8 = var7.crystalSize.getValue().floatValue();
        if ((var8 == 1.0f) || (var8 <= 0.0f)) break label_0;
        var8 = (1.0f / var8);
        arg0.scale(var8, var8, var8);
        label_0:
    }

}
