package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.AtmosphereModule;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class WeatherMixin {
    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void night$isRaining(CallbackInfoReturnable arg0) {
        Object var2 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if (var2.isToggled() && !(var2.weather.getValue().equalsIgnoreCase("Unchanged"))) {
            Object var3 = var2.weather.getValue();
            if (!(var3.equalsIgnoreCase("Rain"))) {
                if (var3.equalsIgnoreCase("Thunder")) {
                }
            } else {
            }
            1.setReturnValue(false);
        }
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void night$isThundering(CallbackInfoReturnable arg0) {
        Object var2 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if ((!(var2.isToggled())) || (var2.weather.getValue().equalsIgnoreCase("Unchanged"))) {
            return;
        }
        arg0.setReturnValue(var2.weather.getValue().equalsIgnoreCase("Thunder"));
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void night$getRainLevel(float arg0, CallbackInfoReturnable arg1) {
        Object var3 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if ((!(var3.isToggled())) || (var3.weather.getValue().equalsIgnoreCase("Unchanged"))) {
            return;
        }
        Object var4 = var3.weather.getValue();
        if (!(var4.equalsIgnoreCase("Rain"))) {
            if (var4.equalsIgnoreCase("Thunder")) {
            }
        } else {
        }
        1.0f.setReturnValue(0.0f);
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void night$getThunderLevel(float arg0, CallbackInfoReturnable arg1) {
        Object var3 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if ((!(var3.isToggled())) || (var3.weather.getValue().equalsIgnoreCase("Unchanged"))) {
            return;
        }
        arg1.setReturnValue((var3.weather.getValue().equalsIgnoreCase("Thunder") ? 1.0f : 0.0f));
    }

}
