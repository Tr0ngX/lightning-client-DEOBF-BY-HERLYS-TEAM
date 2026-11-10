package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Component arg0) {
        super(arg0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo arg0) {
        if (!PingBypassFlags.proxyForwardingActive) return;
        addRenderableWidget(Button.builder(Component.literal("\u00a7cDisconnect Proxy"), (arg0) -> {
    Object var1 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
    if (var1 != null && var1.isToggled()) {
        var1.setToggled(false);
    }
}).bounds(((this.width / 2) - 102), (this.height - 40), 204, 20).build());
    }

}
