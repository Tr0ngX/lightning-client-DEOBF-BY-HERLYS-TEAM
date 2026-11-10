package lightning.settings.impl;

import lightning.settings.Setting;
import lombok.Generated;

public class PositionSetting extends Setting {
    private float x;
    private float y;

    public PositionSetting(String arg0, String arg1) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public void set(float arg0, float arg1) {
        this.x = arg0;
        this.y = arg1;
    }

    public void resetValue() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public void setX(float arg0) {
        this.x = arg0;
    }

    @Generated
    public void setY(float arg0) {
        this.y = arg0;
    }

}
