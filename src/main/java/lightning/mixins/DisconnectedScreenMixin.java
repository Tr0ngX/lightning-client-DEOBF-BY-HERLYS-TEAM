package lightning.mixins;

import java.util.Map;
import lightning.LightningClient;
import lightning.gui.components.AccountButton;
import lightning.modules.impl.miscellaneous.AutoReconnectModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
    @Shadow
    @Final
    private LinearLayout layout;
    @Unique
    private Button toggleButton;
    @Unique
    private Button button;
    @Unique
    private double time;

    protected DisconnectedScreenMixin(Component arg0) {
        super(arg0);
        this.time = 100.0;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = At.Shift.BEFORE))
    private void init(CallbackInfo arg0) {
        if (LightningClient.SERVER_MANAGER.getLastConnection() != null) {
            this.button = new Button.Builder(Component.literal(getText()), () -> tryConnecting()).width(200).build();
            this.toggleButton = new Button.Builder(Component.literal(("Toggle " + String.valueOf((((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).isToggled() ? ChatFormatting.GREEN : ChatFormatting.RED)) + "AutoReconnect")), () -> {
    ((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).setToggled(!(((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).isToggled()), false);
    this.toggleButton.setMessage(Component.literal(("Toggle " + String.valueOf((((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).isToggled() ? ChatFormatting.GREEN : ChatFormatting.RED)) + "AutoReconnect")));
    this.button.setMessage(Component.literal(getText()));
    this.time = ((double) (((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).delay.getValue().intValue() * 20));
}).width(200).build();
            this.layout.addChild(this.button);
            this.layout.addChild(this.toggleButton);
        }
        addRenderableWidget(AccountButton.create(8, 8, 75, 20, this));
    }

    public void tick() {
        if ((!(((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).isToggled())) || (LightningClient.SERVER_MANAGER.getLastConnection() == null)) {
            return;
        }
        if (this.time <= 0.0) {
            tryConnecting();
            break label_3;
        }
        this.time = (this.time - 1.0);
        if (this.button == null) break label_3;
        this.button.setMessage(Component.literal(getText()));
        label_3:
    }

    @Unique
    private String getText() {
        Object var1 = "Reconnect";
        if (((AutoReconnectModule) LightningClient.MODULE_MANAGER.getModule(AutoReconnectModule.class)).isToggled()) {
            var tmp0 = new Object[1];
            tmp0[0] = (this.time / 20.0);
            var1 = (var1 + " " + String.format(("(" + String.valueOf(ChatFormatting.GREEN) + "%.1fs" + String.valueOf(ChatFormatting.RESET) + ")"), tmp0));
        }
        return var1;
    }

    @Unique
    private void tryConnecting() {
        ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), ((ServerAddress) LightningClient.SERVER_MANAGER.getLastConnection().left()), ((ServerData) LightningClient.SERVER_MANAGER.getLastConnection().right()), false, new TransferState(Map.of(), Map.of(), false));
    }

}
