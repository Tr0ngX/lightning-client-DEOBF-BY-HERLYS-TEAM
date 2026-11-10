package lightning.pingbypass.modules;

import java.awt.Color;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;

public class SyncModule {
    public static void applyToggle(PbModule arg0, boolean arg1) {
        arg0.setToggled(arg1);
    }

    public static void applySetting(PbModule arg0, String arg1, String arg2) {
        Object var3 = arg0.getSetting(arg1);
        if (var3 == null) return;
        if (!((var3 instanceof BooleanSetting))) break label_1;
        Object var4 = ((BooleanSetting) var3);
        var4.setValue(Boolean.parseBoolean(arg2));
        break label_11;
        label_1:
        if ((var3 instanceof NumberSetting)) {
            var4 = ((NumberSetting) var3);
            switch (SyncModule.1.$SwitchMap$night$settings$impl$NumberSetting$Type[var4.getType().ordinal()]) {
                case 1 -> { break label_2; }
                case 2 -> { break label_3; }
                case 3 -> { break label_4; }
                case 4 -> { break label_5; }
                default -> { break label_6; }
            }
            label_2:
            var4.setValue(Integer.parseInt(arg2));
            break label_6;
            label_3:
            var4.setValue(Long.parseLong(arg2));
            break label_6;
            label_4:
            var4.setValue(Float.parseFloat(arg2));
            break label_6;
            label_5:
            var4.setValue(Double.parseDouble(arg2));
            label_6:
            break label_11;
        }
        if ((var3 instanceof ModeSetting)) {
            var4 = ((ModeSetting) var3);
            var4.setValue(arg2);
            break label_11;
        }
        if ((var3 instanceof StringSetting)) {
            var4 = ((StringSetting) var3);
            var4.setValue(arg2);
            break label_11;
        }
        if (!((var3 instanceof ColorSetting))) break label_11;
        var4 = ((ColorSetting) var3);
        Object var5 = arg2.split(",");
        if (var5.length < 4) break label_11;
        var4.setColor(new Color(Integer.parseInt(var5[0]), Integer.parseInt(var5[1]), Integer.parseInt(var5[2]), Integer.parseInt(var5[3])));
        if (var5.length >= 5) {
            var4.setSync(Boolean.parseBoolean(var5[4]));
        }
        if (var5.length < 6) break label_11;
        var4.setRainbow(Boolean.parseBoolean(var5[5]));
        label_11:
    }


    class 1 {

        static {
            $SwitchMap$night$settings$impl$NumberSetting$Type = new int[NumberSetting.Type.values().length];
            try {
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.INTEGER.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.LONG.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.DOUBLE.ordinal()] = 4;
            } catch (NoSuchFieldError ex11) {
                var0 = ex11;
            }
        }

    }
}
