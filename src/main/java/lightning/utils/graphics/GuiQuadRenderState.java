package lightning.utils.graphics;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record GuiQuadRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, float[] us, float[] vs, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {

    public GuiQuadRenderState(Matrix3x2fc arg0, float[] arg1, float[] arg2, int[] arg3, ScreenRectangle arg4) {
        this(RenderPipelines.GUI, TextureSetup.noTexture(), arg0, arg1, arg2, arg3, null, null, arg4, computeBounds(arg1, arg2, arg0, arg4));
    }

    public GuiQuadRenderState(TextureSetup arg0, Matrix3x2fc arg1, float[] arg2, float[] arg3, int[] arg4, float[] arg5, float[] arg6, ScreenRectangle arg7) {
        this(RenderPipelines.GUI_TEXTURED, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, computeBounds(arg2, arg3, arg1, arg7));
    }

    public GuiQuadRenderState(RenderPipeline arg0, TextureSetup arg1, Matrix3x2fc arg2, float[] arg3, float[] arg4, int[] arg5, float[] arg6, float[] arg7, ScreenRectangle arg8) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, computeBounds(arg3, arg4, arg2, arg8));
    }

    public GuiQuadRenderState(RenderPipeline arg0, TextureSetup arg1, Matrix3x2fc arg2, float[] arg3, float[] arg4, int[] arg5, float[] arg6, float[] arg7, ScreenRectangle arg8, ScreenRectangle arg9) {
        this.pipeline = arg0;
        this.textureSetup = arg1;
        this.pose = arg2;
        this.xs = arg3;
        this.ys = arg4;
        this.cols = arg5;
        this.us = arg6;
        this.vs = arg7;
        this.scissorArea = arg8;
        this.bounds = arg9;
    }

    public void buildVertices(VertexConsumer arg0) {
        if (this.us != null && this.vs != null) {
            int var2 = 0;
            while (var2 < this.xs.length) {
                arg0.addVertexWith2DPose(this.pose, this.xs[var2], this.ys[var2]).setUv(this.us[var2], this.vs[var2]).setColor(this.cols[var2]);
                var2++;
            }
            break label_4;
        }
        var2 = 0;
        while (var2 < this.xs.length) {
            arg0.addVertexWith2DPose(this.pose, this.xs[var2], this.ys[var2]).setColor(this.cols[var2]);
            var2++;
        }
        label_4:
    }

    private static ScreenRectangle computeBounds(float[] arg0, float[] arg1, Matrix3x2fc arg2, ScreenRectangle arg3) {
        if (arg0.length == 0) return arg3;
        float var4 = arg0[0];
        float var5 = arg0[0];
        float var6 = arg1[0];
        float var7 = arg1[0];
        int var8 = 1;
        while (var8 < arg0.length) {
            if (arg0[var8] < var4) {
                var4 = arg0[var8];
            }
            if (arg0[var8] > var5) {
                var5 = arg0[var8];
            }
            if (arg1[var8] < var6) {
                var6 = arg1[var8];
            }
            if (arg1[var8] <= var7) {
            } else {
                var7 = arg1[var8];
            }
            var8++;
        }
        var8 = ((int) Math.floor(((double) var4)));
        int var9 = ((int) Math.floor(((double) var6)));
        int var10 = ((int) Math.ceil(((double) var5)));
        int var11 = ((int) Math.ceil(((double) var7)));
        Object var12 = new ScreenRectangle(var8, var9, (var10 - var8), (var11 - var9)).transformMaxBounds(arg2);
        if (arg3 != null) {
        } else {
        }
        return var12;
    }

    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    public TextureSetup textureSetup() {
        return this.textureSetup;
    }

    public Matrix3x2fc pose() {
        return this.pose;
    }

    public float[] xs() {
        return this.xs;
    }

    public float[] ys() {
        return this.ys;
    }

    public int[] cols() {
        return this.cols;
    }

    public float[] us() {
        return this.us;
    }

    public float[] vs() {
        return this.vs;
    }

    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    public ScreenRectangle bounds() {
        return this.bounds;
    }

}
