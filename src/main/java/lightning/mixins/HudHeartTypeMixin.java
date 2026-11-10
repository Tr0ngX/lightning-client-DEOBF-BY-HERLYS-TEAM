package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Hud$HeartType")
public class HudHeartTypeMixin {
    @ModifyExpressionValue(method = "forPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1))
    private static boolean night$hideWitherHeart(boolean arg0) {
        if (!(!arg0) && LightningClient.MODULE_MANAGER != null) {
            var tmp0 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
            Object var1 = tmp0;
            if (tmp0.isToggled() && var1.wither.getValue()) return false;
        }
        return arg0;
    }

}
