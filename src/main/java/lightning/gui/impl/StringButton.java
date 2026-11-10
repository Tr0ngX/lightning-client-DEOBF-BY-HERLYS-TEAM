package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.StringSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class StringButton extends Button {
    private final StringSetting setting;
    private String currentString;
    private boolean listening;
    private boolean selecting;
    private int cursorIndex;

    public StringButton(StringSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.currentString = "";
        this.listening = false;
        this.selecting = false;
        this.cursorIndex = 0;
        this.setting = arg0;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getHeight()) - 1)), new Color(0, 0, 0, 40));
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getHeight()) - 1)), ClickGuiScreen.getButtonColor(getY(), 100));
        Object var5 = this.currentString;
        if (!(!this.listening)) {
            if (!(!this.selecting)) {
                var5 = this.currentString;
                break label_4;
            }
            int var6 = Math.max(0, Math.min(this.cursorIndex, this.currentString.length()));
            Object var7 = this.currentString.substring(0, var6);
            Object var8 = this.currentString.substring(var6);
            Object var9 = (LightningClient.CLICK_GUI.isShowLine() ? "|" : " ");
            var5 = (var7 + var9 + var8);
            break label_4;
        }
        var5 = (this.setting.getTag() + " " + String.valueOf(ChatFormatting.GRAY) + this.setting.getValue());
        label_4:
        if (!(!this.selecting)) {
        } else {
        }
        arg0.drawTextWithShadow(((String) var5), ((getX() + getTextPadding()) + 1), (getY() + 2), ClickGuiScreen.getButtonColor(getY(), 255), Color.WHITE);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (arg2 != 0) break label_1;
        if (isHovering(arg0, arg1) && !this.listening) {
            this.listening = true;
            this.currentString = this.setting.getValue();
            this.cursorIndex = this.currentString.length();
            this.selecting = false;
            playClickSound();
            break label_1;
        }
        if (isHovering(arg0, arg1)) break label_1;
        this.listening = false;
        this.selecting = false;
        label_1:
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.listening) return;
        Object var4 = mc.getWindow();
        int var5 = InputConstants.isKeyDown(var4, (Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (arg0 == 256) {
            this.selecting = false;
            return;
        }
        if (arg0 == 257) {
            this.setting.setValue(this.currentString);
            this.selecting = false;
            this.listening = false;
            return;
        }
        if (arg0 == 263 && !(this.currentString.isEmpty())) {
            if (!(!this.selecting)) {
                this.selecting = false;
                this.cursorIndex = 0;
                break label_6;
            }
            if (this.cursorIndex <= 0) break label_6;
            this.cursorIndex = (this.cursorIndex - 1);
            label_6:
            return;
        }
        if (arg0 == 262 && !(this.currentString.isEmpty())) {
            if (!(!this.selecting)) {
                this.selecting = false;
                this.cursorIndex = this.currentString.length();
                break label_9;
            }
            if (this.cursorIndex >= this.currentString.length()) break label_9;
            this.cursorIndex = (this.cursorIndex + 1);
            label_9:
            return;
        }
        if (arg0 == 259) {
            if (!(!this.selecting)) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
                break label_12;
            }
            if (this.cursorIndex <= 0) break label_12;
            this.currentString = (this.currentString.substring(0, (this.cursorIndex - 1)) + this.currentString.substring(this.cursorIndex));
            this.cursorIndex = (this.cursorIndex - 1);
            label_12:
            return;
        }
        if (arg0 == 261) {
            if (!(!this.selecting)) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
                break label_15;
            }
            if (this.cursorIndex >= this.currentString.length()) break label_15;
            this.currentString = (this.currentString.substring(0, this.cursorIndex) + this.currentString.substring((this.cursorIndex + 1)));
            label_15:
            return;
        }
        if (!var5) break label_28;
        if (arg0 != 86) break label_22;
        try {
            Object var6 = mc.keyboardHandler.getClipboard();
            if (!(!this.selecting)) {
                this.currentString = var6;
                this.cursorIndex = this.currentString.length();
                this.selecting = false;
            } else {
                this.currentString = (this.currentString.substring(0, this.cursorIndex) + var6 + this.currentString.substring(this.cursorIndex));
                this.cursorIndex = (this.cursorIndex + var6.length());
            }
        } catch (Exception ex20) {
            var6 = ex20;
            LightningClient.LOGGER.error("{}: Failed to process clipboard paste", var6.getClass().getName(), var6);
        }
        return;
        label_22:
        if ((arg0 != 67) || (!this.selecting)) break label_27;
        try {
            mc.keyboardHandler.setClipboard(this.currentString);
        } catch (Exception ex25) {
            var6 = ex25;
            LightningClient.LOGGER.error("{}: Failed to process clipboard change", var6.getClass().getName(), var6);
        }
        return;
        label_27:
        if ((arg0 != 65) || (this.currentString.isEmpty())) break label_28;
        this.selecting = true;
        this.cursorIndex = this.currentString.length();
        label_28:
    }

    public void charTyped(char arg0, int arg1) {
        if (!this.listening) return;
        if (Character.isISOControl(arg0)) return;
        if (!(!this.selecting)) {
            this.currentString = String.valueOf(arg0);
            this.cursorIndex = 1;
            this.selecting = false;
        } else {
            this.currentString = (this.currentString.substring(0, this.cursorIndex) + arg0 + this.currentString.substring(this.cursorIndex));
            this.cursorIndex = (this.cursorIndex + 1);
        }
    }

}
