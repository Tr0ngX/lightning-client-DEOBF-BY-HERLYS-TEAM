package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.ModeSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class ModeButton extends Button {
    private final ModeSetting setting;
    private boolean open;
    private final Animation openAnim;
    private final Animation hoverAnim;
    private final Animation textAnim;
    private static final int VALUE_SLIDE_MS = 180;
    private static final int VALUE_SLIDE_DISTANCE = 10;
    private String lastValue;
    private String previousValue;
    private long valueChangeTime;

    public ModeButton(ModeSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.open = false;
        this.openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.setting = arg0;
        this.lastValue = arg0.getValue();
    }

    private float getOpenAmount() {
        return this.openAnim.get((!(!this.open) ? 1.0f : 0.0f));
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = isHovering(((double) arg1), ((double) arg2));
        float var6 = this.hoverAnim.get((!(!var5) ? 1.0f : 0.0f));
        float var7 = this.textAnim.get((!(!var5) ? 1.0f : 0.0f));
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 1)), new Color(0, 0, 0, 40));
        if (var6 > 0.001f) {
            Object var8 = new Color(255, 255, 255, ((int) (20.0f * var6)));
            LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 1)), var8);
        }
        var8 = (((float) ((getX() + getTextPadding()) + 3)) + (var7 * 1.5f));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.setting.getTag(), ((int) var8), (getY() + 2), Color.WHITE);
        Object var9 = this.setting.getValue();
        if (!(var9.equals(this.lastValue))) {
            this.previousValue = this.lastValue;
            this.lastValue = var9;
            this.valueChangeTime = System.currentTimeMillis();
        }
        if (this.valueChangeTime == 0L) {
        } else {
        }
        float var10 = Easing.ease(Easing.toDelta(this.valueChangeTime, 180), Easing.Method.EASE_OUT_CUBIC);
        int var11 = (((getX() + getWidth()) - getTextPadding()) - 1);
        if (var10 < 1.0f && this.previousValue != null) {
            int var12 = Math.round((var10 * 10.0f));
            int var13 = Math.round((255.0f * (1.0f - var10)));
            Object var14 = (String.valueOf(ChatFormatting.GRAY) + this.previousValue);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var14, ((var11 - LightningClient.FONT_MANAGER.getWidth(var14)) - var12), (getY() + 2), new Color(255, 255, 255, var13));
        }
        var12 = Math.round(((1.0f - var10) * 10.0f));
        var13 = Math.round((255.0f * var10));
        var14 = (String.valueOf(ChatFormatting.GRAY) + var9);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var14, ((var11 - LightningClient.FONT_MANAGER.getWidth(var14)) + var12), (getY() + 2), new Color(255, 255, 255, var13));
        float var15 = getOpenAmount();
        if (var15 <= 0.001f) break label_19;
        int var16 = Mth.clamp(Math.round((((float) this.setting.getModes().size()) * var15)), 0, this.setting.getModes().size());
        int var17 = (var16 * getParent().getHeight());
        int var18 = (getY() + getParent().getHeight());
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 2)), ((float) var18), ((float) (((getX() + getWidth()) - getPadding()) - 2)), ((float) (var18 + var17)), new Color(11, 11, 11, 210));
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 2)), ((float) var18), ((float) ((getX() + getPadding()) + 4)), ((float) (var18 + var17)), ClickGuiScreen.getButtonColor(getY(), 160));
        int var19 = 0;
        while (var19 < var16) {
            Object var21 = ((String) this.setting.getModes().get(var19));
            int var22 = (var18 + (var19 * getParent().getHeight()));
            int var23 = this.setting.getValue().equals(var21);
            if (((arg1 < (getX() + getPadding())) || (arg1 > ((getX() + getWidth()) - getPadding()))) || (arg2 < var22)) break label_10;
            var tmp0 = (arg2 < (var22 + getParent().getHeight()) ? 1 : 0);
            int var20 = tmp0;
            int var24 = tmp0;
            if (!(!var20)) {
                LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 4)), ((float) var22), ((float) (((getX() + getWidth()) - getPadding()) - 2)), ((float) ((var22 + getParent().getHeight()) - 1)), new Color(255, 255, 255, 18));
            }
            if (!(!var23)) {
                LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 4)), ((float) var22), ((float) (((getX() + getWidth()) - getPadding()) - 2)), ((float) ((var22 + getParent().getHeight()) - 1)), ClickGuiScreen.getButtonColor(getY(), 45));
            }
            if (!(!var23)) {
                break label_16;
            }
            if (!(!var20)) {
                break label_16;
            }
            label_16:
            Object var25 = new Color(150, 150, 160);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, ((!(!var23) ? "\u2022 " : "  ") + var21), ((getX() + getTextPadding()) + 5), (var22 + 2), var25);
            var19++;
        }
        label_19:
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isHovering(arg0, arg1))) break label_5;
        if (arg2 == 0) {
            int var6 = this.setting.getModes().indexOf(this.setting.getValue());
            if (var6 < (this.setting.getModes().size() - 1)) {
                this.setting.setValue(((String) this.setting.getModes().get((var6 + 1))));
                break label_1;
            }
            this.setting.setValue(((String) this.setting.getModes().get(0)));
            label_1:
            playClickSound();
            break label_5;
        }
        if (arg2 != 1) break label_5;
        this.open = !this.open;
        playClickSound();
        label_5:
        if (!this.open) break label_11;
        var6 = getOpenAmount();
        int var7 = Mth.clamp(Math.round((((float) this.setting.getModes().size()) * var6)), 0, this.setting.getModes().size());
        int var8 = 0;
        while (var8 < var7) {
            Object var10 = ((String) this.setting.getModes().get(var8));
            int var11 = ((getY() + getParent().getHeight()) + (var8 * getParent().getHeight()));
            if (((arg0 < ((double) (getX() + getPadding()))) || (arg0 > ((double) ((getX() + getWidth()) - getPadding())))) || (arg1 < ((double) var11))) break label_7;
            var tmp0 = (arg1 < ((double) (var11 + getParent().getHeight())) ? 1 : 0);
            int var9 = tmp0;
            int var12 = tmp0;
            if (!var9) break label_10;
            if (arg2 != 0) {
                break label_10;
            }
            this.setting.setValue(var10);
            playClickSound();
            label_10:
            var8++;
        }
        label_11:
    }

    public int getHeight() {
        return Math.round((((float) getParent().getHeight()) + (((float) (this.setting.getModes().size() * getParent().getHeight())) * getOpenAmount())));
    }

}
