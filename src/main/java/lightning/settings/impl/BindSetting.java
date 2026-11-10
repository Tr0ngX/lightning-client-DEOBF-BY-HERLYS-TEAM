package lightning.settings.impl;

import java.util.Arrays;
import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class BindSetting extends Setting {
    private int value;
    private final int defaultValue;
    public static final String[] MODES;
    private String mode;
    private boolean holdModesSupported;

    public BindSetting disableHoldModes() {
        this.holdModesSupported = false;
        return this;
    }

    public BindSetting(String arg0, String arg1, int arg2) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.mode = "Bind";
        this.holdModesSupported = true;
        this.value = arg2;
        this.defaultValue = arg2;
    }

    public BindSetting(String arg0, String arg1, String arg2, int arg3) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.mode = "Bind";
        this.holdModesSupported = true;
        this.value = arg3;
        this.defaultValue = arg3;
    }

    public BindSetting(String arg0, String arg1, Setting.Visibility arg2, int arg3) {
        super(arg0, arg0, arg1, arg2);
        this.mode = "Bind";
        this.holdModesSupported = true;
        this.value = arg3;
        this.defaultValue = arg3;
    }

    public BindSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, int arg4) {
        super(arg0, arg1, arg2, arg3);
        this.mode = "Bind";
        this.holdModesSupported = true;
        this.value = arg4;
        this.defaultValue = arg4;
    }

    public void resetValue() {
        this.value = this.defaultValue;
        this.mode = "Bind";
    }

    public void setValue(int arg0) {
        this.value = arg0;
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void cycleMode() {
        if (!this.holdModesSupported) return;
        int var1 = ((Arrays.asList(MODES).indexOf(this.mode) + 1) % MODES.length);
        this.mode = MODES[var1];
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void setMode(String arg0) {
        if (!this.holdModesSupported && !(arg0.equals("Bind"))) return;
        if (!(Arrays.asList(MODES).contains(arg0))) return;
        this.mode = arg0;
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    @Generated
    public int getValue() {
        return this.value;
    }

    @Generated
    public int getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public String getMode() {
        return this.mode;
    }

    @Generated
    public boolean isHoldModesSupported() {
        return this.holdModesSupported;
    }

    @Generated
    public void setHoldModesSupported(boolean arg0) {
        this.holdModesSupported = arg0;
    }

    static {
        var tmp0 = new String[3];
        tmp0[0] = "Bind";
        tmp0[1] = "Hold";
        tmp0[2] = "ReverseHold";
        MODES = tmp0;
    }


    public class Visibility extends Setting.Visibility {
        private final BindSetting value;
        private final int targetValue;

        public Visibility(BindSetting arg0, int arg1) {
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
            setVisible(this.value.getValue() == this.targetValue);
        }

    }
}
