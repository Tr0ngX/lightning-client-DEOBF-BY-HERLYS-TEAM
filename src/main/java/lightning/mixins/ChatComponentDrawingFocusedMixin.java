package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.animations.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
public class ChatComponentDrawingFocusedMixin {
    @Unique
    private int night$xOffset;

    public ChatComponentDrawingFocusedMixin() {
        this.night$xOffset = 0;
    }

    @Inject(method = "handleMessage", at = @At("HEAD"))
    private void night$computeOffset(int arg0, float arg1, FormattedCharSequence arg2, CallbackInfoReturnable arg3) {
        this.night$xOffset = 0;
        Object var6 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if ((!(var6.isToggled())) || (!(var6.animation.getValue()))) {
            return;
        }
        Object var7 = var6.getAnimationStart(arg2);
        if (var7 == null) return;
        int var8 = var6.delay.getValue().intValue();
        if (var8 <= 0) {
        } else {
        }
        var tmp0 = Easing.toDelta(var7.longValue(), var8);
        float var5 = tmp0;
        float var9 = tmp0;
        if (var5 >= 1.0f) return;
        float var10 = ((float) Minecraft.getInstance().font.width(arg2));
        this.night$xOffset = Math.round((-(var10) * (1.0f - var5)));
    }

    @ModifyArg(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V"), index = 1)
    private int night$slideInX(int arg0) {
        return (arg0 + this.night$xOffset);
    }

}
