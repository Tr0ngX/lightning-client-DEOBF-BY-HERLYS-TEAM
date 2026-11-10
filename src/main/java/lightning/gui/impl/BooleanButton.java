package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BooleanButton extends Button {
    private final BooleanSetting setting;
    private final Animation fillAnim;
    private final Animation hoverAnim;
    private final Animation textAnim;
    private final Animation slideAnim;
    private boolean prevVisible;

    public BooleanButton(BooleanSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.prevVisible = false;
        this.setting = arg0;
        float var4 = (arg0.getValue() ? 1.0f : 0.0f);
        this.fillAnim = new Animation(var4, var4, 180, Easing.Method.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(var4, var4, 150, Easing.Method.EASE_OUT_CUBIC);
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean arg0) {
        this.prevVisible = arg0;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!this.prevVisible) {
            resetSlide();
            this.prevVisible = true;
        }
        var tmp0 = isHovering(((double) arg1), ((double) arg2));
        int var5 = tmp0;
        float var6 = this.hoverAnim.get((!(!tmp0) ? 1.0f : 0.0f));
        float var7 = this.fillAnim.get((this.setting.getValue() ? 1.0f : 0.0f));
        if (!(this.setting.getValue())) {
            if (!(!var5)) {
            }
        } else {
        }
        float var8 = 1.0f.get(0.0f);
        float var9 = this.slideAnim.get(1.0f);
        int var10 = Math.round(((1.0f - var9) * -20.0f));
        int var11 = (((getX() + getPadding()) + 1) + var10);
        int var12 = ((((getX() + getWidth()) - getPadding()) - 1) + var10);
        LightningSkin.fillQuad(arg0, ((float) var11), ((float) getY()), ((float) var12), ((float) ((getY() + getHeight()) - 1)), new Color(0, 0, 0, 40));
        if (var6 > 0.001f) {
            Object var13 = new Color(255, 255, 255, ((int) (20.0f * var6)));
            LightningSkin.fillQuad(arg0, ((float) var11), ((float) getY()), ((float) var12), ((float) ((getY() + getHeight()) - 1)), var13);
        }
        if (var7 > 0.001f) {
            var13 = (var11 + Math.round((((float) (var12 - var11)) * var7)));
            Object var14 = ClickGuiScreen.getButtonColor(getY(), Math.round((100.0f * var7)));
            LightningSkin.fillQuad(arg0, ((float) var11), ((float) getY()), ((float) var13), ((float) ((getY() + getHeight()) - 1)), var14);
        }
        var13 = ((((float) ((getX() + getTextPadding()) + 3)) + (var8 * 1.5f)) + ((float) var10));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (String.valueOf((this.setting.getValue() ? "" : ChatFormatting.GRAY)) + this.setting.getTag()), ((int) var13), (getY() + 2), Color.WHITE);
        var14 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        if (var14 != null) {
            int var15 = 14;
            int var16 = 7;
            int var17 = ((var12 - var15) - 1);
            int var18 = (getY() + ((getHeight() - var16) / 2));
            if (this.setting.getValue()) {
            } else {
            }
            Object var19 = new Color(41, 41, 41, 160);
            LightningSkin.fillQuad(arg0, ((float) var17), ((float) var18), ((float) (var17 + var15)), ((float) (var18 + var16)), var19);
            LightningSkin.drawOutline(arg0, ((float) var17), ((float) var18), ((float) (var17 + var15)), ((float) (var18 + var16)), new Color(0, 0, 0, 80));
            int var20 = 5;
            int var21 = (var17 + Math.round((var7 * ((float) (var15 - var20)))));
            if (this.setting.getValue()) {
            } else {
            }
            Object var22 = new Color(180, 180, 190);
            LightningSkin.fillQuad(arg0, ((float) var21), ((float) (var18 - 1)), ((float) (var21 + var20)), ((float) ((var18 + var16) + 1)), var22);
        }
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 0) {
            this.setting.setValue(!(this.setting.getValue()));
            playClickSound();
        }
    }

}
