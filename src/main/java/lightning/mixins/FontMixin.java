package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.core.FontModule;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontMixin {
    @Inject(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void width$String(String arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var3 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        if (var3 != null && var3.isToggled() && var3.customFont.getValue() && var3.global.getValue() && LightningClient.FONT_MANAGER.getFontRenderer() != null) {
            arg1.setReturnValue(((int) LightningClient.FONT_MANAGER.getFontRenderer().getTextWidth(arg0)));
        }
    }

    @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
    private void width$FormattedText(FormattedText arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var3 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        if (var3 != null && var3.isToggled() && var3.customFont.getValue() && var3.global.getValue() && LightningClient.FONT_MANAGER.getFontRenderer() != null) {
            arg1.setReturnValue(((int) LightningClient.FONT_MANAGER.getFontRenderer().getTextWidth(arg0.getString())));
        }
    }

    @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
    private void width$FormattedCharSequence(FormattedCharSequence arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var3 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        if (var3 != null && var3.isToggled() && var3.customFont.getValue() && var3.global.getValue() && LightningClient.FONT_MANAGER.getFontRenderer() != null) {
            arg1.setReturnValue(((int) LightningClient.FONT_MANAGER.getFontRenderer().getTextWidth(arg0)));
        }
    }

}
