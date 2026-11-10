package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.BindSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BindButton extends Button {
    private final BindSetting setting;
    private boolean listening;
    private final Animation hoverAnim;
    private final Animation listenAnim;
    private static int listenersActive;

    public static boolean isAnyListening() {
        return listenersActive > 0;
    }

    public BindButton(BindSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.listening = false;
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.listenAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.setting = arg0;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = isHovering(((double) arg1), ((double) arg2));
        float var6 = this.hoverAnim.get((!(!var5) ? 1.0f : 0.0f));
        float var7 = this.listenAnim.get((!(!this.listening) ? 1.0f : 0.0f));
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getHeight()) - 1)), new Color(0, 0, 0, 40));
        if (var6 <= 0.001f) {
            if (var7 > 0.001f) {
            }
            int var8 = Math.min(255, ((int) ((20.0f * var6) + (50.0f * var7))));
            if (!(!this.listening)) {
            } else {
            }
            Object var9 = new Color(255, 255, 255, var8);
            LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getHeight()) - 1)), var9);
        }
        if (this.setting.getMode().equals("Bind")) {
        } else {
        }
        var8 = this.setting.getMode();
        var9 = (((float) ((getX() + getTextPadding()) + 3)) + (var6 * 1.5f));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var8, ((int) var9), (getY() + 2), Color.WHITE);
        if (!(!this.listening)) {
        } else {
        }
        Object var10 = KeyboardUtils.getKeyName(this.setting.getValue());
        Object var11 = (!(!this.listening) ? Color.YELLOW : Color.WHITE);
        int var12 = (((((getX() + getWidth()) - getTextPadding()) - 1) - LightningClient.FONT_MANAGER.getWidth(var10)) - ((int) (var7 * 2.0f)));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (String.valueOf((!(!this.listening) ? ChatFormatting.YELLOW : ChatFormatting.GRAY)) + var10), var12, (getY() + 2), var11);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isHovering(arg0, arg1))) return;
        if (!(!this.listening)) {
            if (arg2 >= 0 && arg2 <= 4) {
                this.setting.setValue((-(arg2) - 1));
                stopListening();
            }
            return;
        }
        if (arg2 == 0) {
            startListening();
            playClickSound();
            break label_4;
        }
        if (arg2 != 1) break label_4;
        cycleMode();
        playClickSound();
        label_4:
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.listening) return;
        if (arg0 != 261) {
            if (arg0 == 256) {
            }
            this.setting.setValue(0);
            stopListening();
            return;
        }
        this.setting.setValue(arg0);
        stopListening();
    }

    public void keyPressed(int arg0) {
        keyPressed(arg0, 0, 0);
    }

    private void startListening() {
        this.listening = true;
        listenersActive = (listenersActive + 1);
    }

    private void stopListening() {
        if (!(!this.listening)) {
            this.listening = false;
            listenersActive = Math.max(0, (listenersActive - 1));
        }
    }

    private void cycleMode() {
        Object var1 = this.setting.getMode();
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1106107743 -> { break label_2; }
            case 2070621 -> { break label_0; }
            case 2255071 -> { break label_1; }
            default -> { break label_3; }
        }
        label_0:
        if (!(var1.equals("Bind"))) break label_3;
        var2 = 0;
        break label_3;
        label_1:
        if (!(var1.equals("Hold"))) break label_3;
        var2 = 1;
        break label_3;
        label_2:
        if (!(var1.equals("ReverseHold"))) break label_3;
        var2 = 2;
        label_3:
        switch (var2) {
            case 0 -> { break label_4; }
            case 1 -> { break label_5; }
            case 2 -> { break label_6; }
            default -> { break label_7; }
        }
        label_4:
        this.setting.setMode("Hold");
        break label_7;
        label_5:
        this.setting.setMode("ReverseHold");
        break label_7;
        label_6:
        this.setting.setMode("Bind");
        label_7:
    }

    static {
        listenersActive = 0;
    }

}
