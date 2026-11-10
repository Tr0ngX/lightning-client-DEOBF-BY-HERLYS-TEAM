package lightning.mixins;

import java.util.function.Consumer;
import lightning.LightningClient;
import lightning.modules.impl.miscellaneous.ScreenshotModule;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @ModifyVariable(method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Consumer night$wrapScreenshotConsumer(Consumer arg0) {
        return (arg1) -> {
    if (LightningClient.MODULE_MANAGER != null) {
    } else {
    }
    Object var3 = null;
    if (var3 == null) break label_2;
    var tmp0 = (var3.isToggled() ? 1 : 0);
    int var2 = tmp0;
    int var4 = tmp0;
    if (!var2) {
        arg0.accept(arg1);
        try {
        }
        if (!(!var2)) {
            var3.onScreenshotCaptured(arg1);
        }
    } catch (Throwable ex6) {
        Object var5 = ex6;
        LightningClient.LOGGER.error("Failed to process screenshot callback in ScreenshotModule", var5);
    }
};
    }

}
