package lightning.settings.impl;

import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class StringSetting extends Setting {
    private String value;
    private final String defaultValue;

    public StringSetting(String arg0, String arg1, String arg2) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.value = arg2;
        this.defaultValue = arg2;
    }

    public StringSetting(String arg0, String arg1, String arg2, String arg3) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.value = arg3;
        this.defaultValue = arg3;
    }

    public StringSetting(String arg0, String arg1, Setting.Visibility arg2, String arg3) {
        super(arg0, arg0, arg1, arg2);
        this.value = arg3;
        this.defaultValue = arg3;
    }

    public StringSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, String arg4) {
        super(arg0, arg1, arg2, arg3);
        this.value = arg4;
        this.defaultValue = arg4;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public void setValue(String arg0) {
        this.value = arg0;
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }


    public class Visibility extends Setting.Visibility {
        private final StringSetting value;
        private final String targetValue;

        public Visibility(StringSetting arg0, String arg1) {
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
            setVisible(this.value.getValue().equals(this.targetValue));
        }

    }
}
