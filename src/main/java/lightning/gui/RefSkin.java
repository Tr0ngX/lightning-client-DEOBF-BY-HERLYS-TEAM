package lightning.gui;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Purified RefSkin: Cryptic methods fq() and ol() renamed to fillToggleQuad() and drawToggleOutline().
 */
public final class RefSkin {
    private static float trackX;
    private static float trackY;
    private static boolean haveTrack;

    private RefSkin() {}

    public static Color pick(int markerAlpha) {
        if (markerAlpha == 253) {
            return new Color(168, 140, 255);
        }
        if (markerAlpha == 254) {
            return new Color(56, 242, 196);
        }
        return null;
    }

    public static void fillToggleQuad(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, Color color) {
        float w = x2 - x1;
        float h = y2 - y1;
        if (w == 14.0f && h == 7.0f) {
            boolean isOffTrack = color.getRed() == 40 && color.getGreen() == 40
                    && color.getBlue() == 50 && color.getAlpha() == 160;
            Color pillColor = isOffTrack
                    ? new Color(60, 60, 60, 235)
                    : new Color(127, 127, 127, 240);
            trackX = x1;
            trackY = y1;
            haveTrack = true;
            LightningSkin.drawRoundedRectAll(context, x1 - 1.0f, y1 - 1.0f, x1 + 15.0f, y1 + 8.0f, 4.5f, pillColor);
            return;
        }
        if (w == 5.0f && h == 9.0f && haveTrack) {
            haveTrack = false;
            float progress = Math.clamp((x1 - trackX) / 9.0f, 0.0f, 1.0f);
            float knobX = trackX + progress * 7.0f;
            float knobY = trackY;
            LightningSkin.drawRoundedRectAll(context, knobX, knobY, knobX + 7.0f, knobY + 7.0f, 3.5f, color);
            return;
        }
        LightningSkin.fillQuad(context, x1, y1, x2, y2, color);
    }

    public static void drawToggleOutline(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2, Color color) {
        if ((x2 - x1) == 14.0f && (y2 - y1) == 7.0f) {
            return;
        }
        LightningSkin.drawOutline(context, x1, y1, x2, y2, color);
    }
}
