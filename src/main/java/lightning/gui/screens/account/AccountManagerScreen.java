package lightning.gui.screens.account;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import lightning.LightningClient;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AccountManagerScreen extends Screen {
    private final Screen parent;
    private Account selectedAccount;
    private float scrollOffset;
    private float targetScroll;
    private String statusMessage;
    private long statusMessageTime;
    private long lastClickTime;
    private Account lastClickedAccount;
    private final Map hoverAnimations;
    private Button loginButton;
    private Button deleteButton;

    public AccountManagerScreen(Screen arg0) {
        super(Component.literal("Account Manager"));
        this.selectedAccount = null;
        this.scrollOffset = 0.0f;
        this.targetScroll = 0.0f;
        this.statusMessage = "";
        this.statusMessageTime = 0L;
        this.lastClickTime = 0L;
        this.lastClickedAccount = null;
        this.hoverAnimations = new HashMap();
        this.parent = arg0;
    }

    protected void init() {
        int var1 = (this.width / 2);
        int var2 = (this.height - 55);
        this.loginButton = Button.builder(Component.literal("Login"), () -> loginSelected()).bounds((var1 - 160), var2, 102, 20).build();
        addRenderableWidget(this.loginButton);
        addRenderableWidget(Button.builder(Component.literal("Add Microsoft"), () -> Minecraft.getInstance().gui.setScreen(new MicrosoftLoginScreen(this))).bounds((var1 - 52), var2, 104, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Add Offline"), () -> Minecraft.getInstance().gui.setScreen(new AddOfflineAccountScreen(this))).bounds((var1 + 58), var2, 102, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Random Offline"), () -> {
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
    Object var3 = tmp0;
    Object var4 = (var3[new Random().nextInt(var3.length)] + "_" + (100 + new Random().nextInt(900)));
    var tmp1 = LightningClient.ACCOUNT_MANAGER.loginOffline(var4);
    Object var2 = tmp1;
    this.selectedAccount = tmp1;
    setStatus(("Logged in as " + var4));
}).bounds((var1 - 160), (var2 + 25), 102, 20).build());
        this.deleteButton = Button.builder(Component.literal("Delete"), () -> {
    if (this.selectedAccount != null) {
        LightningClient.ACCOUNT_MANAGER.removeAccount(this.selectedAccount);
        this.selectedAccount = null;
        setStatus("Account deleted");
    }
}).bounds((var1 - 52), (var2 + 25), 104, 20).build();
        addRenderableWidget(this.deleteButton);
        addRenderableWidget(Button.builder(Component.literal("Back"), () -> onClose()).bounds((var1 + 58), (var2 + 25), 102, 20).build());
        updateButtonStates();
    }

    private void loginSelected() {
        if (this.selectedAccount != null) {
            setStatus(("Logging in to " + this.selectedAccount.getName() + "..."));
            CompletableFuture.runAsync(() -> {
    int var1 = LightningClient.ACCOUNT_MANAGER.login(this.selectedAccount);
    if (!(!var1)) {
        setStatus(("Logged in as " + this.selectedAccount.getName()));
    } else {
        setStatus(("\u00a7cFailed to login to " + this.selectedAccount.getName()));
    }
});
        }
    }

    private void updateButtonStates() {
        if (this.loginButton != null) {
            this.loginButton.active = this.selectedAccount != null;
            int var1 = (this.selectedAccount != null ? 1 : 0);
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = this.selectedAccount != null;
        }
    }

    private void setStatus(String arg0) {
        this.statusMessage = arg0;
        this.statusMessageTime = System.currentTimeMillis();
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderGradient(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(24, 24, 24), new Color(12, 12, 12));
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = (this.width / 2);
        Object var6 = "Account Manager";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var6, (var5 - (LightningClient.FONT_MANAGER.getWidth(var6) / 2)), 12, Color.WHITE);
        Object var7 = LightningClient.ACCOUNT_MANAGER.getCurrentName();
        if (((System.currentTimeMillis() - this.statusMessageTime) >= 5000L) || (this.statusMessage.isEmpty())) break label_3;
        if (this.statusMessage.startsWith("\u00a7c")) {
            break label_2;
        }
        if (!(this.statusMessage.startsWith("Logging"))) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var8 = new Color(255, 255, 100);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.statusMessage, (var5 - (LightningClient.FONT_MANAGER.getWidth(this.statusMessage) / 2)), 28, var8);
        break label_4;
        label_3:
        var8 = ("Logged in as: \u00a7f" + var7);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var8, (var5 - (LightningClient.FONT_MANAGER.getWidth(var8) / 2)), 28, new Color(180, 180, 180));
        label_4:
        var8 = (var5 - 160);
        int var9 = 55;
        int var10 = 320;
        int var11 = (this.height - 120);
        Renderer2D.renderQuad(arg0, ((float) var8), ((float) var9), ((float) (var8 + var10)), ((float) (var9 + var11)), new Color(24, 24, 24, 230));
        Renderer2D.renderOutline(arg0, ((float) var8), ((float) var9), ((float) (var8 + var10)), ((float) (var9 + var11)), new Color(60, 60, 60, 160));
        this.scrollOffset = (this.scrollOffset + ((this.targetScroll - this.scrollOffset) * 0.3f));
        Object var12 = LightningClient.ACCOUNT_MANAGER.getAccounts();
        int var13 = 32;
        int var14 = Math.max(0, ((var12.size() * var13) - var11));
        this.targetScroll = Mth.clamp(this.targetScroll, ((float) -(var14)), 0.0f);
        arg0.enableScissor(var8, var9, (var8 + var10), (var9 + var11));
        float var15 = (((float) var9) + this.scrollOffset);
        Object var16 = var12.iterator();
        while (var16.hasNext()) {
            Object var17 = ((Account) var16.next());
            if (((var15 + ((float) var13)) < ((float) var9)) || (var15 > ((float) (var9 + var11)))) break label_31;
            int var19 = (var17 == this.selectedAccount ? 1 : 0);
            int var20 = var17.getName().equalsIgnoreCase(var7);
            if (((((arg1 < (var8 + 4)) || (arg1 > ((var8 + var10) - 4))) || (((float) arg2) < (var15 + 2.0f))) || (((float) arg2) > ((var15 + ((float) var13)) - 2.0f))) || (arg2 < var9)) break label_8;
            int var21 = (arg2 <= (var9 + var11) ? 1 : 0);
            float var22 = (!(!var21) ? 1.0f : 0.0f);
            float var23 = ((Float) this.hoverAnimations.getOrDefault(var17, 0.0f)).floatValue();
            var tmp0 = (var23 + ((var22 - var23) * 0.22f));
            var23 = tmp0;
            if (Math.abs((var22 - tmp0)) < 0.005f) {
                var23 = var22;
            }
            this.hoverAnimations.put(var17, var23);
            if (!(!var19)) {
                break label_15;
            }
            if (!(!var20)) {
                break label_15;
            }
            label_15:
            Object var24 = new Color(30, 30, 30, 150);
            Renderer2D.renderQuad(arg0, ((float) (var8 + 4)), (var15 + 2.0f), ((float) ((var8 + var10) - 4)), ((var15 + ((float) var13)) - 2.0f), var24);
            Object var25 = getAccentColor();
            if (var23 > 0.001f) {
                float var26 = (((float) (var10 - 8)) * var23);
                Object var27 = new Color(var25.getRed(), var25.getGreen(), var25.getBlue(), ((int) (90.0f * var23)));
                Object var28 = new Color(var25.getRed(), var25.getGreen(), var25.getBlue(), ((int) (20.0f * var23)));
                Renderer2D.renderSidewaysGradient(arg0, ((float) (var8 + 4)), (var15 + 2.0f), (((float) (var8 + 4)) + var26), ((var15 + ((float) var13)) - 2.0f), var27, var28);
                Object var29 = new Color(var25.getRed(), var25.getGreen(), var25.getBlue(), ((int) (200.0f * var23)));
                Renderer2D.renderQuad(arg0, (((float) (var8 + 3)) + var26), (var15 + 2.0f), (((float) (var8 + 5)) + var26), ((var15 + ((float) var13)) - 2.0f), var29);
            }
            var tmp1 = (!(!var19) ? 1.0f : var23);
            float var18 = tmp1;
            var26 = tmp1;
            if (var18 > 0.001f) {
                if (!(!var19)) {
                } else {
                }
                var27 = (3.0f * var23);
                var28 = new Color(var25.getRed(), var25.getGreen(), var25.getBlue(), ((int) (255.0f * var18)));
                Renderer2D.renderQuad(arg0, ((float) (var8 + 4)), (var15 + 2.0f), (((float) (var8 + 4)) + var27), ((var15 + ((float) var13)) - 2.0f), var28);
            }
            var27 = (3.0f * var23);
            var28 = ((!(!var20) ? "\u00a7a\u2713 " : "") + var17.getName());
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var28, ((int) (((float) (var8 + 14)) + var27)), ((int) (var15 + 6.0f)), (!(!var20) ? Color.GREEN : Color.WHITE));
            var29 = ("[" + var17.getType().getDisplayName() + "]");
            switch (AccountManagerScreen.1.$SwitchMap$night$accounts$AccountType[var17.getType().ordinal()]) {
                case 1 -> { break label_27; }
                case 2 -> { break label_28; }
                case 3 -> { break label_29; }
                default -> { break label_26; }
            }
            label_26:
            throw new MatchException(null, null);
            label_27:
            break label_30;
            label_28:
            break label_30;
            label_29:
            label_30:
            Object var30 = new Color(220, 120, 255);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var29, ((int) (((float) (var8 + 14)) + var27)), ((int) (var15 + 18.0f)), var30);
            if (!var20) break label_31;
            Object var31 = "\u00a7aACTIVE";
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var31, (((var8 + var10) - LightningClient.FONT_MANAGER.getWidth(var31)) - 12), ((int) (var15 + 11.0f)), Color.GREEN);
            label_31:
            var15 = (var15 + ((float) var13));
        }
        arg0.disableScissor();
        if (var12.isEmpty()) {
            var16 = "No saved accounts. Click 'Add Offline' or 'Add Microsoft' below.";
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var16, (var5 - (LightningClient.FONT_MANAGER.getWidth(var16) / 2)), ((var9 + (var11 / 2)) - 5), new Color(140, 140, 160));
        }
        updateButtonStates();
        super.extractRenderState(arg0, arg1, arg2, arg3);
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        double var3 = arg0.x();
        double var5 = arg0.y();
        int var7 = arg0.button();
        int var8 = (this.width / 2);
        int var9 = (var8 - 160);
        int var10 = 55;
        int var11 = 320;
        int var12 = (this.height - 120);
        if (((((var7) || (var3 < ((double) var9))) || (var3 > ((double) (var9 + var11)))) || (var5 < ((double) var10))) || (var5 > ((double) (var10 + var12)))) break label_3;
        Object var13 = LightningClient.ACCOUNT_MANAGER.getAccounts();
        float var14 = ((float) ((var5 - ((double) var10)) - ((double) this.scrollOffset)));
        int var15 = 32;
        int var16 = ((int) (var14 / ((float) var15)));
        if ((var16 < 0) || (var16 >= var13.size())) break label_3;
        Object var17 = ((Account) var13.get(var16));
        long var18 = System.currentTimeMillis();
        if (var17 != this.lastClickedAccount) break label_1;
        if ((var18 - this.lastClickTime) >= 350L) {
            if (!arg1) break label_1;
        }
        this.selectedAccount = var17;
        loginSelected();
        break label_2;
        label_1:
        this.selectedAccount = var17;
        label_2:
        this.lastClickedAccount = var17;
        this.lastClickTime = var18;
        updateButtonStates();
        return true;
        label_3:
        return super.mouseClicked(arg0, arg1);
    }

    public boolean mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        Object var9 = LightningClient.ACCOUNT_MANAGER.getAccounts();
        int var10 = (this.height - 120);
        int var11 = 32;
        int var12 = Math.max(0, ((var9.size() * var11) - var10));
        this.targetScroll = (this.targetScroll + ((float) (arg3 * 24.0)));
        this.targetScroll = Mth.clamp(this.targetScroll, ((float) -(var12)), 0.0f);
        return true;
    }

    private Color getAccentColor() {
        Object var1 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        if (var1 != null) return var1.color.getColor();
        return new Color(130, 90, 240);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }


    class 1 {

        static {
            $SwitchMap$night$accounts$AccountType = new int[AccountType.values().length];
            try {
                $SwitchMap$night$accounts$AccountType[AccountType.MICROSOFT.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$accounts$AccountType[AccountType.OFFLINE.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$accounts$AccountType[AccountType.SESSION.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }
}
