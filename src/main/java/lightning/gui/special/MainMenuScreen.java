package lightning.gui.special;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.screens.account.AccountManagerScreen;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.MenuShader;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class MainMenuScreen extends Screen {
    private final List entries;
    private static final long LERP_DURATION_MS = 4000L;
    private static final long BUTTON_FADE_DURATION_MS = 800L;
    private static long initialOpenTime;

    public MainMenuScreen() {
        super(Component.literal("Lightning Client"));
        this.entries = new ArrayList();
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
    }

    protected void init() {
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
        this.entries.clear();
        if (LightningClient.FONT_MANAGER != null) {
            LightningClient.FONT_MANAGER.getWidth("Singleplayer Multiplayer Account Manager Options Quit LightningClient");
            LightningClient.FONT_MANAGER.getHeight();
        }
        int var1 = 230;
        int var2 = 24;
        int var3 = 6;
        int var4 = ((this.width / 2) - (var1 / 2));
        int var5 = ((this.height / 2) - 18);
        this.entries.add(new MainMenuScreen.Entry("S  Singleplayer", var4, var5, var1, var2, () -> this.minecraft.gui.setScreen(new SelectWorldScreen(this))));
        var tmp0 = (var5 + (var2 + var3));
        var5 = tmp0;
        this.entries.add(new MainMenuScreen.Entry("M  Multiplayer", var4, tmp0, var1, var2, () -> this.minecraft.gui.setScreen(new JoinMultiplayerScreen(this))));
        var tmp1 = (var5 + (var2 + var3));
        var5 = tmp1;
        this.entries.add(new MainMenuScreen.Entry("A  Account Manager", var4, tmp1, var1, var2, () -> this.minecraft.gui.setScreen(new AccountManagerScreen(this))));
        int var6 = ((var1 - var3) / 2);
        var tmp2 = (var5 + (var2 + var3));
        var5 = tmp2;
        this.entries.add(new MainMenuScreen.Entry("O  Options", var4, tmp2, var6, var2, () -> this.minecraft.gui.setScreen(new OptionsScreen(this, this.minecraft.options, false))));
        this.entries.add(new MainMenuScreen.Entry("Q  Quit", ((var4 + var6) + var3), var5, ((var1 - var6) - var3), var2, () -> this.minecraft.stop()));
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        super.extractRenderState(arg0, arg1, arg2, arg3);
        long var5 = (System.currentTimeMillis() - initialOpenTime);
        float var7 = Math.min(1.0f, (((float) var5) / 4000.0f));
        float var8 = 0.0f;
        if (var7 >= 1.0f) {
            long var9 = (var5 - 4000L);
            float var11 = Math.min(1.0f, (((float) var9) / 800.0f));
            var8 = (1.0f - ((float) Math.pow(((double) (1.0f - var11)), 3.0)));
        }
        if (var8 > 0.001f) {
            renderLogo(arg0, var8);
            var9 = this.entries.iterator();
            while (var9.hasNext()) {
                Object var10 = ((MainMenuScreen.Entry) var9.next());
                var10.render(arg0, arg1, arg2, arg3, var8);
            }
            renderFooter(arg0, var8);
        }
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        long var5 = (System.currentTimeMillis() - initialOpenTime);
        float var7 = Math.min(1.0f, (((float) var5) / 4000.0f));
        MenuShader.render(arg0, this.width, this.height, 1.0f, var7);
    }

    private void renderLogo(GuiGraphicsExtractor arg0, float arg1) {
        arg0.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("lightning", "textures/gui/logo_menu.png"), ((this.width / 2) - 42), ((this.height / 2) - 154), 0.0f, 0.0f, 84, 84, 1254, 1254);
        Object var3 = "Lightning Client";
        int var4 = LightningClient.FONT_MANAGER.getWidth(var3);
        int var5 = ((this.width / 2) - (var4 / 2));
        int var6 = ((this.height / 2) - 70);
        Object var7 = accentColor(((int) (220.0f * arg1)));
        Renderer2D.renderQuad(arg0, (((((float) this.width) / 2.0f) - (((float) var4) / 2.0f)) - 6.0f), ((float) ((var6 + LightningClient.FONT_MANAGER.getHeight()) + 3)), (((((float) this.width) / 2.0f) + (((float) var4) / 2.0f)) + 6.0f), ((float) ((var6 + LightningClient.FONT_MANAGER.getHeight()) + 4)), var7);
        if (isRainbow()) {
            LightningClient.FONT_MANAGER.drawRainbowString(arg0, var3, var5, var6, 6L);
        } else {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var3, var5, var6, new Color(255, 255, 255, ((int) (255.0f * arg1))));
        }
        Object var8 = "Recode By PhatNho";
        int var9 = LightningClient.FONT_MANAGER.getWidth(var8);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var8, ((this.width / 2) - (var9 / 2)), ((var6 + LightningClient.FONT_MANAGER.getHeight()) + 8), new Color(170, 170, 180, ((int) (180.0f * arg1))));
    }

    private void renderFooter(GuiGraphicsExtractor arg0, float arg1) {
        if (LightningClient.ACCOUNT_MANAGER != null) {
        } else {
        }
        Object var3 = ("Logged in as: \u00a7f" + "");
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var3, 6, 6, new Color(200, 200, 200, ((int) (200.0f * arg1))));
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        long var3 = (System.currentTimeMillis() - initialOpenTime);
        if (var3 < 4000L) return false;
        double var5 = arg0.x();
        double var7 = arg0.y();
        if (arg0.button()) break label_12;
        Object var9 = this.entries.iterator();
        label_7:
        if (!(var9.hasNext())) break label_12;
        Object var10 = ((MainMenuScreen.Entry) var9.next());
        if (!(var10.isHovered(var5, var7))) {
            break label_7;
        }
        playClick();
        var10.action.run();
        return true;
        label_12:
        return super.mouseClicked(arg0, arg1);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void playClick() {
        if (((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).sounds.getValue()) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    private static boolean isRainbow() {
        return ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).isRainbow();
    }

    private static Color accentColor(int arg0) {
        return ClickGuiScreen.getButtonColor(0, arg0);
    }

    static {
        initialOpenTime = -1L;
    }


    class Entry {
        private final String label;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final Runnable action;
        private float hover;

        private Entry(String arg0, int arg1, int arg2, int arg3, int arg4, Runnable arg5) {
            this.label = arg0;
            this.x = arg1;
            this.y = arg2;
            this.width = arg3;
            this.height = arg4;
            this.action = arg5;
        }

        private boolean isHovered(double arg0, double arg1) {
            if (((arg0 < ((double) this.x)) || (arg0 > ((double) (this.x + this.width)))) || (arg1 < ((double) this.y))) break label_1;
            return arg1 <= ((double) (this.y + this.height));
        }

        private static int clamp255(float arg0) {
            return Math.max(0, Math.min(255, ((int) arg0)));
        }

        private static int mixInt(int arg0, int arg1, float arg2) {
            return ((int) (((float) arg0) + (((float) (arg1 - arg0)) * arg2)));
        }

        private static float insetAt(float arg0, float arg1) {
            if ((arg1 <= 0.0f) || (arg0 >= arg1)) {
                return 0.0f;
            }
            float var2 = (arg1 - arg0);
            return (arg1 - ((float) Math.sqrt(((double) Math.max(0.0f, ((arg1 * arg1) - (var2 * var2)))))));
        }

        private static void quad(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
            if ((arg3 - arg1) <= 0.0f) return;
            Renderer2D.renderQuad(arg0, arg1, arg2, arg3, arg4, arg5);
        }

        private void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, float arg4) {
            float var6 = (isHovered(((double) arg1), ((double) arg2)) ? 1.0f : 0.0f);
            this.hover = (this.hover + ((var6 - this.hover) * 0.2f));
            float var7 = ((float) this.width);
            float var8 = ((float) this.height);
            float var9 = Math.min(6.0f, (var8 / 2.0f));
            float var10 = 1.0f;
            float var11 = 0.5f;
            int var12 = Math.round((var8 / var11));
            int var13 = clamp255((arg4 * (80.0f + (this.hover * 55.0f))));
            int var14 = clamp255((arg4 * (125.0f + (this.hover * 45.0f))));
            Object var15 = new Color(225, 205, 255, clamp255((arg4 * (75.0f + (this.hover * 140.0f)))));
            int var16 = 0;
            while (var16 < var12) {
                float var17 = ((((float) var16) + 0.5f) * var11);
                float var18 = (((float) this.y) + (((float) var16) * var11));
                float var19 = (var18 + var11);
                float var20 = Math.min(var17, (var8 - var17));
                float var21 = insetAt(var20, var9);
                if (var20 < var10) {
                    quad(arg0, (((float) this.x) + var21), var18, ((((float) this.x) + var7) - var21), var19, var15);
                } else {
                    float var22 = (var10 + insetAt((var20 - var10), (var9 - var10)));
                    float var23 = (var17 / var8);
                    Object var24 = new Color(mixInt(130, 55, var23), mixInt(90, 25, var23), mixInt(220, 120, var23), mixInt(var13, var14, var23));
                    quad(arg0, (((float) this.x) + var21), var18, (((float) this.x) + var22), var19, var15);
                    quad(arg0, ((((float) this.x) + var7) - var22), var18, ((((float) this.x) + var7) - var21), var19, var15);
                    quad(arg0, (((float) this.x) + var22), var18, ((((float) this.x) + var7) - var22), var19, var24);
                }
                var16++;
            }
            var16 = this.label.indexOf("  ");
            if (var16 > 0) {
            } else {
            }
            var17 = "";
            if (var16 > 0) {
            } else {
            }
            var18 = this.label;
            var19 = LightningClient.FONT_MANAGER.getHeight();
            var20 = (this.y + ((this.height - var19) / 2));
            var21 = new Color(255, 255, 255, clamp255((arg4 * 255.0f)));
            if (var17.isEmpty()) {
                var22 = LightningClient.FONT_MANAGER.getWidth(var18);
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var18, (this.x + ((this.width - var22) / 2)), var20, var21);
            } else {
                var22 = new Color(205, 170, 255, clamp255((arg4 * 255.0f)));
                var23 = LightningClient.FONT_MANAGER.getWidth(var17);
                var24 = LightningClient.FONT_MANAGER.getWidth(var18);
                int var25 = 6;
                int var26 = (this.x + ((this.width - ((var23 + var25) + var24)) / 2));
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var17, var26, var20, var22);
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var18, ((var26 + var23) + var25), var20, var21);
            }
        }

    }
}
