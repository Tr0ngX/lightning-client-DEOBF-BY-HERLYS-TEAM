package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CategoryButton extends Button {
    private final CategorySetting setting;
    private final BooleanSetting enableSetting;
    private final Animation fillAnim;

    public CategoryButton(CategorySetting arg0, BooleanSetting arg1, Frame arg2, int arg3) {
        super(arg0, arg2, arg3, arg0.getDescription());
        this.setting = arg0;
        this.enableSetting = arg1;
        if (arg1 == null) break label_0;
        float var5 = (arg1.getValue() ? 1.0f : 0.0f);
        this.fillAnim = new Animation(var5, var5, 180, Easing.Method.EASE_OUT_CUBIC);
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getHeight()) - 1)), new Color(0, 0, 0, 40));
        if (this.enableSetting == null) break label_0;
        float var5 = this.fillAnim.get((this.enableSetting.getValue() ? 1.0f : 0.0f));
        if (var5 > 0.001f) {
            int var6 = (((getX() + getPadding()) + 1) + Math.round((((float) (getWidth() - ((getPadding() + 1) * 2))) * var5)));
            Object var7 = ClickGuiScreen.getButtonColor(getY(), Math.round((100.0f * var5)));
            LightningSkin.fillQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) var6), ((float) ((getY() + getHeight()) - 1)), var7);
        }
        if (this.enableSetting != null && this.enableSetting.getValue()) {
        } else {
        }
        var6 = ClickGuiScreen.getButtonColor(getY(), 180);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.setting.getTag(), ((getX() + getTextPadding()) + 4), (getY() + 2), var6);
        var7 = (this.setting.isOpen() ? "..." : "+");
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var7, ((((getX() + getWidth()) - getTextPadding()) - 1) - LightningClient.FONT_MANAGER.getWidth(var7)), (getY() + 2), Color.WHITE);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isHovering(arg0, arg1))) return;
        if (this.enableSetting != null && arg2 == 0) {
            this.enableSetting.setValue(!(this.enableSetting.getValue()));
            playClickSound();
            break label_7;
        }
        if (arg2 != 1) {
            if ((this.enableSetting != null) || (arg2 != 0)) break label_7;
        }
        this.setting.setOpen(!(this.setting.isOpen()));
        playClickSound();
        label_7:
    }

}
