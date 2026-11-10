package lightning.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.managers.HudElementRegistry;
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
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.NeekeriFill;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HudElementButton extends Button implements ExpandableRow {
    private final HudElementRegistry.Element element;
    private boolean open;
    private final ArrayList buttons;
    private String searchQuery;
    private final Animation openAnim;
    private int revealHeight;
    private final Animation fillAnim;

    public HudElementRegistry.Element getElement() {
        return this.element;
    }

    public boolean isOpen() {
        if (this.element.category() != null) {
        } else {
        }
        return this.open;
    }

    public float getOpenAmount() {
        if (this.element.category() != null) {
        } else {
        }
        return this.openAnim.get((!(!this.open) ? 1.0f : 0.0f));
    }

    public String getRowName() {
        return this.element.name();
    }

    public List getButtons() {
        return this.buttons;
    }

    public void setRevealHeight(int arg0) {
        this.revealHeight = arg0;
    }

    public void setSearchQuery(String arg0) {
        this.searchQuery = arg0;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public HudElementButton(HudElementRegistry.Element arg0, List arg1, Frame arg2, int arg3) {
        super(arg2, arg3, arg0.name());
        this.open = false;
        this.buttons = new ArrayList();
        this.searchQuery = "";
        this.openAnim = new Animation(180, Easing.Method.EASE_OUT_QUAD);
        this.revealHeight = 0;
        this.element = arg0;
        float var5 = (arg0.enabled().getValue() ? 1.0f : 0.0f);
        this.fillAnim = new Animation(var5, var5, 180, Easing.Method.EASE_OUT_QUAD);
        Object var6 = arg1.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Setting) var6.next());
            if ((var7 instanceof BooleanSetting)) {
                Object var8 = ((BooleanSetting) var7);
                this.buttons.add(new BooleanButton(var8, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof NumberSetting)) {
                var8 = ((NumberSetting) var7);
                this.buttons.add(new NumberButton(var8, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof CategorySetting)) {
                var8 = ((CategorySetting) var7);
                this.buttons.add(new CategoryButton(var8, null, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof BindSetting)) {
                var8 = ((BindSetting) var7);
                this.buttons.add(new BindButton(var8, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof ModeSetting)) {
                var8 = ((ModeSetting) var7);
                this.buttons.add(new ModeButton(var8, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof WhitelistSetting)) {
                var8 = ((WhitelistSetting) var7);
                this.buttons.add(new WhitelistButton(var8, arg2, arg3));
                break label_2;
            }
            if ((var7 instanceof StringSetting)) {
                var8 = ((StringSetting) var7);
                this.buttons.add(new StringButton(var8, arg2, arg3));
                break label_2;
            }
            if (!((var7 instanceof ColorSetting))) {
                break label_2;
            }
            var8 = ((ColorSetting) var7);
            this.buttons.add(new ColorButton(var8, arg2, arg3));
        }
    }

    private void updateChildBounds() {
        int var1 = (getY() + getHeight());
        Object var2 = this.buttons.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Button) var2.next());
            var3.setX(getX());
            var3.setY(var1);
            if (!(var3.isVisible())) {
                break label_0;
            }
            var1 = (var1 + var3.getHeight());
        }
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        updateChildBounds();
        if (isHovering(((double) arg1), ((double) arg2)) && LightningClient.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            LightningClient.CLICK_GUI.getDescriptionFrame().setDescription(getDescription());
        }
        if (!(isHovering(((double) arg1), ((double) arg2)))) break label_1;
        break label_2;
        label_1:
        label_2:
        var tmp0 = new Color(0, 0, 0, 0);
        Object var6 = tmp0;
        Object var7 = tmp0;
        if (var6.getAlpha() > 0) {
            Renderer2D.renderQuad(arg0, ((float) (getX() + getPadding())), ((float) getY()), ((float) ((getX() + getWidth()) - getPadding())), ((float) ((getY() + getHeight()) - 1)), var6);
        }
        var tmp1 = this.fillAnim.get((this.element.enabled().getValue() ? 1.0f : 0.0f));
        float var5 = tmp1;
        if (tmp1 <= 0.001f) break label_9;
        Object var8 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        int var9 = (!(var8.fillMode.getValue().equalsIgnoreCase("Default")) ? 1 : 0);
        int var10 = ((getX() + getPadding()) + Math.round((((float) (getWidth() - (getPadding() * 2))) * var5)));
        if (!(!var9)) {
            int var11 = Math.round(((var8.neekeriOpacity.getValue().floatValue() / 100.0f) * 255.0f));
            arg0.enableScissor((getX() + getPadding()), getY(), var10, ((getY() + getHeight()) - 1));
            NeekeriFill.fill(arg0, (getX() + getPadding()), getY(), (getWidth() - (getPadding() * 2)), (getHeight() - 1), var11);
            arg0.disableScissor();
            break label_9;
        }
        var11 = ColorUtils.getColor(var8.color.getColor(), 90);
        Renderer2D.renderQuad(arg0, ((float) (getX() + getPadding())), ((float) getY()), ((float) var10), ((float) ((getY() + getHeight()) - 1)), var11);
        label_9:
        var8 = ClickGuiScreen.getButtonColor(getY(), 60);
        Renderer2D.renderQuad(arg0, ((float) (getX() + getPadding())), ((float) ((getY() + getHeight()) - 1)), ((float) ((getX() + getWidth()) - getPadding())), ((float) (getY() + getHeight())), var8);
        var9 = ((getX() + getTextPadding()) + (this.element.enabled().getValue() ? 1 : 0));
        var10 = (getY() + 2);
        if (this.element.enabled().getValue()) {
        } else {
        }
        var11 = ChatFormatting.GRAY.toString();
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (var11 + this.element.name()), var9, var10, Color.WHITE);
        if (!(this.buttons.isEmpty())) {
            Object var12 = (isOpen() ? "..." : "+");
            int var13 = ((((getX() + getWidth()) - getTextPadding()) - LightningClient.FONT_MANAGER.getWidth(var12)) - 1);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var12, var13, (getY() + 2), Color.WHITE);
        }
        if (this.revealHeight > 0) {
            var12 = (getY() + getHeight());
            arg0.enableScissor(getX(), var12, (getX() + getWidth()), (var12 + this.revealHeight));
            var13 = this.buttons.iterator();
            while (var13.hasNext()) {
                Object var14 = ((Button) var13.next());
                if (!(var14.isVisible())) {
                    if ((var14 instanceof BooleanButton)) {
                        Object var15 = ((BooleanButton) var14);
                        var15.setPrevVisible(false);
                        break label_17;
                    }
                    if (!((var14 instanceof NumberButton))) {
                        break label_17;
                    }
                    var15 = ((NumberButton) var14);
                    var15.setPrevVisible(false);
                    break label_17;
                }
                var14.render(arg0, arg1, arg2, arg3);
                if (!(var14.isHovering(((double) arg1), ((double) arg2)))) break label_17;
                if (!(LightningClient.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty())) {
                    break label_17;
                }
                LightningClient.CLICK_GUI.getDescriptionFrame().setDescription(var14.getDescription());
            }
            arg0.disableScissor();
            break label_27;
        }
        var12 = this.buttons.iterator();
        while (var12.hasNext()) {
            var13 = ((Button) var12.next());
            if ((var13 instanceof BooleanButton)) {
                var14 = ((BooleanButton) var13);
                var14.setPrevVisible(false);
                break label_24;
            }
            if (!((var13 instanceof NumberButton))) {
                break label_24;
            }
            var14 = ((NumberButton) var13);
            var14.setPrevVisible(false);
        }
        label_27:
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        updateChildBounds();
        if (!(isHovering(arg0, arg1))) break label_15;
        if (arg2 == 0) {
            this.element.enabled().setValue(!(this.element.enabled().getValue()));
            playClickSound();
            break label_15;
        }
        if (arg2 != 1) break label_15;
        if (this.element.category() != null) {
            this.element.category().setOpen(!(this.element.category().isOpen()));
        } else {
            this.open = !this.open;
            this.openAnim.setEasing((!(!this.open) ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD));
        }
        Object var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            if ((var7 instanceof BooleanButton)) {
                Object var8 = ((BooleanButton) var7);
                var8.resetSlide();
                break label_11;
            }
            if (!((var7 instanceof NumberButton))) {
                break label_11;
            }
            var8 = ((NumberButton) var7);
            var8.resetSlide();
        }
        playClickSound();
        label_15:
        if (!(isOpen())) break label_18;
        var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            var7 = ((Button) var6.next());
            if (!(var7.isVisible())) {
                break label_16;
            }
            var7.mouseClicked(arg0, arg1, arg2);
        }
        label_18:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        updateChildBounds();
        if (!(isOpen())) break label_2;
        Object var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            if (!(var7.isVisible())) {
                break label_0;
            }
            var7.mouseReleased(arg0, arg1, arg2);
        }
        label_2:
    }

    public void mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
        updateChildBounds();
        if (!(isOpen())) break label_2;
        Object var10 = this.buttons.iterator();
        while (var10.hasNext()) {
            Object var11 = ((Button) var10.next());
            if (!(var11.isVisible())) {
                break label_0;
            }
            var11.mouseDragged(arg0, arg1, arg2, arg3, arg4);
        }
        label_2:
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!(isOpen())) break label_2;
        Object var4 = this.buttons.iterator();
        while (var4.hasNext()) {
            Object var5 = ((Button) var4.next());
            if (!(var5.isVisible())) {
                break label_0;
            }
            var5.keyPressed(arg0, arg1, arg2);
        }
        label_2:
    }

    public void charTyped(char arg0, int arg1) {
        if (!(isOpen())) break label_2;
        Object var3 = this.buttons.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Button) var3.next());
            if (!(var4.isVisible())) {
                break label_0;
            }
            var4.charTyped(arg0, arg1);
        }
        label_2:
    }

}
