package lightning.settings.impl;

import java.util.Arrays;
import java.util.List;
import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class ModeSetting extends Setting {
    private String value;
    private final String defaultValue;
    private final List modes;

    public ModeSetting(String arg0, String arg1, String arg2, String[] arg3) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.value = arg2;
        this.defaultValue = arg2;
        this.modes = Arrays.asList(arg3);
    }

    public ModeSetting(String arg0, String arg1, String arg2, String arg3, String[] arg4) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.value = arg3;
        this.defaultValue = arg3;
        this.modes = Arrays.asList(arg4);
    }

    public ModeSetting(String arg0, String arg1, Setting.Visibility arg2, String arg3, String[] arg4) {
        super(arg0, arg0, arg1, arg2);
        this.value = arg3;
        this.defaultValue = arg3;
        this.modes = Arrays.asList(arg4);
    }

    public ModeSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, String arg4, String[] arg5) {
        super(arg0, arg1, arg2, arg3);
        this.value = arg4;
        this.defaultValue = arg4;
        this.modes = Arrays.asList(arg5);
    }

    public void setValue(String arg0) {
        if (!(this.modes.contains(arg0))) return;
        this.value = arg0;
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public List getModes() {
        return this.modes;
    }


    public class Visibility extends Setting.Visibility {
        private final ModeSetting value;
        private final List targetValues;

        public Visibility(ModeSetting arg0, String... arg1) {
            super(arg0);
            this.value = arg0;
            this.targetValues = Arrays.asList(arg1);
        }

        public void update() {
            if (this.value.getVisibility() == null) break label_0;
            this.value.getVisibility().update();
            if (this.value.getVisibility().isVisible()) break label_0;
            setVisible(false);
            return;
            label_0:
            int var1 = 0;
            Object var2 = this.targetValues.iterator();
            label_1:
            if (!(var2.hasNext())) break label_3;
            Object var3 = ((String) var2.next());
            if (!(this.value.getValue().equals(var3))) {
                break label_1;
            }
            var1 = 1;
            break label_3;
            label_3:
            setVisible(var1);
        }

    }
}
