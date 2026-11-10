package lightning.mixins;

import lightning.LightningClient;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.impl.player.RotationLockModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements IMinecraft {
    @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
    private void night$unlockViewYaw(float arg0, CallbackInfoReturnable arg1) {
        if (((LocalPlayer) this).isPassenger()) return;
        Object var3 = ((RotationLockModule) LightningClient.MODULE_MANAGER.getModule(RotationLockModule.class));
        if ((!(var3.isToggled())) || (!(var3.custom.getValue()))) break label_2;
        if (!(var3.mode.getValue().equals("Yaw"))) {
            if (!(var3.mode.getValue().equals("Both"))) break label_2;
        }
        arg1.setReturnValue(((EntityAccessor) this).getRawYRot());
        label_2:
    }

    @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
    private void night$unlockViewPitch(float arg0, CallbackInfoReturnable arg1) {
        Object var3 = ((RotationLockModule) LightningClient.MODULE_MANAGER.getModule(RotationLockModule.class));
        if ((!(var3.isToggled())) || (!(var3.custom.getValue()))) break label_1;
        if (!(var3.mode.getValue().equals("Pitch"))) {
            if (!(var3.mode.getValue().equals("Both"))) break label_1;
        }
        arg1.setReturnValue(((EntityAccessor) this).getRawXRot());
        label_1:
    }

}
