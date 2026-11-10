package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.UnfilteredKeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void keyPress(long arg0, int arg1, KeyEvent arg2, CallbackInfo arg3) {
        LightningClient.EVENT_HANDLER.post(new UnfilteredKeyInputEvent(arg2.key(), arg2.scancode(), arg1, arg2.modifiers()));
        if (arg0 == this.minecraft.getWindow().handle() && arg1 == 1 && this.minecraft.gui.screen() == null) {
            LightningClient.EVENT_HANDLER.post(new KeyInputEvent(arg2.key(), arg2.modifiers()));
        }
    }

}
