package lightning.utils.graphics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public class KillEffectMemeRenderer implements IMinecraft {
    public static final KillEffectMemeRenderer INSTANCE;
    private static final Identifier CIRCLE_TEXTURE_ID;
    private static final Identifier ARROW_TEXTURE_ID;
    private static final int BLINK_COUNT = 3;
    private static final float BLINK_ON_SEC = 0.18f;
    private static final float BLINK_OFF_SEC = 0.12f;
    private static final float CYCLE_SEC = 0.3f;
    private static RenderType circleLayer;
    private static RenderType arrowLayer;
    private static final RenderPipeline NO_DEPTH_MEME_PIPELINE;
    private final List instances;
    private long lastNanos;

    public KillEffectMemeRenderer() {
        this.instances = new ArrayList();
        this.lastNanos = 0L;
    }

    private static RenderType buildLayer(String arg0, Identifier arg1) {
        return RenderType.create(("night_" + arg0), RenderSetup.builder(NO_DEPTH_MEME_PIPELINE).withTexture("Sampler0", arg1).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(RenderSetup.OutlineProperty.NONE).createRenderSetup());
    }

    private static RenderType getCircleLayer() {
        if (circleLayer != null) return circleLayer;
        circleLayer = buildLayer("kill_effect_meme_circle", CIRCLE_TEXTURE_ID);
        return circleLayer;
    }

    private static RenderType getArrowLayer() {
        if (arrowLayer != null) return arrowLayer;
        arrowLayer = buildLayer("kill_effect_meme_arrow", ARROW_TEXTURE_ID);
        return arrowLayer;
    }

    public void spawn(Vector3f arg0, float arg1) {
        Object var3 = new KillEffectMemeRenderer.MemeInstance();
        var3.pos.set(arg0);
        var3.size = arg1;
        this.instances.add(var3);
    }

    public void render(PoseStack arg0) {
        if (this.instances.isEmpty()) {
            this.lastNanos = 0L;
            return;
        }
        if (mc.player == null) break label_1;
        if (mc.level != null) break label_2;
        label_1:
        return;
        label_2:
        long var2 = System.nanoTime();
        if (this.lastNanos != 0L) break label_3;
        break label_4;
        label_3:
        label_4:
        float var4 = Math.min((((float) (var2 - this.lastNanos)) / 1.0E9f), 0.1f);
        this.lastNanos = var2;
        float var5 = 0.90000004f;
        Object var6 = this.instances.iterator();
        while (var6.hasNext()) {
            Object var7 = ((KillEffectMemeRenderer.MemeInstance) var6.next());
            var7.age = (var7.age + var4);
            if (var7.age < var5) {
                break label_5;
            }
            var6.remove();
        }
        if (this.instances.isEmpty()) return;
        var7 = arg0.last();
        Object var8 = mc.gameRenderer.mainCamera();
        Object var9 = new Vector3f(var8.upVector());
        Object var10 = new Vector3f(var8.leftVector());
        Object var11 = var8.position();
        Object var12 = new Vector3f(((float) var11.x), ((float) var11.y), ((float) var11.z));
        Object var13 = new Vector3f();
        Object var14 = getCircleLayer();
        Object var15 = new ByteBufferBuilder(1024);
        try {
            Object var16 = new BufferBuilder(var15, var14.primitiveTopology(), var14.format());
            Object var17 = this.instances.iterator();
            while (var17.hasNext()) {
                Object var18 = ((KillEffectMemeRenderer.MemeInstance) var17.next());
                if ((var18.age % 0.3f) >= 0.18f) {
                    break label_10;
                }
                var13.set(var18.pos).sub(var12);
                emitBillboard(var16, var7, var13, var9, var10, var18.size, 255);
            }
            var17 = var16.build();
            if (var17 != null) {
                Renderer3D.draw(var14, var17);
            }
            var15.close();
        } catch (Throwable ex14) {
            var16 = ex14;
            try {
                var15.close();
            } catch (Throwable ex17) {
                var17 = ex17;
                var16.addSuppressed(var17);
            }
            throw var16;
        }
        var15 = new Vector3f();
        var16 = getArrowLayer();
        var17 = new ByteBufferBuilder(1024);
        try {
            var18 = new BufferBuilder(var17, var16.primitiveTopology(), var16.format());
            Object var19 = this.instances.iterator();
            while (var19.hasNext()) {
                Object var20 = ((KillEffectMemeRenderer.MemeInstance) var19.next());
                if ((var20.age % 0.3f) >= 0.18f) {
                    break label_21;
                }
                var13.set(var20.pos).sub(var12);
                var15.set(var10).mul((-(var20.size) * 1.1f)).add(((var9.x * var20.size) * 1.1f), ((var9.y * var20.size) * 1.1f), ((var9.z * var20.size) * 1.1f)).add(var13);
                emitBillboard(var18, var7, var15, var9, var10, (var20.size * 0.8f), 255);
            }
            var19 = var18.build();
            if (var19 != null) {
                Renderer3D.draw(var16, var19);
            }
            var17.close();
        } catch (Throwable ex25) {
            var18 = ex25;
            try {
                var17.close();
            } catch (Throwable ex28) {
                var19 = ex28;
                var18.addSuppressed(var19);
            }
            throw var18;
        }
    }

    private static void emitBillboard(BufferBuilder arg0, PoseStack.Pose arg1, Vector3f arg2, Vector3f arg3, Vector3f arg4, float arg5, int arg6) {
        Object var7 = new Vector3f(arg3).mul(arg5);
        Object var8 = new Vector3f(arg4).mul(arg5);
        Object var9 = new Vector3f(arg2).sub(var7).sub(var8);
        Object var10 = new Vector3f(arg2).sub(var7).add(var8);
        Object var11 = new Vector3f(arg2).add(var7).add(var8);
        Object var12 = new Vector3f(arg2).add(var7).sub(var8);
        int var13 = ((arg6 << 24) | 16777215);
        arg0.addVertex(arg1, var9.x, var9.y, var9.z).setColor(var13).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(arg1, 0.0f, 1.0f, 0.0f);
        arg0.addVertex(arg1, var10.x, var10.y, var10.z).setColor(var13).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(arg1, 0.0f, 1.0f, 0.0f);
        arg0.addVertex(arg1, var11.x, var11.y, var11.z).setColor(var13).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(arg1, 0.0f, 1.0f, 0.0f);
        arg0.addVertex(arg1, var12.x, var12.y, var12.z).setColor(var13).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(arg1, 0.0f, 1.0f, 0.0f);
    }

    static {
        INSTANCE = new KillEffectMemeRenderer();
        CIRCLE_TEXTURE_ID = Identifier.fromNamespaceAndPath("lightning", "textures/effect/meme_circle.png");
        ARROW_TEXTURE_ID = Identifier.fromNamespaceAndPath("lightning", "textures/effect/meme_arrow.png");
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getEntitySnippet();
        NO_DEPTH_MEME_PIPELINE = RenderPipeline.builder(tmp0).withLocation("lightning/no_depth_meme").withShaderDefine("ALPHA_CUTOUT", 0.1f).withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build();
    }


    final class MemeInstance {
        final Vector3f pos;
        float age;
        float size;

        private MemeInstance() {
            this.pos = new Vector3f();
        }

    }
}
