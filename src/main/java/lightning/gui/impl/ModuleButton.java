package lightning.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import lightning.LightningClient;
import lightning.gui.ColorGui;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ImageSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ModuleButton extends Button implements ExpandableRow {
    private static final int TEXT_INSET = 6;
    private static final int TEXT_SLIDE = 3;
    private static final int RIGHT_INSET = 5;
    private static final int PILL_W = 16;
    private static final int PILL_H = 8;
    private final Module module;
    private boolean open;
    private final ArrayList buttons;
    private String searchQuery;
    private final Animation openAnim;
    private int revealHeight;
    private final Animation fillAnim;

    public float getOpenAmount() {
        return this.openAnim.get((!(!this.open) ? 1.0f : 0.0f));
    }

    public String getRowName() {
        return this.module.getName();
    }

    public ModuleButton(Module arg0, Frame arg1, int arg2) {
        super(arg1, arg2, arg0.getDescription());
        this.open = false;
        this.buttons = new ArrayList();
        this.searchQuery = "";
        this.openAnim = new Animation(220, Easing.Method.EASE_OUT_CUBIC);
        this.revealHeight = 0;
        this.module = arg0;
        float var4 = (arg0.isToggled() ? 1.0f : 0.0f);
        this.fillAnim = new Animation(var4, var4, 200, Easing.Method.EASE_OUT_CUBIC);
        int var5 = 13;
        Object var6 = arg0.getSettings();
        int var7 = 0;
        while (var7 < var6.size()) {
            Object var8 = ((Setting) var6.get(var7));
            if ((var8 instanceof BooleanSetting)) {
                Object var9 = ((BooleanSetting) var8);
                if (!(var9.getTag().equals("Enabled"))) break label_3;
                Object var10 = var9.getVisibility();
                if (!((var10 instanceof CategorySetting.Visibility))) break label_3;
                Object var11 = ((CategorySetting.Visibility) var10);
                if ((var7 <= 0) || (var6.get((var7 - 1)) != var11.getValue())) break label_3;
                break label_14;
                label_3:
                this.buttons.add(new BooleanButton(var9, arg1, var5));
                break label_14;
            }
            if ((var8 instanceof NumberSetting)) {
                this.buttons.add(new NumberButton(((NumberSetting) var8), arg1, var5));
                break label_14;
            }
            if ((var8 instanceof CategorySetting)) {
                var9 = ((CategorySetting) var8);
                var10 = null;
                if ((var7 + 1) >= var6.size()) break label_6;
                var11 = ((Setting) var6.get((var7 + 1)));
                if (!((var11 instanceof BooleanSetting))) break label_6;
                Object var12 = ((BooleanSetting) var11);
                if (!(var12.getTag().equals("Enabled"))) break label_6;
                Object var13 = var12.getVisibility();
                if ((!((var13 instanceof CategorySetting.Visibility))) || (((CategorySetting.Visibility) var13).getValue() != var9)) break label_6;
                var10 = var12;
                label_6:
                this.buttons.add(new CategoryButton(var9, var10, arg1, var5));
                break label_14;
            }
            if ((var8 instanceof BindSetting)) {
                this.buttons.add(new BindButton(((BindSetting) var8), arg1, var5));
                break label_14;
            }
            if ((var8 instanceof ModeSetting)) {
                this.buttons.add(new ModeButton(((ModeSetting) var8), arg1, var5));
                break label_14;
            }
            if ((var8 instanceof WhitelistSetting)) {
                this.buttons.add(new WhitelistButton(((WhitelistSetting) var8), arg1, var5));
                break label_14;
            }
            if ((var8 instanceof StringSetting)) {
                this.buttons.add(new StringButton(((StringSetting) var8), arg1, var5));
                break label_14;
            }
            if ((var8 instanceof ImageSetting)) {
                this.buttons.add(new ImageButton(((ImageSetting) var8), arg1, var5));
                break label_14;
            }
            if (!((var8 instanceof ColorSetting))) {
                break label_14;
            }
            this.buttons.add(new ColorButton(((ColorSetting) var8), arg1, var5));
            label_14:
            var7++;
        }
    }

    public int getRequiredWidth() {
        int var1 = 0;
        try {
            Object var2 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
            if (var2.triangles.getValue() && !(this.buttons.isEmpty())) {
                var1 = (LightningClient.FONT_MANAGER.getWidth("\u25bc") + 4);
            }
        } catch (Throwable ex2) {
            var2 = ex2;
        }
        var2 = LightningClient.FONT_MANAGER.getWidth(this.module.getName());
        return ((((((((getPadding() * 2) + 6) + 3) + var2) + 8) + 16) + var1) + 5);
    }

    private static Color mixColor(Color arg0, Color arg1, float arg2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, arg2));
        int var4 = Math.round((((float) arg0.getRed()) + (((float) (arg1.getRed() - arg0.getRed())) * var3)));
        int var5 = Math.round((((float) arg0.getGreen()) + (((float) (arg1.getGreen() - arg0.getGreen())) * var3)));
        int var6 = Math.round((((float) arg0.getBlue()) + (((float) (arg1.getBlue() - arg0.getBlue())) * var3)));
        int var7 = Math.round((((float) arg0.getAlpha()) + (((float) (arg1.getAlpha() - arg0.getAlpha())) * var3)));
        return new Color(var4, var5, var6, var7);
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        ColorGui.renderModule(this, arg0, arg1, arg2, arg3);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isHovering(arg0, arg1))) break label_11;
        if (arg2 == 0) {
            this.module.setToggled(!(this.module.isToggled()));
            playClickSound();
            break label_11;
        }
        if (arg2 != 1) break label_11;
        this.open = !this.open;
        this.openAnim.setEasing((!(!this.open) ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD));
        if (!this.open) break label_10;
        Object var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            if ((var7 instanceof BooleanButton)) {
                ((BooleanButton) var7).resetSlide();
                break label_7;
            }
            if (!((var7 instanceof NumberButton))) {
                break label_7;
            }
            ((NumberButton) var7).resetSlide();
        }
        label_10:
        playClickSound();
        label_11:
        if (!this.open) break label_14;
        var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            var7 = ((Button) var6.next());
            if (!(var7.isVisible())) {
                break label_12;
            }
            var7.mouseClicked(arg0, arg1, arg2);
        }
        label_14:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        Object var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            var7.mouseReleased(arg0, arg1, arg2);
        }
    }

    public void mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
        Object var10 = this.buttons.iterator();
        while (var10.hasNext()) {
            Object var11 = ((Button) var10.next());
            var11.mouseDragged(arg0, arg1, arg2, arg3, arg4);
        }
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.open) break label_2;
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
        if (!this.open) break label_2;
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

    public Module getModule() {
        return this.module;
    }

    public boolean isOpen() {
        return this.open;
    }

    public ArrayList getButtons() {
        return this.buttons;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public Animation getOpenAnim() {
        return this.openAnim;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public Animation getFillAnim() {
        return this.fillAnim;
    }

    public void setOpen(boolean arg0) {
        this.open = arg0;
    }

    public void setSearchQuery(String arg0) {
        this.searchQuery = arg0;
    }

    public void setRevealHeight(int arg0) {
        this.revealHeight = arg0;
    }

}
