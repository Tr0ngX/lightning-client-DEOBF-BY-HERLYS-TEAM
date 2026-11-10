package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import java.util.OptionalDouble;
import lightning.LightningClient;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class SkyShader implements IMinecraft {
    private static final Identifier FRAGMENT;
    private static final BindGroupLayout SKY_SETTINGS_LAYOUT;
    private static final RenderPipeline SKY_PIPELINE;
    private static final int UBO_SIZE;
    private static float time;

    public static void renderSky() {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        if (LightningClient.MODULE_MANAGER == null) break label_2;
        break label_3;
        label_2:
        label_3:
        Object var1 = null;
        Object var2 = null;
        if (((var1 == null) || (!(var1.isToggled()))) || (!(var1.modifySky.getValue()))) {
            return;
        }
        if (!(var1.skyMode.getValue().equalsIgnoreCase("Custom"))) return;
        int var3 = mc.getWindow().getWidth();
        int var4 = mc.getWindow().getHeight();
        if ((var3 <= 0) || (var4 <= 0)) {
            return;
        }
        float var5 = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        time = (time + ((var5 * 0.05f) * var1.speed.getValue().floatValue()));
        int var6 = (var1.customSkyMode.getValue().equalsIgnoreCase("Storm") ? 1 : 0);
        Object var7 = var1.skyTint.getColor();
        float var8 = (((float) var7.getRed()) / 255.0f);
        float var9 = (((float) var7.getGreen()) / 255.0f);
        float var10 = (((float) var7.getBlue()) / 255.0f);
        float var11 = (((float) var7.getAlpha()) / 255.0f);
        Object var12 = new Vector4f(var8, var9, var10, var11);
        Object var13 = new Vector4f((var8 * 0.1f), (var9 * 0.1f), (var10 * 0.1f), 1.0f);
        Object var14 = new Vector4f((var8 * 0.8f), (var9 * 0.8f), (var10 * 0.8f), 1.0f);
        float var15 = 0.0f;
        float var16 = 0.0f;
        float var17 = ((float) Math.toRadians(70.0));
        if (mc.gameRenderer != null && mc.gameRenderer.mainCamera() != null) {
            Object var18 = mc.gameRenderer.mainCamera();
            var15 = ((float) Math.toRadians(((double) var18.yRot())));
            var16 = ((float) Math.toRadians(((double) var18.xRot())));
        }
        if (mc.options != null) {
            var17 = ((float) Math.toRadians(((double) ((Integer) mc.options.fov().get()).intValue())));
        }
        var18 = new Vector4f(var15, var16, var17, 0.0f);
        Object var19 = RenderSystem.getDevice().createBuffer(() -> "Lightning Sky shader UBO", 136, ((long) UBO_SIZE));
        Object var20 = MemoryStack.stackPush();
        try {
            Object var21 = Std140Builder.onStack(var20, UBO_SIZE).putVec4(var12).putVec4(var13).putVec4(var14).putVec4(var18).putVec2(((float) var3), ((float) var4)).putFloat(time).putFloat(var1.intensity.getValue().floatValue()).putFloat(var1.speed.getValue().floatValue()).putInt(var6).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(var19.slice(), var21);
            if (var20 == null) break label_20;
            var20.close();
        } catch (Throwable ex15) {
            var21 = ex15;
            if (var20 == null) break label_19;
            try {
                var20.close();
            } catch (Throwable ex18) {
                Object var22 = ex18;
                var21.addSuppressed(var22);
            }
            label_19:
            throw var21;
        }
        label_20:
        var20 = OutputTarget.MAIN_TARGET.getRenderTarget();
        if (RenderSystem.outputColorTextureOverride != null) {
        } else {
        }
        var tmp0 = var20.getColorTextureView();
        Object var0 = tmp0;
        var21 = tmp0;
        if (!(!var20.useDepth)) {
            if (RenderSystem.outputDepthTextureOverride != null) {
                break label_25;
            }
            break label_25;
        }
        label_25:
        var22 = null;
        Object var23 = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Lightning Sky shader draw", var0, Optional.empty(), var22, OptionalDouble.empty());
        try {
            var23.setPipeline(SKY_PIPELINE);
            var23.setUniform("SkySettings", var19);
            var23.draw(3, 1, 0, 0);
            if (var23 == null) break label_33;
            var23.close();
        } catch (Throwable ex28) {
            Object var24 = ex28;
            if (var23 == null) break label_32;
            try {
                var23.close();
            } catch (Throwable ex31) {
                Object var25 = ex31;
                var24.addSuppressed(var25);
            }
            label_32:
            throw var24;
        }
        label_33:
        var19.close();
    }

    static {
        FRAGMENT = Identifier.fromNamespaceAndPath("lightning", "core/sky");
        SKY_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("SkySettings", UniformType.UNIFORM_BUFFER).build();
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getGlobalsSnippet();
        SKY_PIPELINE = RenderPipeline.builder(tmp0).withLocation("lightning/sky_shader").withVertexShader("core/screenquad").withFragmentShader(FRAGMENT).withBindGroupLayout(SKY_SETTINGS_LAYOUT).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
        UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putInt().align(16).get();
        time = 0.0f;
    }

}
