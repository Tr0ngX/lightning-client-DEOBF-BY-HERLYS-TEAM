package lightning.mixins;

import com.mojang.blaze3d.font.GlyphInfo;
import lightning.LightningClient;
import lightning.managers.FontManager;
import lightning.modules.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphInfo.class)
public interface GlyphMixin {
    @Inject(method = "getShadowOffset", at = @At("HEAD"), cancellable = true)
    private void getShadowOffset(CallbackInfoReturnable arg0) {
        if (!(FontManager.isClientTextRendering())) return;
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        var tmp0 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        Object var2 = tmp0;
        if (((tmp0 == null) || (!(var2.isToggled()))) || (var2.shadowMode.getValue().equalsIgnoreCase("Default"))) break label_1;
        arg0.setReturnValue(LightningClient.FONT_MANAGER.getShadowOffset());
        label_1:
    }

}
