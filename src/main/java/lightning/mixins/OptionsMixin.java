package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.api.IFreecamModule;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void night$blockThirdPersonOnFreecam(CameraType arg0, CallbackInfo arg1) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        Object var3 = tmp0;
        if (((tmp0 == null) || (!(var3.isToggled()))) || (arg0.isFirstPerson())) break label_0;
        arg1.cancel();
        label_0:
    }

}
