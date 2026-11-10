package lightning.utils.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.awt.Color;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class ClickGuiBackground {
    private static final Identifier CHAIN_ID;
    private static final Identifier TARGET_ID;
    private static final Identifier TEXTURE_ID;
    private static final String UNIFORM_NAME = "ClickGuiBgConfig";
    private static final int UBO_SIZE;
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static ClickGuiBackground.BgTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed(int arg0, float arg1, float arg2, Color arg3, boolean arg4) {
        long var5 = System.currentTimeMillis();
        if ((var5 - lastProcessedAtMs) < 8L) return;
        lastProcessedAtMs = var5;
        Object var7 = Minecraft.getInstance().getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (var7 != chain) {
            chain = var7;
            uboBuffer = null;
        }
        if (chain == null) return;
        writeUniforms(arg0, arg1, arg2, arg3, arg4);
        chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        Object var8 = ((RenderTarget) ((PostEffectProcessorAccessor) chain).getPersistentTargets().get(TARGET_ID));
        if ((var8 == null) || (var8.getColorTextureView() == null)) {
            return;
        }
        if (texture != null) {
            if (!(texture.wraps(var8.getColorTextureView()))) {
            }
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new ClickGuiBackground.BgTexture(var8.getColorTexture(), var8.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
        }
    }

    private static void writeUniforms(int arg0, float arg1, float arg2, Color arg3, boolean arg4) {
        Object var5 = ((PostEffectProcessorAccessor) chain).getPasses().iterator();
        while (var5.hasNext()) {
            Object var6 = ((PostPass) var5.next());
            Object var8 = ((PostPassAccessor) var6).night$getCustomUniforms();
            if (var8.containsKey("ClickGuiBgConfig")) break label_1;
            break label_0;
            label_1:
            if (uboBuffer != null) {
                if (var8.get("ClickGuiBgConfig") == uboBuffer) break label_3;
            }
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning ClickGUI BG UBO", 136, ((long) UBO_SIZE));
            uboBuffer = tmp0;
            var tmp1 = ((GpuBuffer) var8.put("ClickGuiBgConfig", tmp0));
            Object var7 = tmp1;
            if (tmp1 == null) break label_3;
            var7.close();
            label_3:
            long var9 = System.currentTimeMillis();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds = (phaseSeconds + ((((float) (var9 - lastPhaseUpdateMs)) / 1000.0f) * arg1));
            }
            lastPhaseUpdateMs = var9;
            float var11 = (((float) arg3.getRed()) / 255.0f);
            float var12 = (((float) arg3.getGreen()) / 255.0f);
            float var13 = (((float) arg3.getBlue()) / 255.0f);
            float var14 = (!(!arg4) ? 1.0f : 0.0f);
            Object var15 = MemoryStack.stackPush();
            Object var16 = Std140Builder.onStack(var15, UBO_SIZE).putVec4(phaseSeconds, ((float) arg0), arg2, arg1).putVec4(var11, var12, var13, var14).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), var16);
            if (var15 == null) {
                break label_0;
            }
            var15.close();
            break label_13;
            Object var17 = null;
            if (var15 == null) {
                break label_0;
            }
            var15.close();
            throw var17;
            label_13:
        }
    }

    public static void renderBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, float arg4, float arg5, Color arg6, boolean arg7) {
        if ((arg5 <= 0.001f) || (arg3 < 0)) {
            return;
        }
        ensureProcessed(arg3, arg4, arg5, arg6, arg7);
        if (texture == null) return;
        int var8 = Math.min(255, Math.max(0, Math.round((arg5 * 255.0f))));
        int var9 = ((var8 << 24) | 16777215);
        arg0.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, arg1, arg2, arg1, arg2, arg1, arg2, var9);
    }

    static {
        CHAIN_ID = Identifier.fromNamespaceAndPath("lightning", "clickgui_bg");
        TARGET_ID = Identifier.withDefaultNamespace("clickgui_bg_target");
        TEXTURE_ID = Identifier.fromNamespaceAndPath("lightning", "clickgui_bg_texture");
        UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().align(16).get();
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }


    class BgTexture extends AbstractTexture {
        BgTexture(GpuTexture arg0, GpuTextureView arg1) {
            this.texture = arg0;
            this.textureView = arg1;
        }

        boolean wraps(GpuTextureView arg0) {
            return this.textureView == arg0;
        }

        public void close() {
        }

    }
}
