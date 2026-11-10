package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.ClientClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientClockManager.class)
public class PropertiesMixin {
    @Inject(method = "getTotalTicks", at = @At("HEAD"), cancellable = true)
    private void getTotalTicks(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifyTime.getValue()) {
            arg0.setReturnValue((var2.time.getValue().longValue() * 100L));
        }
    }

}
