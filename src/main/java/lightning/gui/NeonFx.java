package lightning.gui;

import java.awt.Color;
import lightning.managers.FontManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NeonFx {
    public static void ring(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, float arg5, float arg6, Color arg7) {
        int var8 = arg7.getRed();
        int var9 = arg7.getGreen();
        int var10 = arg7.getBlue();
        LightningSkin.ring(arg0, (arg1 - 3.0f), (arg2 - 3.0f), (arg3 + 3.0f), (arg4 + 3.0f), (arg5 + 3.0f), 4.0f, new Color(var8, var9, var10, 34));
        LightningSkin.ring(arg0, (arg1 - 1.0f), (arg2 - 1.0f), (arg3 + 1.0f), (arg4 + 1.0f), (arg5 + 1.0f), 2.4f, new Color(var8, var9, var10, 90));
        LightningSkin.ring(arg0, arg1, arg2, arg3, arg4, arg5, 1.8f, new Color(var8, var9, var10, 255));
    }

    public static void glowText(FontManager arg0, GuiGraphicsExtractor arg1, String arg2, int arg3, int arg4, Color arg5, int arg6) {
        int var7 = arg5.getRed();
        if (((var7 <= 170) || (arg2 == null)) || (arg2.isEmpty())) break label_8;
        int var8 = Math.min(95, ((int) (((float) (var7 - 170)) * 1.15f)));
        var8 = ((var8 * arg5.getAlpha()) / 255);
        if (var8 <= 3) break label_8;
        Object var9 = new Color(((arg6 >> 16) & 255), ((arg6 >> 8) & 255), (arg6 & 255), var8);
        arg0.drawText(arg1, arg2, (arg3 - 1), arg4, var9);
        arg0.drawText(arg1, arg2, (arg3 + 1), arg4, var9);
        arg0.drawText(arg1, arg2, arg3, (arg4 - 1), var9);
        arg0.drawText(arg1, arg2, arg3, (arg4 + 1), var9);
        label_8:
        arg0.drawTextWithShadow(arg1, arg2, arg3, arg4, arg5);
    }

}
