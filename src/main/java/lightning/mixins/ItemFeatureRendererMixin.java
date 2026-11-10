package lightning.mixins;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {
    @ModifyArg(method = "prepareMainSubmit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;setColor(I)V"))
    private int night$handsOpacity(int arg0, ItemFeatureRenderer.Submit arg1) {
        if (!(arg1.displayContext().firstPerson())) return arg0;
        return applyHandsOpacity(arg0);
    }

    private static int applyHandsOpacity(int arg0) {
        return arg0;
    }

}
