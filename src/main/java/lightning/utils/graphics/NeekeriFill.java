package lightning.utils.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.Arrays;
import lightning.LightningClient;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import lightning.modules.impl.core.ClickGuiModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class NeekeriFill {
    private static final Identifier CHAIN_ID;
    private static final Identifier PATTERN_TARGET_ID;
    private static final Identifier TEXTURE_ID;
    private static final String UNIFORM_NAME = "NeekeriConfig";
    private static final int UBO_SIZE;
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static NeekeriFill.PatternTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed() {
        long var0 = System.currentTimeMillis();
        if ((var0 - lastProcessedAtMs) < 10L) return;
        lastProcessedAtMs = var0;
        Object var2 = Minecraft.getInstance().getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (var2 == chain) break label_1;
        chain = var2;
        uboBuffer = null;
        label_1:
        if (chain == null) {
            return;
            try {
            }
            writeTime();
            chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        } catch (IllegalStateException ex4) {
            Object var3 = ex4;
            return;
        }
        var3 = ((RenderTarget) ((PostEffectProcessorAccessor) chain).getPersistentTargets().get(PATTERN_TARGET_ID));
        if ((var3 == null) || (var3.getColorTextureView() == null)) {
            return;
        }
        if (texture != null) {
            if (!(texture.wraps(var3.getColorTextureView()))) {
            }
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new NeekeriFill.PatternTexture(var3.getColorTexture(), var3.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
        }
    }

    private static void writeTime() {
        Object var0 = ((PostEffectProcessorAccessor) chain).getPasses().iterator();
        while (var0.hasNext()) {
            Object var1 = ((PostPass) var0.next());
            Object var3 = ((PostPassAccessor) var1).night$getCustomUniforms();
            if (var3.containsKey("NeekeriConfig")) break label_1;
            break label_0;
            label_1:
            if (uboBuffer != null) {
                if (var3.get("NeekeriConfig") == uboBuffer) break label_3;
            }
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning neekeri fill UBO", 136, ((long) UBO_SIZE));
            uboBuffer = tmp0;
            var tmp1 = ((GpuBuffer) var3.put("NeekeriConfig", tmp0));
            Object var2 = tmp1;
            if (tmp1 == null) break label_3;
            var2.close();
            label_3:
            Object var4 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
            long var5 = System.currentTimeMillis();
            float var7 = var4.neekeriSpeed.getValue().floatValue();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds = (phaseSeconds + ((((float) (var5 - lastPhaseUpdateMs)) / 1000.0f) * var7));
            }
            lastPhaseUpdateMs = var5;
            int var8 = Arrays.asList(EspShader.MODES).indexOf(var4.fillMode.getValue());
            if (var8 <= 0) {
                var8 = (EspShader.MODES.length - 1);
            }
            Object var9 = MemoryStack.stackPush();
            Object var10 = Std140Builder.onStack(var9, UBO_SIZE).putVec4(phaseSeconds, ((float) var8), 0.0f, 0.0f).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), var10);
            if (var9 == null) {
                break label_0;
            }
            var9.close();
            break label_12;
            Object var11 = null;
            if (var9 == null) {
                break label_0;
            }
            var9.close();
            throw var11;
            label_12:
        }
    }

    public static void fill(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        ensureProcessed();
        if (texture == null) return;
        int var6 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int var7 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        arg0.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, arg1, arg2, ((float) arg1), ((float) arg2), arg3, arg4, arg3, arg4, var6, var7, ((arg5 << 24) | 16777215));
    }

    static {
        CHAIN_ID = Identifier.fromNamespaceAndPath("lightning", "neekeri_ui");
        PATTERN_TARGET_ID = Identifier.withDefaultNamespace("pattern");
        TEXTURE_ID = Identifier.fromNamespaceAndPath("lightning", "neekeri_ui_pattern");
        UBO_SIZE = new Std140SizeCalculator().putVec4().get();
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }


    class PatternTexture extends AbstractTexture {
        PatternTexture(GpuTexture arg0, GpuTextureView arg1) {
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
