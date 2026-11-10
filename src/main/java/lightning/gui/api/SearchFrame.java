package lightning.gui.api;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class SearchFrame {
    private String query;
    private boolean visible;
    private boolean focused;
    private int cursorIndex;
    private final Timer cursorTimer;
    private boolean showCursor;
    private int width;
    private int height;

    public SearchFrame() {
        this.query = "";
        this.visible = false;
        this.focused = false;
        this.cursorIndex = 0;
        this.cursorTimer = new Timer();
        this.showCursor = true;
        this.width = 190;
        this.height = 16;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!this.visible) break label_9;
        int var5 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int var6 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int var7 = ((var5 - this.width) / 2);
        int var8 = ((var6 - this.height) - 8);
        if (this.cursorTimer.hasTimeElapsed(400L)) {
            this.showCursor = !this.showCursor;
            this.cursorTimer.reset();
        }
        LightningSkin.fillQuad(arg0, ((float) var7), ((float) var8), ((float) (var7 + this.width)), ((float) (var8 + this.height)), new Color(14, 14, 14, 230));
        if (!(!this.focused)) {
        } else {
        }
        Object var9 = ClickGuiScreen.getButtonColor(var8, 90);
        LightningSkin.drawOutline(arg0, ((float) var7), ((float) var8), ((float) (var7 + this.width)), ((float) (var8 + this.height)), var9);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "\u2315", (var7 + 5), (var8 + 4), Color.WHITE);
        int var10 = 18;
        if (this.query.isEmpty() && !this.focused) {
            Object var11 = "[ESC]";
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var11, (((var7 + this.width) - LightningClient.FONT_MANAGER.getWidth(var11)) - 4), (var8 + 4), new Color(120, 120, 130));
        }
        if (this.query.isEmpty() && !this.focused) {
            var11 = "Search modules...";
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var11, (var7 + var10), (var8 + 4), new Color(140, 140, 150));
            break label_9;
        }
        if (!this.focused) break label_7;
        var11 = (!(!this.showCursor) ? "|" : "");
        Object var12 = this.query.substring(0, this.cursorIndex);
        Object var13 = this.query.substring(this.cursorIndex);
        Object var14 = (var12 + var11 + var13);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var14, (var7 + var10), (var8 + 4), Color.WHITE);
        label_9:
    }

    public boolean mouseClicked(double arg0, double arg1, int arg2) {
        if (!this.visible) return false;
        int var6 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int var7 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int var8 = ((var6 - this.width) / 2);
        int var9 = ((var7 - this.height) - 6);
        if (arg2 == 0 && arg0 >= ((double) var8) && arg0 <= ((double) (var8 + this.width)) && arg1 >= ((double) var9) && arg1 <= ((double) (var9 + this.height))) {
            this.focused = true;
            return true;
        }
        this.focused = false;
        return false;
    }

    public boolean keyPressed(int arg0, int arg1, int arg2) {
        if ((!this.visible) || (!this.focused)) {
            return false;
        }
        Object var4 = Minecraft.getInstance().getWindow();
        int var5 = InputConstants.isKeyDown(var4, (Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (arg0 == 256) {
            if (!(this.query.isEmpty())) {
                this.query = "";
                this.cursorIndex = 0;
                return true;
            }
            this.visible = false;
            this.focused = false;
            return true;
        }
        if (arg0 == 259) {
            if (!(this.query.isEmpty()) && this.cursorIndex > 0) {
                this.query = (this.query.substring(0, (this.cursorIndex - 1)) + this.query.substring(this.cursorIndex));
                this.cursorIndex = (this.cursorIndex - 1);
            }
            return true;
        }
        if (arg0 == 261) {
            if (this.cursorIndex < this.query.length()) {
                this.query = (this.query.substring(0, this.cursorIndex) + this.query.substring((this.cursorIndex + 1)));
            }
            return true;
        }
        if (arg0 == 263) {
            if (this.cursorIndex > 0) {
                this.cursorIndex = (this.cursorIndex - 1);
            }
            return true;
        }
        if (arg0 == 262) {
            if (this.cursorIndex < this.query.length()) {
                this.cursorIndex = (this.cursorIndex + 1);
            }
            return true;
        }
        if (arg0 == 268) {
            this.cursorIndex = 0;
            return true;
        }
        if (arg0 == 269) {
            this.cursorIndex = this.query.length();
            return true;
        }
        if ((!var5) || (arg0 != 86)) break label_20;
        try {
            Object var6 = Minecraft.getInstance().keyboardHandler.getClipboard();
            this.query = (this.query.substring(0, this.cursorIndex) + var6 + this.query.substring(this.cursorIndex));
            this.cursorIndex = (this.cursorIndex + var6.length());
        } catch (Exception ex18) {
            var6 = ex18;
        }
        return true;
        label_20:
        if (!(!var5) && arg0 == 65) {
            this.cursorIndex = this.query.length();
            return true;
        }
        return true;
    }

    public boolean charTyped(char arg0, int arg1) {
        if ((!this.visible) || (!this.focused)) {
            return false;
        }
        if (Character.isISOControl(arg0)) return false;
        this.query = (this.query.substring(0, this.cursorIndex) + arg0 + this.query.substring(this.cursorIndex));
        this.cursorIndex = (this.cursorIndex + 1);
        return true;
    }

    public void toggle() {
        this.visible = !this.visible;
        int var1 = (!this.visible ? 1 : 0);
        if (!(!this.visible)) {
            this.focused = true;
            this.query = "";
            this.cursorIndex = 0;
        } else {
            this.focused = false;
        }
    }

    public boolean isSearching() {
        return !(this.query.isEmpty());
    }

    public boolean matches(String arg0) {
        if (this.query.isEmpty()) return true;
        return arg0.toLowerCase().contains(this.query.toLowerCase());
    }

    @Generated
    public String getQuery() {
        return this.query;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public boolean isFocused() {
        return this.focused;
    }

    @Generated
    public int getCursorIndex() {
        return this.cursorIndex;
    }

    @Generated
    public Timer getCursorTimer() {
        return this.cursorTimer;
    }

    @Generated
    public boolean isShowCursor() {
        return this.showCursor;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public void setQuery(String arg0) {
        this.query = arg0;
    }

    @Generated
    public void setVisible(boolean arg0) {
        this.visible = arg0;
    }

    @Generated
    public void setFocused(boolean arg0) {
        this.focused = arg0;
    }

    @Generated
    public void setCursorIndex(int arg0) {
        this.cursorIndex = arg0;
    }

    @Generated
    public void setShowCursor(boolean arg0) {
        this.showCursor = arg0;
    }

    @Generated
    public void setWidth(int arg0) {
        this.width = arg0;
    }

    @Generated
    public void setHeight(int arg0) {
        this.height = arg0;
    }

}
