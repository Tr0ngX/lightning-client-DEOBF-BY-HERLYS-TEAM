package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class FreecamV1KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void night$freecamV1$tick(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var2 = ((FreecamV1Module) LightningClient.MODULE_MANAGER.getModule(FreecamV1Module.class));
        if (var2 != null && var2.isToggled()) {
            var2.onInput(this.moveVector.y, this.moveVector.x);
            this.keyPresses = new Input(false, false, false, false, false, false, false);
            this.moveVector = new Vec2(0.0f, 0.0f);
        }
    }

}
