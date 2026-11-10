package lightning.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import lightning.LightningClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class CustomImageTexture extends AbstractTexture {
    public static final CustomImageTexture INSTANCE;
    private NativeImage currentPixels;

    public void update(NativeImage arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2 != null && !(var2.isSameThread())) {
            var2.execute(() -> updateDirect(arg0));
        } else {
            updateDirect(arg0);
        }
    }

    private synchronized void updateDirect(NativeImage arg0) {
        if ((this.currentPixels == null) || (this.currentPixels == arg0)) break label_3;
        try {
            this.currentPixels.close();
        } catch (Throwable ex2) {
            Object var2 = ex2;
        }
        label_3:
        this.currentPixels = arg0;
        if (this.texture == null) break label_8;
        try {
            this.texture.close();
        } catch (Throwable ex6) {
            var2 = ex6;
        }
        this.texture = null;
        label_8:
        if (arg0 == null) {
            this.textureView = null;
            return;
            try {
            }
            var2 = RenderSystem.getDevice();
            if (var2 == null) {
                return;
                try {
                }
                this.texture = var2.createTexture(() -> "lightning/custom_image", 5, GpuFormat.RGBA8_UNORM, arg0.getWidth(), arg0.getHeight(), 1, 1);
                this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
                this.textureView = var2.createTextureView(this.texture);
                var2.createCommandEncoder().writeToTexture(this.texture, arg0);
            } catch (Throwable ex13) {
                var2 = ex13;
                LightningClient.LOGGER.error("Failed to upload custom image texture to GPU", var2);
                if (this.texture == null) break label_18;
                try {
                    this.texture.close();
                } catch (Throwable ex16) {
                    Object var3 = ex16;
                }
                this.texture = null;
                label_18:
                this.textureView = null;
            }
        }
    }

    public GpuTextureView getTextureView() {
        if (this.textureView != null) break label_4;
        if (LightningClient.IMAGE_MANAGER == null) break label_0;
        var tmp0 = LightningClient.IMAGE_MANAGER.getCurrentImageName();
        Object var1 = tmp0;
        if (((tmp0 == null) || (var1.equalsIgnoreCase("None"))) || (var1.isEmpty())) break label_0;
        LightningClient.IMAGE_MANAGER.setCurrentActiveImage(var1);
        label_0:
        if (this.textureView != null) break label_4;
        try {
            Object var2 = new NativeImage(1, 1, false);
            var2.setPixel(0, 0, 0);
            updateDirect(var2);
        } catch (Throwable ex3) {
            var2 = ex3;
        }
        label_4:
        return super.getTextureView();
    }

    public synchronized void close() {
        if (this.currentPixels == null) break label_4;
        try {
            this.currentPixels.close();
        } catch (Throwable ex2) {
            Object var1 = ex2;
        }
        this.currentPixels = null;
        label_4:
        if (this.texture == null) break label_9;
        try {
            this.texture.close();
        } catch (Throwable ex7) {
            var1 = ex7;
        }
        this.texture = null;
        label_9:
        this.textureView = null;
    }

    static {
        INSTANCE = new CustomImageTexture();
    }

}
