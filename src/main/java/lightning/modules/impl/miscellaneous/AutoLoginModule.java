package lightning.modules.impl.miscellaneous;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@RegisterModule(name = "AutoLogin", description = "Automatically logs in on cracked servers.", category = Module.Category.MISCELLANEOUS)
public class AutoLoginModule extends Module {
    public StringSetting password;
    private final Timer timer;

    public AutoLoginModule() {
        this.password = new StringSetting("Password", "The password to use when logging in.", "password");
        this.timer = new Timer();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((getNull()) || (!(this.timer.hasTimeElapsed(10000)))) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundSystemChatPacket))) break label_3;
        Object var3 = ((ClientboundSystemChatPacket) var2);
        Object var4 = var3.content().getString().toLowerCase();
        if (var4.contains("/register")) {
            mc.getConnection().sendCommand(("register " + this.password.getValue() + " " + this.password.getValue()));
            LightningClient.CHAT_MANAGER.tagged("Registered successfully.", getName());
            this.timer.reset();
            break label_3;
        }
        if (!(var4.contains("/login"))) break label_3;
        mc.getConnection().sendCommand(("login " + this.password.getValue()));
        LightningClient.CHAT_MANAGER.tagged(("Logged in as " + mc.getUser().getName() + "."), getName());
        this.timer.reset();
        label_3:
    }

}
