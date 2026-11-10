package lightning.utils.graphics;

import java.awt.Color;
import java.awt.Font;
import lightning.utils.font.FontRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class LoadingText {
    private static final String TEXT = "LightningClient";
    private static final long STAGGER_MS = 200L;
    private static final long DROP_MS = 1400L;
    private static final float DROP_HEIGHT = 120.0f;
    private static final Color TRACK;
    private static long start;
    private static float shown;
    private static long lastFrame;
    private static FontRenderer title;
    private static FontRenderer small;
    private static boolean fontFailed;
    private static Identifier bg;

    private static void initFonts() {
        if (title != null) break label_1;
        if (!(!fontFailed)) {
            label_1:
            return;
            try {
            }
            Object var0 = LoadingText.class.getResourceAsStream("/assets/lightning/fonts/LightningRound.ttf");
            try {
                Object var1 = Font.createFont(0, var0);
                var tmp0 = new Font[1];
                tmp0[0] = var1;
                title = new FontRenderer(tmp0, 40.0f);
                var tmp1 = new Font[1];
                tmp1[0] = var1;
                small = new FontRenderer(tmp1, 17.0f);
                if (var0 == null) break label_12;
                var0.close();
            } catch (Throwable ex7) {
                var1 = ex7;
                if (var0 == null) break label_11;
                try {
                    var0.close();
                } catch (Throwable ex10) {
                    Object var2 = ex10;
                    var1.addSuppressed(var2);
                }
                label_11:
                throw var1;
            }
            label_12:
        } catch (Throwable ex13) {
            var0 = ex13;
            fontFailed = true;
        }
    }

    public static boolean ready() {
        if (start < 0L) break label_1;
        return LoadingClock.elapsed() > (((((long) ("LightningClient".length() - 1)) * 200L) + 1400L) + 600L);
    }

    public static void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, float arg4, boolean arg5) {
        long var6 = System.currentTimeMillis();
        if (start < 0L) break label_2;
        if ((var6 - lastFrame) <= 3000L) break label_3;
        label_2:
        start = var6;
        shown = 0.0f;
        label_3:
        lastFrame = var6;
        long var8 = LoadingClock.tick(var6);
        float var10 = Math.min(0.1f, (((float) (var6 - lastFrame)) / 1000.0f));
        drawBackground(arg0, arg1, arg2, arg3);
        initFonts();
        if (title == null) return;
        if (!(!arg5)) {
        } else {
        }
        float var11 = Math.max(0.0f, Math.min(1.0f, arg4));
        shown = (shown + ((var11 - shown) * 0.12f));
        if (!(!arg5) && shown > 0.995f) {
            shown = 1.0f;
        }
        int var12 = "LightningClient".length();
        Object var13 = new float[var12];
        float var14 = 0.0f;
        int var15 = 0;
        while (var15 < var12) {
            var13[var15] = title.getTextWidth(String.valueOf("LightningClient".charAt(var15)));
            var14 = (var14 + var13[var15]);
            var15++;
        }
        var15 = title.getHeight();
        float var16 = ((((float) arg1) / 2.0f) - (var14 / 2.0f));
        float var17 = (((((float) arg2) / 2.0f) - (var15 / 2.0f)) - 14.0f);
        float var18 = var16;
        int var19 = 0;
        while (var19 < var12) {
            float var20 = (((float) (var8 - (((long) var19) * 200L))) / 1400.0f);
            if (var20 > 0.0f) {
                float var21 = Math.min(var20, 1.0f);
                float var22 = (var17 - (120.0f * (1.0f - bounce(var21))));
                float var23 = (Math.min(1.0f, (var21 * 3.0f)) * arg3);
                float var24 = (((float) var19) / ((float) (var12 - 1)));
                int var25 = argb(var23, lerp(240, 127, var24), lerp(210, 240, var24), lerp(255, 255, var24));
                LoadingGlow.draw(title, arg0, String.valueOf("LightningClient".charAt(var19)), var18, var22, var23);
                title.drawString(arg0, String.valueOf("LightningClient".charAt(var19)), var18, var22, var25, false);
            }
            var18 = (var18 + var13[var19]);
            var19++;
        }
        var19 = (Math.min(1.0f, Math.max(0.0f, ((((float) var8) - 500.0f) / 400.0f))) * arg3);
        if (var19 <= 0.0f) return;
        var20 = (var14 * 0.92f);
        var21 = 6.0f;
        var22 = ((((float) arg1) / 2.0f) - (var20 / 2.0f));
        var23 = ((var17 + var15) + 18.0f);
        roundedBar(arg0, var22, var23, var20, var21, 1.0f, TRACK, TRACK, (var19 * 0.85f));
        var24 = (var20 * shown);
        if (var24 > 1.0f) {
            roundedBar(arg0, var22, var23, var24, var21, (var24 / var20), new Color(220, 185, 255), new Color(127, 240, 255), var19);
        }
        var25 = (Math.round((shown * 100.0f)) + "%");
        float var26 = small.getTextWidth(var25);
        small.drawString(arg0, var25, ((((float) arg1) / 2.0f) - (var26 / 2.0f)), ((var23 + var21) + 8.0f), argb(var19, 245, 230, 255), false);
    }

    private static void drawBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (bg == null) {
            bg = Identifier.fromNamespaceAndPath("lightning", "textures/gui/moonlight_lg.png");
        }
        int var4 = Math.max(0, Math.min(255, Math.round((arg3 * 255.0f))));
        arg0.blit(RenderPipelines.GUI_TEXTURED, bg, 0, 0, 0.0f, 0.0f, arg1, arg2, arg1, arg2, arg1, arg2, ((var4 << 24) | 16777215));
    }

    private static void roundedBar(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, float arg5, Color arg6, Color arg7, float arg8) {
        float var9 = (arg4 / 2.0f);
        Object var10 = mix(arg6, arg7, Math.min(1.0f, arg5));
        int var11 = 0;
        while (var11 < ((int) arg4)) {
            float var12 = (var9 - (((float) var11) + 0.5f));
            float var13 = (var9 - ((float) Math.sqrt(((double) Math.max(0.0f, ((var9 * var9) - (var12 * var12)))))));
            float var14 = (arg1 + var13);
            float var15 = ((arg1 + arg3) - var13);
            if (var15 <= var14) {
            } else {
                float var16 = ((var14 - arg1) / arg3);
                float var17 = ((var15 - arg1) / arg3);
                Object var18 = withA(mix(arg6, var10, var16), arg8);
                Object var19 = withA(mix(arg6, var10, var17), arg8);
                Renderer2D.renderSidewaysGradient(arg0, var14, (arg2 + ((float) var11)), var15, ((arg2 + ((float) var11)) + 1.0f), var18, var19);
            }
            var11++;
        }
    }

    private static Color mix(Color arg0, Color arg1, float arg2) {
        return new Color(lerp(arg0.getRed(), arg1.getRed(), arg2), lerp(arg0.getGreen(), arg1.getGreen(), arg2), lerp(arg0.getBlue(), arg1.getBlue(), arg2));
    }

    private static Color withA(Color arg0, float arg1) {
        return new Color(arg0.getRed(), arg0.getGreen(), arg0.getBlue(), Math.max(0, Math.min(255, Math.round((arg1 * 255.0f)))));
    }

    private static int lerp(int arg0, int arg1, float arg2) {
        return Math.round((((float) arg0) + (((float) (arg1 - arg0)) * arg2)));
    }

    private static int argb(float arg0, int arg1, int arg2, int arg3) {
        return ((((Math.max(0, Math.min(255, Math.round((arg0 * 255.0f)))) << 24) | (arg1 << 16)) | (arg2 << 8)) | arg3);
    }

    private static float bounce(float arg0) {
        float var1 = 7.5625f;
        float var2 = 2.75f;
        if (arg0 < (1.0f / var2)) return ((var1 * arg0) * arg0);
        if (arg0 < (2.0f / var2)) {
            arg0 = (arg0 - (1.5f / var2));
            return (((var1 * arg0) * arg0) + 0.75f);
        }
        if (arg0 < (2.5f / var2)) {
            arg0 = (arg0 - (2.25f / var2));
            return (((var1 * arg0) * arg0) + 0.9375f);
        }
        arg0 = (arg0 - (2.625f / var2));
        return (((var1 * arg0) * arg0) + 0.984375f);
    }

    static {
        TRACK = new Color(70, 45, 120);
        start = -1L;
        shown = 0.0f;
        lastFrame = -1L;
    }

}
