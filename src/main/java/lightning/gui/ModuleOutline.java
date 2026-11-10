package lightning.gui;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class ModuleOutline {
    static void draw(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, int arg5, boolean arg6, boolean arg7, float arg8) {
        if ((((!arg6) || (arg8 <= 0.01f)) || ((arg3 - arg1) < 6.0f)) || ((arg4 - arg2) < 6.0f)) {
            return;
        }
        int var9 = (!(!arg7) ? 210 : 150);
        int var10 = Math.round((((float) var9) * Math.min(1.0f, arg8)));
        if (var10 > 255) {
            var10 = 255;
        }
        if (var10 < 0) {
            var10 = 0;
        }
        Object var11 = new Color(((arg5 >> 16) & 255), ((arg5 >> 8) & 255), (arg5 & 255), var10);
        LightningSkin.ring(arg0, arg1, arg2, arg3, arg4, 4.0f, 1.7f, var11);
    }

}
