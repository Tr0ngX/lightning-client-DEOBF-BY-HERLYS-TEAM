package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class FreecamV1MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"))
    private void night$freecamV1$onScroll(long arg0, double arg1, double arg2, CallbackInfo arg3) {
        if (((LightningClient.MODULE_MANAGER == null) || (this.minecraft.gui.screen() != null)) || (arg0 != this.minecraft.getWindow().handle())) {
            return;
        }
        Object var8 = ((FreecamV1Module) LightningClient.MODULE_MANAGER.getModule(FreecamV1Module.class));
        if (var8 != null && var8.isToggled() && arg2 != 0.0) {
            var8.onScroll(arg2);
        }
    }

}
