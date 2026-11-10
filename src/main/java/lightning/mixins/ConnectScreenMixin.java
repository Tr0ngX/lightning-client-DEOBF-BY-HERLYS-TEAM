package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.ServerConnectEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void connect(Screen arg0, Minecraft arg1, ServerAddress arg2, ServerData arg3, boolean arg4, TransferState arg5, CallbackInfo arg6) {
        LightningClient.EVENT_HANDLER.post(new ServerConnectEvent(arg2, arg3));
    }

}
