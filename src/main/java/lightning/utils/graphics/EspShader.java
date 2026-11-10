package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import lightning.LightningClient;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.modules.impl.core.ColorModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class EspShader implements IMinecraft {
    public static final String[] MODES;
    private static final Identifier FRAGMENT;
    private static final BindGroupLayout ESP_SETTINGS_LAYOUT;
    private static final RenderPipeline QUADS_PIPELINE;
    private static final RenderPipeline LINES_PIPELINE;
    private static final int UBO_SIZE;
    private static final String OUTLINE_UNIFORM = "EspSettings";
    private static final int OUTLINE_UBO_SIZE;
    private static final String STAR_GLOW_UNIFORM = "StarGlowConfig";
    private static final String STAR_GLOW_UNIFORM_V = "StarGlowConfigV";
    private static final int STAR_GLOW_UBO_SIZE;
    private static GpuBuffer starGlowUboBuffer;
    private static GpuBuffer starGlowUboBufferV;
    private static final String OUTLINE_GLOW_UNIFORM = "OutlineGlowConfig";
    private static final String OUTLINE_GLOW_UNIFORM_V = "OutlineGlowConfigV";
    private static final int OUTLINE_GLOW_UBO_SIZE;
    private static GpuBuffer outlineGlowUboBuffer;
    private static GpuBuffer outlineGlowUboBufferV;
    private static final String BLOOM_UNIFORM = "BloomConfig";
    private static final int BLOOM_UBO_SIZE;
    private static GpuBuffer bloomUboBuffer;
    private static GpuBuffer uboBuffer;
    private static GpuBuffer outlineUboBuffer;
    private static EspShader.Settings settings;
    public static final String[] SHADER_ACTIVE_MODES;

    public static void writeStarGlowSettings(PostChain arg0, float arg1) {
        float var2 = Math.min((4.0f + (arg1 * 0.05f)), 14.0f);
        Object var3 = ((PostEffectProcessorAccessor) arg0).getPasses().iterator();
        while (var3.hasNext()) {
            Object var4 = ((PostPass) var3.next());
            Object var8 = ((PostPassAccessor) var4).night$getCustomUniforms();
            if (!(var8.containsKey("StarGlowConfig"))) break label_8;
            if (starGlowUboBuffer == null) break label_1;
            if (var8.get("StarGlowConfig") == starGlowUboBuffer) break label_2;
            label_1:
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning star glow H shader UBO", 136, ((long) STAR_GLOW_UBO_SIZE));
            starGlowUboBuffer = tmp0;
            var tmp1 = ((GpuBuffer) var8.put("StarGlowConfig", tmp0));
            Object var7 = tmp1;
            if (tmp1 == null) break label_2;
            var7.close();
            label_2:
            Object var6 = MemoryStack.stackPush();
            Object var5 = Std140Builder.onStack(var6, STAR_GLOW_UBO_SIZE).putVec4(arg1, var2, 1.0f, 0.0f).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBuffer.slice(), var5);
            if (var6 == null) break label_8;
            var6.close();
            break label_8;
            Object var9 = null;
            if (var6 != null) {
                var6.close();
            }
            throw var9;
            label_8:
            if (!(var8.containsKey("StarGlowConfigV"))) {
                break label_0;
            }
            if (starGlowUboBufferV != null) {
                if (var8.get("StarGlowConfigV") == starGlowUboBufferV) break label_11;
            }
            var tmp2 = RenderSystem.getDevice().createBuffer(() -> "Lightning star glow V shader UBO", 136, ((long) STAR_GLOW_UBO_SIZE));
            starGlowUboBufferV = tmp2;
            var tmp3 = ((GpuBuffer) var8.put("StarGlowConfigV", tmp2));
            var7 = tmp3;
            if (tmp3 == null) break label_11;
            var7.close();
            label_11:
            var6 = MemoryStack.stackPush();
            var5 = Std140Builder.onStack(var6, STAR_GLOW_UBO_SIZE).putVec4(arg1, var2, 0.0f, 1.0f).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBufferV.slice(), var5);
            if (var6 == null) {
                break label_0;
            }
            var6.close();
            break label_18;
            Object var10 = null;
            if (var6 == null) {
                break label_0;
            }
            var6.close();
            throw var10;
            label_18:
        }
    }

    public static int modeIndex(String arg0) {
        int var1 = 0;
        while (var1 < MODES.length) {
            if (!(MODES[var1].equalsIgnoreCase(arg0))) {
            } else {
                return var1;
            }
            var1++;
        }
        return 0;
    }

    public static void setSettings(EspShader.Settings arg0) {
        settings = arg0;
    }

    public static void writeOutlineSettings(PostChain arg0, EspShader.Settings arg1, float arg2) {
        float var3 = Math.min((6.0f + (arg2 * 18.0f)), 24.0f);
        Object var4 = ((PostEffectProcessorAccessor) arg0).getPasses().iterator();
        while (var4.hasNext()) {
            Object var5 = ((PostPass) var4.next());
            Object var11 = ((PostPassAccessor) var5).night$getCustomUniforms();
            if (!(var11.containsKey("BloomConfig"))) break label_30;
            if (bloomUboBuffer == null) break label_1;
            if (var11.get("BloomConfig") == bloomUboBuffer) break label_2;
            label_1:
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning outline bloom UBO", 136, ((long) BLOOM_UBO_SIZE));
            bloomUboBuffer = tmp0;
            var tmp1 = ((GpuBuffer) var11.put("BloomConfig", tmp0));
            Object var10 = tmp1;
            if (tmp1 == null) break label_2;
            var10.close();
            label_2:
            if (LightningClient.MODULE_MANAGER == null) break label_3;
            break label_4;
            label_3:
            label_4:
            Object var9 = null;
            if (var9 == null) break label_5;
            break label_6;
            label_5:
            label_6:
            float var12 = 3.0f;
            if (var9 == null) break label_7;
            float var13 = (var9.bloomGlowInside.getValue() ? 1.0f : 0.0f);
            if (var9 != null) {
            } else {
            }
            float var14 = 1.0f;
            if (var9 != null) {
            } else {
            }
            float var15 = 1.0f;
            if (var9 != null) {
            } else {
            }
            float var16 = 1.0f;
            if (var9 != null) {
            } else {
            }
            float var17 = 1.0f;
            if (var9 != null) {
            } else {
            }
            float var18 = 0.0f;
            if (var9 != null) {
            } else {
            }
            float var19 = 200.0f;
            if (var9 != null) {
            } else {
            }
            Object var20 = Color.RED;
            float var21 = ((float) (System.currentTimeMillis() % 2000000L));
            Object var22 = MemoryStack.stackPush();
            try {
                Object var23 = Std140Builder.onStack(var22, BLOOM_UBO_SIZE).putVec4(var12, var13, var14, var15).putVec4(var16, var17, var18, var19).putVec4((((float) var20.getRed()) / 255.0f), (((float) var20.getGreen()) / 255.0f), (((float) var20.getBlue()) / 255.0f), (((float) var20.getAlpha()) / 255.0f)).putVec4(var21, 0.0f, ((float) mc.getWindow().getWidth()), ((float) mc.getWindow().getHeight())).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(bloomUboBuffer.slice(), var23);
                if (var22 == null) break label_30;
                var22.close();
            } catch (Throwable ex25) {
                var23 = ex25;
                if (var22 == null) break label_29;
                try {
                    var22.close();
                } catch (Throwable ex28) {
                    Object var24 = ex28;
                    var23.addSuppressed(var24);
                }
                label_29:
                throw var23;
            }
            label_30:
            if (!(var11.containsKey("OutlineGlowConfig"))) break label_44;
            if (outlineGlowUboBuffer != null) {
                if (var11.get("OutlineGlowConfig") == outlineGlowUboBuffer) break label_32;
            }
            var tmp2 = RenderSystem.getDevice().createBuffer(() -> "Lightning outline glow H UBO", 136, ((long) OUTLINE_GLOW_UBO_SIZE));
            outlineGlowUboBuffer = tmp2;
            var tmp3 = ((GpuBuffer) var11.put("OutlineGlowConfig", tmp2));
            var10 = tmp3;
            if (tmp3 == null) break label_32;
            var10.close();
            label_32:
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            var9 = null;
            if (var9 != null && var9.glowColor != null) {
            } else {
            }
            var12 = Color.MAGENTA;
            var13 = MemoryStack.stackPush();
            try {
                var14 = Std140Builder.onStack(var13, OUTLINE_GLOW_UBO_SIZE).putVec4(arg2, var3, 1.0f, 0.0f).putVec4((((float) var12.getRed()) / 255.0f), (((float) var12.getGreen()) / 255.0f), (((float) var12.getBlue()) / 255.0f), (((float) var12.getAlpha()) / 255.0f)).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBuffer.slice(), var14);
                if (var13 == null) break label_44;
                var13.close();
            } catch (Throwable ex39) {
                var14 = ex39;
                if (var13 == null) break label_43;
                try {
                    var13.close();
                } catch (Throwable ex42) {
                    var15 = ex42;
                    var14.addSuppressed(var15);
                }
                label_43:
                throw var14;
            }
            label_44:
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            var9 = null;
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var8 = null;
            var12 = null;
            if (!(var11.containsKey("OutlineGlowConfigV"))) break label_65;
            if (outlineGlowUboBufferV != null) {
                if (var11.get("OutlineGlowConfigV") == outlineGlowUboBufferV) break label_50;
            }
            var tmp4 = RenderSystem.getDevice().createBuffer(() -> "Lightning outline glow V UBO", 136, ((long) OUTLINE_GLOW_UBO_SIZE));
            outlineGlowUboBufferV = tmp4;
            var tmp5 = ((GpuBuffer) var11.put("OutlineGlowConfigV", tmp4));
            var13 = tmp5;
            if (tmp5 == null) break label_50;
            var13.close();
            label_50:
            if (var9 != null && var9.glowColor != null) {
            } else {
            }
            var14 = Color.MAGENTA;
            if ((var9 == null) || (var9.glowColor == null)) break label_54;
            if (!(var9.glowColor.isRainbow())) {
                if (((!(var9.glowColor.isSync())) || (var8 == null)) || (!(var8.color.isRainbow()))) break label_54;
            }
            break label_55;
            label_54:
            label_55:
            var15 = 0;
            var16 = MemoryStack.stackPush();
            try {
                Object var7 = Std140Builder.onStack(var16, OUTLINE_GLOW_UBO_SIZE).putVec4(arg2, var3, (!(!var15) ? 1.0f : 0.0f), 1.0f).putVec4((((float) var14.getRed()) / 255.0f), (((float) var14.getGreen()) / 255.0f), (((float) var14.getBlue()) / 255.0f), (((float) var14.getAlpha()) / 255.0f)).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBufferV.slice(), var7);
                if (var16 == null) break label_65;
                var16.close();
            } catch (Throwable ex60) {
                var17 = ex60;
                if (var16 == null) break label_64;
                try {
                    var16.close();
                } catch (Throwable ex63) {
                    var18 = ex63;
                    var17.addSuppressed(var18);
                }
                label_64:
                throw var17;
            }
            label_65:
            if (!(var11.containsKey("EspSettings"))) {
                break label_0;
            }
            if (outlineUboBuffer != null) {
                if (var11.get("EspSettings") == outlineUboBuffer) break label_68;
            }
            var tmp6 = RenderSystem.getDevice().createBuffer(() -> "Lightning outline shader UBO", 136, ((long) OUTLINE_UBO_SIZE));
            outlineUboBuffer = tmp6;
            var tmp7 = ((GpuBuffer) var11.put("EspSettings", tmp6));
            Object var6 = tmp7;
            if (tmp7 == null) break label_68;
            var6.close();
            label_68:
            if ((var9 == null) || (var9.color == null)) break label_70;
            if (!(var9.color.isRainbow())) {
                if (((!(var9.color.isSync())) || (var8 == null)) || (!(var8.color.isRainbow()))) break label_70;
            }
            break label_71;
            label_70:
            label_71:
            var13 = 0;
            if (var8 != null) {
            } else {
            }
            var14 = 1.0f;
            var15 = MemoryStack.stackPush();
            var7 = Std140Builder.onStack(var15, OUTLINE_UBO_SIZE).putVec4(arg1.color0).putVec4(arg1.color1).putVec4(arg1.color2).putVec4(arg1.color3).putVec4(arg1.time, arg1.step, arg1.distance, ((float) arg1.effect)).putVec4(arg1.alpha, arg2, (!(!var13) ? 1.0f : 0.0f), var14).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineUboBuffer.slice(), var7);
            if (var15 == null) {
                break label_0;
            }
            var15.close();
            break label_82;
            Object var25 = (var8.rainbowSaturation.getValue().floatValue() / 100.0f);
            if (var15 == null) {
                break label_0;
            }
            var15.close();
            throw var25;
            label_82:
        }
    }

    public static EspShader.Settings createSettings(String arg0, float arg1, float arg2, float arg3, Color arg4, Color arg5, Color arg6, Color arg7, Color arg8, Vec3 arg9) {
        return createSettings(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, new Vector2f(0.0f, 0.0f));
    }

    public static EspShader.Settings createSettings(String arg0, float arg1, float arg2, float arg3, Color arg4, Color arg5, Color arg6, Color arg7, Color arg8, Vec3 arg9, Vector2f arg10) {
        int var11 = modeIndex(arg0);
        if (!var11) return null;
        float var12 = (((((float) (System.currentTimeMillis() % 2000000L)) / 1000.0f) * arg2) * (var11 == 1 ? arg3 : 1.0f));
        if (arg9 != null && mc.player != null) {
        } else {
        }
        float var13 = 1.0f;
        return new EspShader.Settings(var11, var12, arg3, (arg1 / 100.0f), var13, (var11 == 7 ? arg8 : arg4), arg5, arg6, arg7, arg10);
    }

    public static void draw(List arg0, List arg1) {
        draw(arg0, arg1, settings);
    }

    public static void draw(List arg0, List arg1, EspShader.Settings arg2) {
        if (arg2 != null) {
            if ((!(arg0.isEmpty())) || (!(arg1.isEmpty()))) break label_1;
        }
        arg0.clear();
        arg1.clear();
        return;
        label_1:
        Object var5 = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader UBO", 136, ((long) UBO_SIZE));
        Object var6 = MemoryStack.stackPush();
        try {
            Object var7 = Std140Builder.onStack(var6, UBO_SIZE).putVec4(arg2.color0).putVec4(arg2.color1).putVec4(arg2.color2).putVec4(arg2.color3).putVec2(((float) mc.getWindow().getWidth()), ((float) mc.getWindow().getHeight())).putFloat(arg2.time).putFloat(arg2.step).putFloat(arg2.alpha).putFloat(arg2.distance).putInt(arg2.effect).putVec2(arg2.offset.x, arg2.offset.y).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(var5.slice(), var7);
            if (var6 == null) break label_9;
            var6.close();
        } catch (Throwable ex4) {
            var7 = ex4;
            if (var6 == null) break label_8;
            try {
                var6.close();
            } catch (Throwable ex7) {
                Object var8 = ex7;
                var7.addSuppressed(var8);
            }
            label_8:
            throw var7;
        }
        label_9:
        if (arg0.isEmpty()) break label_17;
        var6 = Renderer3D.vertexCount(arg0);
        Object var4 = ByteBufferBuilder.exactlySized((var6 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        Object var3 = new BufferBuilder(var4, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var7 = arg0.iterator();
        while (var7.hasNext()) {
            var8 = ((Renderer3D.VertexCollection) var7.next());
            var8.quad(var3);
        }
        submit(QUADS_PIPELINE, var3.build(), var5);
        if (var4 == null) break label_17;
        var4.close();
        break label_17;
        Object var9 = null;
        if (var4 != null) {
            var4.close();
        }
        throw var9;
        label_17:
        if (arg1.isEmpty()) break label_23;
        var6 = (Renderer3D.vertexCount(arg1) * 2);
        var4 = ByteBufferBuilder.exactlySized((var6 * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
        var3 = new BufferBuilder(var4, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        Renderer3D.buildLines(var3, arg1);
        submit(LINES_PIPELINE, var3.build(), var5);
        if (var4 == null) break label_23;
        var4.close();
        break label_23;
        Object var10 = null;
        if (var4 != null) {
            var4.close();
        }
        throw var10;
        label_23:
        var5.close();
        arg0.clear();
        arg1.clear();
    }

    private static void submit(RenderPipeline arg0, MeshData arg1, GpuBuffer arg2) {
        if (arg1 == null) return;
        Object var3 = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f());
        Object var4 = arg1;
        try {
            Object var8 = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader vertices", 32, arg1.vertexBuffer());
            Object var9 = null;
            if (arg1.indexBuffer() != null) break label_2;
            Object var10 = RenderSystem.getSequentialBuffer(arg1.drawState().primitiveTopology());
            Object var7 = var10.getBuffer(arg1.drawState().indexCount());
            Object var6 = var10.type();
            break label_3;
            label_2:
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader indices", 64, arg1.indexBuffer());
            var9 = tmp0;
            var7 = tmp0;
            var6 = arg1.drawState().indexType();
            label_3:
            var10 = OutputTarget.MAIN_TARGET.getRenderTarget();
            if (RenderSystem.outputColorTextureOverride != null) {
            } else {
            }
            var tmp1 = var10.getColorTextureView();
            Object var5 = tmp1;
            Object var11 = tmp1;
            if (!(!var10.useDepth)) {
                if (RenderSystem.outputDepthTextureOverride != null) {
                    break label_8;
                }
                break label_8;
            }
            label_8:
            Object var12 = null;
            Object var13 = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Lightning ESP shader draw", var5, Optional.empty(), var12, OptionalDouble.empty());
            try {
                var13.setPipeline(arg0);
                RenderSystem.bindDefaultUniforms(var13);
                var13.setUniform("DynamicTransforms", var3);
                var13.setUniform("EspSettings", arg2);
                var13.setVertexBuffer(0, var8.slice());
                var13.setIndexBuffer(var7, var6);
                var13.drawIndexed(arg1.drawState().indexCount(), 1, 0, 0, 0);
                if (var13 == null) break label_16;
                var13.close();
            } catch (Throwable ex11) {
                Object var14 = ex11;
                if (var13 == null) break label_15;
                try {
                    var13.close();
                } catch (Throwable ex14) {
                    Object var15 = ex14;
                    var14.addSuppressed(var15);
                }
                label_15:
                throw var14;
            }
            label_16:
            var8.close();
            if (var9 != null) {
                var9.close();
            }
            if (var4 == null) break label_23;
            var4.close();
        } catch (Throwable ex18) {
            var5 = ex18;
            if (var4 == null) break label_22;
            try {
                var4.close();
            } catch (Throwable ex21) {
                var6 = ex21;
                var5.addSuppressed(var6);
            }
            label_22:
            throw var5;
        }
        label_23:
    }

    static {
        var tmp0 = new String[11];
        tmp0[0] = "None";
        tmp0[1] = "Gradient";
        tmp0[2] = "Rainbow";
        tmp0[3] = "Garmadon";
        tmp0[4] = "Nebula";
        tmp0[5] = "Blue";
        tmp0[6] = "Purple";
        tmp0[7] = "Glowing";
        tmp0[8] = "Pink";
        tmp0[9] = "Neekeri";
        tmp0[10] = "Image";
        MODES = tmp0;
        FRAGMENT = Identifier.fromNamespaceAndPath("lightning", "core/esp");
        ESP_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("EspSettings", UniformType.UNIFORM_BUFFER).build();
        var tmp1 = new RenderPipeline.Snippet[1];
        tmp1[0] = RenderPipelinesAccessor.getDebugFilledSnippet();
        QUADS_PIPELINE = RenderPipeline.builder(tmp1).withLocation("lightning/esp_shader_quads").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
        var tmp2 = new RenderPipeline.Snippet[1];
        tmp2[0] = RenderPipelinesAccessor.getLinesSnippet();
        LINES_PIPELINE = RenderPipeline.builder(tmp2).withLocation("lightning/esp_shader_lines").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
        UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putFloat().putInt().putVec2().align(16).get();
        OUTLINE_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
        STAR_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().get();
        OUTLINE_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
        BLOOM_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().get();
        SHADER_ACTIVE_MODES = ((String[]) Arrays.copyOfRange(MODES, 1, MODES.length));
    }


    public class Settings {
        public final int effect;
        public final float time;
        public final float step;
        public final float alpha;
        public final float distance;
        public final Vector4f color0;
        public final Vector4f color1;
        public final Vector4f color2;
        public final Vector4f color3;
        public final Vector2f offset;

        public Settings(int arg0, float arg1, float arg2, float arg3, float arg4, Color arg5, Color arg6, Color arg7, Color arg8) {
            this(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, new Vector2f(0.0f, 0.0f));
        }

        public Settings(int arg0, float arg1, float arg2, float arg3, float arg4, Color arg5, Color arg6, Color arg7, Color arg8, Vector2f arg9) {
            this.effect = arg0;
            this.time = arg1;
            this.step = arg2;
            this.alpha = arg3;
            this.distance = arg4;
            this.color0 = toVector(arg5);
            this.color1 = toVector(arg6);
            this.color2 = toVector(arg7);
            this.color3 = toVector(arg8);
            if (arg9 != null) {
            } else {
            }
            arg9.offset = new Vector2f(0.0f, 0.0f);
        }

        private static Vector4f toVector(Color arg0) {
            return new Vector4f((((float) arg0.getRed()) / 255.0f), (((float) arg0.getGreen()) / 255.0f), (((float) arg0.getBlue()) / 255.0f), (((float) arg0.getAlpha()) / 255.0f));
        }

    }
}
