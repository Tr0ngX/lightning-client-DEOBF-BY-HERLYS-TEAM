package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.api.IAmbienceModule;
import lightning.modules.api.IFullBrightModule;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightmapTextureManagerMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void night$render(LightmapRenderState arg0, CallbackInfo arg1) {
        Object var5 = ((IFullBrightModule) LightningClient.MODULE_MANAGER.getModule("FullBright"));
        if (var5.isToggled() && var5.getModeValue().equalsIgnoreCase("Gamma")) {
            arg0.blockFactor = 1.0f;
            arg0.skyFactor = 1.0f;
            arg0.brightness = 1.0f;
            arg0.nightVisionEffectIntensity = 1.0f;
            arg0.darknessEffectScale = 0.0f;
            arg0.bossOverlayWorldDarkening = 0.0f;
            arg0.needsUpdate = true;
        }
        var tmp0 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        Object var4 = tmp0;
        if ((!(tmp0.isToggled())) || (!(var4.blindness.getValue()))) break label_1;
        arg0.darknessEffectScale = 0.0f;
        arg0.needsUpdate = true;
        label_1:
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var6 = null;
        if ((var3 == null) || (!(var3.isToggled()))) break label_12;
        Object var7 = var3.getColor();
        float var8 = (((float) var7.getRed()) / 255.0f);
        float var9 = (((float) var7.getGreen()) / 255.0f);
        float var10 = (((float) var7.getBlue()) / 255.0f);
        float var11 = (((float) var7.getAlpha()) / 255.0f);
        if (!(var3.isLightmapEnabled())) break label_12;
        if (var3.isSkyEnabled()) {
            if (arg0.skyLightColor != null) {
            } else {
            }
            Object var12 = new Vector3f(1.0f, 1.0f, 1.0f);
            arg0.skyLightColor = new Vector3f(((var12.x() * (1.0f - var11)) + (var8 * var11)), ((var12.y() * (1.0f - var11)) + (var9 * var11)), ((var12.z() * (1.0f - var11)) + (var10 * var11)));
            if (arg0.ambientColor != null) {
            } else {
            }
            Object var13 = new Vector3f(0.0f, 0.0f, 0.0f);
            arg0.ambientColor = new Vector3f(((var13.x() * (1.0f - var11)) + (var8 * var11)), ((var13.y() * (1.0f - var11)) + (var9 * var11)), ((var13.z() * (1.0f - var11)) + (var10 * var11)));
        }
        if (var3.isBlocksEnabled()) {
            if (arg0.blockLightTint != null) {
            } else {
            }
            var12 = new Vector3f(1.0f, 1.0f, 1.0f);
            arg0.blockLightTint = new Vector3f(((var12.x() * (1.0f - var11)) + (var8 * var11)), ((var12.y() * (1.0f - var11)) + (var9 * var11)), ((var12.z() * (1.0f - var11)) + (var10 * var11)));
        }
        arg0.needsUpdate = true;
        label_12:
    }

}
