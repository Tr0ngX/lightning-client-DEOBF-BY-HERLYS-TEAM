package lightning.gui;

import java.awt.Color;
import java.util.List;
import lightning.LightningClient;
import lightning.gui.api.Button;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.ModuleButton;
import lightning.gui.impl.NumberButton;
import lightning.managers.FontManager;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ColorGui {
    private static Frame cur;
    private static int curIdx;
    private static int curRgb;
    private static String curName;
    private static float hx1;
    private static float hy1;
    private static float hx2;
    private static float hy2;
    private static final int[] CK;
    private static final Color[][] CV;
    private static int cn;
    private static boolean triCache;
    private static long triStamp;
    private static final long[] cntStamp;
    private static final int[] cntActive;
    private static final int[] cntTotal;
    private static final String[] cntText;

    private static int catRgb(String string) {
        Object var1 = string;
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1932423455 -> { break label_2; }
            case 2074527 -> { break label_7; }
            case 2366700 -> { break label_6; }
            case 426458062 -> { break label_5; }
            case 678949039 -> { break label_4; }
            case 1185082643 -> { break label_3; }
            case 1993470708 -> { break label_1; }
            default -> { break label_8; }
        }
        label_1:
        if (!(var1.equals("COMBAT"))) break label_8;
        var2 = 0;
        break label_8;
        label_2:
        if (!(var1.equals("PLAYER"))) break label_8;
        var2 = 1;
        break label_8;
        label_3:
        if (!(var1.equals("VISUALS"))) break label_8;
        var2 = 2;
        break label_8;
        label_4:
        if (!(var1.equals("MOVEMENT"))) break label_8;
        var2 = 3;
        break label_8;
        label_5:
        if (!(var1.equals("MISCELLANEOUS"))) break label_8;
        var2 = 4;
        break label_8;
        label_6:
        if (!(var1.equals("MISC"))) break label_8;
        var2 = 5;
        break label_8;
        label_7:
        if (!(var1.equals("CORE"))) break label_8;
        var2 = 6;
        label_8:
        switch (var2) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_11; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_13; }
            case 6 -> { break label_14; }
            default -> { break label_15; }
        }
        label_9:
        return 16602749;
        label_10:
        return 11695099;
        label_11:
        return 5421563;
        label_12:
        return 4061877;
        label_13:
        return 16760914;
        label_14:
        return 11713276;
        label_15:
        return 11695099;
    }

    private static Color col(int n, int n2) {
        if (n2 < 0) {
            n2 = 0;
        }
        if (n2 > 255) {
            n2 = 255;
        }
        int i = 0;
        while (i < cn) {
            if (CK[i] != n) {
            } else {
                Color color = CV[i][n2];
                if (color == null) {
                    var tmp0 = new Color(((n >> 16) & 255), ((n >> 8) & 255), (n & 255), n2);
                    color = tmp0;
                    CV[i][n2] = tmp0;
                }
                return color;
            }
            i++;
        }
        if (cn < CK.length) {
            CK[cn] = n;
            CV[cn] = new Color[256];
            cn = (cn + 1);
            return col(n, n2);
        }
        return new Color(((n >> 16) & 255), ((n >> 8) & 255), (n & 255), n2);
    }

    private static Color col(int n, int n2, int n3, int n4) {
        return col((((n << 16) | (n2 << 8)) | n3), n4);
    }

    private static float clamp01(float f) {
        if (f < 0.0f) {
            break label_3;
        }
        return (f > 1.0f ? 1.0f : f);
    }

    private static float easeOut(float f) {
        float f2 = (1.0f - clamp01(f));
        return (1.0f - ((f2 * f2) * f2));
    }

    private static float easeBack(float f) {
        f = clamp01(f);
        float f2 = 1.70158f;
        float f3 = (f2 + 1.0f);
        float f4 = (f - 1.0f);
        return ((1.0f + (((f3 * f4) * f4) * f4)) + ((f2 * f4) * f4));
    }

    private static long openTime() {
        try {
            ClickGuiScreen clickGuiScreen = LightningClient.CLICK_GUI;
            if (clickGuiScreen != null) {
                if (clickGuiScreen.isClosing()) {
                }
                return 100000L;
                try {
                }
                long l = (System.currentTimeMillis() - clickGuiScreen.getSlideAnim().getStartTime());
                return (l < 0L ? 0L : l);
            } catch (Throwable ex8) {
                Throwable throwable = ex8;
                return 100000L;
            }
        }
    }

    private static Frame findFrame(float f, float f2) {
        try {
            ClickGuiScreen clickGuiScreen = LightningClient.CLICK_GUI;
            if (clickGuiScreen == null) {
                return null;
                try {
                }
                int n = Math.round(f);
                int n2 = Math.round(f2);
                int n3 = 0;
                Object var6 = clickGuiScreen.getFrames().iterator();
                while (var6.hasNext()) {
                    Frame frame = ((Frame) var6.next());
                    Frame frame2 = frame;
                    if (frame2.getX() == n && frame2.getY() == n2) {
                        curIdx = n3;
                        return frame2;
                        try {
                        }
                        n3++;
                    }
                } catch (Throwable ex17) {
                    clickGuiScreen = ex17;
                }
                return null;
            }
        }
    }

    private static int frameIndex(Frame frame) {
        try {
            int n = LightningClient.CLICK_GUI.getFrames().indexOf(frame);
            return (n < 0 ? 0 : n);
        } catch (Throwable ex4) {
            Throwable throwable = ex4;
            return 0;
        }
    }

    private static boolean triangles() {
        long l = System.currentTimeMillis();
        if ((l - triStamp) <= 400L) break label_8;
        triStamp = l;
        try {
            ClickGuiModule clickGuiModule = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
            triCache = clickGuiModule.triangles.getValue();
        } catch (Throwable ex6) {
            Throwable throwable = ex6;
            triCache = false;
        }
        label_8:
        return triCache;
    }

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        hx1 = f;
        hy1 = f2;
        hx2 = f3;
        hy2 = f4;
        cur = findFrame(f, f2);
        if (cur != null && cur.getCategory() != null) {
            curName = cur.getCategory().name();
            curRgb = catRgb(curName);
        } else {
            cur = null;
            curName = "";
            curRgb = 11695099;
        }
        LightningSkin.drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 6.0f, true, true, false, false, col(26, 25, 40, 244), col(19, 18, 30, 244));
        LightningSkin.drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 6.0f, true, true, false, false, col(curRgb, 4), col(curRgb, 52));
        long l = openTime();
        long l2 = (l - (((long) curIdx) * 100L));
        if (l2 >= 100000L) {
        } else {
        }
        float f5 = easeOut(((((float) l2) - 100.0f) / 650.0f));
        float f6 = ((f3 - f) * f5);
        if (f6 <= 1.0f) break label_36;
        int n = 4;
        float f7 = (f6 / ((float) n));
        int n2 = blend(curRgb, 16777215, 0.4f);
        int i = 0;
        while (i < n) {
            int n3 = blend(curRgb, n2, (((float) i) / ((float) (n - 1))));
            float f9 = (f + (f7 * ((float) i)));
            if (i == (n - 1)) {
            } else {
            }
            float f8 = ((f + (f7 * ((float) (i + 1)))) + 0.6f);
            float f10 = f8;
            if (i == 0) {
                LightningSkin.drawRoundedRect(guiGraphicsExtractor, f9, f2, f8, (f2 + 1.2f), 3.0f, true, false, false, false, col(n3, 255), col(n3, 255));
            } else {
                Renderer2D.renderQuad(guiGraphicsExtractor, f9, f2, f8, (f2 + 1.2f), col(n3, 255));
            }
            i++;
        }
        label_36:
    }

    private static int blend(int n, int n2, float f) {
        int n3 = ((n >> 16) & 255);
        int n4 = ((n >> 8) & 255);
        int n5 = (n & 255);
        int n6 = ((n2 >> 16) & 255);
        int n7 = ((n2 >> 8) & 255);
        int n8 = (n2 & 255);
        int n9 = Math.round((((float) n3) + (((float) (n6 - n3)) * f)));
        int n10 = Math.round((((float) n4) + (((float) (n7 - n4)) * f)));
        int n11 = Math.round((((float) n5) + (((float) (n8 - n5)) * f)));
        return (((n9 << 16) | (n10 << 8)) | n11);
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 6.0f, false, false, true, true, col(19, 18, 29, 238), col(15, 14, 23, 240));
    }

    public static void panelOutline(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = (f3 - f);
        float f6 = (f4 - f2);
        if ((f5 < 4.0f) || (f6 < 4.0f)) {
            return;
        }
        if (cur != null) {
            LightningSkin.ring(guiGraphicsExtractor, f, f2, f3, f4, Math.min(6.0f, (Math.min(f5, f6) / 2.0f)), 2.0f, col(curRgb, 255));
            LightningSkin.ring(guiGraphicsExtractor, (f - 1.0f), (f2 - 1.0f), (f3 + 1.0f), (f4 + 1.0f), 7.0f, 3.0f, col(curRgb, 95));
            LightningSkin.ring(guiGraphicsExtractor, (f - 3.0f), (f2 - 3.0f), (f3 + 3.0f), (f4 + 3.0f), 9.0f, 4.0f, col(curRgb, 38));
        } else {
            LightningSkin.ring(guiGraphicsExtractor, f, f2, f3, f4, Math.min(6.0f, (Math.min(f5, f6) / 2.0f)), 0.6f, col(255, 255, 255, 40));
        }
    }

    private static void spaced(FontManager fm, GuiGraphicsExtractor g, String s, int x, int y, int sp, int rgbFrom, int rgbTo, int alpha) {
        int len = s.length();
        float cx = ((float) x);
        int i = 0;
        while (i < len) {
            String ch = String.valueOf(s.charAt(i));
            if (len <= 1) {
            } else {
            }
            float t = (((float) i) / ((float) (len - 1)));
            fm.drawText(g, ch, Math.round(cx), y, col(blend(rgbFrom, rgbTo, t), alpha));
            cx = (cx + ((float) (fm.getWidth(ch) + sp)));
            i++;
        }
    }

    private static int spacedWidth(FontManager fm, String s, int sp) {
        int w = 0;
        int i = 0;
        while (i < s.length()) {
            w = (w + (fm.getWidth(String.valueOf(s.charAt(i))) + sp));
            i++;
        }
        return Math.max(0, (w - sp));
    }

    public static void header(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        var tmp0 = (string == null ? "" : string);
        String string2 = tmp0;
        String string3 = tmp0;
        if ((string2.isEmpty()) || (Character.isLetterOrDigit(string2.charAt(0)))) break label_9;
        int n4 = string2.indexOf(32);
        if (n4 >= 0) {
            break label_8;
        }
        label_8:
        string2 = string2;
        long l = string2;
        label_9:
        var tmp1 = string2.trim().toUpperCase();
        string2 = tmp1;
        if (!(tmp1.equals("MISCELLANEOUS"))) break label_11;
        string2 = "MISC";
        label_11:
        float f3 = (hy2 - hy1);
        l = openTime();
        long l2 = (l - (((long) curIdx) * 100L));
        int n5 = curRgb;
        float f4 = Math.min(16.0f, (f3 - 4.0f));
        float f5 = (hx1 + 5.0f);
        float f6 = ((hy1 + ((f3 - f4) / 2.0f)) + 1.0f);
        float f7 = (f5 + (f4 / 2.0f));
        float f8 = (f6 + (f4 / 2.0f));
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, f5, f6, (f5 + f4), (f6 + f4), 5.0f, col(n5, 0));
        LightningSkin.ring(guiGraphicsExtractor, f5, f6, (f5 + f4), (f6 + f4), 5.0f, 0.6f, col(n5, 0));
        if (l2 >= 100000L) {
        } else {
        }
        var tmp2 = easeBack(((((float) l2) - 200.0f) / 500.0f));
        float f2 = tmp2;
        float f = tmp2;
        if (cur != null && f2 > 0.02f) {
            drawIcon(guiGraphicsExtractor, curName, f7, f8, (((f4 / 16.0f) * 0.95f) * f2), n5);
            break label_30;
        }
        if (cur != null) break label_30;
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, (f7 - 2.5f), (f8 - 2.5f), (f7 + 2.5f), (f8 + 2.5f), 1.5f, col(n5, 255));
        label_30:
        int sp = 1;
        int titleX = Math.round(((f5 + f4) + 7.0f));
        int titleY = Math.round((hy1 + ((f3 - ((float) fontManager.getHeight())) / 2.0f)));
        int titleW = spacedWidth(fontManager, string2, sp);
        int light = blend(n5, 16777215, 0.3f);
        spaced(fontManager, guiGraphicsExtractor, string2, (titleX - 1), titleY, sp, n5, n5, 85);
        spaced(fontManager, guiGraphicsExtractor, string2, (titleX + 1), titleY, sp, n5, n5, 85);
        spaced(fontManager, guiGraphicsExtractor, string2, titleX, (titleY - 1), sp, n5, n5, 85);
        spaced(fontManager, guiGraphicsExtractor, string2, titleX, (titleY + 1), sp, n5, n5, 85);
        spaced(fontManager, guiGraphicsExtractor, string2, titleX, titleY, sp, 16777215, light, 255);
        if (cur != null && (hx2 - (((float) (titleX + titleW)) + 6.0f)) > 1.0E9f) {
            float dotCx = (((float) (titleX + titleW)) + 6.0f);
            float dotCy = (hy1 + (f3 / 2.0f));
            float pulse = ((float) ((Math.sin(((((double) System.currentTimeMillis()) / 450.0) + ((double) curIdx))) * 0.5) + 0.5));
            LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, (dotCx - 3.5f), (dotCy - 3.5f), (dotCx + 3.5f), (dotCy + 3.5f), 3.5f, col(n5, (30 + Math.round((40.0f * pulse)))));
            LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, (dotCx - 1.7f), (dotCy - 1.7f), (dotCx + 1.7f), (dotCy + 1.7f), 1.7f, col(light, 255));
        }
        int n8 = 0;
        String string4 = null;
        if (cur == null) break label_69;
        int n3 = Math.min(curIdx, 31);
        long l3 = System.currentTimeMillis();
        if (cntText[n3] != null) {
            if ((l3 - cntStamp[n3]) <= 300L) break label_67;
        }
        cntStamp[n3] = l3;
        int n9 = 0;
        int n10 = 0;
        try {
            List list = LightningClient.MODULE_MANAGER.getModules(cur.getCategory());
            float t = list.iterator();
            while (t.hasNext()) {
                Module object = ((Module) t.next());
                n10++;
                if (!(object.isToggled())) {
                    break label_57;
                }
                n9++;
            }
        } catch (Throwable ex63) {
            list = ex63;
        }
        cntActive[n3] = n9;
        cntTotal[n3] = n10;
        cntText[n3] = ("(" + n9 + "/" + n10 + ")");
        label_67:
        n8 = cntTotal[n3];
        string4 = cntText[n3];
        label_69:
        if (n8 <= 0) break label_80;
        int tw = fontManager.getWidth(string4);
        float right = ((hx2 - 6.0f) - (triangles() ? 11.0f : 0.0f));
        float left = ((right - ((float) tw)) - 9.0f);
        float top = ((hy1 + ((f3 - 12.0f) / 2.0f)) + 1.0f);
        if (left <= ((float) ((titleX + titleW) + 4))) break label_80;
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, left, top, right, (top + 12.0f), 6.0f, col(n5, 0));
        LightningSkin.ring(guiGraphicsExtractor, left, top, right, (top + 12.0f), 6.0f, 0.6f, col(n5, 0));
        fontManager.drawText(guiGraphicsExtractor, string4, Math.round((left + 4.5f)), Math.round(((top + ((12.0f - ((float) fontManager.getHeight())) / 2.0f)) + 0.5f)), col(light, 255));
        label_80:
        if (cur == null) break label_95;
        if (l2 >= 100000L) {
        } else {
        }
        float ua = clamp01(((((float) l2) - 300.0f) / 400.0f));
        float ux0 = (hx1 + 3.0f);
        float ux1 = (hx2 - 3.0f);
        int segs = 18;
        float seg = ((ux1 - ux0) / ((float) segs));
        int i = 0;
        while (i < segs) {
            t = (((float) i) / ((float) (segs - 1)));
            int a = Math.round(((230.0f * (1.0f - t)) * ua));
            if (a <= 0) {
            } else {
                Renderer2D.renderQuad(guiGraphicsExtractor, (ux0 + (seg * ((float) i))), (hy2 - 1.0f), ((ux0 + (seg * ((float) (i + 1)))) + 0.5f), hy2, col(n5, a));
            }
            i++;
        }
        label_95:
        float f12 = clamp01(((((float) l2) - 350.0f) / 900.0f));
        if (((l2 >= 100000L) || (f12 <= 0.0f)) || (f12 >= 1.0f)) break label_113;
        float f13 = (hx2 - hx1);
        float f9 = (hx1 + (f13 * ((f12 * 1.9f) - 0.6f)));
        float f14 = (f13 * 0.4f);
        float f15 = Math.max(hx1, f9);
        float f16 = Math.min(hx2, (f9 + f14));
        if (f16 <= f15) break label_113;
        float f17 = (f14 / 3.0f);
        var tmp3 = new float[4];
        tmp3[0] = f9;
        tmp3[1] = (f9 + f17);
        tmp3[2] = (f9 + (2.0f * f17));
        tmp3[3] = (f9 + f14);
        float[] fArray = tmp3;
        var tmp4 = new int[3];
        tmp4[0] = 14;
        tmp4[1] = 40;
        tmp4[2] = 14;
        int[] nArray = tmp4;
        i = 0;
        while (i < 3) {
            float f10 = Math.max(hx1, fArray[i]);
            float f11 = Math.min(hx2, fArray[(i + 1)]);
            if (f11 <= f10) {
            } else {
                Renderer2D.renderQuad(guiGraphicsExtractor, f10, (hy1 + 3.0f), f11, hy2, col(255, 255, 255, nArray[i]));
            }
            i++;
        }
        label_113:
    }

    private static void drawRoundedBox(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, float f7, Color color) {
        Renderer2D.renderQuad(guiGraphicsExtractor, (f + (f4 * f3)), (f2 + (f5 * f3)), (f + (f6 * f3)), (f2 + (f7 * f3)), color);
    }

    private static void drawRoundedBoxDual(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, float f7, Color color) {
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, (f + (f4 * f3)), (f2 + (f5 * f3)), (f + (f6 * f3)), (f2 + (f7 * f3)), Math.max(0.5f, (((f6 - f4) * f3) / 2.0f)), color);
    }

    private static void drawIcon(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, float f3, int n) {
        Color color = col(n, 255);
        Color color2 = col(30, 28, 48, 255);
        Object var8 = string;
        int var9 = -1;
        switch (var8.hashCode()) {
            case -1932423455 -> { break label_4; }
            case 2074527 -> { break label_9; }
            case 2366700 -> { break label_8; }
            case 426458062 -> { break label_7; }
            case 678949039 -> { break label_6; }
            case 1185082643 -> { break label_5; }
            case 1993470708 -> { break label_3; }
            default -> { break label_10; }
        }
        label_3:
        if (!(var8.equals("COMBAT"))) break label_10;
        var9 = 0;
        break label_10;
        label_4:
        if (!(var8.equals("PLAYER"))) break label_10;
        var9 = 1;
        break label_10;
        label_5:
        if (!(var8.equals("VISUALS"))) break label_10;
        var9 = 2;
        break label_10;
        label_6:
        if (!(var8.equals("MOVEMENT"))) break label_10;
        var9 = 3;
        break label_10;
        label_7:
        if (!(var8.equals("MISCELLANEOUS"))) break label_10;
        var9 = 4;
        break label_10;
        label_8:
        if (!(var8.equals("MISC"))) break label_10;
        var9 = 5;
        break label_10;
        label_9:
        if (!(var8.equals("CORE"))) break label_10;
        var9 = 6;
        label_10:
        switch (var9) {
            case 0 -> { break label_11; }
            case 1 -> { break label_19; }
            case 2 -> { break label_22; }
            case 3 -> { break label_26; }
            case 4 -> { break label_35; }
            case 5 -> { break label_35; }
            case 6 -> { break label_40; }
            default -> { break label_49; }
        }
        label_11:
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -8.0f, -1.0f, -5.0f, 2.5f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -5.0f, -2.0f, 0.0f, 1.5f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 0.0f, -1.4f, 8.0f, 0.6f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 6.6f, -3.0f, 8.0f, -1.4f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -3.0f, 1.5f, -1.0f, 5.5f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 0.5f, 1.5f, 3.0f, 5.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 1.6f, 4.5f, 4.2f, 6.5f, color);
        break label_50;
        label_19:
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, -2.6f, -7.0f, 2.6f, -1.8f, color);
        LightningSkin.drawRoundedRect(guiGraphicsExtractor, (f - (5.2f * f3)), (f2 - (0.6f * f3)), (f + (5.2f * f3)), (f2 + (7.0f * f3)), (3.2f * f3), true, true, false, false, color, color);
        break label_50;
        label_22:
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, (f - (7.5f * f3)), (f2 - (3.8f * f3)), (f + (7.5f * f3)), (f2 + (3.8f * f3)), (3.8f * f3), color);
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, -3.0f, -3.0f, 3.0f, 3.0f, color2);
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, -1.4f, -1.4f, 1.4f, 1.4f, color);
        break label_50;
        label_26:
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, 0.5f, -7.5f, 4.2f, -3.8f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -1.5f, -3.0f, 1.8f, 2.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -5.5f, -2.5f, -1.5f, -1.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 1.8f, -1.2f, 5.5f, 0.4f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -4.5f, 2.0f, -1.0f, 3.6f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -5.8f, 3.6f, -4.2f, 7.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 1.0f, 2.0f, 4.5f, 3.6f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 3.0f, 3.6f, 4.6f, 7.0f, color);
        break label_50;
        label_35:
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -6.5f, -6.5f, -0.6f, -0.6f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 0.6f, -6.5f, 6.5f, -0.6f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -6.5f, 0.6f, -0.6f, 6.5f, color);
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, 0.6f, 0.6f, 6.5f, 6.5f, color);
        break label_50;
        label_40:
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, 0.5f, -8.0f, 5.0f, -6.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -1.0f, -6.0f, 3.5f, -4.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -2.5f, -4.0f, 2.0f, -2.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -4.0f, -2.0f, 4.0f, 0.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -0.5f, 0.0f, 3.0f, 2.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -2.0f, 2.0f, 2.0f, 4.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -3.5f, 4.0f, 0.5f, 6.0f, color);
        drawRoundedBox(guiGraphicsExtractor, f, f2, f3, -5.0f, 6.0f, -1.0f, 8.0f, color);
        break label_50;
        label_49:
        drawRoundedBoxDual(guiGraphicsExtractor, f, f2, f3, -3.0f, -3.0f, 3.0f, 3.0f, color2);
        label_50:
    }

    public static void renderModule(ModuleButton moduleButton, GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        boolean bl = moduleButton.isHovering(((double) n), ((double) n2));
        if (!bl) break label_4;
        var tmp0 = LightningClient.CLICK_GUI.getDescriptionFrame();
        Object object3 = tmp0;
        if (!(((DescriptionFrame) tmp0).getDescription().isEmpty())) break label_4;
        ((DescriptionFrame) object3).setDescription(moduleButton.getDescription());
        label_4:
        object3 = LightningClient.FONT_MANAGER;
        Module module = moduleButton.getModule();
        boolean bl2 = module.isToggled();
        float f2 = moduleButton.getFillAnim().get((!(!bl2) ? 1.0f : 0.0f));
        int n11 = moduleButton.getPadding();
        int n12 = (moduleButton.getX() + n11);
        int n13 = ((moduleButton.getX() + moduleButton.getWidth()) - n11);
        int n14 = (moduleButton.getY() + 1);
        int n15 = ((moduleButton.getY() + moduleButton.getHeight()) - 1);
        Frame frame = moduleButton.getParent();
        int n16 = curRgb;
        int n17 = 0;
        if (frame != null) {
            if (frame.getCategory() != null) {
                n16 = catRgb(frame.getCategory().name());
            }
            n17 = frameIndex(frame);
        }
        long l = openTime();
        float f3 = 1.0f;
        if (l < 100000L) {
            int n10 = 0;
            if (frame == null) break label_32;
            n10 = Math.round((((float) ((moduleButton.getY() - frame.getY()) - frame.getHeight())) / ((float) Math.max(1, moduleButton.getHeight()))));
            if (n10 < 0) {
                n10 = 0;
            }
            if (n10 <= 12) break label_32;
            n10 = 12;
            label_32:
            f3 = easeOut((((((float) (l - (((long) n17) * 100L))) - 250.0f) - (((float) n10) * 55.0f)) / 350.0f));
        }
        n10 = 0;
        if (f3 < 0.999f) {
            n10 = Math.round((-8.0f * (1.0f - f3)));
        }
        n12 = (n12 + n10);
        n13 = (n13 + n10);
        int n18 = Math.round((255.0f * f3));
        int n19 = Math.round(((!(!bl) ? 24.0f : 11.0f) * f3));
        if (f2 < 0.999f) {
            LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, ((float) n12), ((float) n14), ((float) n13), ((float) n15), 4.0f, col(255, 255, 255, n19));
        }
        if (f2 <= 0.001f) break label_52;
        var tmp1 = (n12 + Math.round((((float) (n13 - n12)) * f2)));
        int n9 = tmp1;
        if ((tmp1 - n12) < 4) break label_52;
        LightningSkin.drawRoundedRect(guiGraphicsExtractor, ((float) n12), ((float) n14), ((float) n9), ((float) n15), 4.0f, true, true, true, true, col(n16, Math.round(((!(!bl) ? 88.0f : 78.0f) * f3))), col(n16, Math.round(((!(!bl) ? 68.0f : 58.0f) * f3))));
        col(n16, n18);
        (((float) n15) - 2.0f);
        (((float) n12) + 2.0f);
        (((float) n14) + 2.0f);
        ((float) n12);
        label_52:
        ModuleOutline.draw(guiGraphicsExtractor, ((float) n12), ((float) n14), ((float) n13), ((float) n15), n16, bl2, bl, f3);
        if (f2 > 0.02f) {
            float ga = (f2 * f3);
            int bright = blend(n16, 16777215, 0.08f);
            LightningSkin.ring(guiGraphicsExtractor, (((float) n12) - 1.0f), (((float) n14) - 1.0f), (((float) n13) + 1.0f), (((float) n15) + 1.0f), 5.0f, 2.2f, col(n16, Math.round((85.0f * ga))));
            LightningSkin.ring(guiGraphicsExtractor, ((float) n12), ((float) n14), ((float) n13), ((float) n15), 4.0f, 1.0f, col(bright, Math.round(((!(!bl) ? 255.0f : 235.0f) * ga))));
        }
        String string = module.getName();
        int n20 = Math.round(((float) ((FontManager) object3).getHeight()));
        int n21 = (moduleButton.getY() + Math.round((((float) (moduleButton.getHeight() - ((FontManager) object3).getHeight())) / 2.0f)));
        int n22 = ((n12 + 6) + Math.round((3.0f * f2)));
        Color color = lerpColor(col((!(!bl) ? 215 : 186), (!(!bl) ? 212 : 182), (!(!bl) ? 235 : 208), n18), col(255, 255, 255, n18), f2);
        String string2 = moduleButton.getSearchQuery();
        if (string2 != null && !(string2.isEmpty()) && string.toLowerCase().contains(string2.toLowerCase())) {
            int n8 = string.toLowerCase().indexOf(string2.toLowerCase());
            String string3 = string.substring(0, n8);
            String string4 = string.substring(n8, (n8 + string2.length()));
            String string5 = string.substring((n8 + string2.length()));
            int n7 = n22;
            if (!(string3.isEmpty())) {
                NeonFx.glowText(((FontManager) object3), guiGraphicsExtractor, string3, n7, n21, color, n16);
                n7 = (n7 + ((FontManager) object3).getWidth(string3));
            }
            int n6 = ((FontManager) object3).getWidth(string4);
            LightningSkin.fillQuad(guiGraphicsExtractor, ((float) (n7 - 1)), ((float) (n21 - 1)), ((float) ((n7 + n6) + 1)), ((float) (n21 + n20)), col(n16, 150));
            NeonFx.glowText(((FontManager) object3), guiGraphicsExtractor, string4, n7, n21, Color.WHITE, n16);
            n7 = (n7 + n6);
            if (!(string5.isEmpty())) {
                NeonFx.glowText(((FontManager) object3), guiGraphicsExtractor, string5, n7, n21, color, n16);
            }
        } else {
            NeonFx.glowText(((FontManager) object3), guiGraphicsExtractor, string, n22, n21, color, n16);
        }
        n8 = (n22 + ((FontManager) object3).getWidth(string));
        int n23 = (n13 - 5);
        int n24 = 18;
        int n25 = 9;
        n7 = (n23 - n24);
        n6 = (moduleButton.getY() + ((moduleButton.getHeight() - n25) / 2));
        if (triangles() && !(moduleButton.getButtons().isEmpty())) {
            Object object2 = (moduleButton.isOpen() ? "\u25b2" : "\u25bc");
            int n26 = ((FontManager) object3).getWidth(((String) object2));
            if (moduleButton.isOpen()) {
            } else {
            }
            NeonFx.glowText(guiGraphicsExtractor, ((String) object2), ((n7 - 6) - n26), n21, col(n16, n18), col(255, 255, 255, Math.round((120.0f * f3))), n16);
            n23 = (((n7 - 6) - n26) - 4);
        } else {
            n23 = (n7 - 6);
        }
        if (f2 < 0.01f) {
            break label_109;
        }
        if (f2 > 0.99f) {
            break label_109;
        }
        label_109:
        object2 = lerpColor(col(46, 45, 66, n18), col(n16, n18), f2);
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, ((float) n7), ((float) n6), ((float) (n7 + n24)), ((float) (n6 + n25)), (((float) n25) / 2.0f), ((Color) object2));
        float f4 = ((((float) n7) + 1.5f) + (f2 * ((float) (n24 - n25))));
        if (f2 < 0.01f) {
            break label_115;
        }
        if (f2 > 0.99f) {
            break label_115;
        }
        label_115:
        Color color2 = lerpColor(col(122, 120, 148, n18), col(19, 18, 29, n18), f2);
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, f4, (((float) n6) + 1.5f), (f4 + (((float) n25) - 3.0f)), (((float) (n6 + n25)) - 1.5f), ((((float) n25) - 3.0f) / 2.0f), color2);
        int n27 = module.getBind();
        if (n27 == 0) break label_126;
        int n4 = n23;
        var tmp2 = KeyboardUtils.getKeyName(n27).toUpperCase();
        String object = tmp2;
        var tmp3 = ((FontManager) object3).getWidth(tmp2);
        int n3 = tmp3;
        var tmp4 = ((n23 - tmp3) - 6);
        int n5 = tmp4;
        if (tmp4 < (n8 + 6)) break label_126;
        float f5 = (((float) moduleButton.getY()) + ((((float) moduleButton.getHeight()) - 11.0f) / 2.0f));
        LightningSkin.drawRoundedRectAll(guiGraphicsExtractor, ((float) n5), f5, ((float) n4), (f5 + 11.0f), 3.0f, col(n16, Math.round((44.0f * f3))));
        ((FontManager) object3).drawText(guiGraphicsExtractor, object, (n5 + 3), Math.round(((f5 + ((11.0f - ((float) ((FontManager) object3).getHeight())) / 2.0f)) + 0.5f)), col(n16, n18));
        label_126:
        if (moduleButton.getRevealHeight() > 0) {
            int n28 = (moduleButton.getY() + moduleButton.getHeight());
            guiGraphicsExtractor.enableScissor(moduleButton.getX(), n28, (moduleButton.getX() + moduleButton.getWidth()), (n28 + moduleButton.getRevealHeight()));
            Object e = moduleButton.getButtons().iterator();
            while (e.hasNext()) {
                e = e.next();
                Button button = ((Button) e);
                if (!(button.isVisible())) {
                    if ((button instanceof BooleanButton)) {
                        ((BooleanButton) button).setPrevVisible(false);
                        break label_130;
                    }
                    if (!((button instanceof NumberButton))) {
                        break label_130;
                    }
                    ((NumberButton) button).setPrevVisible(false);
                    break label_130;
                }
                button.render(guiGraphicsExtractor, n, n2, f);
                if (!(button.isHovering(((double) n), ((double) n2)))) break label_130;
                var tmp5 = LightningClient.CLICK_GUI.getDescriptionFrame();
                DescriptionFrame descriptionFrame = tmp5;
                if (!(tmp5.getDescription().isEmpty())) {
                    break label_130;
                }
                descriptionFrame.setDescription(button.getDescription());
            }
            guiGraphicsExtractor.disableScissor();
            break label_155;
        }
        f5 = moduleButton.getButtons().iterator();
        while (f5.hasNext()) {
            e = f5.next();
            button = ((Button) e);
            if ((button instanceof BooleanButton)) {
                ((BooleanButton) button).setPrevVisible(false);
                break label_147;
            }
            if (!((button instanceof NumberButton))) {
                break label_147;
            }
            ((NumberButton) button).setPrevVisible(false);
        }
        label_155:
    }

    private static Color lerpColor(Color color, Color color2, float f) {
        f = clamp01(f);
        return new Color(Math.round((((float) color.getRed()) + (((float) (color2.getRed() - color.getRed())) * f))), Math.round((((float) color.getGreen()) + (((float) (color2.getGreen() - color.getGreen())) * f))), Math.round((((float) color.getBlue()) + (((float) (color2.getBlue() - color.getBlue())) * f))), Math.round((((float) color.getAlpha()) + (((float) (color2.getAlpha() - color.getAlpha())) * f))));
    }

    public static Color catAccent(int n) {
        return new Color(curRgb);
    }

    public static void resetCur() {
        curRgb = 11695099;
        cur = null;
    }

    static {
        curRgb = 11695099;
        curName = "";
        CK = new int[48];
        CV = new Color[][48];
        cn = 0;
        cntStamp = new long[32];
        cntActive = new int[32];
        cntTotal = new int[32];
        cntText = new String[32];
    }

}
