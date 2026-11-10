package lightning.settings.impl;

import lightning.LightningClient;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class NumberSetting extends Setting {
    private Number value;
    private final Number defaultValue;
    private final Number minimum;
    private final Number maximum;
    private final Number step;
    private final boolean zeroIsIgnore;

    public NumberSetting(String arg0, String arg1, Number arg2, Number arg3, Number arg4) {
        this(arg0, arg0, arg1, new Setting.Visibility(), arg2, arg3, arg4, 0);
    }

    public NumberSetting(String arg0, String arg1, String arg2, Number arg3, Number arg4, Number arg5) {
        this(arg0, arg1, arg2, new Setting.Visibility(), arg3, arg4, arg5, 0);
    }

    public NumberSetting(String arg0, String arg1, Setting.Visibility arg2, Number arg3, Number arg4, Number arg5) {
        this(arg0, arg0, arg1, arg2, arg3, arg4, arg5, 0);
    }

    public NumberSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, Number arg4, Number arg5, Number arg6) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, arg6, 0);
    }

    public NumberSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, Number arg4, Number arg5, Number arg6, Number arg7) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, false);
    }

    public NumberSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, Number arg4, Number arg5, Number arg6, Number arg7, boolean arg8) {
        super(arg0, arg1, arg2, arg3);
        this.value = arg4;
        this.defaultValue = arg4;
        this.minimum = arg5;
        this.maximum = arg6;
        this.step = arg7;
        this.zeroIsIgnore = arg8;
    }

    public void setValue(Number arg0) {
        switch (getType().ordinal()) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            default -> { break label_3; }
        }
        label_0:
        this.value = Math.clamp(arg0.longValue(), this.minimum.longValue(), this.maximum.longValue());
        break label_4;
        label_1:
        this.value = Math.clamp(arg0.doubleValue(), this.minimum.doubleValue(), this.maximum.doubleValue());
        break label_4;
        label_2:
        this.value = Math.clamp(arg0.floatValue(), this.minimum.floatValue(), this.maximum.floatValue());
        break label_4;
        label_3:
        this.value = Math.clamp(((long) arg0.intValue()), this.minimum.intValue(), this.maximum.intValue());
        label_4:
        LightningClient.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public NumberSetting.Type getType() {
        if (this.defaultValue.getClass() == Long.class) return NumberSetting.Type.LONG;
        if (this.defaultValue.getClass() == Double.class) return NumberSetting.Type.DOUBLE;
        if (this.defaultValue.getClass() == Float.class) return NumberSetting.Type.FLOAT;
        return NumberSetting.Type.INTEGER;
    }

    @Generated
    public Number getValue() {
        return this.value;
    }

    @Generated
    public Number getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public Number getMinimum() {
        return this.minimum;
    }

    @Generated
    public Number getMaximum() {
        return this.maximum;
    }

    @Generated
    public Number getStep() {
        return this.step;
    }

    @Generated
    public boolean isZeroIsIgnore() {
        return this.zeroIsIgnore;
    }


    public enum Type {
        INTEGER,
        LONG,
        DOUBLE,
        FLOAT;


        private Type() {
        }

    }

    public class Visibility extends Setting.Visibility {
        private final NumberSetting value;
        private final Number targetValue;
        private final NumberSetting.Visibility.Condition condition;

        public Visibility(NumberSetting arg0, Number arg1, NumberSetting.Visibility.Condition arg2) {
            super(arg0);
            this.value = arg0;
            this.targetValue = arg1;
            this.condition = arg2;
        }

        public void update() {
            if (this.value.getVisibility() == null) break label_0;
            this.value.getVisibility().update();
            if (this.value.getVisibility().isVisible()) break label_0;
            setVisible(false);
            return;
            label_0:
            if (this.value.getType() == NumberSetting.Type.INTEGER) {
                if (this.condition != NumberSetting.Visibility.Condition.EQUALS) break label_2;
                if (this.value.getValue().intValue() != this.targetValue.intValue()) break label_1;
                break label_6;
                label_1:
                break label_6;
                label_2:
                if (this.condition == NumberSetting.Visibility.Condition.SMALLER) {
                    if (this.value.getValue().intValue() < this.targetValue.intValue()) {
                        break label_6;
                    }
                    break label_6;
                }
                0.setVisible(this.value.getValue().intValue() > this.targetValue.intValue());
            }
            if (this.value.getType() == NumberSetting.Type.LONG) {
                if (this.condition == NumberSetting.Visibility.Condition.EQUALS) {
                    if (this.value.getValue().longValue() == this.targetValue.longValue()) {
                        break label_13;
                    }
                    break label_13;
                }
                if (this.condition == NumberSetting.Visibility.Condition.SMALLER) {
                    if (this.value.getValue().longValue() < this.targetValue.longValue()) {
                        break label_13;
                    }
                    break label_13;
                }
                0.setVisible(this.value.getValue().longValue() > this.targetValue.longValue());
            }
            if (this.value.getType() == NumberSetting.Type.DOUBLE) {
                if (this.condition == NumberSetting.Visibility.Condition.EQUALS) {
                    if (this.value.getValue().doubleValue() == this.targetValue.doubleValue()) {
                        break label_20;
                    }
                    break label_20;
                }
                if (this.condition == NumberSetting.Visibility.Condition.SMALLER) {
                    if (this.value.getValue().doubleValue() < this.targetValue.doubleValue()) {
                        break label_20;
                    }
                    break label_20;
                }
                0.setVisible(this.value.getValue().doubleValue() > this.targetValue.doubleValue());
            }
            if (this.value.getType() == NumberSetting.Type.FLOAT) {
                if (this.condition == NumberSetting.Visibility.Condition.EQUALS) {
                    if (this.value.getValue().floatValue() == this.targetValue.floatValue()) {
                        break label_27;
                    }
                    break label_27;
                }
                if (this.condition == NumberSetting.Visibility.Condition.SMALLER) {
                    if (this.value.getValue().floatValue() < this.targetValue.floatValue()) {
                        break label_27;
                    }
                    break label_27;
                }
                0.setVisible(this.value.getValue().floatValue() > this.targetValue.floatValue());
            }
        }


        public enum Condition {
            EQUALS,
            SMALLER,
            BIGGER;


            private Condition() {
            }

        }
    }
}
