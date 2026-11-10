package lightning.gui.screens.account;

import java.awt.Color;
import java.net.URI;
import lightning.LightningClient;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.accounts.MicrosoftAuth;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class MicrosoftLoginScreen extends Screen {
    private final Screen parent;
    private volatile MicrosoftAuth.BrowserLoginSession session;
    private volatile String status;
    private volatile boolean failed;

    public MicrosoftLoginScreen(Screen arg0) {
        super(Component.literal("Microsoft Login"));
        this.status = "Opening browser for Microsoft login...";
        this.failed = false;
        this.parent = arg0;
    }

    protected void init() {
        int var1 = (this.width / 2);
        int var2 = (this.height / 2);
        int var3 = 160;
        int var4 = (var2 - (var3 / 2));
        if (this.session == null && !this.failed) {
            startAuth();
        }
        addRenderableWidget(Button.builder(Component.literal("Open Browser Again"), () -> {
    if ((this.session == null) || (this.session.loginUrl() == null)) break label_3;
    try {
        Util.getPlatform().openUri(URI.create(this.session.loginUrl()));
    } catch (Exception ex2) {
        Object var2 = ex2;
        this.status = ("\u00a7cFailed to open browser: " + var2.getMessage());
    }
    label_3:
}).bounds((var1 - 105), (var4 + 85), 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), () -> onClose()).bounds((var1 - 105), (var4 + 112), 210, 20).build());
    }

    private void startAuth() {
        try {
            this.session = MicrosoftAuth.startBrowserLogin(() -> this.status = arg0);
            this.session.future().thenAccept(() -> {
    if (arg0.success()) {
        Object var2 = new Account(arg0.username(), arg0.uuid(), AccountType.MICROSOFT, arg0.refreshToken(), arg0.accessToken());
        LightningClient.ACCOUNT_MANAGER.addAccount(var2);
        LightningClient.ACCOUNT_MANAGER.login(var2);
        Minecraft.getInstance().execute(this::onClose);
    } else {
        this.status = ("\u00a7c" + arg0.errorMessage());
        this.failed = true;
    }
});
        } catch (Exception ex2) {
            Object var1 = ex2;
            this.status = ("\u00a7cFailed to start login: " + var1.getMessage());
            this.failed = true;
        }
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderGradient(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = (this.width / 2);
        int var6 = (this.height / 2);
        int var7 = 290;
        int var8 = 160;
        int var9 = (var5 - (var7 / 2));
        int var10 = (var6 - (var8 / 2));
        Renderer2D.renderQuad(arg0, ((float) var9), ((float) var10), ((float) (var9 + var7)), ((float) (var10 + var8)), new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(arg0, ((float) var9), ((float) var10), ((float) (var9 + var7)), ((float) (var10 + var8)), new Color(70, 70, 70, 200));
        Object var11 = "Microsoft Account Login";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var11, (var5 - (LightningClient.FONT_MANAGER.getWidth(var11) / 2)), (var10 + 12), Color.WHITE);
        Object var12 = "A browser tab has been opened.";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var12, (var5 - (LightningClient.FONT_MANAGER.getWidth(var12) / 2)), (var10 + 32), new Color(180, 180, 180));
        Object var13 = "Log in to your account and approve to continue.";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var13, (var5 - (LightningClient.FONT_MANAGER.getWidth(var13) / 2)), (var10 + 45), new Color(180, 180, 180));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.status, (var5 - (LightningClient.FONT_MANAGER.getWidth(this.status) / 2)), (var10 + 66), new Color(85, 255, 255));
        super.extractRenderState(arg0, arg1, arg2, arg3);
    }

    public void onClose() {
        if (this.session != null && this.session.cancelAction() != null) {
            this.session.cancelAction().run();
        }
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

}
