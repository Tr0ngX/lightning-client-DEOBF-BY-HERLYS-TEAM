package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.mixins.accessors.GameRendererAccessor;
import lightning.mixins.accessors.ItemStackRenderStateAccessor;
import lightning.mixins.accessors.LayerRenderStateAccessor;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class Renderer3D implements IMinecraft {
    public static boolean RENDERING;
    private static final RenderType NO_DEPTH_QUADS;
    private static final RenderType NO_DEPTH_LINES;
    private static final BlendFunction SHINE_BLEND;
    private static final RenderType NO_DEPTH_QUADS_SHINE;
    private static final RenderType NO_DEPTH_LINES_SHINE;
    public static List QUADS;
    public static List DEBUG_LINES;
    public static List SHINE_QUADS;
    public static List SHINE_DEBUG_LINES;
    public static List SHADER_QUADS;
    public static List SHADER_DEBUG_LINES;
    public static List TRACER_DEBUG_LINES;
    private static final QuadInstance QUAD_INSTANCE;
    private static final ItemStackRenderState ITEM_RENDER_STATE;
    private static final Map NO_DEPTH_ITEM_CUTOUT;
    private static final Map NO_DEPTH_ITEM_TRANSLUCENT;
    private static final Map NO_DEPTH_TEXTURE_TYPES;

    public static void draw(RenderType arg0, MeshData arg1) {
        if (arg1 == null) break label_17;
        if (!(arg1.drawState().indexCount())) {
            arg1.close();
            break label_17;
        }
        Object var2 = arg1;
        try {
            Object var3 = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate vertices", 32, arg1.vertexBuffer());
            Object var4 = null;
            if (arg1.indexBuffer() == null) {
                Object var7 = RenderSystem.getSequentialBuffer(arg1.drawState().primitiveTopology());
                Object var5 = var7.getBuffer(arg1.drawState().indexCount());
                Object var6 = var7.type();
            } else {
                var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate indices", 64, arg1.indexBuffer());
                var4 = tmp0;
                var5 = tmp0;
                var6 = arg1.drawState().indexType();
                try {
                }
                arg0.prepare().drawFromBuffer(var3, var5, var6, 0, 0, arg1.drawState().indexCount());
                var3.close();
                if (var4 == null) break label_10;
                var4.close();
            } catch (Exception ex5) {
                var7 = ex5;
                var3.close();
                if (var4 == null) break label_10;
                var4.close();
                break label_10;
                Object var8 = null;
                var3.close();
                if (var4 != null) {
                    var4.close();
                }
                throw var8;
            }
            label_10:
        } catch (Throwable ex11) {
            var3 = ex11;
            if (arg1 == null) break label_15;
            try {
                var2.close();
            } catch (Throwable ex14) {
                var4 = ex14;
                var3.addSuppressed(var4);
            }
            label_15:
            throw var3;
        }
        if (arg1 == null) break label_17;
        arg1.close();
        label_17:
    }

    public static void renderBox(PoseStack arg0, AABB arg1, Color arg2) {
        renderGradientBox(arg0, arg1, arg2, arg2);
    }

    public static void renderGradientBox(PoseStack arg0, AABB arg1, Color arg2, Color arg3) {
        renderGradientBox(QUADS, arg0, arg1, arg2, arg3);
    }

    public static void renderGradientBox(List arg0, PoseStack arg1, AABB arg2, Color arg3, Color arg4) {
        if (!RENDERING) return;
        if (!(isFrustumVisible(arg2))) return;
        Object var5 = arg1.last().pose();
        arg2 = cameraTransform(arg2);
        var tmp0 = new Renderer3D.Vertex[4];
        tmp0[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp0[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp0[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp0[3] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp0));
        var tmp1 = new Renderer3D.Vertex[4];
        tmp1[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp1[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp1[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp1[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp1));
        var tmp2 = new Renderer3D.Vertex[4];
        tmp2[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp2[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp2[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp2[3] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp2));
        var tmp3 = new Renderer3D.Vertex[4];
        tmp3[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp3[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp3[2] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp3[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp3));
        var tmp4 = new Renderer3D.Vertex[4];
        tmp4[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp4[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp4[2] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp4[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp4));
        var tmp5 = new Renderer3D.Vertex[4];
        tmp5[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp5[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp5[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp5[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp5));
    }

    public static void renderBoxOutline(PoseStack arg0, AABB arg1, Color arg2) {
        renderGradientBoxOutline(arg0, arg1, arg2, arg2);
    }

    public static void renderGradientBoxOutline(PoseStack arg0, AABB arg1, Color arg2, Color arg3) {
        renderGradientBoxOutline(DEBUG_LINES, arg0, arg1, arg2, arg3);
    }

    public static void renderGradientBoxOutline(List arg0, PoseStack arg1, AABB arg2, Color arg3, Color arg4) {
        if (!RENDERING) return;
        if (!(isFrustumVisible(arg2))) return;
        Object var5 = arg1.last().pose();
        arg2 = cameraTransform(arg2);
        var tmp0 = new Renderer3D.Vertex[4];
        tmp0[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp0[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp0[2] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp0[3] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp0));
        var tmp1 = new Renderer3D.Vertex[4];
        tmp1[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp1[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp1[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp1[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp1));
        var tmp2 = new Renderer3D.Vertex[4];
        tmp2[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp2[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp2[2] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp2[3] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp2));
        var tmp3 = new Renderer3D.Vertex[4];
        tmp3[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp3[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp3[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp3[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp3));
        var tmp4 = new Renderer3D.Vertex[4];
        tmp4[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp4[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp4[2] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp4[3] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp4));
        var tmp5 = new Renderer3D.Vertex[4];
        tmp5[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp5[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        tmp5[2] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        tmp5[3] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp5));
    }

    public static void renderTargetCircle(PoseStack arg0, Entity arg1, float arg2, Color arg3) {
        renderTargetCircle(SHINE_QUADS, SHINE_DEBUG_LINES, arg0, arg1, arg2, arg3);
    }

    public static void renderTargetCircle(List arg0, List arg1, PoseStack arg2, Entity arg3, float arg4, Color arg5) {
        if (((!RENDERING) || (arg3 == null)) || (mc.player == null)) {
            return;
        }
        Object var6 = EntityUtils.getRenderPos(arg3, arg4);
        double var7 = Math.max(0.5, arg3.getBoundingBox().getYsize());
        double var9 = Math.max(0.4, ((((double) arg3.getBbWidth()) / 2.0) + 0.25));
        Object var11 = new AABB((var6.x - var9), (var6.y - 0.5), (var6.z - var9), (var6.x + var9), ((var6.y + var7) + 0.5), (var6.z + var9));
        if (!(isFrustumVisible(var11))) return;
        Object var12 = arg2.last().pose();
        double var13 = (((double) System.currentTimeMillis()) / 420.0);
        double var15 = Math.sin(var13);
        double var17 = Math.cos(var13);
        double var19 = ((var15 + 1.0) / 2.0);
        double var21 = (var6.y + (var19 * var7));
        double var23 = (-(var17) * 0.225);
        double var25 = (var21 + var23);
        float var27 = (((float) arg5.getAlpha()) / 255.0f);
        if (var27 <= 0.001f) return;
        int var28 = ((int) (180.0f * var27));
        Object var29 = ColorUtils.getColor(arg5, var28);
        Object var30 = ColorUtils.getColor(arg5, 0);
        Object var31 = ColorUtils.getColor(arg5, ((int) (255.0f * var27)));
        int var32 = 48;
        int var33 = 0;
        while (var33 < var32) {
            double var34 = (((((double) var33) * 2.0) * 3.141592653589793) / ((double) var32));
            double var36 = (((((double) (var33 + 1)) * 2.0) * 3.141592653589793) / ((double) var32));
            double var38 = (var6.x + (Math.cos(var34) * var9));
            double var40 = (var6.z + (Math.sin(var34) * var9));
            double var42 = (var6.x + (Math.cos(var36) * var9));
            double var44 = (var6.z + (Math.sin(var36) * var9));
            Object var46 = cameraTransform(new Vec3(var38, var21, var40));
            Object var47 = cameraTransform(new Vec3(var42, var21, var44));
            Object var48 = cameraTransform(new Vec3(var42, var25, var44));
            Object var49 = cameraTransform(new Vec3(var38, var25, var40));
            var tmp0 = new Renderer3D.Vertex[4];
            tmp0[0] = new Renderer3D.Vertex(var12, ((float) var46.x), ((float) var46.y), ((float) var46.z), var29.getRGB());
            tmp0[1] = new Renderer3D.Vertex(var12, ((float) var47.x), ((float) var47.y), ((float) var47.z), var29.getRGB());
            tmp0[2] = new Renderer3D.Vertex(var12, ((float) var48.x), ((float) var48.y), ((float) var48.z), var30.getRGB());
            tmp0[3] = new Renderer3D.Vertex(var12, ((float) var49.x), ((float) var49.y), ((float) var49.z), var30.getRGB());
            arg0.add(new Renderer3D.VertexCollection(tmp0));
            var tmp1 = new Renderer3D.Vertex[4];
            tmp1[0] = new Renderer3D.Vertex(var12, ((float) var49.x), ((float) var49.y), ((float) var49.z), var30.getRGB());
            tmp1[1] = new Renderer3D.Vertex(var12, ((float) var48.x), ((float) var48.y), ((float) var48.z), var30.getRGB());
            tmp1[2] = new Renderer3D.Vertex(var12, ((float) var47.x), ((float) var47.y), ((float) var47.z), var29.getRGB());
            tmp1[3] = new Renderer3D.Vertex(var12, ((float) var46.x), ((float) var46.y), ((float) var46.z), var29.getRGB());
            arg0.add(new Renderer3D.VertexCollection(tmp1));
            var tmp2 = new Renderer3D.Vertex[2];
            tmp2[0] = new Renderer3D.Vertex(var12, ((float) var46.x), ((float) var46.y), ((float) var46.z), var31.getRGB(), 2.0f);
            tmp2[1] = new Renderer3D.Vertex(var12, ((float) var47.x), ((float) var47.y), ((float) var47.z), var31.getRGB(), 2.0f);
            arg1.add(new Renderer3D.VertexCollection(tmp2));
            var33++;
        }
    }

    public static void renderBoxWireframe(PoseStack arg0, AABB arg1, Color arg2) {
        renderBoxWireframe(DEBUG_LINES, arg0, arg1, arg2, arg2);
    }

    public static void renderBoxWireframe(PoseStack arg0, AABB arg1, Color arg2, Color arg3) {
        renderBoxWireframe(DEBUG_LINES, arg0, arg1, arg2, arg3);
    }

    public static void renderBoxWireframe(List arg0, PoseStack arg1, AABB arg2, Color arg3, Color arg4) {
        if (!RENDERING) return;
        if (!(isFrustumVisible(arg2))) return;
        Object var5 = arg1.last().pose();
        arg2 = cameraTransform(arg2);
        var tmp0 = new Renderer3D.Vertex[2];
        tmp0[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp0[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp0));
        var tmp1 = new Renderer3D.Vertex[2];
        tmp1[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.minY), ((float) arg2.minZ), arg4.getRGB());
        tmp1[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.minY), ((float) arg2.maxZ), arg4.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp1));
        var tmp2 = new Renderer3D.Vertex[2];
        tmp2[0] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp2[1] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp2));
        var tmp3 = new Renderer3D.Vertex[2];
        tmp3[0] = new Renderer3D.Vertex(var5, ((float) arg2.maxX), ((float) arg2.maxY), ((float) arg2.minZ), arg3.getRGB());
        tmp3[1] = new Renderer3D.Vertex(var5, ((float) arg2.minX), ((float) arg2.maxY), ((float) arg2.maxZ), arg3.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp3));
    }

    public static void renderQuad(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        if (!RENDERING) return;
        Object var6 = arg0.last().pose();
        int var7 = arg5.getRGB();
        var tmp0 = new Renderer3D.Vertex[4];
        tmp0[0] = new Renderer3D.Vertex(var6, arg1, arg2, 0.0f, var7);
        tmp0[1] = new Renderer3D.Vertex(var6, arg1, arg4, 0.0f, var7);
        tmp0[2] = new Renderer3D.Vertex(var6, arg3, arg4, 0.0f, var7);
        tmp0[3] = new Renderer3D.Vertex(var6, arg3, arg2, 0.0f, var7);
        QUADS.add(new Renderer3D.VertexCollection(tmp0));
    }

    public static void renderOutline(PoseStack arg0, float arg1, float arg2, float arg3, float arg4, Color arg5) {
        if (!RENDERING) return;
        Object var6 = arg0.last().pose();
        int var7 = arg5.getRGB();
        var tmp0 = new Renderer3D.Vertex[2];
        tmp0[0] = new Renderer3D.Vertex(var6, arg1, arg2, 0.0f, var7);
        tmp0[1] = new Renderer3D.Vertex(var6, arg3, arg2, 0.0f, var7);
        DEBUG_LINES.add(new Renderer3D.VertexCollection(tmp0));
        var tmp1 = new Renderer3D.Vertex[2];
        tmp1[0] = new Renderer3D.Vertex(var6, arg3, arg2, 0.0f, var7);
        tmp1[1] = new Renderer3D.Vertex(var6, arg3, arg4, 0.0f, var7);
        DEBUG_LINES.add(new Renderer3D.VertexCollection(tmp1));
        var tmp2 = new Renderer3D.Vertex[2];
        tmp2[0] = new Renderer3D.Vertex(var6, arg3, arg4, 0.0f, var7);
        tmp2[1] = new Renderer3D.Vertex(var6, arg1, arg4, 0.0f, var7);
        DEBUG_LINES.add(new Renderer3D.VertexCollection(tmp2));
        var tmp3 = new Renderer3D.Vertex[2];
        tmp3[0] = new Renderer3D.Vertex(var6, arg1, arg4, 0.0f, var7);
        tmp3[1] = new Renderer3D.Vertex(var6, arg1, arg2, 0.0f, var7);
        DEBUG_LINES.add(new Renderer3D.VertexCollection(tmp3));
    }

    public static void renderLine(PoseStack arg0, Vec3 arg1, Vec3 arg2, Color arg3) {
        renderLine(DEBUG_LINES, arg0, arg1, arg2, arg3);
    }

    public static void renderLine(List arg0, PoseStack arg1, Vec3 arg2, Vec3 arg3, Color arg4) {
        renderLine(arg0, arg1, arg2, arg3, arg4, arg4);
    }

    public static void renderLine(PoseStack arg0, Vec3 arg1, Vec3 arg2, Color arg3, Color arg4) {
        renderLine(DEBUG_LINES, arg0, arg1, arg2, arg3, arg4);
    }

    public static void renderLine(List arg0, PoseStack arg1, Vec3 arg2, Vec3 arg3, Color arg4, Color arg5) {
        if (((!RENDERING) || (mc.gameRenderer == null)) || (mc.gameRenderer.mainCamera() == null)) break label_4;
        Object var6 = mc.gameRenderer.mainCamera().position();
        Object var7 = Vec3.directionFromRotation(mc.gameRenderer.mainCamera().xRot(), mc.gameRenderer.mainCamera().yRot());
        double var8 = ((((arg2.x - var6.x) * var7.x) + ((arg2.y - var6.y) * var7.y)) + ((arg2.z - var6.z) * var7.z));
        double var10 = ((((arg3.x - var6.x) * var7.x) + ((arg3.y - var6.y) * var7.y)) + ((arg3.z - var6.z) * var7.z));
        double var12 = 0.2;
        if (var8 < var12) {
            if (var10 < var12) break label_4;
        }
        if (var8 >= var12 && var10 < var12) {
            double var14 = ((var8 - var12) / (var8 - var10));
            arg3 = new Vec3((arg2.x + ((arg3.x - arg2.x) * var14)), (arg2.y + ((arg3.y - arg2.y) * var14)), (arg2.z + ((arg3.z - arg2.z) * var14)));
            if (!(arg4.equals(arg5))) {
                arg5 = ColorUtils.getColor(arg5, ((int) ((((double) arg5.getAlpha()) * (1.0 - var14)) + (((double) arg4.getAlpha()) * var14))));
            }
            break label_3;
        }
        if ((var8 >= var12) || (var10 < var12)) break label_3;
        var14 = ((var12 - var8) / (var10 - var8));
        arg2 = new Vec3((arg2.x + ((arg3.x - arg2.x) * var14)), (arg2.y + ((arg3.y - arg2.y) * var14)), (arg2.z + ((arg3.z - arg2.z) * var14)));
        if (arg4.equals(arg5)) break label_3;
        arg4 = ColorUtils.getColor(arg4, ((int) ((((double) arg4.getAlpha()) * (1.0 - var14)) + (((double) arg5.getAlpha()) * var14))));
        label_3:
        var14 = arg1.last().pose();
        arg2 = cameraTransform(arg2);
        arg3 = cameraTransform(arg3);
        var tmp0 = new Renderer3D.Vertex[2];
        tmp0[0] = new Renderer3D.Vertex(var14, ((float) arg2.x), ((float) arg2.y), ((float) arg2.z), arg4.getRGB());
        tmp0[1] = new Renderer3D.Vertex(var14, ((float) arg3.x), ((float) arg3.y), ((float) arg3.z), arg5.getRGB());
        arg0.add(new Renderer3D.VertexCollection(tmp0));
        label_4:
    }

    private static RenderType noDepthItemRenderType(BakedQuad.MaterialInfo arg0) {
        int var1 = arg0.layer().translucent();
        Object var2 = arg0.sprite().atlasLocation();
        Object var3 = (!(!var1) ? Renderer3D.NO_DEPTH_ITEM_TRANSLUCENT : Renderer3D.NO_DEPTH_ITEM_CUTOUT);
        return ((RenderType) var3.computeIfAbsent(var2, (arg1) -> {
    var tmp0 = new RenderPipeline.Snippet[1];
    tmp0[0] = RenderPipelinesAccessor.getItemSnippet();
    Object var2 = RenderPipeline.builder(tmp0).withLocation((!(!arg0) ? "lightning/no_depth_item_translucent" : "lightning/no_depth_item_cutout")).withShaderDefine("ALPHA_CUTOUT", 0.1f).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
    if (!(!arg0)) {
        var2.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
    }
    return RenderType.create((!(!arg0) ? "night_no_depth_item_translucent" : "night_no_depth_item_cutout"), RenderSetup.builder(var2.build()).withTexture("Sampler0", arg1).useOverlay().useLightmap().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup());
}));
    }

    private static BufferBuilder itemBuffer(Map arg0, Map arg1, RenderType arg2) {
        return ((BufferBuilder) arg1.computeIfAbsent(arg2, (arg1) -> {
    Object var2 = new ByteBufferBuilder(256);
    arg0.put(arg1, var2);
    return new BufferBuilder(var2, arg1.primitiveTopology(), arg1.format());
}));
    }

    public static void renderItem(PoseStack arg0, ItemStack arg1, ItemOwner arg2) {
        if ((!RENDERING) || (arg1.isEmpty())) {
            return;
        }
        ITEM_RENDER_STATE.clear();
        mc.getItemModelResolver().updateForTopItem(ITEM_RENDER_STATE, arg1, ItemDisplayContext.GUI, mc.level, arg2, 0);
        Object var3 = ((ItemStackRenderStateAccessor) ITEM_RENDER_STATE);
        QUAD_INSTANCE.setLightCoords(15728880);
        QUAD_INSTANCE.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        Object var4 = ((GameRendererAccessor) mc.gameRenderer).getFogRenderer();
        RenderSystem.setShaderFog(var4.getBuffer(FogRenderer.FogMode.NONE));
        Object var5 = new HashMap();
        Object var6 = new HashMap();
        int var7 = 0;
        while (var7 < var3.night$getActiveLayerCount()) {
            Object var8 = ((LayerRenderStateAccessor) var3.night$getLayers()[var7]);
            arg0.pushPose();
            var8.night$applyTransform(arg0.last());
            Object var9 = var8.night$getTintLayers();
            if (var9 != null) {
            } else {
            }
            Object var10 = new int[0];
            int var11 = (var8.night$getFoilType() != ItemStackRenderState.FoilType.NONE ? 1 : 0);
            Object var12 = var8.night$getQuads().iterator();
            while (var12.hasNext()) {
                Object var13 = ((BakedQuad) var12.next());
                Object var14 = var13.materialInfo();
                Object var15 = noDepthItemRenderType(var14);
                if (var14.isTinted()) {
                } else {
                }
                int var16 = -1;
                if (var16 >= 0 && var16 < var10.length) {
                } else {
                }
                var10[var16].setColor(-1);
                if (!(!var11)) {
                    if (!(mc.gameRenderer.gameRenderState().useShaderTransparency())) break label_13;
                    int var17 = (var15.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET ? 1 : 0);
                    if (!(!var17)) {
                    } else {
                    }
                    Object var18 = RenderTypes.glint();
                    itemBuffer(var5, var6, var18).putBakedQuad(arg0.last(), var13, QUAD_INSTANCE);
                }
                itemBuffer(var5, var6, var15).putBakedQuad(arg0.last(), var13, QUAD_INSTANCE);
            }
            arg0.popPose();
            var7++;
        }
        var7 = var6.entrySet().iterator();
        while (var7.hasNext()) {
            var8 = ((Map.Entry) var7.next());
            var9 = ((BufferBuilder) var8.getValue()).build();
            draw(((RenderType) var8.getKey()), var9);
        }
        var7 = var5.values().iterator();
        while (var7.hasNext()) {
            var8 = ((ByteBufferBuilder) var7.next());
            var8.close();
        }
        RenderSystem.setShaderFog(var4.getBuffer(FogRenderer.FogMode.WORLD));
        break label_28;
        Object var19 = RenderTypes.glintTranslucent();
        Object var20 = var5.values().iterator();
        while (var20.hasNext()) {
            Object var21 = ((ByteBufferBuilder) var20.next());
            var21.close();
        }
        RenderSystem.setShaderFog(var4.getBuffer(FogRenderer.FogMode.WORLD));
        throw var19;
        label_28:
    }

    public static void renderScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, Color arg7) {
        renderScaledText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, false, arg7, false);
    }

    public static void renderScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, Color arg7, boolean arg8) {
        renderScaledText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, false, arg7, arg8);
    }

    public static void renderCenteredScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, Color arg7) {
        renderScaledText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true, arg7, false);
    }

    public static void renderCenteredScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, Color arg7, boolean arg8) {
        renderScaledText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true, arg7, arg8);
    }

    public static void renderScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, boolean arg7, Color arg8) {
        renderScaledText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, false);
    }

    public static void renderScaledText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, int arg5, boolean arg6, boolean arg7, Color arg8, boolean arg9) {
        Object var13 = mc.gameRenderer.mainCamera().position();
        float var14 = ((float) Math.sqrt(var13.distanceToSqr(arg2, arg3, arg4)));
        float var15 = (0.0018f + ((((float) arg5) / 10000.0f) * Math.max(var14, 8.0f)));
        renderText(arg0, arg1, arg2, arg3, arg4, var15, arg6, arg7, arg8, arg9);
    }

    public static void renderText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, float arg5, boolean arg6, Color arg7) {
        renderText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, false, arg7, false);
    }

    public static void renderText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, float arg5, boolean arg6, Color arg7, boolean arg8) {
        renderText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, false, arg7, arg8);
    }

    public static void renderText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, float arg5, boolean arg6, boolean arg7, Color arg8) {
        renderText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, false);
    }

    public static void renderText(PoseStack arg0, String arg1, double arg2, double arg3, double arg4, float arg5, boolean arg6, boolean arg7, Color arg8, boolean arg9) {
        Object var14 = mc.gameRenderer.mainCamera().position();
        Object var15 = new Vec3((arg2 - var14.x), (arg3 - var14.y), (arg4 - var14.z));
        arg0.pushPose();
        arg0.translate(var15.x, var15.y, var15.z);
        arg0.mulPose(mc.gameRenderer.mainCamera().rotation());
        arg0.scale(arg5, -(arg5), arg5);
        Object var16 = arg1.split("\n");
        float var17 = ((float) LightningClient.FONT_MANAGER.getHeight());
        float var18 = (var17 + 2.0f);
        float var19 = ((((float) (var16.length - 1)) * var18) + var17);
        if (!(!arg7)) {
            break label_1;
        }
        label_1:
        var tmp0 = ((((float) -(var16.length)) * var18) + 1.0f);
        float var13 = tmp0;
        float var20 = tmp0;
        if (!arg6) break label_13;
        float var21 = 0.0f;
        Object var22 = var16;
        int var23 = var22.length;
        int var24 = 0;
        while (var24 < var23) {
            Object var25 = var22[var24];
            float var26 = ((float) LightningClient.FONT_MANAGER.getWidth(var25));
            if (var26 <= var21) {
            } else {
                var21 = var26;
            }
            var24++;
        }
        var22 = ((-(var21) / 2.0f) - 2.0f);
        var23 = (var13 - 2.0f);
        var24 = ((var21 / 2.0f) + 2.0f);
        var25 = ((var13 + var19) + 2.0f);
        var26 = new Color(0, 0, 0, 100).getRGB();
        Object var27 = arg0.last().pose();
        Object var28 = ByteBufferBuilder.exactlySized((4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var29 = new BufferBuilder(var28, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            var29.addVertex(var27, var22, var23, 0.0f).setColor(var26);
            var29.addVertex(var27, var22, var25, 0.0f).setColor(var26);
            var29.addVertex(var27, var24, var25, 0.0f).setColor(var26);
            var29.addVertex(var27, var24, var23, 0.0f).setColor(var26);
            Object var30 = var29.build();
            draw(NO_DEPTH_QUADS, var30);
            if (var28 == null) break label_13;
            var28.close();
        } catch (Throwable ex8) {
            var29 = ex8;
            if (var28 == null) break label_12;
            try {
                var28.close();
            } catch (Throwable ex11) {
                var30 = ex11;
                var29.addSuppressed(var30);
            }
            label_12:
            throw var29;
        }
        label_13:
        var21 = var16;
        var22 = var21.length;
        var23 = 0;
        while (var23 < var22) {
            var24 = var21[var23];
            if (!(!arg9)) {
                LightningClient.FONT_MANAGER.drawText(arg0, var24, (((float) -(LightningClient.FONT_MANAGER.getWidth(var24))) / 2.0f), var13, arg8, true);
            } else {
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var24, (((float) -(LightningClient.FONT_MANAGER.getWidth(var24))) / 2.0f), var13, arg8, false);
            }
            var13 = (var13 + var18);
            var23++;
        }
        arg0.popPose();
    }

    public static RenderType getNoDepthTexturedType(Identifier arg0) {
        return ((RenderType) NO_DEPTH_TEXTURE_TYPES.computeIfAbsent(arg0, (arg0) -> {
    var tmp0 = new RenderPipeline.Snippet[1];
    tmp0[0] = RenderPipelinesAccessor.getGuiTexturedSnippet();
    return RenderType.create(("night_no_depth_tex_" + arg0.getNamespace() + "_" + arg0.getPath().replace(47, 95)), RenderSetup.builder(RenderPipeline.builder(tmp0).withLocation("lightning/no_depth_tex").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).withTexture("Sampler0", arg0).sortOnUpload().createRenderSetup());
}));
    }

    public static void renderTexture(PoseStack arg0, Identifier arg1, float arg2, float arg3, float arg4, float arg5, int arg6) {
        Object var7 = arg0.last().pose();
        Object var8 = getNoDepthTexturedType(arg1);
        Object var9 = ByteBufferBuilder.exactlySized((4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
        try {
            Object var10 = new BufferBuilder(var9, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            var10.addVertex(var7, arg2, arg5, 0.0f).setUv(0.0f, 1.0f).setColor(arg6);
            var10.addVertex(var7, arg4, arg5, 0.0f).setUv(1.0f, 1.0f).setColor(arg6);
            var10.addVertex(var7, arg4, arg3, 0.0f).setUv(1.0f, 0.0f).setColor(arg6);
            var10.addVertex(var7, arg2, arg3, 0.0f).setUv(0.0f, 0.0f).setColor(arg6);
            Object var11 = var10.build();
            draw(var8, var11);
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

    public static void prepare() {
        QUADS.clear();
        DEBUG_LINES.clear();
        SHINE_QUADS.clear();
        SHINE_DEBUG_LINES.clear();
        SHADER_QUADS.clear();
        SHADER_DEBUG_LINES.clear();
        TRACER_DEBUG_LINES.clear();
        Glint.QUADS.clear();
        RENDERING = true;
    }

    public static void draw(List arg0, List arg1, boolean arg2) {
        if (arg0.isEmpty()) break label_9;
        int var6 = vertexCount(arg0);
        Object var5 = ByteBufferBuilder.exactlySized((var6 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        Object var4 = new BufferBuilder(var5, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Object var7 = arg0.iterator();
        while (var7.hasNext()) {
            Object var8 = ((Renderer3D.VertexCollection) var7.next());
            var8.quad(var4);
        }
        Object var3 = var4.build();
        draw((!(!arg2) ? Renderer3D.NO_DEPTH_QUADS_SHINE : Renderer3D.NO_DEPTH_QUADS), var3);
        if (var5 == null) break label_9;
        var5.close();
        break label_9;
        Object var9 = null;
        if (var5 != null) {
            var5.close();
        }
        throw var9;
        label_9:
        if (arg1.isEmpty()) break label_17;
        var6 = (vertexCount(arg1) * 2);
        var5 = ByteBufferBuilder.exactlySized((var6 * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
        var4 = new BufferBuilder(var5, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        buildLines(var4, arg1);
        var3 = var4.build();
        draw((!(!arg2) ? Renderer3D.NO_DEPTH_LINES_SHINE : Renderer3D.NO_DEPTH_LINES), var3);
        if (var5 == null) break label_17;
        var5.close();
        break label_17;
        Object var10 = null;
        if (var5 != null) {
            var5.close();
        }
        throw var10;
        label_17:
        RenderSystem.getDevice();
        arg0.clear();
        arg1.clear();
    }

    static int vertexCount(List arg0) {
        int var1 = 0;
        Object var2 = arg0.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Renderer3D.VertexCollection) var2.next());
            var1 = (var1 + var3.vertices().length);
        }
        return var1;
    }

    public static void buildLines(BufferBuilder arg0, List arg1) {
        Object var2 = arg1.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Renderer3D.VertexCollection) var2.next());
            Object var4 = var3.vertices();
            int var5 = 0;
            while ((var5 + 1) < var4.length) {
                Object var6 = var4[var5];
                Object var7 = var4[(var5 + 1)];
                float var8 = (var7.x - var6.x);
                float var9 = (var7.y - var6.y);
                float var10 = (var7.z - var6.z);
                float var11 = (((var8 * var8) + (var9 * var9)) + (var10 * var10));
                float var12 = 0.05f;
                if (var11 > 1.0E-6f) {
                    float var13 = ((float) Math.sqrt(((double) var11)));
                    float var14 = (1.0f / var13);
                    var8 = (var8 * var14);
                    var9 = (var9 * var14);
                    var10 = (var10 * var14);
                    var12 = Math.min(0.05f, (var13 * 0.45f));
                } else {
                    var8 = 0.0f;
                    var9 = 1.0f;
                    var10 = 0.0f;
                }
                arg0.addVertex(var6.matrix, var6.x, var6.y, var6.z).setColor(var6.color).setNormal((var8 * var12), (var9 * var12), (var10 * var12)).setLineWidth(var6.lineWidth);
                arg0.addVertex(var7.matrix, var7.x, var7.y, var7.z).setColor(var7.color).setNormal((-(var8) * var12), (-(var9) * var12), (-(var10) * var12)).setLineWidth(var7.lineWidth);
                var5 += 2;
            }
        }
    }

    public static boolean isFrustumVisible(AABB arg0) {
        return mc.gameRenderer.mainCamera().getCullFrustum().isVisible(arg0);
    }

    private static Vec3 cameraTransform(Vec3 arg0) {
        Object var1 = mc.gameRenderer.mainCamera().position();
        return new Vec3((arg0.x - var1.x), (arg0.y - var1.y), (arg0.z - var1.z));
    }

    private static AABB cameraTransform(AABB arg0) {
        Object var1 = mc.gameRenderer.mainCamera().position();
        return new AABB((arg0.minX - var1.x), (arg0.minY - var1.y), (arg0.minZ - var1.z), (arg0.maxX - var1.x), (arg0.maxY - var1.y), (arg0.maxZ - var1.z));
    }

    static {
        RENDERING = false;
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getDebugFilledSnippet();
        NO_DEPTH_QUADS = RenderType.create("night_no_depth_quads", RenderSetup.builder(RenderPipeline.builder(tmp0).withLocation("lightning/no_depth_quads").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build()).sortOnUpload().createRenderSetup());
        var tmp1 = new RenderPipeline.Snippet[1];
        tmp1[0] = RenderPipelinesAccessor.getLinesSnippet();
        NO_DEPTH_LINES = RenderType.create("night_no_depth_lines", RenderSetup.builder(RenderPipeline.builder(tmp1).withLocation("lightning/no_depth_lines").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
        SHINE_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_CONSTANT_ALPHA);
        var tmp2 = new RenderPipeline.Snippet[1];
        tmp2[0] = RenderPipelinesAccessor.getDebugFilledSnippet();
        NO_DEPTH_QUADS_SHINE = RenderType.create("night_no_depth_quads_shine", RenderSetup.builder(RenderPipeline.builder(tmp2).withLocation("lightning/no_depth_quads_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).sortOnUpload().createRenderSetup());
        var tmp3 = new RenderPipeline.Snippet[1];
        tmp3[0] = RenderPipelinesAccessor.getLinesSnippet();
        NO_DEPTH_LINES_SHINE = RenderType.create("night_no_depth_lines_shine", RenderSetup.builder(RenderPipeline.builder(tmp3).withLocation("lightning/no_depth_lines_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).createRenderSetup());
        QUADS = new ArrayList();
        DEBUG_LINES = new ArrayList();
        SHINE_QUADS = new ArrayList();
        SHINE_DEBUG_LINES = new ArrayList();
        SHADER_QUADS = new ArrayList();
        SHADER_DEBUG_LINES = new ArrayList();
        TRACER_DEBUG_LINES = new ArrayList();
        QUAD_INSTANCE = new QuadInstance();
        ITEM_RENDER_STATE = new ItemStackRenderState();
        NO_DEPTH_ITEM_CUTOUT = new HashMap();
        NO_DEPTH_ITEM_TRANSLUCENT = new HashMap();
        NO_DEPTH_TEXTURE_TYPES = new HashMap();
    }


    public record VertexCollection(Renderer3D.Vertex[] vertices) {

        public VertexCollection(Renderer3D.Vertex... arg0) {
            this.vertices = arg0;
        }

        public void quad(BufferBuilder arg0) {
            Object var2 = this.vertices;
            int var3 = var2.length;
            int var4 = 0;
            while (var4 < var3) {
                Object var5 = var2[var4];
                arg0.addVertex(var5.matrix, var5.x, var5.y, var5.z).setColor(var5.color);
                var4++;
            }
        }

        public Renderer3D.Vertex[] vertices() {
            return this.vertices;
        }

    }

    public record Vertex(Matrix4f matrix, float x, float y, float z, int color, float lineWidth) {

        public Vertex(Matrix4f arg0, float arg1, float arg2, float arg3, int arg4) {
            this(arg0, arg1, arg2, arg3, arg4, 1.75f);
        }

        public Vertex(Matrix4f arg0, float arg1, float arg2, float arg3, int arg4, float arg5) {
            this.matrix = arg0;
            this.x = arg1;
            this.y = arg2;
            this.z = arg3;
            this.color = arg4;
            this.lineWidth = arg5;
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

        public int color() {
            return this.color;
        }

        public float lineWidth() {
            return this.lineWidth;
        }

    }
}
