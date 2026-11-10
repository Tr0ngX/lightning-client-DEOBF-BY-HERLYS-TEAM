package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class NumberButton extends Button {
    private final NumberSetting setting;
    private boolean dragging;
    private String currentString;
    private boolean listening;
    private boolean selecting;
    private final Animation slideAnim;
    private final Animation sliderAnim;
    private boolean prevVisible;

    public NumberButton(NumberSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.dragging = false;
        this.currentString = "";
        this.listening = false;
        this.selecting = false;
        this.prevVisible = false;
        this.setting = arg0;
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.sliderAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean arg0) {
        this.prevVisible = arg0;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
        this.sliderAnim.setCurrent(0.0f);
        this.sliderAnim.setPrev(0.0f);
        this.sliderAnim.setActiveDuration(220);
        this.sliderAnim.setStartTime(System.currentTimeMillis());
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!this.prevVisible) {
            resetSlide();
            this.prevVisible = true;
        }
        double var9 = ((double) ((getWidth() - 2) - (getPadding() * 2)));
        double var11 = Math.min(var9, ((double) Math.max(0, (((arg1 - getX()) - 1) - getPadding()))));
        if (this.setting.getType() == NumberSetting.Type.INTEGER) {
            double var7 = ((var9 * ((double) (this.setting.getValue().intValue() - this.setting.getMinimum().intValue()))) / ((double) (this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue())));
            if (!this.dragging) break label_10;
            int var13 = ((int) MathUtils.round((((var11 / var9) * ((double) (this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue()))) + ((double) this.setting.getMinimum().intValue())), 0));
            int var14 = this.setting.getStep().intValue();
            if (var14 <= 0) break label_1;
            break label_2;
            label_1:
            label_2:
            (Math.round((((float) var13) / ((float) var14))) * var14).setValue(var13);
            break label_10;
        }
        if (this.setting.getType() == NumberSetting.Type.FLOAT) {
            var7 = ((var9 * ((double) (this.setting.getValue().floatValue() - this.setting.getMinimum().floatValue()))) / ((double) (this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue())));
            if (!this.dragging) break label_10;
            var13 = ((float) MathUtils.round((((var11 / var9) * ((double) (this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue()))) + ((double) this.setting.getMinimum().floatValue())), 2));
            var14 = this.setting.getStep().floatValue();
            if (var14 > 0.0f) {
            } else {
            }
            float var15 = var13;
            this.setting.setValue(var15);
            break label_10;
        }
        if (this.setting.getType() == NumberSetting.Type.DOUBLE) {
            var7 = ((var9 * (this.setting.getValue().doubleValue() - this.setting.getMinimum().doubleValue())) / (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue()));
            if (!this.dragging) break label_10;
            var13 = MathUtils.round((((var11 / var9) * (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue())) + this.setting.getMinimum().doubleValue()), 2);
            var15 = this.setting.getStep().doubleValue();
            if (var15 > 0.0) {
            } else {
            }
            double var17 = var13;
            this.setting.setValue(var17);
            break label_10;
        }
        var7 = ((var9 * ((double) (this.setting.getValue().longValue() - this.setting.getMinimum().longValue()))) / ((double) (this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue())));
        if (!this.dragging) break label_10;
        var13 = ((long) MathUtils.round((((var11 / var9) * ((double) (this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue()))) + ((double) this.setting.getMinimum().longValue())), 0));
        this.setting.setValue(var13);
        label_10:
        var13 = this.slideAnim.get(1.0f);
        var14 = Math.round(((1.0f - var13) * -20.0f));
        var15 = (((getX() + getPadding()) + 1) + var14);
        int var16 = ((((getX() + getWidth()) - getPadding()) - 1) + var14);
        LightningSkin.fillQuad(arg0, ((float) var15), ((float) getY()), ((float) var16), ((float) ((getY() + getHeight()) - 1)), new Color(0, 0, 0, 45));
        if (isHovering(((double) arg1), ((double) arg2))) {
            LightningSkin.fillQuad(arg0, ((float) var15), ((float) getY()), ((float) var16), ((float) ((getY() + getHeight()) - 1)), new Color(255, 255, 255, 12));
        }
        var17 = ((float) Math.max(0.0, Math.min(var9, var7)));
        if (!(!this.dragging)) {
            this.sliderAnim.setCurrent(var17);
            this.sliderAnim.setPrev(var17);
            float var6 = var17;
        } else {
            var6 = this.sliderAnim.get(var17);
        }
        if (var6 > 0.5f) {
            LightningSkin.fillQuad(arg0, ((float) var15), ((float) getY()), (((float) var15) + var6), ((float) ((getY() + getHeight()) - 1)), ClickGuiScreen.getButtonColor(getY(), 95));
            LightningSkin.fillQuad(arg0, ((((float) var15) + var6) - 1.0f), ((float) getY()), ((((float) var15) + var6) + 1.0f), ((float) ((getY() + getHeight()) - 1)), ClickGuiScreen.getButtonColor(getY(), 230));
        }
        var tmp0 = (((System.currentTimeMillis() / 500L) % 2L) == 0L ? 1 : 0);
        int var5 = tmp0;
        int var18 = tmp0;
        if (!(!this.selecting)) {
            break label_19;
        }
        Object var19 = (!(!var5) ? "|" : "");
        if (!(!this.listening)) {
        } else {
        }
        arg0.drawTextWithShadow((this.currentString + var19), this.setting.getTag(), (((var15 + getTextPadding()) - getPadding()) + 2), (getY() + 2), Color.WHITE);
        if (!this.listening) {
            if (this.setting.isZeroIsIgnore() && this.setting.getValue().doubleValue() == 0.0) {
            } else {
            }
            Object var20 = String.valueOf(this.setting.getValue());
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (String.valueOf(ChatFormatting.GRAY) + var20), (((var16 - getTextPadding()) + getPadding()) - LightningClient.FONT_MANAGER.getWidth(var20)), (getY() + 2), Color.WHITE);
        }
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 0) {
            this.dragging = true;
        }
        if (arg2 != 1) break label_2;
        if (isHovering(arg0, arg1) && !this.listening) {
            this.listening = true;
            this.currentString = "";
            break label_2;
        }
        this.listening = false;
        this.selecting = false;
        label_2:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        if (arg2 == 0) {
            if (!(!this.dragging)) {
                playClickSound();
            }
            this.dragging = false;
        }
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.listening) return;
        if (!(InputConstants.isKeyDown(mc.getWindow(), 256))) break label_1;
        this.selecting = false;
        return;
        label_1:
        if (!(InputConstants.isKeyDown(mc.getWindow(), 257))) break label_10;
        try {
            switch (NumberButton.1.$SwitchMap$night$settings$impl$NumberSetting$Type[this.setting.getType().ordinal()]) {
                case 1 -> { break label_3; }
                case 2 -> { break label_4; }
                case 3 -> { break label_5; }
                default -> { break label_6; }
            }
            label_3:
            this.setting.setValue(Long.parseLong(this.currentString));
            break label_7;
            label_4:
            this.setting.setValue(Double.parseDouble(this.currentString));
            break label_7;
            label_5:
            this.setting.setValue(Float.parseFloat(this.currentString));
            break label_7;
            label_6:
            this.setting.setValue(Integer.parseInt(this.currentString));
            label_7:
        } catch (NumberFormatException ex8) {
            Object var4 = ex8;
            LightningClient.CHAT_MANAGER.warn(("Please input a valid " + String.valueOf(ChatUtils.getPrimary()) + this.setting.getType().name().toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " number."));
        }
        this.selecting = false;
        this.listening = false;
        return;
        label_10:
        if (InputConstants.isKeyDown(mc.getWindow(), 259)) {
            if (!(!this.selecting)) {
                break label_13;
            }
            if (!(this.currentString.isEmpty())) {
                break label_13;
            }
            label_13:
            this.currentString.substring(0, (this.currentString.length() - 1)).currentString = this.currentString;
            this.selecting = false;
            return;
        }
        if (InputConstants.isKeyDown(mc.getWindow(), (Util.getPlatform() == Util.OS.OSX ? 343 : 341)) && InputConstants.isKeyDown(mc.getWindow(), 65)) {
            this.selecting = true;
        }
    }

    public void charTyped(char arg0, int arg1) {
        if (!this.listening) break label_2;
        if (!(!this.selecting)) {
        } else {
        }
        String.valueOf(arg0).currentString = (this.currentString + arg0);
        Object var3 = (this.currentString + arg0);
        if (!this.selecting) break label_2;
        this.selecting = false;
        label_2:
    }


    class 1 {

        static {
            $SwitchMap$night$settings$impl$NumberSetting$Type = new int[NumberSetting.Type.values().length];
            try {
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.LONG.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }
}
