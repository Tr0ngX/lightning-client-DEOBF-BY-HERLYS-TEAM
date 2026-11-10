package lightning.settings.impl;

import lightning.settings.Setting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lombok.Generated;

public class CategorySetting extends Setting {
    private boolean open;
    private final Animation openAnim;

    public CategorySetting(String arg0, String arg1) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.open = false;
        this.openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    }

    public CategorySetting(String arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.open = false;
        this.openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    }

    public CategorySetting(String arg0, String arg1, Setting.Visibility arg2) {
        super(arg0, arg0, arg1, arg2);
        this.open = false;
        this.openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    }

    public CategorySetting(String arg0, String arg1, String arg2, Setting.Visibility arg3) {
        super(arg0, arg1, arg2, arg3);
        this.open = false;
        this.openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    }

    public float getOpenAmount() {
        return this.openAnim.get((!(!this.open) ? 1.0f : 0.0f));
    }

    @Generated
    public boolean isOpen() {
        return this.open;
    }

    @Generated
    public Animation getOpenAnim() {
        return this.openAnim;
    }

    @Generated
    public void setOpen(boolean arg0) {
        this.open = arg0;
    }


    public class Visibility extends Setting.Visibility {
        private final CategorySetting value;

        public Visibility(CategorySetting arg0) {
            super(arg0);
            this.value = arg0;
        }

        public CategorySetting getValue() {
            return this.value;
        }

        public void update() {
            if (this.value.getVisibility() == null) break label_0;
            this.value.getVisibility().update();
            if (this.value.getVisibility().isVisible()) break label_0;
            setVisible(false);
            return;
            label_0:
            setVisible(this.value.getOpenAmount() > 0.001f);
        }

    }
}
