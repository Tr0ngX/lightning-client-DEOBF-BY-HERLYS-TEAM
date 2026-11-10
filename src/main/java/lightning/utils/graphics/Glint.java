package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.ArrayList;
import java.util.List;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class Glint {
    private static final Identifier GLINT_TEXTURE;
    private static final RenderType NO_DEPTH_GLINT_QUADS;
    public static List QUADS;
    public static final Glint.GlintParams ENTITY;
    public static final Glint.GlintParams CRYSTAL;
    private static long lastMs;

    public static void tick() {
        long var0 = System.currentTimeMillis();
        if (lastMs == 0L) {
            lastMs = var0;
        }
        float var2 = (((float) (var0 - lastMs)) / 1000.0f);
        lastMs = var0;
        ENTITY.tick(var2);
        CRYSTAL.tick(var2);
    }

    public static void draw() {
        if (QUADS.isEmpty()) return;
        int var0 = 0;
        Object var1 = QUADS.iterator();
        while (var1.hasNext()) {
            Object var2 = ((Glint.TexturedVertexCollection) var1.next());
            var0 = (var0 + var2.vertices().length);
        }
        var1 = ByteBufferBuilder.exactlySized((var0 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
        try {
            var2 = new BufferBuilder(var1, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            Object var3 = QUADS.iterator();
            while (var3.hasNext()) {
                Object var4 = ((Glint.TexturedVertexCollection) var3.next());
                var4.vertex(var2);
            }
            var3 = var2.build();
            Renderer3D.draw(NO_DEPTH_GLINT_QUADS, var3);
            if (var1 == null) break label_12;
            var1.close();
        } catch (Throwable ex7) {
            var2 = ex7;
            if (var1 == null) break label_11;
            try {
                var1.close();
            } catch (Throwable ex10) {
                var3 = ex10;
                var2.addSuppressed(var3);
            }
            label_11:
            throw var2;
        }
        label_12:
        QUADS.clear();
    }

    static {
        GLINT_TEXTURE = Identifier.fromNamespaceAndPath("lightning", "textures/glint.png");
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getGuiTexturedSnippet();
        NO_DEPTH_GLINT_QUADS = RenderType.create("night_no_depth_glint", RenderSetup.builder(RenderPipeline.builder(tmp0).withLocation("lightning/no_depth_glint").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE))).build()).withTexture("Sampler0", GLINT_TEXTURE).sortOnUpload().createRenderSetup());
        QUADS = new ArrayList();
        ENTITY = new Glint.GlintParams();
        CRYSTAL = new Glint.GlintParams();
        lastMs = 0L;
    }


    public record TexturedVertex(Matrix4f matrix, float x, float y, float z, float u, float v, int color) {

        public TexturedVertex(Matrix4f arg0, float arg1, float arg2, float arg3, float arg4, float arg5, int arg6) {
            this.matrix = arg0;
            this.x = arg1;
            this.y = arg2;
            this.z = arg3;
            this.u = arg4;
            this.v = arg5;
            this.color = arg6;
        }

        public Matrix4f matrix() {
            return this.matrix;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public float z() {
            return this.z;
        }

        public float u() {
            return this.u;
        }

        public float v() {
            return this.v;
        }

        public int color() {
            return this.color;
        }

    }

    public class GlintParams {
        public float speedU;
        public float speedV;
        public float scale;
        public String direction;
        public float scrollU;
        public float scrollV;

        public GlintParams() {
            this.speedU = 0.25f;
            this.speedV = 0.1f;
            this.scale = 1.0f;
            this.direction = "Diagonal";
            this.scrollU = 0.0f;
            this.scrollV = 0.0f;
        }

        public void tick(float arg0) {
            this.scrollU = ((this.scrollU + (this.speedU * arg0)) % 1.0f);
            this.scrollV = ((this.scrollV + (this.speedV * arg0)) % 1.0f);
            if (this.scrollU < 0.0f) {
                this.scrollU = (this.scrollU + 1.0f);
            }
            if (this.scrollV < 0.0f) {
                this.scrollV = (this.scrollV + 1.0f);
            }
        }

        public float[] getGlintUV(float arg0, float arg1, float arg2) {
            float var4 = (this.scale * 0.06f);
            Object var7 = this.direction;
            int var8 = -1;
            switch (var7.hashCode()) {
                case -1919497322 -> { break label_1; }
                case -913872828 -> { break label_0; }
                default -> { break label_2; }
            }
            label_0:
            if (!(var7.equals("Horizontal"))) break label_2;
            var8 = 0;
            break label_2;
            label_1:
            if (!(var7.equals("Vertical"))) break label_2;
            var8 = 1;
            label_2:
            switch (var8) {
                case 0 -> { break label_3; }
                case 1 -> { break label_4; }
                default -> { break label_5; }
            }
            label_3:
            float var5 = ((arg0 + (arg2 * 0.5f)) * var4);
            float var6 = (arg1 * var4);
            break label_6;
            label_4:
            var5 = (arg1 * var4);
            var6 = ((arg0 + (arg2 * 0.5f)) * var4);
            break label_6;
            label_5:
            var5 = ((arg0 * var4) + ((arg2 * var4) * 0.5f));
            var6 = ((arg1 * var4) + ((arg2 * var4) * 0.5f));
            label_6:
            var5 = (var5 + this.scrollU);
            var6 = (var6 + this.scrollV);
            var tmp0 = new float[2];
            tmp0[0] = var5;
            tmp0[1] = var6;
            return tmp0;
        }

    }

    public record TexturedVertexCollection(Glint.TexturedVertex[] vertices) {

        public TexturedVertexCollection(Glint.TexturedVertex... arg0) {
            this.vertices = arg0;
        }

        public void vertex(BufferBuilder arg0) {
            Object var2 = this.vertices;
            int var3 = var2.length;
            int var4 = 0;
            while (var4 < var3) {
                Object var5 = var2[var4];
                arg0.addVertex(var5.matrix, var5.x, var5.y, var5.z).setUv(var5.u, var5.v).setColor(var5.color);
                var4++;
            }
        }

        public Glint.TexturedVertex[] vertices() {
            return this.vertices;
        }

    }
}
