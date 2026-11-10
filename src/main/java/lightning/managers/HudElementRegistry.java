package lightning.managers;

import java.util.LinkedHashMap;
import java.util.Map;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.PositionSetting;

public class HudElementRegistry {
    private static final Map ELEMENTS;
    private static final Map RAW_BOUNDS;

    public static void register(String arg0, BooleanSetting arg1, PositionSetting arg2) {
        register(arg0, arg1, arg2, null);
    }

    public static void register(String arg0, BooleanSetting arg1, PositionSetting arg2, CategorySetting arg3) {
        ELEMENTS.put(arg0, new HudElementRegistry.Element(arg0, arg1, arg2, arg3));
    }

    public static void reportBounds(String arg0, float arg1, float arg2, float arg3, float arg4) {
        var tmp0 = new float[4];
        tmp0[0] = arg1;
        tmp0[1] = arg2;
        tmp0[2] = arg3;
        tmp0[3] = arg4;
        RAW_BOUNDS.put(arg0, tmp0);
    }

    public static Map getElements() {
        return ELEMENTS;
    }

    public static float[] getBounds(String arg0) {
        Object var1 = ((float[]) RAW_BOUNDS.get(arg0));
        Object var2 = ((HudElementRegistry.Element) ELEMENTS.get(arg0));
        if ((var1 == null) || (var2 == null)) {
            return var1;
        }
        float var3 = var2.offset().getX();
        float var4 = var2.offset().getY();
        var tmp0 = new float[4];
        tmp0[0] = (var1[0] + var3);
        tmp0[1] = (var1[1] + var4);
        tmp0[2] = (var1[2] + var3);
        tmp0[3] = (var1[3] + var4);
        return tmp0;
    }

    public static void clamp(String arg0, int arg1, int arg2) {
        Object var3 = ((HudElementRegistry.Element) ELEMENTS.get(arg0));
        Object var4 = ((float[]) RAW_BOUNDS.get(arg0));
        if ((var3 == null) || (var4 == null)) {
            return;
        }
        float var5 = (var4[2] - var4[0]);
        float var6 = (var4[3] - var4[1]);
        float var7 = -(var4[0]);
        float var8 = Math.max(var7, ((((float) arg1) - var5) - var4[0]));
        float var9 = -(var4[1]);
        float var10 = Math.max(var9, ((((float) arg2) - var6) - var4[1]));
        var3.offset().set(Math.clamp(var3.offset().getX(), var7, var8), Math.clamp(var3.offset().getY(), var9, var10));
    }

    static {
        ELEMENTS = new LinkedHashMap();
        RAW_BOUNDS = new LinkedHashMap();
    }


    public record Element(String name, BooleanSetting enabled, PositionSetting offset, CategorySetting category) {

        public Element(String arg0, BooleanSetting arg1, PositionSetting arg2, CategorySetting arg3) {
            this.name = arg0;
            this.enabled = arg1;
            this.offset = arg2;
            this.category = arg3;
        }

        public String name() {
            return this.name;
        }

        public BooleanSetting enabled() {
            return this.enabled;
        }

        public PositionSetting offset() {
            return this.offset;
        }

        public CategorySetting category() {
            return this.category;
        }

    }
}
