package lightning.gui;

import java.awt.Color;
import java.util.Arrays;
import java.util.Random;
import lightning.managers.FontManager;
import lightning.utils.graphics.GuiQuadRenderState;
import lightning.utils.graphics.MenuShader;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;

public final class LightningSkin {
    private static final int VR = 181;
    private static final int VG = 139;
    private static final int VB = 255;
    private static final int AR = 56;
    private static final int AG = 242;
    private static final int AB = 196;
    private static final float PR = 4.5f;
    private static final float RR = 3.5f;
    private static final float STEP = 0.5f;
    private static final Color PANEL;
    private static final Color ROW;
    private static final float[] PX;
    private static final float[] PY;
    private static final long T0;
    static float timeOverride;
    private static float[] BX;
    private static float[] BY;
    private static int[] BC;
    private static int bn;

    public static Color accent(int n) {
        float f = (((float) (Math.abs(n) % 480)) / 240.0f);
        if (f <= 1.0f) {
        } else {
        }
        float f2 = (2.0f - f);
        return mix(f2, 255);
    }

    private static int lerp(int n, int n2, float f) {
        return Math.round((((float) n) + (((float) (n2 - n)) * f)));
    }

    private static Color mix(float f, int n) {
        return new Color(lerp(181, 56, f), lerp(139, 242, f), lerp(255, 196, f), n);
    }

    private static Color volt(int n) {
        return new Color(181, 139, 255, n);
    }

    private static Color arc(int n) {
        return new Color(56, 242, 196, n);
    }

    private static Color lerpC(Color color, Color color2, float f) {
        f = Math.max(0.0f, Math.min(1.0f, f));
        return new Color(lerp(color.getRed(), color2.getRed(), f), lerp(color.getGreen(), color2.getGreen(), f), lerp(color.getBlue(), color2.getBlue(), f), lerp(color.getAlpha(), color2.getAlpha(), f));
    }

    private static void bufferQuadGradient(float f, float f2, float f3, float f4, int n, int n2) {
        if ((f3 <= f) || (f4 <= f2)) {
            return;
        }
        if ((bn + 4) > BX.length) {
            int n3 = (BX.length * 2);
            BX = Arrays.copyOf(BX, n3);
            BY = Arrays.copyOf(BY, n3);
            BC = Arrays.copyOf(BC, n3);
        }
        BX[bn] = f;
        BY[bn] = f2;
        BC[bn] = n;
        var tmp0 = (bn + 1);
        bn = tmp0;
        BX[tmp0] = f;
        BY[bn] = f4;
        BC[bn] = n;
        var tmp1 = (bn + 1);
        bn = tmp1;
        BX[tmp1] = f3;
        BY[bn] = f4;
        BC[bn] = n2;
        var tmp2 = (bn + 1);
        bn = tmp2;
        BX[tmp2] = f3;
        BY[bn] = f2;
        BC[bn] = n2;
        bn = (bn + 1);
    }

    private static void bufferQuadSolid(float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        bufferQuadGradient(f, f2, f3, f4, n, n);
    }

    private static void flush(GuiGraphicsExtractor guiGraphicsExtractor) {
        if (!bn) return;
        int n = bn;
        bn = 0;
        GuiRenderState guiRenderState = guiGraphicsExtractor.night$getGuiRenderState();
        guiRenderState.addGuiElement(new GuiQuadRenderState(new Matrix3x2f(guiGraphicsExtractor.pose()), Arrays.copyOf(BX, n), Arrays.copyOf(BY, n), Arrays.copyOf(BC, n), Renderer2D.peekScissor(guiGraphicsExtractor)));
    }

    private static void strip(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, Color color, Color color2) {
        if ((f2 <= f) || (f4 <= f3)) {
            return;
        }
        if (color == color2) {
            bufferQuadSolid(f, f3, f2, f4, color);
            return;
        }
        float f7 = (f6 - f5);
        bufferQuadGradient(f, f3, f2, f4, lerpC(color, color2, ((f - f5) / f7)).getRGB(), lerpC(color, color2, ((f2 - f5) / f7)).getRGB());
    }

    private static float inset(float f, float f2) {
        float f3 = ((f * f) - (f2 * f2));
        return (f - ((float) Math.sqrt(((double) Math.max(0.0f, f3)))));
    }

    static void drawRoundedRect(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, boolean bl, boolean bl2, boolean bl3, boolean bl4, Color color, Color color2) {
        float f11 = (f3 - f);
        float f12 = (f4 - f2);
        if (f11 <= 0.0f) break label_3;
        if (f12 > 0.0f) break label_4;
        label_3:
        return;
        label_4:
        f5 = Math.min(f5, (Math.min(f11, f12) / 2.0f));
        if (!bl) {
            if (!(!bl2)) {
            }
        } else {
        }
        float f13 = 0.0f;
        if (!bl4) {
            if (!(!bl3)) {
            }
        } else {
        }
        float f10 = 0.0f;
        float f14 = 0.0f;
        if (f5 < 0.05f) {
            strip(guiGraphicsExtractor, f, f3, f2, f4, f, f3, color, color2);
            flush(guiGraphicsExtractor);
            return;
        }
        float f9 = f2;
        while (f9 < ((f2 + f13) - 0.001f)) {
            float f8 = Math.min((f9 + 0.5f), (f2 + f13));
            float f7 = ((f2 + f5) - ((f9 + f8) / 2.0f));
            float f6 = inset(f5, f7);
            strip(guiGraphicsExtractor, (f + (!(!bl) ? f6 : 0.0f)), (f3 - (!(!bl2) ? f6 : 0.0f)), f9, f8, f, f3, color, color2);
            f9 = (f9 + 0.5f);
        }
        strip(guiGraphicsExtractor, f, f3, (f2 + f13), (f4 - f10), f, f3, color, color2);
        f9 = (f4 - f10);
        while (f9 < (f4 - 0.001f)) {
            f8 = Math.min((f9 + 0.5f), f4);
            f7 = (((f9 + f8) / 2.0f) - (f4 - f5));
            f6 = inset(f5, f7);
            strip(guiGraphicsExtractor, (f + (!(!bl4) ? f6 : 0.0f)), (f3 - (!(!bl3) ? f6 : 0.0f)), f9, f8, f, f3, color, color2);
            f9 = (f9 + 0.5f);
        }
        flush(guiGraphicsExtractor);
    }

    static void drawRoundedRectAll(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color) {
        drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, f5, true, true, true, true, color, color);
    }

    static void ring(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, Color color) {
        float f13 = (f3 - f);
        float f14 = (f4 - f2);
        if (f13 <= 0.0f) break label_3;
        if (f14 <= 0.0f) {
            label_3:
            return;
        }
        var tmp0 = Math.min(f5, (Math.min(f13, f14) / 2.0f));
        f5 = tmp0;
        if (tmp0 < 0.05f) {
            Renderer2D.renderOutline(guiGraphicsExtractor, f, f2, f3, f4, color);
            return;
        }
        float f15 = Math.max(0.0f, (f5 - f6));
        float f12 = f2;
        while (f12 < ((f2 + f5) - 0.001f)) {
            float f11 = Math.min((f12 + 0.5f), (f2 + f5));
            float f10 = ((f12 + f11) / 2.0f);
            float f9 = ((f2 + f5) - f10);
            float f8 = inset(f5, f9);
            if (f10 >= (f2 + f6)) {
                if (f9 >= f15) {
                }
                bufferQuadSolid((f + f8), f12, (f3 - f8), f11, color);
            } else {
                float f7 = (f5 - ((float) Math.sqrt(((double) Math.max(0.0f, ((f15 * f15) - (f9 * f9)))))));
                bufferQuadSolid((f + f8), f12, (f + f7), f11, color);
                bufferQuadSolid((f3 - f7), f12, (f3 - f8), f11, color);
            }
            f12 = (f12 + 0.5f);
        }
        f12 = (f4 - f5);
        while (f12 < (f4 - 0.001f)) {
            f11 = Math.min((f12 + 0.5f), f4);
            f10 = ((f12 + f11) / 2.0f);
            f9 = (f10 - (f4 - f5));
            f8 = inset(f5, f9);
            if (f10 <= (f4 - f6)) {
                if (f9 >= f15) {
                }
                bufferQuadSolid((f + f8), f12, (f3 - f8), f11, color);
            } else {
                f7 = (f5 - ((float) Math.sqrt(((double) Math.max(0.0f, ((f15 * f15) - (f9 * f9)))))));
                bufferQuadSolid((f + f8), f12, (f + f7), f11, color);
                bufferQuadSolid((f3 - f7), f12, (f3 - f8), f11, color);
            }
            f12 = (f12 + 0.5f);
        }
        if ((f4 - f5) > (f2 + f5)) {
            bufferQuadSolid(f, (f2 + f5), (f + f6), (f4 - f5), color);
            bufferQuadSolid((f3 - f6), (f2 + f5), f3, (f4 - f5), color);
        }
        flush(guiGraphicsExtractor);
    }

    public static void fillQuad(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = (f3 - f);
        float f6 = (f4 - f2);
        if (!(color.getAlpha())) return;
        if (f6 <= 1.01f && f5 > 20.0f) return;
        if (f5 >= 4.0f && f6 >= 4.0f) {
            drawRoundedRectAll(guiGraphicsExtractor, f, f2, f3, f4, Math.min(3.5f, (Math.min(f5, f6) / 2.0f)), color);
        } else {
            Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, f4, color);
        }
    }

    public static void drawOutline(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        glow(guiGraphicsExtractor, f, f2, f3, f4, color);
        ColorGui.panelOutline(guiGraphicsExtractor, f, f2, f3, f4, color);
    }

    public static void glow(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, Color c) {
        glow(g, x1, y1, x2, y2, 4.5f, c);
    }

    public static void glow(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float r, Color c) {
        float t = (((float) (System.currentTimeMillis() - T0)) / 1000.0f);
        float pulse = (0.85f + (0.15f * ((float) Math.sin(((double) (t * 2.0f))))));
        int layers = 8;
        int i = layers;
        while (i >= 1) {
            float k = (1.0f - ((((float) i) - 1.0f) / ((float) layers)));
            int a = Math.round((((120.0f * k) * k) * pulse));
            if (a <= 0) {
            } else {
                ring(g, (x1 - ((float) i)), (y1 - ((float) i)), (x2 + ((float) i)), (y2 + ((float) i)), (r + ((float) i)), 1.0f, new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
            }
            i--;
        }
    }

    public static void drawFrameTop(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, true, true, false, false, color, color);
    }

    public static void drawFrameBottom(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, false, false, true, true, color, color);
    }

    public static void drawNoOp(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    public static void bolt(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, int n) {
        boltFlat(guiGraphicsExtractor, f, f2, f3, RefSkin.pick(n), n);
    }

    private static void boltFlat(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, Color color, int n) {
        int n2 = Math.max(4, Math.round((f3 * 2.0f)));
        float f4 = (f3 / 1029.0f);
        int i = 0;
        while (i < n2) {
            float f7 = (125.0f + (((((float) i) + 0.5f) / ((float) n2)) * 1029.0f));
            float[] fArray = new float[8];
            int n4 = 0;
            int j = 0;
            while (j < 7) {
                int n3 = ((j + 1) % 7);
                float f6 = PY[j];
                float f5 = PY[n3];
                if (f6 <= f7) {
                    if (f5 > f7) break label_14;
                }
                if (f5 > f7) break label_15;
                if (f6 > f7) break label_14;
                break label_15;
                label_14:
                n4++;
                fArray[n4] = (PX[j] + (((f7 - f6) * (PX[n3] - PX[j])) / (f5 - f6)));
                label_15:
                j++;
            }
            if (n4 < 2) {
                break label_44;
            }
            if (fArray[0] > fArray[1]) {
                float f8 = fArray[0];
                fArray[0] = fArray[1];
                fArray[1] = f8;
            }
            if (n4 < 4) break label_33;
            if (fArray[2] > fArray[3]) {
                float f9 = fArray[2];
                fArray[2] = fArray[3];
                fArray[3] = f9;
            }
            if (fArray[0] <= fArray[2]) break label_33;
            float f10 = fArray[0];
            fArray[0] = fArray[2];
            fArray[2] = f10;
            f10 = fArray[1];
            fArray[1] = fArray[3];
            fArray[3] = f10;
            label_33:
            if (color != null) {
            } else {
            }
            Color color2 = mix((((float) i) / ((float) (n2 - 1))), n);
            n3 = 0;
            f6 = (f3 / ((float) n2));
            while ((n3 + 1) < n4) {
                f5 = (f + ((fArray[n3] - 351.0f) * f4));
                float f11 = (f + ((fArray[(n3 + 1)] - 351.0f) * f4));
                bufferQuadSolid(f5, (f2 + (((float) i) * f6)), f11, (f2 + (((float) (i + 1)) * f6)), color2);
                n3 += 2;
            }
            label_44:
            i++;
        }
        flush(guiGraphicsExtractor);
    }

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, true, true, false, false, PANEL, PANEL);
        float f5 = (f + 2.0f);
        float f6 = (f3 - 2.0f);
        float f7 = (f2 + 1.5f);
        float f8 = (f4 - 1.5f);
        float f9 = Math.min(4.0f, ((f8 - f7) / 2.0f));
        drawRoundedRect(guiGraphicsExtractor, f5, f7, f6, f8, f9, true, true, true, true, volt(120), arc(60));
        ring(guiGraphicsExtractor, f5, f7, f6, f8, f9, 0.5f, mix(0.35f, 150));
        float f10 = ((f8 - f7) - 2.5f);
        float f11 = ((f6 - 1.2f) - f10);
        float f12 = (f7 + (((f8 - f7) - f10) / 2.0f));
        drawRoundedRect(guiGraphicsExtractor, f11, f12, (f11 + f10), (f12 + f10), (f10 / 2.0f), true, true, true, true, volt(255), arc(255));
        float f13 = (f10 * 0.68f);
        boltFlat(guiGraphicsExtractor, (f11 + ((f10 - (f13 * 0.561f)) / 2.0f)), (f12 + ((f10 - f13) / 2.0f)), f13, new Color(12, 12, 12, 235), 255);
    }

    public static void headerLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        drawRoundedRect(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, false, false, true, true, PANEL, PANEL);
    }

    public static void idleRow(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        drawRoundedRectAll(guiGraphicsExtractor, f, (f2 + 0.5f), f3, f4, 3.5f, ROW);
    }

    public static void chargeFill(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = (f3 - f);
        if (f5 <= 0.0f) return;
        float f6 = (f2 + 0.5f);
        float f7 = (f4 - f6);
        drawRoundedRect(guiGraphicsExtractor, f, f6, f3, f4, 3.5f, true, true, true, true, new Color(127, 127, 127, 175), new Color(127, 127, 127, 80));
        drawRoundedRect(guiGraphicsExtractor, (f + 0.5f), (f6 + 0.5f), (f3 - 0.5f), (f6 + (f7 / 2.0f)), 3.0f, true, true, false, false, new Color(255, 255, 255, 30), new Color(255, 255, 255, 6));
        if (f5 > 6.0f) {
            ring(guiGraphicsExtractor, f, f6, f3, f4, 3.5f, 0.5f, mix(0.2f, 190));
            float f8 = Math.min(6.0f, (f7 - 3.0f));
            drawRoundedRect(guiGraphicsExtractor, (f + 0.45f), (f6 + ((f7 - f8) / 2.0f)), (f + 1.25f), (f6 + ((f7 + f8) / 2.0f)), 0.4f, true, true, true, true, new Color(255, 255, 255, 235), new Color(255, 255, 255, 235));
        }
    }

    public static void rowLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    private static float hash(float f) {
        float f2 = (((float) Math.sin(((double) ((f * 127.1f) + 311.7f)))) * 43758.547f);
        return (f2 - ((float) Math.floor(((double) f2))));
    }

    private static void boltStep(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, int n, float f4, float f5) {
        int n2 = Math.max(4, Math.round((f3 / f4)));
        float f6 = (f3 / 1029.0f);
        float f7 = (f3 / ((float) n2));
        int i = 0;
        while (i < n2) {
            float f10 = (125.0f + (((((float) i) + 0.5f) / ((float) n2)) * 1029.0f));
            float[] fArray = new float[8];
            int n4 = 0;
            int j = 0;
            while (j < 7) {
                int n3 = ((j + 1) % 7);
                float f9 = PY[j];
                float f8 = PY[n3];
                if (f9 <= f10) {
                    if (f8 > f10) break label_15;
                }
                if (f8 > f10) break label_16;
                if (f9 > f10) break label_15;
                break label_16;
                label_15:
                n4++;
                fArray[n4] = (PX[j] + (((f10 - f9) * (PX[n3] - PX[j])) / (f8 - f9)));
                label_16:
                j++;
            }
            if (n4 < 2) {
                break label_41;
            }
            if (fArray[0] > fArray[1]) {
                float f11 = fArray[0];
                fArray[0] = fArray[1];
                fArray[1] = f11;
            }
            if (n4 < 4) break label_34;
            if (fArray[2] > fArray[3]) {
                float f12 = fArray[2];
                fArray[2] = fArray[3];
                fArray[3] = f12;
            }
            if (fArray[0] <= fArray[2]) break label_34;
            float f13 = fArray[0];
            fArray[0] = fArray[2];
            fArray[2] = f13;
            f13 = fArray[1];
            fArray[1] = fArray[3];
            fArray[3] = f13;
            label_34:
            Color color = mix((((float) i) / ((float) (n2 - 1))), n);
            n3 = 0;
            while ((n3 + 1) < n4) {
                f9 = ((f + ((fArray[n3] - 351.0f) * f6)) - f5);
                f8 = ((f + ((fArray[(n3 + 1)] - 351.0f) * f6)) + f5);
                Renderer2D.renderQuad(guiGraphicsExtractor, f9, (f2 + (((float) i) * f7)), f8, (f2 + (((float) (i + 1)) * f7)), color);
                n3 += 2;
            }
            label_41:
            i++;
        }
    }

    private static void strike(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, long l, float f, float f2) {
        Random random = new Random(((l * 7919L) + 13L));
        float f3 = (((float) n) * (0.1f + (0.8f * random.nextFloat())));
        float f4 = (((float) n2) * (0.55f + (0.3f * random.nextFloat())));
        int n4 = 18;
        float[] fArray = new float[(n4 + 1)];
        float[] fArray2 = new float[(n4 + 1)];
        int n3 = 0;
        while (n3 <= n4) {
            fArray2[n3] = ((f4 * ((float) n3)) / ((float) n4));
            fArray[n3] = f3;
            f3 = (f3 + (((random.nextFloat() - 0.5f) * ((float) n2)) * 0.075f));
            n3++;
        }
        boltPath(guiGraphicsExtractor, fArray, fArray2, f, f2, 1.0f);
        n3 = (3 + random.nextInt(8));
        float f5 = (random.nextBoolean() ? 1.0f : -1.0f);
        int n5 = 7;
        float[] fArray3 = new float[(n5 + 1)];
        float[] fArray4 = new float[(n5 + 1)];
        fArray3[0] = fArray[n3];
        fArray4[0] = fArray2[n3];
        int i = 1;
        while (i <= n5) {
            fArray3[i] = (fArray3[(i - 1)] + ((f5 * ((float) n2)) * (0.02f + (0.03f * random.nextFloat()))));
            fArray4[i] = (fArray4[(i - 1)] + (((float) n2) * (0.02f + (0.03f * random.nextFloat()))));
            i++;
        }
        boltPath(guiGraphicsExtractor, fArray3, fArray4, f, f2, 0.55f);
    }

    private static void boltPath(GuiGraphicsExtractor guiGraphicsExtractor, float[] fArray, float[] fArray2, float f, float f2, float f3) {
        float f4 = (2.0f * f3);
        int i = 0;
        while (i < (fArray.length - 1)) {
            float f5 = fArray2[i];
            float f6 = fArray2[(i + 1)];
            float f7 = fArray[i];
            float f8 = fArray[(i + 1)];
            int n = Math.max(1, ((int) Math.ceil(((double) ((f6 - f5) / 2.0f)))));
            int j = 0;
            while (j < n) {
                float f9 = (((float) j) / ((float) n));
                float f10 = (f7 + ((f8 - f7) * f9));
                float f11 = (f5 + ((f6 - f5) * f9));
                Color color = new Color(56, 242, 196, Math.round(((46.0f * f) * f2)));
                Color color2 = new Color(245, 240, 255, Math.round(((235.0f * f) * f2)));
                Renderer2D.renderQuad(guiGraphicsExtractor, (f10 - (f4 * 3.0f)), f11, (f10 + (f4 * 3.0f)), (f11 + 2.0f), color);
                Renderer2D.renderQuad(guiGraphicsExtractor, (f10 - (f4 * 0.5f)), f11, (f10 + (f4 * 0.5f)), (f11 + 2.0f), color2);
                j++;
            }
            i++;
        }
    }

    public static void menuFx(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        try {
            float f2 = Math.max(0.0f, Math.min(1.0f, f));
            if (timeOverride < 0.0f) break label_2;
            break label_3;
            label_2:
            label_3:
            float f3 = (((float) (System.currentTimeMillis() - T0)) / 1000.0f);
            float f4 = 4.2f;
            long l = ((long) Math.floor(((double) (f3 / f4))));
            float f5 = (f3 - (((float) l) * f4));
            float f6 = ((float) (Math.exp(((double) (-(f5) * 6.0f))) + (0.5 * Math.exp(((double) (-(Math.abs((f5 - 0.25f))) * 25.0f))))));
            if (f5 > 1.5f) {
                f6 = 0.0f;
            }
            f6 = Math.min(1.0f, f6);
            Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, ((float) n), ((float) n2), new Color(6, 4, 20, Math.round((165.0f * f2))));
            Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, ((float) n), ((float) n2), new Color(56, 242, 196, Math.round((40.0f * f2))), new Color(181, 139, 255, Math.round((34.0f * f2))));
            float f7 = (((float) n2) * 0.84f);
            float f8 = ((((float) n) * 0.5f) - ((f7 * 0.561f) * 0.5f));
            float f9 = (((float) n2) * 0.08f);
            float f10 = (0.5f + (0.5f * ((float) Math.sin(((double) (f3 * 0.9f))))));
            boltStep(guiGraphicsExtractor, f8, f9, f7, Math.round((((20.0f + (18.0f * f10)) + (120.0f * f6)) * f2)), 2.0f, 5.0f);
            boltStep(guiGraphicsExtractor, f8, f9, f7, Math.round((((34.0f + (26.0f * f10)) + (110.0f * f6)) * f2)), 2.0f, 0.0f);
            if (f5 < 0.55f) {
                float f11 = (0.65f + (0.35f * ((float) (((int) (f5 * 22.0f)) % 2))));
                strike(guiGraphicsExtractor, n, n2, l, (f11 * (1.0f - ((f5 / 0.55f) * 0.35f))), f2);
            }
            if (f6 > 0.01f) {
                Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, ((float) n), ((float) n2), new Color(200, 190, 255, Math.round(((70.0f * f6) * f2))));
            }
            int i = 0;
            while (i < 38) {
                float f12 = ((((hash((((float) i) * 1.3f)) * ((float) n)) + (((float) Math.sin(((double) ((f3 * 0.35f) + ((float) i))))) * 14.0f)) + ((float) n)) % ((float) n));
                float f13 = (8.0f + (20.0f * hash(((((float) i) * 2.7f) + 5.0f))));
                float f14 = (((float) n2) - (((f3 * f13) + (hash((((float) i) * 4.1f)) * ((float) n2))) % ((float) n2)));
                float f15 = (1.0f + (1.6f * hash((((float) i) * 9.9f))));
                int n3 = Math.round(((50.0f + (150.0f * (0.5f + (0.5f * ((float) Math.sin(((double) ((f3 * 2.4f) + (((float) i) * 1.7f))))))))) * f2));
                if ((i % 3) == 0) {
                } else {
                }
                Renderer2D.renderQuad(f12, f14, (f12 + f15), (f14 + f15), volt(n3), arc(n3));
                i++;
            }
        } catch (Throwable ex36) {
            f2 = ex36;
        }
    }

    public static void signature(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        int n3 = 7;
        int n4 = Math.min(255, (color.getAlpha() + 70));
        int n5 = fontManager.getWidth(string);
        int n6 = fontManager.getHeight();
        fontManager.drawTextWithShadow(guiGraphicsExtractor, string, n, n3, new Color(181, 139, 255, n4));
        Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, ((float) n), ((float) ((n3 + n6) + 1)), ((float) (n + n5)), ((float) ((n3 + n6) + 2)), volt(n4), arc(n4));
        float f = (((float) n6) + 3.0f);
        boltStep(guiGraphicsExtractor, ((((float) n) - 5.0f) - (f * 0.561f)), (((float) n3) - 1.0f), f, n4, 1.0f, 0.0f);
        boltStep(guiGraphicsExtractor, (((float) (n + n5)) + 5.0f), (((float) n3) - 1.0f), f, n4, 1.0f, 0.0f);
    }

    public static void skipText(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
    }

    public static void screenBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        MenuShader.render(guiGraphicsExtractor, Math.round(f3), Math.round(f4), 1.0f, 1.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, f4, new Color(5, 5, 5, 95));
    }

    public static void topLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, (f4 + 1.0f), accent(((int) f)));
    }

    static {
        PANEL = new Color(8, 8, 8, 222);
        ROW = new Color(24, 24, 24, 150);
        var tmp0 = new float[7];
        tmp0[0] = 752.0f;
        tmp0[1] = 853.0f;
        tmp0[2] = 666.0f;
        tmp0[3] = 928.0f;
        tmp0[4] = 477.0f;
        tmp0[5] = 589.0f;
        tmp0[6] = 351.0f;
        PX = tmp0;
        var tmp1 = new float[7];
        tmp1[0] = 125.0f;
        tmp1[1] = 125.0f;
        tmp1[2] = 539.0f;
        tmp1[3] = 539.0f;
        tmp1[4] = 1154.0f;
        tmp1[5] = 690.0f;
        tmp1[6] = 690.0f;
        PY = tmp1;
        T0 = System.currentTimeMillis();
        timeOverride = -1.0f;
        BX = new float[1024];
        BY = new float[1024];
        BC = new int[1024];
        bn = 0;
    }

}
