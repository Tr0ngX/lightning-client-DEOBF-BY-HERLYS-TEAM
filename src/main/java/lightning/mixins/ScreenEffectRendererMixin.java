package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
    private static void night$renderFire(PoseStack arg0, SubmitNodeCollector arg1, TextureAtlasSprite arg2, CallbackInfo arg3) {
        Object var4 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var4.isToggled() && var4.fireOverlay.getValue()) {
            arg3.cancel();
        }
    }

    @Inject(method = "submitWater", at = @At("HEAD"), cancellable = true)
    private static void night$renderWater(Minecraft arg0, PoseStack arg1, SubmitNodeCollector arg2, CallbackInfo arg3) {
        Object var4 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var4.isToggled() && var4.liquidOverlay.getValue()) {
            arg3.cancel();
        }
    }

    @Inject(method = "submitBlockSprite", at = @At("HEAD"), cancellable = true)
    private static void night$renderTex(TextureAtlasSprite arg0, PoseStack arg1, SubmitNodeCollector arg2, int arg3, CallbackInfo arg4) {
        Object var5 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (var5.isToggled() && var5.blockOverlay.getValue()) {
            arg4.cancel();
        }
    }

}
