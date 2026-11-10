package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.UnfilteredMouseInputEvent;
import lightning.modules.impl.combat.MainhandModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long arg0, MouseButtonInfo arg1, int arg2, CallbackInfo arg3) {
        int var6 = arg1.button();
        int var7 = arg1.modifiers();
        LightningClient.EVENT_HANDLER.post(new UnfilteredMouseInputEvent(var6, arg2, var7));
        if (arg0 == this.minecraft.getWindow().handle() && arg2 == 1 && this.minecraft.gui.screen() == null) {
            LightningClient.EVENT_HANDLER.post(new MouseInputEvent(var6));
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void night$onScroll(long arg0, double arg1, double arg2, CallbackInfo arg3) {
        if (((this.minecraft.gui.screen() != null) || (arg0 != this.minecraft.getWindow().handle())) || (LightningClient.MODULE_MANAGER == null)) break label_0;
        var tmp0 = ((MainhandModule) LightningClient.MODULE_MANAGER.getModule(MainhandModule.class));
        Object var8 = tmp0;
        if ((tmp0 == null) || (!(var8.shouldLockSlot()))) break label_0;
        arg3.cancel();
        label_0:
    }

}
