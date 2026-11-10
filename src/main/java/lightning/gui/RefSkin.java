package lightning.gui;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RefSkin {
    private static float trackX;
    private static float trackY;
    private static boolean haveTrack;

    public static Color pick(int arg0) {
        if (arg0 == 253) return new Color(168, 140, 255);
        if (arg0 == 254) return new Color(56, 242, 196);
        return null;
    }

    public static void fillToggleQuad(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        float var6 = (arg3 - arg1);
        float var7 = (arg4 - arg2);
        if (var6 == 14.0f && var7 == 7.0f) {
            if (((arg5.getRed() != 40) || (arg5.getGreen() != 40)) || (arg5.getBlue() != 50)) break label_0;
            int var8 = (arg5.getAlpha() == 160 ? 1 : 0);
            if (!(!var8)) {
            } else {
            }
            Object var9 = new Color(127, 127, 127, 240);
            trackX = arg1;
            trackY = arg2;
            haveTrack = true;
            LightningSkin.drawRoundedRectAll(arg0, (arg1 - 1.0f), (arg2 - 1.0f), (arg1 + 15.0f), (arg2 + 8.0f), 4.5f, var9);
            return;
        }
        if (var6 == 5.0f && var7 == 9.0f && !(!haveTrack)) {
            haveTrack = false;
            var8 = ((arg1 - trackX) / 9.0f);
            if (var8 < 0.0f) {
                var8 = 0.0f;
            }
            if (var8 > 1.0f) {
                var8 = 1.0f;
            }
            var9 = (trackX + (var8 * 7.0f));
            float var10 = trackY;
            LightningSkin.drawRoundedRectAll(arg0, var9, var10, (var9 + 7.0f), (var10 + 7.0f), 3.5f, arg5);
            return;
        }
        LightningSkin.fillQuad(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public static void drawToggleOutline(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        if ((arg3 - arg1) == 14.0f && (arg4 - arg2) == 7.0f) return;
        LightningSkin.drawOutline(arg0, arg1, arg2, arg3, arg4, arg5);
    }

}
