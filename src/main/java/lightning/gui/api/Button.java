package lightning.gui.api;

import lightning.LightningClient;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class Button implements IMinecraft {
    private final Setting setting;
    private final Frame parent;
    private int x;
    private int y;
    private int height;
    private int padding;
    private int textPadding;
    private final String description;
    private boolean visible;

    public Button(Frame arg0, int arg1, String arg2) {
        this.padding = 2;
        this.textPadding = 5;
        this.visible = true;
        this.setting = null;
        this.parent = arg0;
        this.height = arg1;
        this.description = arg2;
    }

    public Button(Setting arg0, Frame arg1, int arg2, String arg3) {
        this.padding = 2;
        this.textPadding = 5;
        this.visible = true;
        this.setting = arg0;
        this.parent = arg1;
        this.height = arg2;
        this.description = arg3;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
    }

    public void mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
    }

    public void mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
    }

    public void charTyped(char arg0, int arg1) {
    }

    public int getWidth() {
        return this.parent.getWidth();
    }

    public boolean isHovering(double arg0, double arg1) {
        if (((((double) (this.x + this.padding)) > arg0) || (((double) this.y) > arg1)) || (((double) ((this.x + getWidth()) - this.padding)) <= arg0)) break label_0;
        return ((double) (this.y + getHeight())) > arg1;
    }

    public void playClickSound() {
        if (((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).sounds.getValue()) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Generated
    public Setting getSetting() {
        return this.setting;
    }

    @Generated
    public Frame getParent() {
        return this.parent;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public void setX(int arg0) {
        this.x = arg0;
    }

    @Generated
    public void setY(int arg0) {
        this.y = arg0;
    }

    @Generated
    public void setHeight(int arg0) {
        this.height = arg0;
    }

    @Generated
    public void setPadding(int arg0) {
        this.padding = arg0;
    }

    @Generated
    public void setTextPadding(int arg0) {
        this.textPadding = arg0;
    }

    @Generated
    public void setVisible(boolean arg0) {
        this.visible = arg0;
    }

}
