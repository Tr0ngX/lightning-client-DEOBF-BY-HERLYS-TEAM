package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ImageSelectScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.ImageSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ImageButton extends Button {
    private final ImageSetting setting;
    private final Animation hoverAnim;
    private final Animation textAnim;

    public ImageButton(ImageSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.setting = arg0;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = isHovering(((double) arg1), ((double) arg2));
        float var6 = this.hoverAnim.get((!(!var5) ? 1.0f : 0.0f));
        float var7 = this.textAnim.get((!(!var5) ? 1.0f : 0.0f));
        Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 1)), new Color(0, 0, 0, 40));
        if (var6 > 0.001f) {
            Object var8 = new Color(255, 255, 255, ((int) (20.0f * var6)));
            Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + getParent().getHeight()) - 1)), var8);
        }
        var8 = (((float) ((getX() + getTextPadding()) + 3)) + (var7 * 1.5f));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.setting.getTag(), ((int) var8), (getY() + 2), Color.WHITE);
        Object var9 = this.setting.getValue();
        if (var9 != null && !(var9.isEmpty())) {
            if (var9.equalsIgnoreCase("None")) {
            }
            var9 = "[Select...]";
        }
        Object var10 = var9;
        int var11 = (((getWidth() - LightningClient.FONT_MANAGER.getWidth(this.setting.getTag())) - (getTextPadding() * 2)) - 15);
        if (LightningClient.FONT_MANAGER.getWidth(((String) var10)) > var11) {
            while (LightningClient.FONT_MANAGER.getWidth((((String) var10) + "...")) > var11 && ((String) var10).length() > 3) {
                var10 = ((String) var10).substring(0, (((String) var10).length() - 1));
            }
            var10 = (((String) var10) + "...");
        }
        Object var12 = (String.valueOf(ChatFormatting.GRAY) + ((String) var10));
        int var13 = (((getX() + getWidth()) - getTextPadding()) - 1);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var12, (var13 - LightningClient.FONT_MANAGER.getWidth(var12)), (getY() + 2), Color.LIGHT_GRAY);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isHovering(arg0, arg1))) break label_1;
        if (arg2 != 0) {
            if (arg2 != 1) break label_1;
        }
        Object var6 = Minecraft.getInstance();
        var6.gui.setScreen(new ImageSelectScreen(this.setting, var6.gui.screen()));
        playClickSound();
        label_1:
    }

}
