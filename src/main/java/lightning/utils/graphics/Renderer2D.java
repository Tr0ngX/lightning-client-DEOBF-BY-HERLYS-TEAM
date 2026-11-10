package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import lightning.LightningClient;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import lightning.utils.IMinecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer2D implements IMinecraft {
    public static final Matrix4f LAST_PROJECTION_MATRIX;
    public static final Matrix4f LAST_MODEL_MATRIX;
    public static final Matrix4f LAST_WORLD_MATRIX;
    private static final Map TEXTURE_TYPES;
    private static MethodHandle SCISSOR_GET;
    private static MethodHandle SCISSOR_PEEK;
    private static boolean scissorHandleInitialized;

    public static void renderQuad(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        int var6 = arg5.getRGB();
        var tmp0 = new float[4];
        tmp0[0] = arg1;
        tmp0[1] = arg1;
        tmp0[2] = arg3;
        tmp0[3] = arg3;
        var tmp1 = new float[4];
        tmp1[0] = arg2;
        tmp1[1] = arg4;
        tmp1[2] = arg4;
        tmp1[3] = arg2;
        var tmp2 = new int[4];
        tmp2[0] = var6;
        tmp2[1] = var6;
        tmp2[2] = var6;
        tmp2[3] = var6;
        submit(arg0, tmp0, tmp1, tmp2);
    }

    public static void renderGradient(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5, Color arg6) {
        int var7 = arg5.getRGB();
        int var8 = arg6.getRGB();
        var tmp0 = new float[4];
        tmp0[0] = arg1;
        tmp0[1] = arg1;
        tmp0[2] = arg3;
        tmp0[3] = arg3;
        var tmp1 = new float[4];
        tmp1[0] = arg2;
        tmp1[1] = arg4;
        tmp1[2] = arg4;
        tmp1[3] = arg2;
        var tmp2 = new int[4];
        tmp2[0] = var7;
        tmp2[1] = var8;
        tmp2[2] = var8;
        tmp2[3] = var7;
        submit(arg0, tmp0, tmp1, tmp2);
    }

    public static void renderSidewaysGradient(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5, Color arg6) {
        int var7 = arg5.getRGB();
        int var8 = arg6.getRGB();
        var tmp0 = new float[4];
        tmp0[0] = arg1;
        tmp0[1] = arg1;
        tmp0[2] = arg3;
        tmp0[3] = arg3;
        var tmp1 = new float[4];
        tmp1[0] = arg2;
        tmp1[1] = arg4;
        tmp1[2] = arg4;
        tmp1[3] = arg2;
        var tmp2 = new int[4];
        tmp2[0] = var7;
        tmp2[1] = var7;
        tmp2[2] = var8;
        tmp2[3] = var8;
        submit(arg0, tmp0, tmp1, tmp2);
    }

    public static void renderCheckerboard(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, float arg5, Color arg6, Color arg7) {
        int var9 = arg6.getRGB();
        int var10 = arg7.getRGB();
        int var11 = ((int) Math.ceil(((double) ((arg3 - arg1) / arg5))));
        var tmp0 = ((int) Math.ceil(((double) ((arg4 - arg2) / arg5))));
        int var8 = tmp0;
        int var12 = (var11 * tmp0);
        if (var12 <= 0) return;
        Object var13 = new float[(var12 * 4)];
        Object var14 = new float[(var12 * 4)];
        Object var15 = new int[(var12 * 4)];
        int var16 = 0;
        int var17 = 0;
        while (var17 < var8) {
            float var18 = (arg2 + (((float) var17) * arg5));
            float var19 = Math.min((arg2 + (((float) (var17 + 1)) * arg5)), arg4);
            int var20 = 0;
            while (var20 < var11) {
                float var21 = (arg1 + (((float) var20) * arg5));
                float var22 = Math.min((arg1 + (((float) (var20 + 1)) * arg5)), arg3);
                int var23 = (((var17 + var20) % 2) == 0 ? var9 : var10);
                var16++;
                quad(var13, var14, var15, var16, var21, var18, var22, var19, var23);
                var20++;
            }
            var17++;
        }
        submit(arg0, var13, var14, var15);
    }

    public static void renderOutline(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        int var6 = arg5.getRGB();
        Object var7 = new float[16];
        Object var8 = new float[16];
        Object var9 = new int[16];
        quad(var7, var8, var9, 0, arg1, arg2, (arg1 + 0.5f), arg4, var6);
        quad(var7, var8, var9, 1, (arg3 - 0.5f), arg2, arg3, arg4, var6);
        quad(var7, var8, var9, 2, arg1, (arg4 - 0.5f), arg3, arg4, var6);
        quad(var7, var8, var9, 3, arg1, arg2, arg3, (arg2 + 0.5f), var6);
        submit(arg0, var7, var8, var9);
    }

    public static ScreenRectangle peekScissor(GuiGraphicsExtractor arg0) {
        if (scissorHandleInitialized) break label_3;
        scissorHandleInitialized = true;
        try {
            Object var1 = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
            var1.setAccessible(true);
            SCISSOR_GET = MethodHandles.lookup().unreflectGetter(var1);
            Object var2 = var1.getType().getDeclaredMethod("peek", new Class[0]);
            var2.setAccessible(true);
            SCISSOR_PEEK = MethodHandles.lookup().unreflect(var2);
        } catch (Throwable ex2) {
            var1 = ex2;
            LightningClient.LOGGER.debug("Could not bind scissorStack handle: {}", var1.getMessage());
        }
        label_3:
        if ((SCISSOR_GET == null) || (SCISSOR_PEEK == null)) break label_8;
        try {
            var1 = SCISSOR_GET.invoke(arg0);
            if (var1 != null) return SCISSOR_PEEK.invoke(var1);
        } catch (Throwable ex7) {
            var1 = ex7;
        }
        label_8:
        return null;
    }

    private static void submit(GuiGraphicsExtractor arg0, float[] arg1, float[] arg2, int[] arg3) {
        Object var4 = ((GuiGraphicsExtractorAccessor) arg0).night$getGuiRenderState();
        var4.addGuiElement(new GuiQuadRenderState(new Matrix3x2f(arg0.pose()), arg1, arg2, arg3, peekScissor(arg0)));
    }

    private static void quad(float[] arg0, float[] arg1, int[] arg2, int arg3, float arg4, float arg5, float arg6, float arg7, int arg8) {
        int var9 = (arg3 * 4);
        arg0[var9] = arg4;
        arg1[var9] = arg5;
        arg0[(var9 + 1)] = arg4;
        arg1[(var9 + 1)] = arg7;
        arg0[(var9 + 2)] = arg6;
        arg1[(var9 + 2)] = arg7;
        arg0[(var9 + 3)] = arg6;
        arg1[(var9 + 3)] = arg5;
        int var10 = arg8;
        arg2[(var9 + 3)] = var10;
        arg2[(var9 + 2)] = var10;
        arg2[(var9 + 1)] = var10;
        arg2[var9] = var10;
    }

    public static void renderImmediateQuad(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        Object var6 = arg0.last().pose();
        int var7 = arg5.getRGB();
        Object var8 = ByteBufferBuilder.exactlySized((4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var9 = new BufferBuilder(var8, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            var9.addVertex(var6, arg1, arg2, 0.0f).setColor(var7);
            var9.addVertex(var6, arg1, arg4, 0.0f).setColor(var7);
            var9.addVertex(var6, arg3, arg4, 0.0f).setColor(var7);
            var9.addVertex(var6, arg3, arg2, 0.0f).setColor(var7);
            Object var10 = var9.build();
            Renderer3D.draw(RenderTypes.debugQuads(), var10);
            if (var8 == null) break label_7;
            var8.close();
        } catch (Throwable ex2) {
            var9 = ex2;
            if (var8 == null) break label_6;
            try {
                var8.close();
            } catch (Throwable ex5) {
                var10 = ex5;
                var9.addSuppressed(var10);
            }
            label_6:
            throw var9;
        }
        label_7:
    }

    public static void renderCircle(PoseStack arg0, float arg1, float arg2, float arg3, Color arg4) {
        Object var5 = arg0.last().pose();
        int var6 = arg4.getRGB();
        Object var7 = ByteBufferBuilder.exactlySized((361 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var8 = new BufferBuilder(var7, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            int var9 = 0;
            while (var9 <= 360) {
                var8.addVertex(var5, ((float) (((double) arg1) + (Math.sin(((((double) var9) * 3.141592653589793) / 180.0)) * ((double) arg3)))), ((float) (((double) arg2) + (Math.cos(((((double) var9) * 3.141592653589793) / 180.0)) * ((double) arg3)))), 0.0f).setColor(var6);
                var9++;
            }
            var9 = var8.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), var9);
            if (var7 == null) break label_9;
            var7.close();
        } catch (Throwable ex4) {
            var8 = ex4;
            if (var7 == null) break label_8;
            try {
                var7.close();
            } catch (Throwable ex7) {
                var9 = ex7;
                var8.addSuppressed(var9);
            }
            label_8:
            throw var8;
        }
        label_9:
    }

    public static void renderTexture(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Identifier arg5, Color arg6) {
        Object var7 = arg0.last().pose();
        int var8 = arg6.getRGB();
        Object var9 = ByteBufferBuilder.exactlySized((4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
        try {
            Object var10 = new BufferBuilder(var9, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            var10.addVertex(var7, arg1, arg2, 0.0f).setUv(0.0f, 0.0f).setColor(var8);
            var10.addVertex(var7, arg1, arg4, 0.0f).setUv(0.0f, 1.0f).setColor(var8);
            var10.addVertex(var7, arg3, arg4, 0.0f).setUv(1.0f, 1.0f).setColor(var8);
            var10.addVertex(var7, arg3, arg2, 0.0f).setUv(1.0f, 0.0f).setColor(var8);
            Object var11 = var10.build();
            Renderer3D.draw(texturedType(arg5), var11);
            if (var9 == null) break label_7;
            var9.close();
        } catch (Throwable ex2) {
            var10 = ex2;
            if (var9 == null) break label_6;
            try {
                var9.close();
            } catch (Throwable ex5) {
                var11 = ex5;
                var10.addSuppressed(var11);
            }
            label_6:
            throw var10;
        }
        label_7:
    }

    public static void renderArrow(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        int var6 = arg5.getRGB();
        float var7 = (arg2 + (arg4 * 0.75f));
        var tmp0 = new float[8];
        tmp0[0] = arg1;
        tmp0[1] = (arg1 - arg3);
        tmp0[2] = arg1;
        tmp0[3] = arg1;
        tmp0[4] = arg1;
        tmp0[5] = arg1;
        tmp0[6] = (arg1 + arg3);
        tmp0[7] = arg1;
        Object var8 = tmp0;
        var tmp1 = new float[8];
        tmp1[0] = arg2;
        tmp1[1] = (arg2 + arg4);
        tmp1[2] = var7;
        tmp1[3] = arg2;
        tmp1[4] = arg2;
        tmp1[5] = var7;
        tmp1[6] = (arg2 + arg4);
        tmp1[7] = arg2;
        Object var9 = tmp1;
        var tmp2 = new int[8];
        tmp2[0] = var6;
        tmp2[1] = var6;
        tmp2[2] = var6;
        tmp2[3] = var6;
        tmp2[4] = var6;
        tmp2[5] = var6;
        tmp2[6] = var6;
        tmp2[7] = var6;
        Object var10 = tmp2;
        submit(arg0, var8, var9, var10);
    }

    public static void renderArrowOutline(GuiGraphicsExtractor arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        int var6 = arg5.getRGB();
        float var7 = (arg2 + (arg4 * 0.75f));
        Object var8 = new float[16];
        Object var9 = new float[16];
        Object var10 = new int[16];
        quadLine(var8, var9, var10, 0, arg1, arg2, (arg1 - arg3), (arg2 + arg4), 0.5f, var6);
        quadLine(var8, var9, var10, 1, (arg1 - arg3), (arg2 + arg4), arg1, var7, 0.5f, var6);
        quadLine(var8, var9, var10, 2, arg1, var7, (arg1 + arg3), (arg2 + arg4), 0.5f, var6);
        quadLine(var8, var9, var10, 3, (arg1 + arg3), (arg2 + arg4), arg1, arg2, 0.5f, var6);
        submit(arg0, var8, var9, var10);
    }

    private static void quadLine(float[] arg0, float[] arg1, int[] arg2, int arg3, float arg4, float arg5, float arg6, float arg7, float arg8, int arg9) {
        float var10 = (arg6 - arg4);
        float var11 = (arg7 - arg5);
        float var12 = ((float) Math.sqrt(((double) ((var10 * var10) + (var11 * var11)))));
        if (var12 < 1.0E-4f) return;
        float var13 = ((-(var11) / var12) * arg8);
        float var14 = ((var10 / var12) * arg8);
        int var15 = (arg3 * 4);
        arg0[var15] = (arg4 + var13);
        arg1[var15] = (arg5 + var14);
        arg0[(var15 + 1)] = (arg4 - var13);
        arg1[(var15 + 1)] = (arg5 - var14);
        arg0[(var15 + 2)] = (arg6 - var13);
        arg1[(var15 + 2)] = (arg7 - var14);
        arg0[(var15 + 3)] = (arg6 + var13);
        arg1[(var15 + 3)] = (arg7 + var14);
        int var16 = arg9;
        arg2[(var15 + 3)] = var16;
        arg2[(var15 + 2)] = var16;
        arg2[(var15 + 1)] = var16;
        arg2[var15] = var16;
    }

    public static void renderArrow(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        Object var6 = arg0.last().pose();
        int var7 = arg5.getRGB();
        Object var8 = ByteBufferBuilder.exactlySized((5 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var9 = new BufferBuilder(var8, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            var9.addVertex(var6, arg1, arg2, 0.0f).setColor(var7);
            var9.addVertex(var6, (arg1 - arg3), (arg2 + arg4), 0.0f).setColor(var7);
            var9.addVertex(var6, arg1, (arg2 + arg4), 0.0f).setColor(var7);
            var9.addVertex(var6, (arg1 + arg3), (arg2 + arg4), 0.0f).setColor(var7);
            var9.addVertex(var6, arg1, arg2, 0.0f).setColor(var7);
            Object var10 = var9.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), var10);
            if (var8 == null) break label_7;
            var8.close();
        } catch (Throwable ex2) {
            var9 = ex2;
            if (var8 == null) break label_6;
            try {
                var8.close();
            } catch (Throwable ex5) {
                var10 = ex5;
                var9.addSuppressed(var10);
            }
            label_6:
            throw var9;
        }
        label_7:
    }

    public static void renderArrowOutline(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        Object var6 = arg0.last().pose();
        int var7 = arg5.getRGB();
        var tmp0 = new float[5];
        tmp0[0] = arg1;
        tmp0[1] = (arg1 - arg3);
        tmp0[2] = arg1;
        tmp0[3] = (arg1 + arg3);
        tmp0[4] = arg1;
        Object var8 = tmp0;
        var tmp1 = new float[5];
        tmp1[0] = arg2;
        tmp1[1] = (arg2 + arg4);
        tmp1[2] = (arg2 + arg4);
        tmp1[3] = (arg2 + arg4);
        tmp1[4] = arg2;
        Object var9 = tmp1;
        Object var10 = ByteBufferBuilder.exactlySized((((var8.length - 1) * 4) * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var11 = new BufferBuilder(var10, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            int var12 = 0;
            while (var12 < (var8.length - 1)) {
                segment(var11, var6, var8[var12], var9[var12], var8[(var12 + 1)], var9[(var12 + 1)], 0.5f, var7);
                var12++;
            }
            var12 = var11.build();
            Renderer3D.draw(RenderTypes.debugQuads(), var12);
            if (var10 == null) break label_9;
            var10.close();
        } catch (Throwable ex4) {
            var11 = ex4;
            if (var10 == null) break label_8;
            try {
                var10.close();
            } catch (Throwable ex7) {
                var12 = ex7;
                var11.addSuppressed(var12);
            }
            label_8:
            throw var11;
        }
        label_9:
    }

    private static void segment(BufferBuilder arg0, Matrix4f arg1, float arg2, float arg3, float arg4, float arg5, float arg6, int arg7) {
        float var8 = (arg4 - arg2);
        float var9 = (arg5 - arg3);
        float var10 = ((float) Math.sqrt(((double) ((var8 * var8) + (var9 * var9)))));
        if (var10 == 0.0f) return;
        float var11 = ((-(var9) / var10) * arg6);
        float var12 = ((var8 / var10) * arg6);
        arg0.addVertex(arg1, (arg2 + var11), (arg3 + var12), 0.0f).setColor(arg7);
        arg0.addVertex(arg1, (arg2 - var11), (arg3 - var12), 0.0f).setColor(arg7);
        arg0.addVertex(arg1, (arg4 - var11), (arg5 - var12), 0.0f).setColor(arg7);
        arg0.addVertex(arg1, (arg4 + var11), (arg5 + var12), 0.0f).setColor(arg7);
    }

    private static RenderType texturedType(Identifier arg0) {
        return ((RenderType) TEXTURE_TYPES.computeIfAbsent(arg0, (arg0) -> RenderType.create(("night_textured_" + String.valueOf(arg0)), RenderSetup.builder(RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", arg0).createRenderSetup())));
    }

    public static Vec3 project(Vec3 arg0) {
        Object var1 = mc.gameRenderer.mainCamera().position();
        var tmp0 = new int[4];
        tmp0[0] = 0;
        tmp0[1] = 0;
        tmp0[2] = mc.getWindow().getWidth();
        tmp0[3] = mc.getWindow().getHeight();
        Object var2 = tmp0;
        Object var3 = new Vector3f();
        Object var4 = new Vector4f(((float) (arg0.x - var1.x)), ((float) (arg0.y - var1.y)), ((float) (arg0.z - var1.z)), 1.0f).mul(LAST_WORLD_MATRIX);
        Object var5 = new Matrix4f(LAST_PROJECTION_MATRIX);
        Object var6 = new Matrix4f(LAST_MODEL_MATRIX);
        var5.mul(var6).project(var4.x(), var4.y(), var4.z(), var2, var3);
        return new Vec3(((double) (var3.x / ((float) mc.getWindow().getGuiScale()))), ((double) ((((float) mc.getWindow().getHeight()) - var3.y) / ((float) mc.getWindow().getGuiScale()))), ((double) var3.z));
    }

    static {
        LAST_PROJECTION_MATRIX = new Matrix4f();
        LAST_MODEL_MATRIX = new Matrix4f();
        LAST_WORLD_MATRIX = new Matrix4f();
        TEXTURE_TYPES = new HashMap();
        scissorHandleInitialized = false;
    }

}
