package lightning.mixins;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPass.class)
public abstract class RenderPassMixin {
    @Shadow
    @Final
    private RenderPass.RenderArea renderArea;
    @Shadow
    @Final
    private RenderPassBackend backend;

    @Shadow
    public abstract void disableScissor();

    @Inject(method = "enableScissor", at = @At("HEAD"), cancellable = true)
    private void night$clampScissorToBounds(int arg0, int arg1, int arg2, int arg3, CallbackInfo arg4) {
        if (this.renderArea == null) return;
        int var6 = this.renderArea.x();
        int var7 = this.renderArea.y();
        int var8 = this.renderArea.width();
        int var9 = this.renderArea.height();
        int var10 = arg0;
        int var11 = arg1;
        int var12 = (arg0 + arg2);
        int var13 = (arg1 + arg3);
        if (var10 < var6) {
            var10 = var6;
        }
        if (var11 < var7) {
            var11 = var7;
        }
        if (var12 > (var6 + var8)) {
            var12 = (var6 + var8);
        }
        if (var13 > (var7 + var9)) {
            var13 = (var7 + var9);
        }
        int var14 = (var12 - var10);
        int var15 = (var13 - var11);
        if (var14 > 0) {
            if (var15 <= 0) {
            }
            disableScissor();
            arg4.cancel();
            return;
        }
        if (var10 == arg0 && var11 == arg1 && var14 == arg2) {
            if (var15 != arg3) {
            }
            this.backend.enableScissor(var10, var11, var14, var15);
            arg4.cancel();
        }
    }

}
