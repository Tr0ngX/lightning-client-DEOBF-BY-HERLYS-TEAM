package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.EventInput;
import lightning.events.impl.KeyboardTickEvent;
import lightning.modules.api.IAutoWalkModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.ISprintModule;
import lightning.utils.minecraft.BaritoneUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick$TAIL(CallbackInfo arg0) {
        Object var3 = new KeyboardTickEvent(this.moveVector.y, this.moveVector.x);
        LightningClient.EVENT_HANDLER.post(var3);
        if (var3.isCancelled()) {
            this.moveVector = new Vec2(var3.getMovementSideways(), var3.getMovementForward());
        }
        if (LightningClient.MODULE_MANAGER != null) {
            Object var4 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
            if ((var4 == null) || (!(var4.isToggled()))) break label_1;
            this.keyPresses = new Input(false, false, false, false, false, false, false);
            this.moveVector = new Vec2(0.0f, 0.0f);
            return;
            label_1:
            Object var5 = ((IAutoWalkModule) LightningClient.MODULE_MANAGER.getModule("AutoWalk"));
            if (var5 != null && var5.isToggled() && !(BaritoneUtils.isActive())) {
                int var6 = var5.isBackward();
                int var7 = (!var6 ? 1 : 0);
                this.keyPresses = new Input(var7, var6, this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
                this.moveVector = new Vec2(this.moveVector.x, (!(!var6) ? -1.0f : 1.0f));
            }
            var6 = new EventInput(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
            LightningClient.EVENT_HANDLER.post(var6);
            this.keyPresses = new Input(var6.forward, var6.backward, var6.left, var6.right, var6.jumping, var6.sneaking, var6.sprinting);
        }
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var2 = tmp0;
        var4 = tmp0;
        if (var2 != null && var2.isGrimCompensating()) {
            var5 = var2.getGrimStrafe();
            this.keyPresses = new Input(true, false, var5 > 0, var5 < 0, this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
            if (!var5) {
            } else {
            }
            new Vec2(0.0f, 1.0f).moveVector = new Vec2((var5 > 0 ? 1.0f : -1.0f), 1.0f);
            Minecraft.getInstance().player.setSprinting(true);
        }
    }

}
