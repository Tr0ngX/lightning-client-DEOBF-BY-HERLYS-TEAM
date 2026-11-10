package lightning.mixins;

import java.util.Optional;
import java.util.function.Consumer;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.LoadingText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin extends Overlay implements IMinecraft {
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    @Final
    private Consumer onFinish;
    @Shadow
    @Final
    private boolean fadeIn;
    @Shadow
    private float currentProgress;
    @Shadow
    private long fadeOutStart;
    @Shadow
    private long fadeInStart;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void night$renderLoadingScreen(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        arg4.cancel();
        int var6 = mc.getWindow().getGuiScaledWidth();
        int var7 = mc.getWindow().getGuiScaledHeight();
        long var8 = Util.getMillis();
        if (!(!this.fadeIn) && this.fadeInStart == -1L) {
            this.fadeInStart = var8;
        }
        if (this.fadeOutStart <= -1L) break label_1;
        break label_2;
        label_1:
        label_2:
        float var10 = -1.0f;
        if (this.fadeInStart > -1L) {
        } else {
        }
        float var11 = -1.0f;
        float var12 = 1.0f;
        if (var10 < 1.0f) break label_9;
        if (mc.gui.screen() != null) {
            mc.gui.screen().init(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            try {
            }
            this.reload.checkExceptions();
            this.onFinish.accept(Optional.empty());
        } catch (Throwable ex7) {
            Object var13 = ex7;
            this.onFinish.accept(Optional.of(var13));
        }
        mc.gui.setOverlay(null);
        return;
        label_9:
        if (var10 >= 0.0f) {
            var12 = (1.0f - Mth.clamp(var10, 0.0f, 1.0f));
            break label_11;
        }
        if ((!this.fadeIn) || (var11 < 0.0f)) break label_11;
        var12 = Mth.clamp(var11, 0.0f, 1.0f);
        label_11:
        var13 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(((this.currentProgress * 0.95f) + (var13 * 0.05f)), 0.0f, 1.0f);
        LoadingText.render(arg0, var6, var7, var12, this.currentProgress, this.reload.isDone());
        if (this.reload.isDone() && LoadingText.ready() && this.fadeOutStart == -1L) {
            this.fadeOutStart = var8;
        }
    }

}
