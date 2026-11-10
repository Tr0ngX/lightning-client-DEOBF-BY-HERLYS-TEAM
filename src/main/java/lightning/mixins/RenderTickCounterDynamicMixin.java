package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.LightningClient;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeltaTracker.Timer.class)
public class RenderTickCounterDynamicMixin {
    @ModifyExpressionValue(method = "advanceGameTime", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F"))
    private float night$scaleMspt(float arg0) {
        return (arg0 / LightningClient.WORLD_MANAGER.getTimerMultiplier());
    }

}
