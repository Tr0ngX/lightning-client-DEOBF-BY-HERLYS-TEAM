package lightning.gui;

import java.net.InetSocketAddress;
import java.net.Socket;
import lightning.LightningClient;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PingBypassScreen extends Screen {
    private final Screen parent;
    private EditBox ipField;
    private EditBox portField;
    private EditBox passwordField;
    private EditBox serverField;
    private Button connectButton;
    private Button resumeButton;
    private volatile String proxyStatus;
    private volatile boolean pinged;

    public PingBypassScreen(Screen arg0) {
        super(Component.literal("PingBypass"));
        this.proxyStatus = "Pinging...";
        this.pinged = false;
        this.parent = arg0;
    }

    protected void init() {
        int var1 = (this.width / 2);
        int var2 = 50;
        this.ipField = new EditBox(this.font, (var1 - 150), var2, 300, 20, Component.literal("Proxy IP"));
        this.ipField.setMaxLength(256);
        this.ipField.setValue(getConfig().getIp());
        this.ipField.setHint(Component.literal("127.0.0.1"));
        addRenderableWidget(this.ipField);
        this.portField = new EditBox(this.font, (var1 - 150), (var2 + 34), 145, 20, Component.literal("Port"));
        this.portField.setMaxLength(5);
        this.portField.setValue(String.valueOf(getConfig().getPort()));
        this.portField.setHint(Component.literal("25565"));
        addRenderableWidget(this.portField);
        this.passwordField = new EditBox(this.font, (var1 + 5), (var2 + 34), 145, 20, Component.literal("Password"));
        this.passwordField.setMaxLength(64);
        this.passwordField.setValue(getConfig().getPassword());
        this.passwordField.setHint(Component.literal("Password"));
        addRenderableWidget(this.passwordField);
        this.serverField = new EditBox(this.font, (var1 - 150), (var2 + 68), 300, 20, Component.literal("Target Server"));
        this.serverField.setMaxLength(256);
        Object var3 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class)).server.getValue();
        this.serverField.setValue((var3.isEmpty() ? "" : var3));
        this.serverField.setHint(Component.literal("mc.server.com:25565"));
        addRenderableWidget(this.serverField);
        this.connectButton = Button.builder(Component.literal("Connect"), () -> connect()).bounds((var1 - 150), (var2 + 110), 300, 20).build();
        addRenderableWidget(this.connectButton);
        Object var4 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        this.resumeButton = Button.builder(Component.literal("Resume Session"), () -> resume()).bounds((var1 - 150), (var2 + 138), 300, 20).build();
        if (var4 == null) break label_2;
        this.resumeButton.active = var4.isToggled();
        addRenderableWidget(this.resumeButton);
        addRenderableWidget(Button.builder(Component.literal("Back"), () -> onClose()).bounds((var1 - 150), (this.height - 30), 300, 20).build());
        pingProxy();
    }

    private void pingProxy() {
        this.pinged = false;
        this.proxyStatus = "Pinging...";
        Object var1 = getConfig().getIp();
        int var2 = getConfig().getPort();
        new Thread(() -> {
    try {
        Object var3 = new Socket();
        try {
            var3.connect(new InetSocketAddress(arg0, arg1), 2000);
            this.proxyStatus = "Proxy online";
            this.pinged = true;
            var3.close();
        } catch (Throwable ex3) {
            Object var4 = ex3;
            try {
                var3.close();
            } catch (Throwable ex6) {
                Object var5 = ex6;
                var4.addSuppressed(var5);
            }
            throw var4;
        }
    } catch (Exception ex9) {
        var3 = ex9;
        this.proxyStatus = "\u00a7cOffline \u2014 cannot reach proxy";
        this.pinged = true;
    }
}, "PingBypass-Pinger").start();
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        super.extractRenderState(arg0, arg1, arg2, arg3);
        arg0.centeredText(this.font, "PingBypass", (this.width / 2), 20, 16777215);
        int var5 = (this.width / 2);
        int var6 = 50;
        arg0.text(this.font, "Proxy IP", (var5 - 150), (var6 - 11), 10526880);
        arg0.text(this.font, "Port", (var5 - 150), (var6 + 23), 10526880);
        arg0.text(this.font, "Password", (var5 + 5), (var6 + 23), 10526880);
        arg0.text(this.font, "Target Server", (var5 - 150), (var6 + 57), 10526880);
        if (this.proxyStatus.contains("Connected")) {
            break label_3;
        }
        if (this.proxyStatus.contains("Idle")) {
            break label_3;
        }
        int var7 = (this.proxyStatus.contains("Offline") ? 16733525 : 11184810);
        arg0.centeredText(this.font, this.proxyStatus, (this.width / 2), (var6 + 165), var7);
        if (!(!PingBypassFlags.proxyForwardingActive)) {
            Object var8 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
            if (var8.getServerName() != null) {
            } else {
            }
            Object var9 = ("Session active \u2014 " + "connected");
            arg0.centeredText(this.font, var9, (this.width / 2), (var6 + 178), 5635925);
        }
    }

    private void connect() {
        Object var1 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        var1.ip.setValue(this.ipField.getValue().trim());
        try {
            var1.port.setValue(Integer.parseInt(this.portField.getValue().trim()));
        } catch (NumberFormatException ex2) {
            Object var2 = ex2;
        }
        var1.password.setValue(this.passwordField.getValue());
        var1.server.setValue(this.serverField.getValue().trim());
        if (var1.isToggled()) {
            var1.setToggled(false, false);
        }
        var1.setToggled(true);
    }

    private void resume() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private PingBypassConfig getConfig() {
        return LightningClient.PINGBYPASS_CONFIG;
    }

}
