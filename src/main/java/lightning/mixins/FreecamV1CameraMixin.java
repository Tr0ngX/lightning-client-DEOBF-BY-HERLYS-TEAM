package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class FreecamV1CameraMixin {
    @Shadow
    private boolean detached;

    private static FreecamV1Module night$freecamV1() {
        if (LightningClient.MODULE_MANAGER == null) return null;
        Object var0 = ((FreecamV1Module) LightningClient.MODULE_MANAGER.getModule(FreecamV1Module.class));
        if (var0 == null) break label_1;
        return (var0.isToggled() ? var0 : null);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void night$freecamV1$update(DeltaTracker arg0, CallbackInfo arg1) {
        if (night$freecamV1() != null) {
            this.detached = true;
        }
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void night$freecamV1$setRotation(Args arg0) {
        Object var2 = night$freecamV1();
        if (var2 != null) {
            var tmp0 = new Object[2];
            tmp0[0] = var2.getFakeYaw();
            tmp0[1] = var2.getFakePitch();
            arg0.setAll(tmp0);
        }
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void night$freecamV1$setPos(Args arg0) {
        Object var2 = night$freecamV1();
        if (var2 != null) {
            var tmp0 = new Object[3];
            tmp0[0] = var2.getFakeX();
            tmp0[1] = var2.getFakeY();
            tmp0[2] = var2.getFakeZ();
            arg0.setAll(tmp0);
        }
    }

}
