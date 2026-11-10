package lightning.settings.impl;

import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;

public class BooleanSetting extends Setting {
    private boolean value;
    private final boolean defaultValue;
    private final Animation openAnim;

    public float getOpenAmount() {
        return this.openAnim.get((!(!this.value) ? 1.0f : 0.0f));
    }

    public BooleanSetting(String arg0, String arg1, boolean arg2) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.value = arg2;
        this.defaultValue = arg2;
        this.openAnim = newOpenAnim(arg2);
    }

    public BooleanSetting(String arg0, String arg1, String arg2, boolean arg3) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.value = arg3;
        this.defaultValue = arg3;
        this.openAnim = newOpenAnim(arg3);
    }

    public BooleanSetting(String arg0, String arg1, Setting.Visibility arg2, boolean arg3) {
        super(arg0, arg0, arg1, arg2);
        this.value = arg3;
        this.defaultValue = arg3;
        this.openAnim = newOpenAnim(arg3);
    }

    public BooleanSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, boolean arg4) {
        super(arg0, arg1, arg2, arg3);
        this.value = arg4;
        this.defaultValue = arg4;
        this.openAnim = newOpenAnim(arg4);
    }

    private static Animation newOpenAnim(boolean arg0) {
        float var1 = (!(!arg0) ? 1.0f : 0.0f);
        return new Animation(var1, var1, 180, Easing.Method.EASE_OUT_QUAD);
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean arg0) {
        this.value = arg0;
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }


    public class Visibility extends Setting.Visibility {
        private final BooleanSetting value;
        private final boolean targetValue;

        public Visibility(BooleanSetting arg0, boolean arg1) {
            super(arg0);
            this.value = arg0;
            this.targetValue = arg1;
        }

        public void update() {
            if (this.value.getVisibility() == null) break label_0;
            this.value.getVisibility().update();
            if (this.value.getVisibility().isVisible()) break label_0;
            setVisible(false);
            return;
            label_0:
            setVisible(getOpenAmount() > 0.001f);
        }

        public float getOpenAmount() {
            float var1 = this.value.getOpenAmount();
            if (!(!this.targetValue)) {
            } else {
            }
            return (1.0f - var1);
        }

    }
}
