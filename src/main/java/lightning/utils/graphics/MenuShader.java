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
import lightning.utils.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class MenuShader implements IMinecraft {
    private static final Identifier CHAIN_ID;
    private static final Identifier TARGET_ID;
    private static final Identifier TEXTURE_ID;
    private static final String UNIFORM_NAME = "MenuBgConfig";
    private static final int UBO_SIZE;
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static MenuShader.MenuTexture texture;
    private static final long START_TIME;
    private static final Identifier STATIC_BG;

    private static void ensureProcessed(float arg0, float arg1, boolean arg2) {
        Object var3 = Minecraft.getInstance();
        if ((((var3 == null) || (var3.getShaderManager() == null)) || (var3.gameRenderer == null)) || (var3.gameRenderer.mainRenderTarget() == null)) {
            return;
        }
        if (!arg2) {
            return;
            try {
            }
            Object var4 = var3.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (var4 != chain) {
                chain = var4;
                uboBuffer = null;
            }
            if (chain == null) {
                return;
                try {
                }
                writeUniforms(arg0, arg1);
                chain.process(var3.gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
                Object var5 = ((RenderTarget) ((PostEffectProcessorAccessor) chain).getPersistentTargets().get(TARGET_ID));
                if (var5 != null) {
                    if (var5.getColorTextureView() == null) {
                    }
                    return;
                    try {
                    }
                    if (texture != null) {
                        if (!(texture.wraps(var5.getColorTextureView()))) {
                        }
                        if (texture != null) {
                            var3.getTextureManager().release(TEXTURE_ID);
                        }
                        texture = new MenuShader.MenuTexture(var5.getColorTexture(), var5.getColorTextureView());
                        var3.getTextureManager().register(TEXTURE_ID, texture);
                    }
                } catch (Throwable ex11) {
                    var4 = ex11;
                }
            }
        }
    }

    private static void writeUniforms(float arg0, float arg1) {
        if (chain == null) return;
        Object var2 = ((PostEffectProcessorAccessor) chain).getPasses().iterator();
        while (var2.hasNext()) {
            Object var3 = ((PostPass) var2.next());
            Object var5 = ((PostPassAccessor) var3).night$getCustomUniforms();
            if (!(var5.containsKey("MenuBgConfig"))) {
                break label_1;
            }
            if (uboBuffer != null) {
                if (var5.get("MenuBgConfig") == uboBuffer) break label_4;
            }
            var tmp0 = RenderSystem.getDevice().createBuffer(() -> "Lightning Menu BG UBO", 136, ((long) UBO_SIZE));
            uboBuffer = tmp0;
            var tmp1 = ((GpuBuffer) var5.put("MenuBgConfig", tmp0));
            Object var4 = tmp1;
            if (tmp1 == null) break label_4;
            var4.close();
            label_4:
            float var6 = (((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f);
            Object var7 = MemoryStack.stackPush();
            Object var8 = Std140Builder.onStack(var7, UBO_SIZE).putVec4(var6, arg0, arg1, 0.0f).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), var8);
            if (var7 == null) {
                break label_1;
            }
            var7.close();
            break label_11;
            Object var9 = null;
            if (var7 == null) {
                break label_1;
            }
            var7.close();
            throw var9;
            label_11:
        }
    }

    public static void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, float arg4) {
        render(arg0, arg1, arg2, arg3, arg4, true);
    }

    public static void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, float arg4, boolean arg5) {
        try {
            ensureProcessed(arg3, arg4, arg5);
            int var6 = Math.min(255, Math.max(0, Math.round((arg3 * 255.0f))));
            int var7 = ((var6 << 24) | 16777215);
            if (texture != null) {
                arg0.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, arg1, arg2, arg1, arg2, arg1, arg2, var7);
            } else {
                arg0.blit(RenderPipelines.GUI_TEXTURED, STATIC_BG, 0, 0, 0.0f, 0.0f, arg1, arg2, arg1, arg2, arg1, arg2, var7);
            }
        } catch (Throwable ex3) {
            var6 = ex3;
            Renderer2D.renderQuad(arg0, 0.0f, 0.0f, ((float) arg1), ((float) arg2), new Color(8, 8, 12, ((int) (255.0f * arg3))));
        }
    }

    public static void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        render(arg0, arg1, arg2, arg3, 1.0f);
    }

    static {
        CHAIN_ID = Identifier.fromNamespaceAndPath("lightning", "menu_bg");
        TARGET_ID = Identifier.withDefaultNamespace("menu_bg_target");
        TEXTURE_ID = Identifier.fromNamespaceAndPath("lightning", "menu_bg_texture");
        UBO_SIZE = new Std140SizeCalculator().putVec4().align(16).get();
        lastProcessedAtMs = -1L;
        START_TIME = System.currentTimeMillis();
        STATIC_BG = Identifier.fromNamespaceAndPath("lightning", "textures/gui/moonlight_bg.png");
    }


    class MenuTexture extends AbstractTexture {
        MenuTexture(GpuTexture arg0, GpuTextureView arg1) {
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
