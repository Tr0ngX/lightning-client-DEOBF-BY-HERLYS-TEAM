package lightning.mixins;

import lightning.LightningClient;
import lightning.mixins.accessors.GuiTextRenderStateAccessor;
import lightning.modules.impl.core.FontModule;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {
    @Shadow
    private int firstStratumAfterBlur;

    @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"), cancellable = true)
    private void night$noopIfAlreadyBlurredThisFrame(CallbackInfo arg0) {
        if (this.firstStratumAfterBlur != 2147483647) {
            arg0.cancel();
        }
    }

    @Inject(method = "addText", at = @At("HEAD"), cancellable = true)
    private void night$globalFont(GuiTextRenderState arg0, CallbackInfo arg1) {
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        Object var3 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        if ((((((var3 == null) || (!(var3.isToggled()))) || (!(var3.customFont.getValue()))) || (!(var3.global.getValue()))) || (LightningClient.FONT_MANAGER == null)) || (LightningClient.FONT_MANAGER.getFontRenderer() == null)) break label_1;
        Object var4 = ((GuiTextRenderStateAccessor) arg0);
        Object var5 = ((GuiRenderState) this);
        if (var4.isDropShadow()) {
            float var6 = 1.0f;
            LightningClient.FONT_MANAGER.getFontRenderer().submitGlyphs(var5, var4.getPose(), var4.getScissor(), var4.getText(), (((float) var4.getX()) + var6), (((float) var4.getY()) + var6), var4.getColor(), true);
        }
        LightningClient.FONT_MANAGER.getFontRenderer().submitGlyphs(var5, var4.getPose(), var4.getScissor(), var4.getText(), ((float) var4.getX()), ((float) var4.getY()), var4.getColor(), false);
        arg1.cancel();
        label_1:
    }

}
