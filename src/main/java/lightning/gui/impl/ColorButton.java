package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.impl.ColorSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ColorButton extends Button {
    private final ColorSetting setting;
    private boolean open;
    private boolean hoveringHex;
    private boolean hoveringHue;
    private boolean hoveringColor;
    private boolean hoveringAlpha;
    private boolean hoveringCopy;
    private boolean hoveringPaste;
    private boolean hoveringSync;
    private boolean hoveringRainbow;
    private boolean draggingHue;
    private boolean draggingColor;
    private boolean draggingAlpha;
    private boolean listeningHex;
    private String hexInput;
    private int hexCursor;
    private int colorWidth;
    private int totalHeight;
    private float[] hsb;

    public ColorButton(ColorSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.open = false;
        this.hoveringHex = false;
        this.hoveringHue = false;
        this.hoveringColor = false;
        this.hoveringAlpha = false;
        this.hoveringCopy = false;
        this.hoveringPaste = false;
        this.hoveringSync = false;
        this.hoveringRainbow = false;
        this.draggingHue = false;
        this.draggingColor = false;
        this.draggingAlpha = false;
        this.listeningHex = false;
        this.hexInput = "";
        this.hexCursor = 0;
        this.colorWidth = 84;
        this.totalHeight = 0;
        this.setting = arg0;
        this.hsb = Color.RGBtoHSB(arg0.getColor().getRed(), arg0.getColor().getGreen(), arg0.getColor().getBlue(), null);
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!this.draggingHue && !this.draggingColor && !this.listeningHex) {
            Object var5 = this.setting.getValue().getColor();
            this.hsb = Color.RGBtoHSB(var5.getRed(), var5.getGreen(), var5.getBlue(), null);
        }
        var5 = Color.BLACK;
        Object var6 = Color.getHSBColor(this.hsb[0], 1.0f, 1.0f);
        Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 1)), new Color(0, 0, 0, 40));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.setting.getTag(), ((getX() + getTextPadding()) + 3), (getY() + 2), Color.WHITE);
        Renderer2D.renderQuad(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 9)), ((float) (getY() + 2)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 3)), ColorUtils.getColor(this.setting.getColor(), 255));
        Renderer2D.renderOutline(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 9)), ((float) (getY() + 2)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 3)), var5);
        if (!(!this.open)) {
            int var9 = getParent().getHeight();
            int var10 = 13;
            int var11 = ((getX() + getPadding()) + 1);
            int var12 = (((getX() + getWidth()) - getPadding()) - 1);
            int var13 = (getY() + var9);
            int var14 = (var13 + var10);
            Renderer2D.renderCheckerboard(arg0, ((float) var11), ((float) var13), ((float) var12), ((float) var14), 4.0f, new Color(210, 210, 210), Color.WHITE);
            Renderer2D.renderOutline(arg0, ((float) var11), ((float) var13), ((float) var12), ((float) var14), var5);
            var tmp0 = (LightningClient.CLICK_GUI.isShowLine() ? "|" : "");
            Object var8 = tmp0;
            Object var15 = tmp0;
            if (!(!this.listeningHex)) {
                if (this.hexInput.isEmpty()) {
                    Object var16 = toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                    LightningClient.FONT_MANAGER.drawText(arg0, ("Hex: " + var8), (var11 + 3), (var13 + 2), Color.BLACK);
                    int var17 = LightningClient.FONT_MANAGER.getWidth("Hex: ");
                    LightningClient.FONT_MANAGER.drawText(arg0, var16, ((var11 + 3) + var17), (var13 + 2), new Color(130, 130, 130));
                    break label_5;
                }
                var16 = Math.max(0, Math.min(this.hexCursor, this.hexInput.length()));
                var17 = this.hexInput.substring(0, var16);
                Object var18 = this.hexInput.substring(var16);
                Object var19 = ("Hex: " + var17 + var8 + var18);
                LightningClient.FONT_MANAGER.drawText(arg0, var19, (var11 + 3), (var13 + 2), Color.BLACK);
                break label_5;
            }
            var16 = ("Hex: " + toHex(this.setting.getValue().getColor(), this.setting.getAlpha()));
            LightningClient.FONT_MANAGER.drawText(arg0, var16, (var11 + 3), (var13 + 2), Color.BLACK);
            label_5:
            this.hoveringHex = isHoveringComponent(((double) arg1), ((double) arg2), ((double) var11), ((double) var13), ((double) var12), ((double) var14));
            var16 = Mth.clamp((((arg1 - getX()) - getPadding()) - 1), 0, this.colorWidth);
            var tmp1 = (var9 + (var10 + 2));
            var9 = tmp1;
            var17 = Mth.clamp(((arg2 - getY()) - tmp1), 0, this.colorWidth);
            var18 = (((float) this.colorWidth) * this.hsb[0]);
            var19 = (((float) this.colorWidth) * this.hsb[1]);
            float var20 = (((float) this.colorWidth) * (1.0f - this.hsb[2]));
            float var21 = (((float) this.colorWidth) * (((float) this.setting.getAlpha()) / 255.0f));
            float var22 = 0.0f;
            while (var22 < ((float) this.colorWidth)) {
                Renderer2D.renderQuad(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 9)), (((float) (getY() + var9)) + var22), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((((float) (getY() + var9)) + var22) + 0.5f), Color.getHSBColor((var22 / ((float) this.colorWidth)), 1.0f, 1.0f));
                var22 = (var22 + 0.5f);
            }
            Renderer2D.renderOutline(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 9)), ((float) (getY() + var9)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + var9) + this.colorWidth)), var5);
            this.hoveringHue = isHoveringComponent(((double) arg1), ((double) arg2), ((double) (((getX() + getWidth()) - getPadding()) - 9)), ((double) (getY() + var9)), ((double) (((getX() + getWidth()) - getPadding()) - 1)), ((double) ((getY() + var9) + this.colorWidth)));
            Renderer2D.renderQuad(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 10)), ((((float) (getY() + var9)) + var18) - 1.5f), ((float) ((getX() + getWidth()) - getPadding())), ((((float) (getY() + var9)) + var18) + 1.5f), var5);
            Renderer2D.renderQuad(arg0, ((float) (((getX() + getWidth()) - getPadding()) - 9)), ((((float) (getY() + var9)) + var18) - 0.5f), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((((float) (getY() + var9)) + var18) + 0.5f), Color.WHITE);
            if (!(!this.draggingHue)) {
                this.setting.setSync(false);
                this.hsb[0] = Math.clamp((((float) var17) / ((float) this.colorWidth)), 0.0f, 1.0f);
                var22 = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                Object var7 = new Color(var22.getRed(), var22.getGreen(), var22.getBlue(), this.setting.getAlpha());
                this.setting.setColor(var7);
            }
            Renderer2D.renderSidewaysGradient(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getPadding()) + 1) + this.colorWidth)), ((float) ((getY() + var9) + this.colorWidth)), Color.WHITE, var6);
            Renderer2D.renderGradient(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getPadding()) + 1) + this.colorWidth)), ((float) ((getY() + var9) + this.colorWidth)), new Color(0, 0, 0, 0), Color.BLACK);
            Renderer2D.renderOutline(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getPadding()) + 1) + this.colorWidth)), ((float) ((getY() + var9) + this.colorWidth)), var5);
            this.hoveringColor = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((getX() + getPadding()) + 1)), ((double) (getY() + var9)), ((double) (((getX() + getPadding()) + 1) + this.colorWidth)), ((double) ((getY() + var9) + this.colorWidth)));
            Renderer2D.renderQuad(arg0, ((((float) ((getX() + getPadding()) + 1)) + var19) - 1.5f), ((((float) (getY() + var9)) + var20) - 1.5f), ((((float) ((getX() + getPadding()) + 1)) + var19) + 1.5f), ((((float) (getY() + var9)) + var20) + 1.5f), var5);
            Renderer2D.renderQuad(arg0, ((((float) ((getX() + getPadding()) + 1)) + var19) - 0.5f), ((((float) (getY() + var9)) + var20) - 0.5f), ((((float) ((getX() + getPadding()) + 1)) + var19) + 0.5f), ((((float) (getY() + var9)) + var20) + 0.5f), Color.WHITE);
            if (!(!this.draggingColor)) {
                this.setting.setSync(false);
                this.hsb[1] = Math.clamp((((float) var16) / ((float) this.colorWidth)), 0.0f, 1.0f);
                this.hsb[2] = Math.clamp((1.0f - (((float) var17) / ((float) this.colorWidth))), 0.0f, 1.0f);
                var22 = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                var7 = new Color(var22.getRed(), var22.getGreen(), var22.getBlue(), this.setting.getAlpha());
                this.setting.setColor(var7);
            }
            var tmp2 = (var9 + (this.colorWidth + 2));
            var9 = tmp2;
            Renderer2D.renderSidewaysGradient(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + tmp2)), ((float) (((getX() + getPadding()) + 1) + this.colorWidth)), ((float) ((getY() + var9) + 8)), Color.BLACK, ColorUtils.getColor(this.setting.getColor(), 255));
            Renderer2D.renderOutline(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getPadding()) + 1) + this.colorWidth)), ((float) ((getY() + var9) + 8)), var5);
            this.hoveringAlpha = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((getX() + getPadding()) + 1)), ((double) (getY() + var9)), ((double) (((getX() + getPadding()) + 1) + this.colorWidth)), ((double) ((getY() + var9) + 8)));
            Renderer2D.renderQuad(arg0, ((((float) ((getX() + getPadding()) + 1)) + var21) - 1.5f), ((float) ((getY() + var9) - 1)), ((((float) ((getX() + getPadding()) + 1)) + var21) + 1.5f), ((float) ((getY() + var9) + 9)), var5);
            Renderer2D.renderQuad(arg0, ((((float) ((getX() + getPadding()) + 1)) + var21) - 0.5f), ((float) (getY() + var9)), ((((float) ((getX() + getPadding()) + 1)) + var21) + 0.5f), ((float) ((getY() + var9) + 8)), Color.WHITE);
            if (!(!this.draggingAlpha)) {
                var22 = Math.clamp(((long) ((int) ((255.0f * ((float) var16)) / ((float) this.colorWidth)))), 0, 255);
                Object var23 = this.setting.getValue().getColor();
                Object var24 = new Color(var23.getRed(), var23.getGreen(), var23.getBlue(), var22);
                this.setting.setColor(var24);
            }
            var9 += 10;
            Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((((float) getX()) + (((float) getWidth()) / 2.0f)) - 0.5f), ((float) ((getY() + var9) + getParent().getHeight())), ClickGuiScreen.getButtonColor(getY(), 100));
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Copy", ((((getX() + getPadding()) + (getWidth() / 4)) - 1) - (LightningClient.FONT_MANAGER.getWidth("Copy") / 2)), ((getY() + var9) + 2), Color.WHITE);
            this.hoveringCopy = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((getX() + getPadding()) + 1)), ((double) (getY() + var9)), ((double) ((((float) getX()) + (((float) getWidth()) / 2.0f)) - 0.5f)), ((double) ((getY() + var9) + getParent().getHeight())));
            Renderer2D.renderQuad(arg0, ((((float) getX()) + (((float) getWidth()) / 2.0f)) + 0.5f), ((float) (getY() + var9)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + var9) + getParent().getHeight())), ClickGuiScreen.getButtonColor(getY(), 100));
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Paste", ((((getX() + (getWidth() / 2)) + (getWidth() / 4)) - 1) - (LightningClient.FONT_MANAGER.getWidth("Paste") / 2)), ((getY() + var9) + 2), Color.WHITE);
            this.hoveringPaste = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((((float) getX()) + (((float) getWidth()) / 2.0f)) + 0.5f)), ((double) (getY() + var9)), ((double) (((getX() + getWidth()) - getPadding()) - 1)), ((double) ((getY() + var9) + getParent().getHeight())));
            var9 = (var9 + (getParent().getHeight() + 1));
            if (hasSync()) {
                if (this.setting.isSync()) {
                    Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + var9) + getParent().getHeight())), ClickGuiScreen.getButtonColor(getY(), 100));
                }
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Sync", ((getX() + (getWidth() / 2)) - (LightningClient.FONT_MANAGER.getWidth("Sync") / 2)), ((getY() + var9) + 2), Color.WHITE);
                this.hoveringSync = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((getX() + getPadding()) + 1)), ((double) (getY() + var9)), ((double) (((getX() + getWidth()) - getPadding()) - 1)), ((double) ((getY() + var9) + getParent().getHeight())));
                var9 = (var9 + (getParent().getHeight() + 1));
            } else {
                this.hoveringSync = false;
            }
            if (this.setting.isRainbow()) {
                Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) (getY() + var9)), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + var9) + getParent().getHeight())), ClickGuiScreen.getButtonColor(getY(), 100));
            }
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Rainbow", ((getX() + (getWidth() / 2)) - (LightningClient.FONT_MANAGER.getWidth("Rainbow") / 2)), ((getY() + var9) + 2), Color.WHITE);
            this.hoveringRainbow = isHoveringComponent(((double) arg1), ((double) arg2), ((double) ((getX() + getPadding()) + 1)), ((double) (getY() + var9)), ((double) (((getX() + getWidth()) - getPadding()) - 1)), ((double) ((getY() + var9) + getParent().getHeight())));
        }
    }

    private String toHex(Color arg0, int arg1) {
        var tmp0 = new Object[4];
        tmp0[0] = arg1;
        tmp0[1] = arg0.getRed();
        tmp0[2] = arg0.getGreen();
        tmp0[3] = arg0.getBlue();
        return String.format("%02X%02X%02X%02X", tmp0);
    }

    private Color parseHex(String arg0) {
        if (arg0 == null) return null;
        Object var2 = arg0.trim().replace("#", "");
        try {
            if (var2.length() == 6) {
                int var3 = Integer.parseInt(var2.substring(0, 2), 16);
                int var4 = Integer.parseInt(var2.substring(2, 4), 16);
                int var5 = Integer.parseInt(var2.substring(4, 6), 16);
                return new Color(var3, var4, var5, this.setting.getAlpha());
                try {
                }
                if (var2.length() == 8) {
                    var3 = Integer.parseInt(var2.substring(0, 2), 16);
                    var4 = Integer.parseInt(var2.substring(2, 4), 16);
                    var5 = Integer.parseInt(var2.substring(4, 6), 16);
                    int var6 = Integer.parseInt(var2.substring(6, 8), 16);
                    return new Color(var4, var5, var6, var3);
                }
            } catch (Exception ex6) {
                var3 = ex6;
            }
            return null;
        }
    }

    private void applyHexLive() {
        if ((this.hexInput == null) || (this.hexInput.isEmpty())) {
            return;
        }
        Object var1 = parseHex(this.hexInput);
        if (var1 != null) {
            this.setting.setSync(false);
            this.setting.setColor(var1);
            this.hsb = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
        }
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.listeningHex) return;
        Object var4 = mc.getWindow();
        int var5 = InputConstants.isKeyDown(var4, (Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (arg0 != 256) {
            if (arg0 == 257) {
            }
            applyHexLive();
            this.listeningHex = false;
            this.hexInput = "";
            return;
        }
        if (arg0 == 263) {
            if (this.hexCursor > 0) {
                this.hexCursor = (this.hexCursor - 1);
            }
            return;
        }
        if (arg0 == 262) {
            if (this.hexCursor < this.hexInput.length()) {
                this.hexCursor = (this.hexCursor + 1);
            }
            return;
        }
        if (arg0 == 259) {
            if (this.hexCursor > 0 && !(this.hexInput.isEmpty())) {
                this.hexInput = (this.hexInput.substring(0, (this.hexCursor - 1)) + this.hexInput.substring(this.hexCursor));
                this.hexCursor = (this.hexCursor - 1);
                applyHexLive();
            }
            return;
        }
        if (arg0 == 261) {
            if (this.hexCursor < this.hexInput.length()) {
                this.hexInput = (this.hexInput.substring(0, this.hexCursor) + this.hexInput.substring((this.hexCursor + 1)));
                applyHexLive();
            }
            return;
        }
        if (!var5) break label_29;
        if (arg0 != 86) break label_24;
        try {
            Object var6 = mc.keyboardHandler.getClipboard();
            if (var6 != null) {
                var6 = var6.trim().replace("#", "").toUpperCase();
                Object var7 = new StringBuilder();
                Object var8 = var6.toCharArray();
                int var9 = var8.length;
                int var10 = 0;
                while (var10 < var9) {
                    int var11 = var8[var10];
                    if (var11 >= 48) {
                        if (var11 <= 57) break label_16;
                    }
                    if (var11 < 65) break label_17;
                    if (var11 <= 70) break label_16;
                    break label_17;
                    label_16:
                    var7.append(var11);
                    label_17:
                    var10++;
                }
                var8 = var7.toString();
                if ((this.hexInput.length() + var8.length()) <= 8) {
                    this.hexInput = (this.hexInput.substring(0, this.hexCursor) + var8 + this.hexInput.substring(this.hexCursor));
                    this.hexCursor = (this.hexCursor + var8.length());
                } else {
                    this.hexInput = var8.substring(0, Math.min(8, var8.length()));
                    this.hexCursor = this.hexInput.length();
                }
                applyHexLive();
            }
        } catch (Exception ex22) {
            var6 = ex22;
        }
        return;
        label_24:
        if (arg0 != 67) break label_29;
        try {
            mc.keyboardHandler.setClipboard(this.hexInput);
        } catch (Exception ex27) {
            var6 = ex27;
        }
        return;
        label_29:
    }

    public void charTyped(char arg0, int arg1) {
        if (!this.listeningHex) return;
        int var3 = Character.toUpperCase(arg0);
        if (var3 >= 48) {
            if (var3 > 57) {
            }
            if ((var3 < 65) || (var3 > 70)) break label_3;
        }
        if (this.hexInput.length() >= 8) break label_3;
        this.hexInput = (this.hexInput.substring(0, this.hexCursor) + var3 + this.hexInput.substring(this.hexCursor));
        this.hexCursor = (this.hexCursor + 1);
        applyHexLive();
        label_3:
    }

    private boolean hasSync() {
        Object var1 = ((ColorModule) LightningClient.MODULE_MANAGER.getModule(ColorModule.class));
        if (var1 != null) {
            if (this.setting != var1.color) {
            }
        } else {
        }
        return false;
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 1) {
            this.open = !this.open;
            int var6 = (!this.open ? 1 : 0);
            if (!(!this.listeningHex)) {
                applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            playClickSound();
        }
        if (arg2 != 0) break label_16;
        if (!(!this.open) && !(!this.hoveringHex)) {
            this.listeningHex = true;
            this.hexInput = "";
            this.hexCursor = 0;
            playClickSound();
            return;
        }
        if (!(!this.listeningHex)) {
            applyHexLive();
            this.listeningHex = false;
            this.hexInput = "";
        }
        if (!(!this.hoveringHue)) {
            this.draggingHue = true;
        }
        if (!(!this.hoveringColor)) {
            this.draggingColor = true;
        }
        if (!(!this.hoveringAlpha)) {
            this.draggingAlpha = true;
        }
        if (!(!this.hoveringCopy)) {
            LightningClient.CLICK_GUI.setColorClipboard(this.setting.getValue().getColor());
            playClickSound();
        }
        if (!(!this.hoveringPaste) && LightningClient.CLICK_GUI.getColorClipboard() != null) {
            var6 = LightningClient.CLICK_GUI.getColorClipboard();
            this.setting.setColor(var6);
            this.hsb = Color.RGBtoHSB(var6.getRed(), var6.getGreen(), var6.getBlue(), null);
            playClickSound();
        }
        if (hasSync() && !(!this.hoveringSync)) {
            this.setting.setSync(!(this.setting.isSync()));
            playClickSound();
        }
        if (!this.hoveringRainbow) break label_16;
        this.setting.setRainbow(!(this.setting.isRainbow()));
        playClickSound();
        label_16:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        this.draggingHue = false;
        this.draggingColor = false;
        this.draggingAlpha = false;
    }

    public int getHeight() {
        if (!(!this.open)) {
            if (hasSync()) {
                break label_2;
            }
            break label_2;
        }
        label_2:
        return getParent().getHeight();
    }

    public boolean isHovering(double arg0, double arg1) {
        if (((((double) (getX() + getPadding())) > arg0) || (((double) getY()) > arg1)) || (((double) ((getX() + getWidth()) - getPadding())) <= arg0)) break label_0;
        return ((double) (getY() + getParent().getHeight())) > arg1;
    }

    private boolean isHoveringComponent(double arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {
        if (((arg2 > arg0) || (arg3 > arg1)) || (arg4 <= arg0)) break label_0;
        return arg5 > arg1;
    }

}
