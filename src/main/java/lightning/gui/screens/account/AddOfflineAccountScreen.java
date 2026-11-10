package lightning.gui.screens.account;

import java.awt.Color;
import java.util.Random;
import lightning.LightningClient;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddOfflineAccountScreen extends Screen {
    private final Screen parent;
    private EditBox usernameField;
    private String error;

    public AddOfflineAccountScreen(Screen arg0) {
        super(Component.literal("Add Offline Account"));
        this.error = "";
        this.parent = arg0;
    }

    protected void init() {
        int var1 = (this.width / 2);
        int var2 = (this.height / 2);
        int var3 = 175;
        int var4 = (var2 - (var3 / 2));
        this.usernameField = new EditBox(this.font, (var1 - 115), (var4 + 42), 230, 20, Component.literal("Username"));
        this.usernameField.setMaxLength(16);
        this.usernameField.setHint(Component.literal("Enter username..."));
        addRenderableWidget(this.usernameField);
        setInitialFocus(this.usernameField);
        addRenderableWidget(Button.builder(Component.literal("Add & Login"), () -> {
    Object var2 = this.usernameField.getValue().trim();
    if (var2.isEmpty()) {
        this.error = "Username cannot be empty!";
        return;
    }
    Object var3 = LightningClient.ACCOUNT_MANAGER.loginOffline(var2);
    if (var3 != null) {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}).bounds((var1 - 115), (var4 + 68), 230, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Random Name"), () -> {
    var tmp0 = new String[10];
    tmp0[0] = "Shadow";
    tmp0[1] = "Lightning";
    tmp0[2] = "Ghost";
    tmp0[3] = "Viper";
    tmp0[4] = "Phantom";
    tmp0[5] = "Raven";
    tmp0[6] = "Frost";
    tmp0[7] = "Nova";
    tmp0[8] = "Dark";
    tmp0[9] = "Storm";
    Object var2 = tmp0;
    Object var3 = (var2[new Random().nextInt(var2.length)] + "_" + (100 + new Random().nextInt(900)));
    this.usernameField.setValue(var3);
}).bounds((var1 - 115), (var4 + 94), 230, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), () -> onClose()).bounds((var1 - 115), (var4 + 120), 230, 20).build());
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderGradient(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = (this.width / 2);
        int var6 = (this.height / 2);
        int var7 = 270;
        int var8 = 175;
        int var9 = (var5 - (var7 / 2));
        int var10 = (var6 - (var8 / 2));
        Renderer2D.renderQuad(arg0, ((float) var9), ((float) var10), ((float) (var9 + var7)), ((float) (var10 + var8)), new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(arg0, ((float) var9), ((float) var10), ((float) (var9 + var7)), ((float) (var10 + var8)), new Color(70, 70, 70, 200));
        Object var11 = "Add Offline / Cracked Account";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var11, (var5 - (LightningClient.FONT_MANAGER.getWidth(var11) / 2)), (var10 + 12), Color.WHITE);
        if (!(this.error.isEmpty())) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, ("\u00a7c" + this.error), (var5 - (LightningClient.FONT_MANAGER.getWidth(this.error) / 2)), (var10 + 28), Color.RED);
        }
        super.extractRenderState(arg0, arg1, arg2, arg3);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

}
