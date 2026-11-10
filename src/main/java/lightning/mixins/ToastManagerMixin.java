package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void night$noAdvancementToast(Toast arg0, CallbackInfo arg1) {
        if ((!((arg0 instanceof AdvancementToast))) || (LightningClient.MODULE_MANAGER == null)) break label_0;
        var tmp0 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        Object var3 = tmp0;
        if ((!(tmp0.isToggled())) || (!(var3.advancements.getValue()))) break label_0;
        arg1.cancel();
        label_0:
    }

}
