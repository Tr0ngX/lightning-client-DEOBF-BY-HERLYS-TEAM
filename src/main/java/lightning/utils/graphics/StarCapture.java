package lightning.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;
import lightning.utils.IMinecraft;

public class StarCapture implements IMinecraft {
    private static TextureTarget target;

    public static TextureTarget ensure() {
        int var0 = mc.getWindow().getWidth();
        int var1 = mc.getWindow().getHeight();
        if (target != null && target.width == var0) {
            if (target.height != var1) {
            }
            if (target != null) {
                target.destroyBuffers();
            }
            target = new TextureTarget("night_star_capture", var0, var1, true, GpuFormat.RGBA8_UNORM);
        }
        return target;
    }

    public static TextureTarget get() {
        return target;
    }

}
