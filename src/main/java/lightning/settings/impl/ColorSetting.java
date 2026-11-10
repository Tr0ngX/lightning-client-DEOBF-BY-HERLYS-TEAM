package lightning.settings.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.Setting;
import lightning.utils.color.ColorUtils;
import lombok.Generated;

public class ColorSetting extends Setting {
    private ColorSetting.Color value;
    private final ColorSetting.Color defaultValue;

    public ColorSetting(String arg0, String arg1, ColorSetting.Color arg2) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.value = arg2;
        this.defaultValue = new ColorSetting.Color(arg2.getColor(), arg2.isSync(), arg2.isRainbow());
    }

    public ColorSetting(String arg0, String arg1, String arg2, ColorSetting.Color arg3) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.value = arg3;
        this.defaultValue = new ColorSetting.Color(arg3.getColor(), arg3.isSync(), arg3.isRainbow());
    }

    public ColorSetting(String arg0, String arg1, Setting.Visibility arg2, ColorSetting.Color arg3) {
        super(arg0, arg0, arg1, arg2);
        this.value = arg3;
        this.defaultValue = new ColorSetting.Color(arg3.getColor(), arg3.isSync(), arg3.isRainbow());
    }

    public ColorSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, ColorSetting.Color arg4) {
        super(arg0, arg1, arg2, arg3);
        this.value = arg4;
        this.defaultValue = new ColorSetting.Color(arg4.getColor(), arg4.isSync(), arg4.isRainbow());
    }

    public Color getColor() {
        return getColor(0L);
    }

    public Color getColor(long arg0) {
        if (isSync()) return ColorUtils.getGlobalColor(getAlpha(), arg0);
        if (isRainbow()) {
            if (LightningClient.MODULE_MANAGER == null) break label_1;
            break label_2;
            label_1:
            label_2:
            Object var3 = null;
            if (var3 != null) {
            } else {
            }
            long var4 = 6L;
            if (var3 != null) {
            } else {
            }
            float var6 = 1.0f;
            if (var3 != null) {
            } else {
            }
            float var7 = 1.0f;
            if (var3 != null && var3.rainbowScale != null) {
            } else {
            }
            float var8 = 1.0f;
            return ColorUtils.getRainbow(var4, var6, var7, getAlpha(), ((long) (((float) arg0) * var8)));
        }
        if (LightningClient.MODULE_MANAGER != null && this == ((ColorModule) LightningClient.MODULE_MANAGER.getModule(ColorModule.class)).color) return ColorUtils.getColor(this.value.getColor(), 255);
        return this.value.getColor();
    }

    public void setValue(ColorSetting.Color arg0) {
        this.value = arg0;
        if (LightningClient.EVENT_HANDLER != null) {
            LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void setColor(Color arg0) {
        this.value.setColor(arg0);
        if (LightningClient.EVENT_HANDLER != null) {
            LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public int getAlpha() {
        return getValue().getColor().getAlpha();
    }

    public boolean isSync() {
        return this.value.isSync();
    }

    public void setSync(boolean arg0) {
        this.value.setSync(arg0);
        if (LightningClient.EVENT_HANDLER != null) {
            LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public boolean isRainbow() {
        return this.value.isRainbow();
    }

    public void setRainbow(boolean arg0) {
        this.value.setRainbow(arg0);
        if (LightningClient.EVENT_HANDLER != null) {
            LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void resetValue() {
        this.value = new ColorSetting.Color(new Color(this.defaultValue.getColor().getRGB(), true), this.defaultValue.isSync(), this.defaultValue.isRainbow());
        if (LightningClient.EVENT_HANDLER != null) {
            LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    @Generated
    public ColorSetting.Color getValue() {
        return this.value;
    }

    @Generated
    public ColorSetting.Color getDefaultValue() {
        return this.defaultValue;
    }


    public class Color {
        private Color color;
        private boolean sync;
        private boolean rainbow;

        @Generated
        public Color getColor() {
            return this.color;
        }

        @Generated
        public boolean isSync() {
            return this.sync;
        }

        @Generated
        public boolean isRainbow() {
            return this.rainbow;
        }

        @Generated
        public void setColor(Color arg0) {
            this.color = arg0;
        }

        @Generated
        public void setSync(boolean arg0) {
            this.sync = arg0;
        }

        @Generated
        public void setRainbow(boolean arg0) {
            this.rainbow = arg0;
        }

        @Generated
        public Color(Color arg0, boolean arg1, boolean arg2) {
            this.color = arg0;
            this.sync = arg1;
            this.rainbow = arg2;
        }

    }

    public class Visibility extends Setting.Visibility {
        private final ColorSetting value;
        private final boolean targetValue;
        private final ColorSetting.Visibility.Target target;

        public Visibility(ColorSetting arg0, boolean arg1, ColorSetting.Visibility.Target arg2) {
            super(arg0);
            this.value = arg0;
            this.targetValue = arg1;
            this.target = arg2;
        }

        public void update() {
            if (this.value.getVisibility() == null) break label_0;
            this.value.getVisibility().update();
            if (this.value.getVisibility().isVisible()) break label_0;
            setVisible(false);
            return;
            label_0:
            if (this.target == ColorSetting.Visibility.Target.RAINBOW) {
                if (this.value.isRainbow() == this.targetValue) {
                    break label_4;
                }
                break label_4;
            }
            0.setVisible(this.value.isSync() == this.targetValue);
        }


        public enum Target {
            RAINBOW,
            SYNC;


            private Target() {
            }

        }
    }
}
