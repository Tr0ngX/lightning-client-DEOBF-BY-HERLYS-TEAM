package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.ClientConnectEvent;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin implements IMinecraft {
    @Unique
    private float handleMovePlayer$prevYaw;
    @Unique
    private float handleMovePlayer$prevPitch;
    @Unique
    private boolean handleMovePlayer$guard;

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onGameJoin(ClientboundLoginPacket arg0, CallbackInfo arg1) {
        LightningClient.EVENT_HANDLER.post(new ClientConnectEvent());
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void handleMovePlayer$HEAD(ClientboundPlayerPositionPacket arg0, CallbackInfo arg1) {
        this.handleMovePlayer$guard = false;
        if (mc.player == null) return;
        if (PingBypassFlags.isPingBypassActive()) return;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var4 = null;
        if ((var4 == null) || (!(var4.isToggled()))) break label_4;
        var tmp0 = (var4.mode.getValue().equalsIgnoreCase("Bounce") ? 1 : 0);
        int var3 = tmp0;
        int var5 = tmp0;
        if (LightningClient.ROTATION_MANAGER != null) {
            if (!(LightningClient.ROTATION_MANAGER.isPacketRotateActive())) {
            }
            if (!(!var3)) {
            }
            this.handleMovePlayer$prevYaw = mc.player.getYRot();
            if (!(!var3) && var4.isBouncePitchOverrideActive()) {
            } else {
            }
            var4.getBounceSavedCameraPitch().handleMovePlayer$prevPitch = mc.player.getXRot();
            this.handleMovePlayer$guard = true;
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void handleMovePlayer$TAIL(ClientboundPlayerPositionPacket arg0, CallbackInfo arg1) {
        if (!this.handleMovePlayer$guard) return;
        this.handleMovePlayer$guard = false;
        mc.player.setYRot(this.handleMovePlayer$prevYaw);
        mc.player.setXRot(this.handleMovePlayer$prevPitch);
        mc.player.xRotO = this.handleMovePlayer$prevPitch;
    }

}
