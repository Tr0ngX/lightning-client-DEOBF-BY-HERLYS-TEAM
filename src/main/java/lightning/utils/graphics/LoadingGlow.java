package lightning.utils.graphics;

import lightning.utils.font.FontRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LoadingGlow {
    private static final float[][] RING1;
    private static final float[][] RING2;

    public static void draw(FontRenderer arg0, GuiGraphicsExtractor arg1, String arg2, float arg3, float arg4, float arg5) {
        if ((arg0 == null) || (arg5 <= 0.01f)) {
            return;
        }
        int var6 = col((arg5 * 0.1f));
        int var7 = col((arg5 * 0.22f));
        Object var8 = RING2;
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            arg0.drawString(arg1, arg2, (arg3 + var11[0]), (arg4 + var11[1]), var6, false);
            var10++;
        }
        var8 = RING1;
        var9 = var8.length;
        var10 = 0;
        while (var10 < var9) {
            var11 = var8[var10];
            arg0.drawString(arg1, arg2, (arg3 + var11[0]), (arg4 + var11[1]), var7, false);
            var10++;
        }
    }

    private static int col(float arg0) {
        int var1 = Math.max(0, Math.min(255, Math.round((arg0 * 255.0f))));
        return ((((var1 << 24) | 12124160) | 33280) | 255);
    }

    static {
        var tmp0 = new float[][8];
        var tmp1 = new float[2];
        tmp1[0] = 1.5f;
        tmp1[1] = 0.0f;
        tmp0[0] = tmp1;
        var tmp2 = new float[2];
        tmp2[0] = -1.5f;
        tmp2[1] = 0.0f;
        tmp0[1] = tmp2;
        var tmp3 = new float[2];
        tmp3[0] = 0.0f;
        tmp3[1] = 1.5f;
        tmp0[2] = tmp3;
        var tmp4 = new float[2];
        tmp4[0] = 0.0f;
        tmp4[1] = -1.5f;
        tmp0[3] = tmp4;
        var tmp5 = new float[2];
        tmp5[0] = 1.1f;
        tmp5[1] = 1.1f;
        tmp0[4] = tmp5;
        var tmp6 = new float[2];
        tmp6[0] = -1.1f;
        tmp6[1] = 1.1f;
        tmp0[5] = tmp6;
        var tmp7 = new float[2];
        tmp7[0] = 1.1f;
        tmp7[1] = -1.1f;
        tmp0[6] = tmp7;
        var tmp8 = new float[2];
        tmp8[0] = -1.1f;
        tmp8[1] = -1.1f;
        tmp0[7] = tmp8;
        RING1 = tmp0;
        var tmp9 = new float[][8];
        var tmp10 = new float[2];
        tmp10[0] = 3.2f;
        tmp10[1] = 0.0f;
        tmp9[0] = tmp10;
        var tmp11 = new float[2];
        tmp11[0] = -3.2f;
        tmp11[1] = 0.0f;
        tmp9[1] = tmp11;
        var tmp12 = new float[2];
        tmp12[0] = 0.0f;
        tmp12[1] = 3.2f;
        tmp9[2] = tmp12;
        var tmp13 = new float[2];
        tmp13[0] = 0.0f;
        tmp13[1] = -3.2f;
        tmp9[3] = tmp13;
        var tmp14 = new float[2];
        tmp14[0] = 2.3f;
        tmp14[1] = 2.3f;
        tmp9[4] = tmp14;
        var tmp15 = new float[2];
        tmp15[0] = -2.3f;
        tmp15[1] = 2.3f;
        tmp9[5] = tmp15;
        var tmp16 = new float[2];
        tmp16[0] = 2.3f;
        tmp16[1] = -2.3f;
        tmp9[6] = tmp16;
        var tmp17 = new float[2];
        tmp17[0] = -2.3f;
        tmp17[1] = -2.3f;
        tmp9[7] = tmp17;
        RING2 = tmp9;
    }

}
