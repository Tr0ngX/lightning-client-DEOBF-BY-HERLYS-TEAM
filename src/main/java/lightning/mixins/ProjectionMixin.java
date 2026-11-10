package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.api.IFOVModifierModule;
import lightning.modules.impl.visuals.AspectRatioModule;
import net.minecraft.client.renderer.Projection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Projection.class)
public class ProjectionMixin {
    @Shadow
    private boolean isMatrixDirty;

    @Inject(method = "getMatrix", at = @At("HEAD"))
    private void getMatrix$forceDirty(Matrix4f arg0, CallbackInfoReturnable arg1) {
        this.isMatrixDirty = true;
    }

    @ModifyArgs(method = "getMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;"))
    private void getMatrix$aspectRatio(Args arg0) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var2 = ((AspectRatioModule) LightningClient.MODULE_MANAGER.getModule(AspectRatioModule.class));
        if (var2 != null && var2.isToggled()) {
            arg0.set(1, var2.ratio.getValue().floatValue());
        }
        Object var3 = ((IFOVModifierModule) LightningClient.MODULE_MANAGER.getModule("FOVModifier"));
        if (var3 != null && var3.isToggled()) {
            arg0.set(0, ((float) Math.toRadians(((double) var3.getFovValue()))));
        }
    }

}
