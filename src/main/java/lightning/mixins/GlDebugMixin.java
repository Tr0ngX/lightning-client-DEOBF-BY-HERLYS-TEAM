package lightning.mixins;

import com.mojang.blaze3d.opengl.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlDebug.class)
public class GlDebugMixin {
    @Inject(method = "printDebugLog", at = @At("HEAD"), cancellable = true)
    private void night$silenceFramebufferSpam(int arg0, int arg1, int arg2, int arg3, int arg4, long arg5, long arg6, CallbackInfo arg7) {
        if (arg2 == 1286) {
            arg7.cancel();
        }
    }

}
