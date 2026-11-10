package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lightning.LightningClient;
import lightning.gui.api.GuiDescriptionTranslations;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class DashboardScreen extends ClickGuiScreen {
    private static final int W = 460;
    private static final int H = 280;
    private static final int SB = 92;
    private static final int ST = 132;
    private static final int PAD = 10;
    private static final int CW = 104;
    private static final int CH = 40;
    private static final int GAP = 8;
    private static final int TOP = 30;
    private static final int BAR = 28;
    private static final Module.Category[] CATS;
    private static final Color WINDOW;
    private static final Color SIDE;
    private static final Color CARD;
    private static final Color CARD_SEL;
    private static final Color LINE;
    private static final Color LINE_HI;
    private static final Color CYAN;
    private static final Color TEXT;
    private static final Color DIM;
    private static final Color OFF;
    private static final Color DARK;
    private static final Color VEIL;
    private static final Color[] PALETTE;
    private final List rows;
    private final Map anim;
    private int x;
    private int y;
    private int px;
    private int pw;
    private int cat;
    private float scroll;
    private float sScroll;
    private Module selected;
    private NumberSetting dragging;
    private String query;
    private boolean searchFocus;
    private StringSetting editStr;
    private BindSetting listenBind;
    private NumberSetting editNum;
    private String numBuf;
    private String hoverDesc;

    public DashboardScreen() {
        this.rows = new ArrayList();
        this.anim = new HashMap();
        this.query = "";
        this.numBuf = "";
        this.hoverDesc = "";
    }

    private static boolean isMouseWithinBounds(double arg0, double arg1, int arg2, int arg3, int arg4, int arg5) {
        if (((arg0 < ((double) arg2)) || (arg0 >= ((double) (arg2 + arg4)))) || (arg1 < ((double) arg3))) break label_0;
        return arg1 < ((double) (arg3 + arg5));
    }

    private static float approach(float arg0, float arg1, float arg2) {
        float var3 = (arg1 - arg0);
        if (Math.abs(var3) < 0.01f) {
        } else {
        }
        return (arg0 + (var3 * arg2));
    }

    private static Color mix(Color arg0, Color arg1, float arg2) {
        return new Color(((int) (((float) arg0.getRed()) + (((float) (arg1.getRed() - arg0.getRed())) * arg2))), ((int) (((float) arg0.getGreen()) + (((float) (arg1.getGreen() - arg0.getGreen())) * arg2))), ((int) (((float) arg0.getBlue()) + (((float) (arg1.getBlue() - arg0.getBlue())) * arg2))));
    }

    private static boolean vis(Setting arg0) {
        Object var1 = arg0.getVisibility();
        if (var1 != null) {
            if (var1.isVisible()) {
            }
        } else {
        }
        return false;
    }

    private static void drawRoundedRect(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, float arg5, Color arg6) {
        LightningSkin.drawRoundedRectAll(arg0, arg1, arg2, (arg1 + arg3), (arg2 + arg4), arg5, arg6);
    }

    private void box(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, int arg4, float arg5, Color arg6, Color arg7) {
        drawRoundedRect(arg0, ((float) arg1), ((float) arg2), ((float) arg3), ((float) arg4), arg5, arg6);
        drawRoundedRect(arg0, ((float) (arg1 + 1)), ((float) (arg2 + 1)), ((float) (arg3 - 2)), ((float) (arg4 - 2)), Math.max(0.5f, (arg5 - 1.0f)), arg7);
    }

    private void text(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4) {
        LightningClient.FONT_MANAGER.drawText(arg0, arg1, arg2, arg3, arg4);
    }

    private int getTextWidth(String arg0) {
        return LightningClient.FONT_MANAGER.getWidth(arg0);
    }

    private String fit(String arg0, int arg1) {
        if (arg0 == null) return "";
        Object var3 = LightningClient.FONT_MANAGER;
        if (var3.getWidth(arg0) <= arg1) return arg0;
        while (arg0.length() > 1 && var3.getWidth((arg0 + "..")) > arg1) {
            arg0 = arg0.substring(0, (arg0.length() - 1));
        }
        return (arg0 + "..");
    }

    private void toggle(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        drawRoundedRect(arg0, ((float) arg1), ((float) arg2), 24.0f, 12.0f, 6.0f, mix(OFF, CYAN, arg3));
        drawRoundedRect(arg0, (((float) (arg1 + 2)) + (12.0f * arg3)), ((float) (arg2 + 2)), 8.0f, 8.0f, 4.0f, Color.WHITE);
    }

    private List mods() {
        return LightningClient.MODULE_MANAGER.getModules(CATS[this.cat]);
    }

    private List list() {
        if (this.query.isEmpty()) return mods();
        Object var1 = this.query.toLowerCase(Locale.ROOT);
        Object var2 = new ArrayList();
        Object var3 = CATS;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            Object var7 = LightningClient.MODULE_MANAGER.getModules(var6).iterator();
            while (var7.hasNext()) {
                Object var8 = ((Module) var7.next());
                if (var8.getName().toLowerCase(Locale.ROOT).contains(var1)) {
                    var2.add(var8);
                }
            }
            var5++;
        }
        return var2;
    }

    private int cardX(int arg0) {
        return (((this.x + 92) + 10) + ((arg0 % 2) * 112));
    }

    private int cardY(int arg0) {
        return (((this.y + 30) + ((arg0 / 2) * 48)) - ((int) this.scroll));
    }

    private int cardsH() {
        return 206;
    }

    private int searchX() {
        return (((((this.x + 92) + 10) + 208) + 8) - 104);
    }

    private static String desc(String arg0) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return "";
        }
        Object var1 = GuiDescriptionTranslations.translate(arg0);
        if (var1 != null) {
            if (var1.isEmpty()) {
            }
        } else {
        }
        return var1;
    }

    private List wrap(String arg0, int arg1, int arg2) {
        Object var4 = new ArrayList();
        if ((arg0 == null) || (arg0.isEmpty())) {
            return var4;
        }
        Object var5 = "";
        Object var6 = arg0.split(" ");
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = var6[var8];
            if (var5.isEmpty()) {
            } else {
            }
            Object var10 = (var5 + " " + var9);
            if (getTextWidth(var10) <= arg1) {
                var5 = var10;
                break label_7;
            }
            if (!(var5.isEmpty())) {
                var4.add(var5);
            }
            var5 = var9;
            if (var4.size() != arg2) break label_7;
            break label_8;
            label_7:
            var8++;
        }
        label_8:
        if (var4.size() < arg2 && !(var5.isEmpty())) {
            var4.add(var5);
        }
        if (!(var4.isEmpty()) && String.join(" ", var4).length() < arg0.length()) {
            var6 = (var4.size() - 1);
            var7 = ((String) var4.get(var6));
            while (var7.length() > 1 && getTextWidth((var7 + "..")) > arg1) {
                var7 = var7.substring(0, (var7.length() - 1));
            }
            var4.set(var6, (var7 + ".."));
        }
        return var4;
    }

    private static void line(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, float arg5, Color arg6) {
        int var7 = Math.max(1, ((int) Math.ceil(((double) (Math.max(Math.abs((arg3 - arg1)), Math.abs((arg4 - arg2))) * 2.0f)))));
        int var8 = 0;
        while (var8 <= var7) {
            float var9 = (((float) var8) / ((float) var7));
            float var10 = (arg1 + ((arg3 - arg1) * var9));
            float var11 = (arg2 + ((arg4 - arg2) * var9));
            Renderer2D.renderQuad(arg0, (var10 - (arg5 / 2.0f)), (var11 - (arg5 / 2.0f)), (var10 + (arg5 / 2.0f)), (var11 + (arg5 / 2.0f)), arg6);
            var8++;
        }
    }

    private static void bolt(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, Color arg4) {
        float var5 = (arg3 / 10.0f);
        line(arg0, (arg1 + (7.0f * var5)), arg2, (arg1 + (2.5f * var5)), (arg2 + (6.0f * var5)), (1.8f * var5), arg4);
        line(arg0, (arg1 + (2.5f * var5)), (arg2 + (6.0f * var5)), (arg1 + (7.0f * var5)), (arg2 + (6.0f * var5)), (1.8f * var5), arg4);
        line(arg0, (arg1 + (7.0f * var5)), (arg2 + (6.0f * var5)), (arg1 + (3.0f * var5)), (arg2 + (10.0f * var5)), (1.8f * var5), arg4);
    }

    private static void searchIcon(GuiGraphicsExtractor arg0, float arg1, float arg2, Color arg3, Color arg4) {
        drawRoundedRect(arg0, arg1, arg2, 7.0f, 7.0f, 3.5f, arg3);
        drawRoundedRect(arg0, (arg1 + 1.4f), (arg2 + 1.4f), 4.2f, 4.2f, 2.1f, arg4);
        line(arg0, (arg1 + 6.0f), (arg2 + 6.0f), (arg1 + 9.5f), (arg2 + 9.5f), 1.6f, arg3);
    }

    private static void catIcon(GuiGraphicsExtractor arg0, int arg1, float arg2, float arg3, Color arg4, Color arg5) {
        switch (arg1) {
            case 0 -> { break label_0; }
            case 1 -> { break label_1; }
            case 2 -> { break label_2; }
            case 3 -> { break label_3; }
            default -> { break label_4; }
        }
        label_0:
        line(arg0, (arg2 + 1.0f), (arg3 + 1.0f), (arg2 + 9.0f), (arg3 + 9.0f), 1.6f, arg4);
        line(arg0, (arg2 + 9.0f), (arg3 + 1.0f), (arg2 + 1.0f), (arg3 + 9.0f), 1.6f, arg4);
        line(arg0, (arg2 + 1.0f), (arg3 + 4.0f), (arg2 + 4.0f), (arg3 + 1.0f), 1.4f, arg4);
        line(arg0, (arg2 + 9.0f), (arg3 + 4.0f), (arg2 + 6.0f), (arg3 + 1.0f), 1.4f, arg4);
        break label_5;
        label_1:
        line(arg0, (arg2 + 1.0f), (arg3 + 1.0f), (arg2 + 4.5f), (arg3 + 5.0f), 1.6f, arg4);
        line(arg0, (arg2 + 4.5f), (arg3 + 5.0f), (arg2 + 1.0f), (arg3 + 9.0f), 1.6f, arg4);
        line(arg0, (arg2 + 5.5f), (arg3 + 1.0f), (arg2 + 9.0f), (arg3 + 5.0f), 1.6f, arg4);
        line(arg0, (arg2 + 9.0f), (arg3 + 5.0f), (arg2 + 5.5f), (arg3 + 9.0f), 1.6f, arg4);
        break label_5;
        label_2:
        drawRoundedRect(arg0, arg2, (arg3 + 1.5f), 10.0f, 7.0f, 3.5f, arg4);
        drawRoundedRect(arg0, (arg2 + 1.2f), (arg3 + 2.7f), 7.6f, 4.6f, 2.3f, arg5);
        drawRoundedRect(arg0, (arg2 + 3.5f), (arg3 + 3.5f), 3.0f, 3.0f, 1.5f, arg4);
        break label_5;
        label_3:
        drawRoundedRect(arg0, (arg2 + 3.0f), arg3, 4.0f, 4.0f, 2.0f, arg4);
        drawRoundedRect(arg0, (arg2 + 0.5f), (arg3 + 5.0f), 9.0f, 5.0f, 2.5f, arg4);
        break label_5;
        label_4:
        drawRoundedRect(arg0, arg2, (arg3 + 1.5f), 10.0f, 1.3f, 0.6f, arg4);
        drawRoundedRect(arg0, arg2, (arg3 + 4.4f), 10.0f, 1.3f, 0.6f, arg4);
        drawRoundedRect(arg0, arg2, (arg3 + 7.3f), 10.0f, 1.3f, 0.6f, arg4);
        drawRoundedRect(arg0, (arg2 + 6.0f), arg3, 2.6f, 4.2f, 1.2f, arg4);
        drawRoundedRect(arg0, (arg2 + 2.0f), (arg3 + 3.0f), 2.6f, 4.2f, 1.2f, arg4);
        drawRoundedRect(arg0, (arg2 + 5.5f), (arg3 + 5.9f), 2.6f, 4.2f, 1.2f, arg4);
        label_5:
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderQuad(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), VEIL);
    }

    private static int rowHeight(Setting arg0) {
        if ((arg0 instanceof NumberSetting)) return 26;
        if ((arg0 instanceof BooleanSetting)) return 16;
        if ((arg0 instanceof ModeSetting)) return 24;
        if ((arg0 instanceof CategorySetting)) return 18;
        if ((arg0 instanceof BindSetting)) return 16;
        if ((arg0 instanceof ColorSetting)) return 36;
        if ((arg0 instanceof StringSetting)) return 26;
        if ((arg0 instanceof WhitelistSetting)) return 16;
        return 0;
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!(isClosing())) break label_1;
        Object var5 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        if (var5 != null && var5.isToggled()) {
            var5.setToggled(false);
        }
        setReadyToRemove(true);
        this.minecraft.gui.setScreen(null);
        return;
        label_1:
        this.x = ((this.width - 460) / 2);
        this.y = ((this.height - 280) / 2);
        this.px = (((this.x + 460) - 132) + 10);
        this.pw = 112;
        this.hoverDesc = "";
        var5 = list();
        if (this.selected == null) break label_3;
        if (!(this.query.isEmpty())) break label_2;
        if (this.selected.getCategory() != CATS[this.cat]) break label_3;
        label_2:
        if (!(var5.contains(this.selected))) {
            label_3:
            if (!(var5.isEmpty())) break label_4;
            break label_5;
            label_4:
            label_5:
            null.selected = ((Module) var5.get(0));
        }
        box(arg0, this.x, this.y, 460, 280, 8.0f, LINE, WINDOW);
        drawRoundedRect(arg0, ((float) (this.x + 1)), ((float) (this.y + 1)), 91.0f, 278.0f, 7.0f, SIDE);
        drawRoundedRect(arg0, ((float) ((this.x + 460) - 132)), ((float) (this.y + 1)), 131.0f, 278.0f, 7.0f, SIDE);
        bolt(arg0, ((float) (this.x + 11)), ((float) (this.y + 10)), 11.0f, CYAN);
        text(arg0, "Lightning", (this.x + 27), (this.y + 12), CYAN);
        int var6 = 0;
        while (var6 < CATS.length) {
            int var7 = ((this.y + 34) + (var6 * 24));
            if (var6 != this.cat) break label_8;
            int var8 = (this.query.isEmpty() ? 1 : 0);
            int var9 = isMouseWithinBounds(((double) arg1), ((double) arg2), (this.x + 6), var7, 80, 20);
            Object var10 = SIDE;
            if (!(!var8)) {
                drawRoundedRect(arg0, ((float) (this.x + 6)), ((float) var7), 80.0f, 20.0f, 5.0f, CARD_SEL);
                drawRoundedRect(arg0, ((float) (this.x + 6)), ((float) (var7 + 3)), 2.0f, 14.0f, 1.0f, CYAN);
                var10 = CARD_SEL;
                break label_11;
            }
            if (!var9) break label_11;
            drawRoundedRect(arg0, ((float) (this.x + 6)), ((float) var7), 80.0f, 20.0f, 5.0f, CARD);
            var10 = CARD;
            label_11:
            catIcon(arg0, var6, ((float) (this.x + 14)), ((float) (var7 + 5)), (!(!var8) ? DashboardScreen.CYAN : DashboardScreen.DIM), var10);
            text(arg0, fit(CATS[var6].getName(), 34), (this.x + 30), (var7 + 6), (!(!var8) ? DashboardScreen.TEXT : DashboardScreen.DIM));
            Object var11 = String.valueOf(LightningClient.MODULE_MANAGER.getModules(CATS[var6]).stream().filter(Module::isToggled).count());
            text(arg0, var11, (((this.x + 92) - 10) - getTextWidth(var11)), (var7 + 6), (!(!var8) ? DashboardScreen.CYAN : DashboardScreen.DIM));
            var6++;
        }
        if (this.query.isEmpty()) {
        } else {
        }
        arg0.text(this, CATS[this.cat].getName().fit("Search", 100), ((this.x + 92) + 10), (this.y + 12), TEXT);
        var6 = searchX();
        var7 = (this.y + 7);
        box(arg0, var6, var7, 104, 16, 5.0f, (!(!this.searchFocus) ? DashboardScreen.CYAN : DashboardScreen.LINE), CARD);
        searchIcon(arg0, ((float) (var6 + 5)), ((float) (var7 + 3)), (!(!this.searchFocus) ? DashboardScreen.CYAN : DashboardScreen.DIM), CARD);
        if (this.query.isEmpty() && !this.searchFocus) {
            text(arg0, "Search", (var6 + 20), (var7 + 4), DIM);
        } else {
            var8 = this.query;
            while (var8.length() > 1 && getTextWidth(var8) > 74) {
                var8 = var8.substring(1);
            }
            if (!this.searchFocus) break label_28;
            var9 = (((System.currentTimeMillis() / 500L) % 2L) == 0L ? 1 : 0);
            text(arg0, (var8 + (!(!var9) ? "|" : "")), (var6 + 20), (var7 + 4), TEXT);
        }
        var8 = cardsH();
        var9 = Math.max(0, ((((var5.size() + 1) / 2) * 48) - var8));
        this.scroll = Math.max(0.0f, Math.min(this.scroll, ((float) var9)));
        arg0.enableScissor((this.x + 92), (this.y + 30), ((this.x + 460) - 132), ((this.y + 30) + var8));
        var10 = 0;
        while (var10 < var5.size()) {
            var11 = ((Module) var5.get(var10));
            int var12 = cardX(var10);
            int var13 = cardY(var10);
            Object var14 = ((float[]) this.anim.computeIfAbsent(var11, (arg0) -> new float[2]));
            if (!(isMouseWithinBounds(((double) arg1), ((double) arg2), var12, var13, 104, 40))) break label_34;
            int var15 = (isMouseWithinBounds(((double) arg1), ((double) arg2), (this.x + 92), (this.y + 30), 236, var8) ? 1 : 0);
            var14[0] = approach(var14[0], (var11.isToggled() ? 1.0f : 0.0f), 0.25f);
            var14[1] = approach(var14[1], (!(!var15) ? 1.0f : 0.0f), 0.3f);
            if (!(!var15)) {
                this.hoverDesc = var11.getDescription();
            }
            if (var11 == this.selected) {
            } else {
            }
            arg0.box(var12, var13, 104, 40, 6.0f, CYAN, mix(LINE, LINE_HI, var14[1]), (var11 == this.selected ? DashboardScreen.CARD_SEL : DashboardScreen.CARD));
            text(arg0, fit(var11.getName(), 58), (var12 + 8), (var13 + 7), mix(DIM, TEXT, Math.max(var14[0], var14[1])));
            toggle(arg0, ((var12 + 104) - 32), (var13 + 6), var14[0]);
            text(arg0, fit(desc(var11.getDescription()), 88), (var12 + 8), (var13 + 24), DIM);
            var10++;
        }
        arg0.disableScissor();
        this.rows.clear();
        var10 = (this.y + 32);
        var11 = ((this.y + 280) - 10);
        if (this.selected != null) {
            text(arg0, fit(this.selected.getName(), this.pw), this.px, (this.y + 12), CYAN);
            var12 = new ArrayList();
            var13 = 0;
            var14 = this.selected.getSettings().iterator();
            while (var14.hasNext()) {
                var15 = ((Setting) var14.next());
                if (!(vis(var15))) {
                    break label_46;
                }
                int var16 = rowHeight(var15);
                if (!var16) {
                    break label_46;
                }
                var12.add(var15);
                var13 = (var13 + (var16 + 6));
            }
            var14 = ((float) Math.max(0, (var13 - ((var11 - var10) - 2))));
            this.sScroll = Math.max(0.0f, Math.min(this.sScroll, var14));
            arg0.enableScissor((this.px - 4), var10, ((this.x + 460) - 2), var11);
            var15 = ((var10 + 2) - ((int) this.sScroll));
            var16 = isMouseWithinBounds(((double) arg1), ((double) arg2), (this.px - 4), var10, (this.pw + 8), (var11 - var10));
            Object var17 = var12.iterator();
            while (var17.hasNext()) {
                Object var18 = ((Setting) var17.next());
                int var19 = rowHeight(var18);
                this.rows.add(new DashboardScreen.Row(var18, var15, var19));
                if (!(!var16) && isMouseWithinBounds(((double) arg1), ((double) arg2), (this.px - 2), var15, (this.pw + 4), var19)) {
                    this.hoverDesc = var18.getDescription();
                }
                drawSetting(arg0, var18, var15, arg1, arg2);
                var15 = (var15 + (var19 + 6));
            }
            arg0.disableScissor();
        }
        var12 = this.hoverDesc;
        if (var12 != null) {
            if (!(var12.isEmpty())) break label_55;
        }
        if (this.selected == null) break label_55;
        var12 = this.selected.getDescription();
        label_55:
        var13 = ((this.x + 92) + 10);
        var14 = ((((this.y + 280) - 10) - 28) + 2);
        var15 = 216;
        box(arg0, var13, var14, var15, 26, 5.0f, LINE, CARD);
        var16 = (var14 + 5);
        var17 = wrap(desc(var12), (var15 - 14), 2).iterator();
        while (var17.hasNext()) {
            var18 = ((String) var17.next());
            text(arg0, var18, (var13 + 7), var16, DIM);
            var16 += 10;
        }
    }

    private void drawSetting(GuiGraphicsExtractor arg0, Setting arg1, int arg2, int arg3, int arg4) {
        if (!((arg1 instanceof CategorySetting))) break label_6;
        Object var6 = ((CategorySetting) arg1);
        int var7 = var6.isOpen();
        drawRoundedRect(arg0, ((float) this.px), ((float) arg2), ((float) this.pw), 16.0f, 4.0f, CARD);
        text(arg0, fit(var6.getName(), (this.pw - 24)), (this.px + 6), (arg2 + 4), (!(!var7) ? DashboardScreen.CYAN : DashboardScreen.TEXT));
        text(arg0, (!(!var7) ? "-" : "+"), ((this.px + this.pw) - 12), (arg2 + 4), (!(!var7) ? DashboardScreen.CYAN : DashboardScreen.DIM));
        return;
        label_6:
        text(arg0, fit(arg1.getName(), (this.pw - 40)), this.px, arg2, DIM);
        if ((arg1 instanceof NumberSetting)) {
            var6 = ((NumberSetting) arg1);
            Object var13 = var6.getValue();
            if ((var13 instanceof Integer)) {
            } else {
            }
            Object var14 = String.valueOf((((double) Math.round((var13.doubleValue() * 100.0))) / 100.0));
            if (this.editNum == var6) {
                int var15 = (((System.currentTimeMillis() / 500L) % 2L) == 0L ? 1 : 0);
                Object var16 = (this.numBuf + (!(!var15) ? "|" : ""));
                int var17 = Math.max(34, (getTextWidth(var16) + 10));
                box(arg0, ((this.px + this.pw) - var17), (arg2 - 3), var17, 14, 4.0f, CYAN, CARD);
                text(arg0, var16, (((this.px + this.pw) - var17) + 5), arg2, CYAN);
            } else {
                text(arg0, var14, ((this.px + this.pw) - getTextWidth(var14)), arg2, TEXT);
            }
            var15 = var6.getMinimum().doubleValue();
            var17 = var6.getMaximum().doubleValue();
            float var19 = ((float) ((var13.doubleValue() - var15) / Math.max(1.0E-4, (var17 - var15))));
            drawRoundedRect(arg0, ((float) this.px), ((float) (arg2 + 15)), ((float) this.pw), 4.0f, 2.0f, OFF);
            drawRoundedRect(arg0, ((float) this.px), ((float) (arg2 + 15)), Math.max(4.0f, (((float) this.pw) * var19)), 4.0f, 2.0f, CYAN);
            break label_60;
        }
        if ((arg1 instanceof BooleanSetting)) {
            var7 = ((BooleanSetting) arg1);
            drawRoundedRect(arg0, ((float) ((this.px + this.pw) - 14)), ((float) (arg2 - 1)), 14.0f, 14.0f, 4.0f, (var7.getValue() ? DashboardScreen.CYAN : DashboardScreen.OFF));
            if (!(var7.getValue())) break label_60;
            drawRoundedRect(arg0, ((float) ((this.px + this.pw) - 10)), ((float) (arg2 + 3)), 6.0f, 6.0f, 2.0f, DARK);
            break label_60;
        }
        if ((arg1 instanceof ModeSetting)) {
            Object var8 = ((ModeSetting) arg1);
            box(arg0, this.px, (arg2 + 10), this.pw, 14, 4.0f, LINE, CARD);
            text(arg0, fit(var8.getValue(), (this.pw - 10)), (this.px + 5), (arg2 + 13), TEXT);
            break label_60;
        }
        if ((arg1 instanceof BindSetting)) {
            Object var9 = ((BindSetting) arg1);
            var13 = (this.listenBind == var9 ? 1 : 0);
            if (!(!var13)) {
                var14 = "...";
                break label_25;
            }
            if (!(var9.getValue())) {
            } else {
            }
            var14 = KeyboardUtils.getKeyName(var9.getValue());
            var15 = var9.getMode();
            if ((var15 == null) || (var15.equals("Bind"))) break label_25;
            var14 = (var14 + " (" + var15 + ")");
            label_25:
            var14 = fit(var14, 62);
            var15 = Math.max(30, (getTextWidth(var14) + 12));
            box(arg0, ((this.px + this.pw) - var15), (arg2 - 2), var15, 15, 4.0f, (!(!var13) ? DashboardScreen.CYAN : DashboardScreen.LINE), CARD);
            text(arg0, var14, (((this.px + this.pw) - var15) + 6), (arg2 + 1), (!(!var13) ? DashboardScreen.CYAN : DashboardScreen.TEXT));
            break label_60;
        }
        if ((arg1 instanceof ColorSetting)) {
            Object var10 = ((ColorSetting) arg1);
            var13 = var10.getColor();
            drawRoundedRect(arg0, ((float) ((this.px + this.pw) - 14)), ((float) (arg2 - 1)), 14.0f, 11.0f, 3.0f, (var10.isRainbow() ? DashboardScreen.CYAN : DashboardScreen.LINE_HI));
            if (var10.isRainbow()) {
            } else {
            }
            drawRoundedRect(((float) ((this.px + this.pw) - 13)), ((float) arg2), 12.0f, 9.0f, 2.5f, new Color(255, 120, 200), new Color(var13.getRed(), var13.getGreen(), var13.getBlue()));
            var14 = 0;
            while (var14 <= PALETTE.length) {
                var15 = (this.px + (var14 * 11));
                var16 = (var14 == PALETTE.length ? 1 : 0);
                if (!(!var16)) {
                } else {
                }
                var17 = PALETTE[var14];
                if (!(!var16)) {
                    break label_42;
                }
                if (var10.isRainbow()) break label_41;
                int var18 = (var17.getRGB() == (var13.getRGB() | -16777216) ? 1 : 0);
                if (!(!var18)) {
                    drawRoundedRect(arg0, ((float) (var15 - 1)), ((float) (arg2 + 13)), 11.0f, 11.0f, 3.0f, TEXT);
                }
                drawRoundedRect(arg0, ((float) var15), ((float) (arg2 + 14)), 9.0f, 9.0f, 2.5f, var17);
                if (!(!var16)) {
                    drawRoundedRect(arg0, (((float) var15) + 2.5f), (((float) arg2) + 16.5f), 4.0f, 4.0f, 2.0f, new Color(92, 225, 255));
                }
                var14++;
            }
            break label_60;
        }
        if ((arg1 instanceof StringSetting)) {
            Object var11 = ((StringSetting) arg1);
            var13 = (this.editStr == var11 ? 1 : 0);
            box(arg0, this.px, (arg2 + 10), this.pw, 14, 4.0f, (!(!var13) ? DashboardScreen.CYAN : DashboardScreen.LINE), CARD);
            if (var11.getValue() == null) {
            } else {
            }
            var14 = var11.getValue();
            while (var14.length() > 1 && getTextWidth(var14) > (this.pw - 14)) {
                var14 = var14.substring(1);
            }
            if (!var13) break label_55;
            var15 = (((System.currentTimeMillis() / 500L) % 2L) == 0L ? 1 : 0);
            text(arg0, (var14 + (!(!var15) ? "|" : "")), (this.px + 5), (arg2 + 13), TEXT);
            break label_60;
        }
        if (!((arg1 instanceof WhitelistSetting))) break label_60;
        Object var12 = ((WhitelistSetting) arg1);
        var13 = (var12.getWhitelist().size() + " items");
        text(arg0, var13, ((this.px + this.pw) - getTextWidth(var13)), arg2, TEXT);
        label_60:
    }

    private void setNum(NumberSetting arg0, double arg1) {
        double var4 = arg0.getMinimum().doubleValue();
        double var6 = arg0.getMaximum().doubleValue();
        double var8 = arg0.getStep().doubleValue();
        double var10 = (var4 + (Math.max(0.0, Math.min(1.0, ((arg1 - ((double) this.px)) / ((double) this.pw)))) * (var6 - var4)));
        if (var8 > 0.0) {
            var10 = (var4 + (((double) Math.round(((var10 - var4) / var8))) * var8));
        }
        applyNum(arg0, var10);
    }

    private void applyNum(NumberSetting arg0, double arg1) {
        double var4 = arg0.getMinimum().doubleValue();
        double var6 = arg0.getMaximum().doubleValue();
        arg1 = Math.max(var4, Math.min(var6, arg1));
        Object var8 = arg0.getValue();
        if ((var8 instanceof Integer)) {
            break label_2;
        }
        if ((var8 instanceof Float)) {
            break label_2;
        }
        label_2:
        ((float) arg1).setValue(arg1);
    }

    private void resetInput() {
        this.searchFocus = false;
        this.editStr = null;
        this.listenBind = null;
        this.editNum = null;
        this.numBuf = "";
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        double var3 = arg0.x();
        double var5 = arg0.y();
        int var7 = arg0.button();
        if (!(!var7) && var7 != 1) return true;
        resetInput();
        if (var7 == 1) {
            if (!(isMouseWithinBounds(var3, var5, (this.px - 4), (this.y + 32), (this.pw + 8), 238))) break label_5;
            Object var8 = this.rows.iterator();
            label_1:
            if (!(var8.hasNext())) break label_5;
            Object var9 = ((DashboardScreen.Row) var8.next());
            if (isMouseWithinBounds(var3, var5, (this.px - 2), (var9.y() - 2), (this.pw + 4), (var9.h() + 2))) break label_2;
            break label_1;
            label_2:
            Object var12 = var9.s();
            if (!((var12 instanceof NumberSetting))) break label_3;
            Object var10 = ((NumberSetting) var12);
            this.editNum = var10;
            this.numBuf = "";
            break label_4;
            label_3:
            var12 = var9.s();
            if (!((var12 instanceof BindSetting))) break label_4;
            Object var11 = ((BindSetting) var12);
            if (!(var11.isHoldModesSupported())) break label_4;
            var11.cycleMode();
            label_4:
            break label_5;
            label_5:
            return true;
        }
        if (isMouseWithinBounds(var3, var5, searchX(), (this.y + 7), 104, 16)) {
            this.searchFocus = true;
            return true;
        }
        var8 = 0;
        while (var8 < CATS.length) {
            if (isMouseWithinBounds(var3, var5, (this.x + 6), ((this.y + 34) + (var8 * 24)), 80, 20)) {
                this.cat = var8;
                this.scroll = 0.0f;
                this.sScroll = 0.0f;
                this.selected = null;
                this.query = "";
                return true;
            }
            var8++;
        }
        if (!(isMouseWithinBounds(var3, var5, (this.x + 92), (this.y + 30), 236, cardsH()))) break label_18;
        var8 = list();
        var9 = 0;
        while (var9 < var8.size()) {
            var10 = cardX(var9);
            var11 = cardY(var9);
            if (!(isMouseWithinBounds(var3, var5, var10, var11, 104, 40))) {
            } else {
                var12 = ((Module) var8.get(var9));
                if (var3 >= ((double) ((var10 + 104) - 34)) && var5 < ((double) (var11 + 22))) {
                    var12.setToggled(!(var12.isToggled()));
                } else {
                    this.selected = var12;
                    this.sScroll = 0.0f;
                }
                return true;
            }
            var9++;
        }
        label_18:
        if (!(isMouseWithinBounds(var3, var5, (this.px - 4), (this.y + 32), (this.pw + 8), 238))) break label_35;
        var8 = this.rows.iterator();
        label_19:
        if (!(var8.hasNext())) break label_35;
        var9 = ((DashboardScreen.Row) var8.next());
        if (!(isMouseWithinBounds(var3, var5, (this.px - 2), (var9.y() - 2), (this.pw + 4), (var9.h() + 2)))) {
            break label_19;
        }
        var10 = var9.s();
        if ((var10 instanceof NumberSetting)) {
            var11 = ((NumberSetting) var10);
            this.dragging = var11;
            setNum(var11, var3);
            break label_34;
        }
        if ((var10 instanceof BooleanSetting)) {
            var12 = ((BooleanSetting) var10);
            var12.setValue(!(var12.getValue()));
            break label_34;
        }
        if ((var10 instanceof ModeSetting)) {
            Object var13 = ((ModeSetting) var10);
            Object var18 = var13.getModes();
            var13.setValue(((String) var18.get(((var18.indexOf(var13.getValue()) + 1) % var18.size()))));
            break label_34;
        }
        if ((var10 instanceof CategorySetting)) {
            Object var14 = ((CategorySetting) var10);
            var14.setOpen(!(var14.isOpen()));
            break label_34;
        }
        if ((var10 instanceof BindSetting)) {
            Object var15 = ((BindSetting) var10);
            this.listenBind = var15;
            break label_34;
        }
        if ((var10 instanceof StringSetting)) {
            Object var16 = ((StringSetting) var10);
            this.editStr = var16;
            break label_34;
        }
        if (!((var10 instanceof ColorSetting))) break label_34;
        Object var17 = ((ColorSetting) var10);
        var18 = ((int) ((var3 - ((double) this.px)) / 11.0));
        if (((var5 < ((double) (var9.y() + 13))) || (var18 < 0)) || (var18 > PALETTE.length)) break label_34;
        if (var18 == PALETTE.length) {
            var17.setRainbow(!(var17.isRainbow()));
            break label_34;
        }
        var17.setRainbow(false);
        Object var19 = PALETTE[var18];
        var17.setColor(new Color(var19.getRed(), var19.getGreen(), var19.getBlue(), var17.getAlpha()));
        label_34:
        return true;
        label_35:
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent arg0, double arg1, double arg2) {
        if (this.dragging != null) {
            setNum(this.dragging, arg0.x());
        }
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent arg0) {
        this.dragging = null;
        return true;
    }

    public boolean mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (isMouseWithinBounds(arg0, arg1, (this.px - 4), (this.y + 32), (this.pw + 8), 238)) {
            this.sScroll = (this.sScroll - (((float) arg3) * 20.0f));
        } else {
            this.scroll = (this.scroll - (((float) arg3) * 20.0f));
        }
        return true;
    }

    public boolean keyPressed(KeyEvent arg0) {
        int var2 = arg0.key();
        if (this.listenBind != null) {
            if (var2 != 256) {
                if (var2 != 261) break label_1;
            }
            break label_2;
            label_1:
            label_2:
            0.setValue(var2);
            this.listenBind = null;
            return true;
        }
        if (this.editNum == null) break label_11;
        if (var2 == 256) {
            this.editNum = null;
            this.numBuf = "";
            break label_10;
        }
        if (var2 != 257) {
            if (var2 != 335) break label_9;
            try {
            }
            applyNum(this.editNum, Double.parseDouble(this.numBuf.trim()));
        } catch (NumberFormatException ex7) {
            Object var3 = ex7;
        }
        this.editNum = null;
        this.numBuf = "";
        break label_10;
        label_9:
        if ((var2 != 259) || (this.numBuf.isEmpty())) break label_10;
        this.numBuf = this.numBuf.substring(0, (this.numBuf.length() - 1));
        label_10:
        return true;
        label_11:
        if (!(!this.searchFocus)) {
            if (var2 == 256) {
                this.query = "";
                this.searchFocus = false;
                break label_15;
            }
            if (var2 != 257) {
                if (var2 == 335) {
                }
                this.searchFocus = false;
                break label_15;
            }
            if ((var2 != 259) || (this.query.isEmpty())) break label_15;
            this.query = this.query.substring(0, (this.query.length() - 1));
            this.scroll = 0.0f;
            label_15:
            return true;
        }
        if (this.editStr != null) {
            if (this.editStr.getValue() == null) {
            } else {
            }
            var3 = this.editStr.getValue();
            if (var2 != 256 && var2 != 257) {
                if (var2 == 335) {
                }
                this.editStr = null;
                break label_21;
            }
            if ((var2 != 259) || (var3.isEmpty())) break label_21;
            this.editStr.setValue(var3.substring(0, (var3.length() - 1)));
            label_21:
            return true;
        }
        return super.keyPressed(arg0);
    }

    public boolean charTyped(CharacterEvent arg0) {
        int var2 = ((char) arg0.codepoint());
        if (this.editNum != null) {
            if (var2 == 44) {
                var2 = 46;
            }
            if (!(Character.isDigit(var2)) && var2 != 46) {
                if (var2 != 45) break label_2;
            }
            if (this.numBuf.length() >= 12) break label_2;
            this.numBuf = (this.numBuf + var2);
            label_2:
            return true;
        }
        if (!(!this.searchFocus)) {
            if (var2 >= 32 && var2 != 127 && this.query.length() < 24) {
                this.query = (this.query + var2);
                this.scroll = 0.0f;
            }
            return true;
        }
        if (this.editStr != null) {
            if (var2 >= 32 && var2 != 127) {
                if (this.editStr.getValue() == null) {
                } else {
                }
                "".setValue((this.editStr.getValue() + var2));
            }
            return true;
        }
        return super.charTyped(arg0);
    }

    public void removed() {
        resetInput();
        this.dragging = null;
        super.removed();
    }

    static {
        var tmp0 = new Module.Category[5];
        tmp0[0] = Module.Category.COMBAT;
        tmp0[1] = Module.Category.MOVEMENT;
        tmp0[2] = Module.Category.VISUALS;
        tmp0[3] = Module.Category.PLAYER;
        tmp0[4] = Module.Category.MISCELLANEOUS;
        CATS = tmp0;
        WINDOW = new Color(18, 18, 18, 245);
        SIDE = new Color(14, 14, 14, 245);
        CARD = new Color(18, 26, 46);
        CARD_SEL = new Color(20, 32, 58);
        LINE = new Color(31, 42, 68);
        LINE_HI = new Color(52, 67, 107);
        CYAN = new Color(92, 225, 255);
        TEXT = new Color(230, 236, 255);
        DIM = new Color(123, 136, 168);
        OFF = new Color(38, 48, 77);
        DARK = new Color(13, 18, 32);
        VEIL = new Color(5, 5, 5, 150);
        var tmp1 = new Color[9];
        tmp1[0] = new Color(255, 75, 75);
        tmp1[1] = new Color(255, 150, 60);
        tmp1[2] = new Color(255, 220, 70);
        tmp1[3] = new Color(80, 220, 120);
        tmp1[4] = new Color(92, 225, 255);
        tmp1[5] = new Color(80, 130, 255);
        tmp1[6] = new Color(170, 100, 255);
        tmp1[7] = new Color(255, 110, 200);
        tmp1[8] = new Color(240, 240, 255);
        PALETTE = tmp1;
    }


    record Row(Setting s, int y, int h) {

        private Row(Setting arg0, int arg1, int arg2) {
            this.s = arg0;
            this.y = arg1;
            this.h = arg2;
        }

        public Setting s() {
            return this.s;
        }

        public int y() {
            return this.y;
        }

        public int h() {
            return this.h;
        }

    }
}
