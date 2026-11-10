package lightning.modules.impl.core;

import java.awt.Color;
import java.lang.reflect.Method;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudListStyle {
    private static Method original;
    private static final Color BIND_OFF;
    private static final Color BIND_ON;
    private static final Color NAME_OFF;
    private static final Color META;

    private static boolean newbie(HUDModule arg0) {
        try {
            return "Newbie".equalsIgnoreCase(arg0.moduleListMode.getValue());
        } catch (Throwable ex2) {
            Object var1 = ex2;
            return false;
        }
    }

    private static String bindOf(Module arg0) {
        int var1 = arg0.getBind();
        if (!var1) return "";
        Object var2 = KeyboardUtils.getKeyName(var1);
        return (var2 == null ? "" : var2);
    }

    private static String metaOf(HUDModule arg0, Module arg1) {
        Object var2 = arg1.getMetaData();
        if (var2 != null && !(var2.isEmpty())) {
            if (!(arg0.metaData.getValue())) {
            }
        } else {
        }
        return var2;
    }

    public static String moduleText(HUDModule arg0, Module arg1) {
        if (newbie(arg0)) break label_8;
        try {
            if (original == null) {
                var tmp0 = new Class[1];
                tmp0[0] = Module.class;
                original = HUDModule.class.getDeclaredMethod("getModuleText", tmp0);
                original.setAccessible(true);
            }
            var tmp1 = new Object[1];
            tmp1[0] = arg1;
            return ((String) original.invoke(arg0, tmp1));
        } catch (Throwable ex6) {
            Object var2 = ex6;
            return arg1.getName();
        }
        label_8:
        var2 = bindOf(arg1);
        Object var3 = metaOf(arg0, arg1);
        Object var4 = new StringBuilder();
        if (!(var2.isEmpty())) {
            var4.append(var2).append(32);
        }
        var4.append(arg1.getName());
        if (!(var3.isEmpty())) {
            var4.append(32).append(var3);
        }
        return var4.toString();
    }

    private static Color lerp(Color arg0, Color arg1, float arg2, int arg3) {
        arg2 = Math.max(0.0f, Math.min(1.0f, arg2));
        return new Color(((int) (((float) arg0.getRed()) + (((float) (arg1.getRed() - arg0.getRed())) * arg2))), ((int) (((float) arg0.getGreen()) + (((float) (arg1.getGreen() - arg0.getGreen())) * arg2))), ((int) (((float) arg0.getBlue()) + (((float) (arg1.getBlue() - arg0.getBlue())) * arg2))), arg3);
    }

    public static void drawNewbie(HUDModule arg0, Module arg1, GuiGraphicsExtractor arg2, float arg3, float arg4, float arg5, Color arg6, boolean arg7) {
        Object var8 = bindOf(arg1);
        Object var9 = metaOf(arg0, arg1);
        Object var10 = arg1.getName();
        if ((var10 == null) || (var10.isEmpty())) {
            return;
        }
        Object var11 = LightningClient.FONT_MANAGER;
        int var12 = var11.getHeight();
        Object var13 = moduleText(arg0, arg1);
        float var14 = ((float) var11.getWidth(var13));
        float var15 = Math.max(0.0f, Math.min(1.0f, arg5));
        Object var16 = (arg6 == null ? Color.WHITE : arg6);
        Object var17 = arg2.pose();
        var17.pushMatrix();
        var17.translate(arg3, arg4);
        int var18 = 0;
        if (!(var8.isEmpty())) {
            var11.drawTextWithShadow(arg2, var8, var18, 0, lerp(BIND_OFF, BIND_ON, var15, 255));
            var18 = (var18 + var11.getWidth((var8 + " ")));
        }
        var11.drawTextWithShadow(arg2, var10, var18, 0, lerp(NAME_OFF, var16, var15, 255));
        if (!(var9.isEmpty())) {
            var18 = (var18 + var11.getWidth((var10 + " ")));
            var11.drawTextWithShadow(arg2, var9, var18, 0, META);
        }
        var17.popMatrix();
    }

    static {
        BIND_OFF = new Color(88, 88, 98);
        BIND_ON = new Color(150, 150, 165);
        NAME_OFF = new Color(125, 125, 138);
        META = new Color(150, 150, 162);
    }

}
