package lightning.utils.graphics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Generated;
import org.joml.Matrix4f;

public class ChamsVertexConsumer implements VertexConsumer {
    private static final Matrix4f IDENTITY;
    private final VertexConsumer real;
    private final boolean fill;
    private final int fillColor;
    private final boolean outline;
    private final int outlineColor;
    private final boolean shine;
    private final boolean glint;
    private final int glintColor;
    private final Glint.GlintParams glintParams;
    private final boolean suppressReal;
    private final float realAlphaMultiplier;
    private final float[] xs;
    private final float[] ys;
    private final float[] zs;
    private int i;

    public ChamsVertexConsumer(VertexConsumer arg0, boolean arg1, int arg2, boolean arg3, int arg4, boolean arg5) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, false, -1, null, false, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer arg0, boolean arg1, int arg2, boolean arg3, int arg4, boolean arg5, boolean arg6) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, false, -1, null, arg6, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer arg0, boolean arg1, int arg2, boolean arg3, int arg4, boolean arg5, boolean arg6, float arg7) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, false, -1, null, arg6, arg7);
    }

    public ChamsVertexConsumer(VertexConsumer arg0, boolean arg1, int arg2, boolean arg3, int arg4, boolean arg5, boolean arg6, int arg7, boolean arg8, float arg9) {
        this(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, null, arg8, arg9);
    }

    public ChamsVertexConsumer(VertexConsumer arg0, boolean arg1, int arg2, boolean arg3, int arg4, boolean arg5, boolean arg6, int arg7, Glint.GlintParams arg8, boolean arg9, float arg10) {
        this.xs = new float[4];
        this.ys = new float[4];
        this.zs = new float[4];
        this.i = 0;
        this.real = arg0;
        this.fill = arg1;
        this.fillColor = arg2;
        this.outline = arg3;
        this.outlineColor = arg4;
        this.shine = arg5;
        this.glint = arg6;
        this.glintColor = arg7;
        this.glintParams = arg8;
        this.suppressReal = arg9;
        this.realAlphaMultiplier = arg10;
    }

    public VertexConsumer addVertex(float arg0, float arg1, float arg2) {
        if (!this.suppressReal && this.real != null) {
            this.real.addVertex(arg0, arg1, arg2);
        }
        this.xs[this.i] = arg0;
        this.ys[this.i] = arg1;
        this.zs[this.i] = arg2;
        this.i = (this.i + 1);
        if (this.i == 4) {
            flushQuad();
            this.i = 0;
        }
        return this;
    }

    private void flushQuad() {
        Object var1 = (!(!this.shine) ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS);
        Object var2 = (!(!this.shine) ? Renderer3D.SHINE_DEBUG_LINES : Renderer3D.DEBUG_LINES);
        if (!(!this.fill)) {
            var tmp0 = new Renderer3D.Vertex[4];
            tmp0[0] = new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.fillColor);
            tmp0[1] = new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.fillColor);
            tmp0[2] = new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.fillColor);
            tmp0[3] = new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.fillColor);
            var1.add(new Renderer3D.VertexCollection(tmp0));
        }
        if (!(!this.outline)) {
            var tmp1 = new Renderer3D.Vertex[2];
            tmp1[0] = new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor);
            tmp1[1] = new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor);
            var2.add(new Renderer3D.VertexCollection(tmp1));
            var tmp2 = new Renderer3D.Vertex[2];
            tmp2[0] = new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor);
            tmp2[1] = new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor);
            var2.add(new Renderer3D.VertexCollection(tmp2));
            var tmp3 = new Renderer3D.Vertex[2];
            tmp3[0] = new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor);
            tmp3[1] = new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor);
            var2.add(new Renderer3D.VertexCollection(tmp3));
            var tmp4 = new Renderer3D.Vertex[2];
            tmp4[0] = new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor);
            tmp4[1] = new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor);
            var2.add(new Renderer3D.VertexCollection(tmp4));
        }
        if (!(!this.glint)) {
            Object var3 = (this.glintParams != null ? this.glintParams : Glint.ENTITY);
            Object var4 = new float[4];
            Object var5 = new float[4];
            int var6 = 0;
            while (var6 < 4) {
                Object var7 = var3.getGlintUV(this.xs[var6], this.ys[var6], this.zs[var6]);
                var4[var6] = var7[0];
                var5[var6] = var7[1];
                var6++;
            }
            var tmp5 = new Glint.TexturedVertex[4];
            tmp5[0] = new Glint.TexturedVertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], var4[0], var5[0], this.glintColor);
            tmp5[1] = new Glint.TexturedVertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], var4[1], var5[1], this.glintColor);
            tmp5[2] = new Glint.TexturedVertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], var4[2], var5[2], this.glintColor);
            tmp5[3] = new Glint.TexturedVertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], var4[3], var5[3], this.glintColor);
            Glint.QUADS.add(new Glint.TexturedVertexCollection(tmp5));
        }
    }

    public VertexConsumer setColor(int arg0, int arg1, int arg2, int arg3) {
        if (!this.suppressReal && this.real != null) {
            if (this.realAlphaMultiplier >= 1.0f) {
            } else {
            }
            int var5 = Math.round((((float) arg3) * this.realAlphaMultiplier));
            this.real.setColor(arg0, arg1, arg2, var5);
        }
        return this;
    }

    public VertexConsumer setColor(int arg0) {
        if ((this.suppressReal) || (this.real == null)) break label_1;
        if (this.realAlphaMultiplier >= 1.0f) {
            this.real.setColor(arg0);
            break label_1;
        }
        int var2 = ((arg0 >>> 24) & 255);
        int var3 = Math.round((((float) var2) * this.realAlphaMultiplier));
        this.real.setColor(((arg0 & 16777215) | (var3 << 24)));
        label_1:
        return this;
    }

    public VertexConsumer setUv(float arg0, float arg1) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv(arg0, arg1);
        }
        return this;
    }

    public VertexConsumer setUv1(int arg0, int arg1) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv1(arg0, arg1);
        }
        return this;
    }

    public VertexConsumer setUv2(int arg0, int arg1) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv2(arg0, arg1);
        }
        return this;
    }

    public VertexConsumer setNormal(float arg0, float arg1, float arg2) {
        if (!this.suppressReal && this.real != null) {
            this.real.setNormal(arg0, arg1, arg2);
        }
        return this;
    }

    public VertexConsumer setLineWidth(float arg0) {
        if (!this.suppressReal && this.real != null) {
            this.real.setLineWidth(arg0);
        }
        return this;
    }

    @Generated
    public VertexConsumer getReal() {
        return this.real;
    }

    static {
        IDENTITY = new Matrix4f();
    }

}
