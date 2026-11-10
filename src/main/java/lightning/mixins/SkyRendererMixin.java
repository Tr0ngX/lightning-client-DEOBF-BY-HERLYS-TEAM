package lightning.mixins;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import lightning.LightningClient;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.SkyShader;
import lightning.utils.graphics.StarCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Redirect(method = "renderStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"))
    private RenderPass night$redirectStars(CommandEncoder arg0, Supplier arg1, GpuTextureView arg2, Optional arg3, GpuTextureView arg4, OptionalDouble arg5) {
        Object var7 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if (var7.isToggled() && var7.starGlow.getValue()) {
            Object var8 = StarCapture.ensure();
            return arg0.createRenderPass(arg1, var8.getColorTextureView(), Optional.of(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), arg4, arg5);
        }
        return arg0.createRenderPass(arg1, arg2, arg3, arg4, arg5);
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
    private void night$resolveStarGlow(PoseStack arg0, float arg1, float arg2, float arg3, MoonPhase arg4, float arg5, float arg6, CallbackInfo arg7) {
        if (arg6 <= 0.0f) return;
        Object var9 = ((AtmosphereModule) LightningClient.MODULE_MANAGER.getModule(AtmosphereModule.class));
        if ((!(var9.isToggled())) || (!(var9.starGlow.getValue()))) {
            return;
        }
        Object var10 = StarCapture.get();
        if (var10 == null) return;
        Object var11 = Minecraft.getInstance();
        Object var12 = var11.getShaderManager().getPostChain(Identifier.fromNamespaceAndPath("lightning", "star_glow"), LevelTargetBundle.MAIN_TARGETS);
        if (var12 == null) return;
        EspShader.writeStarGlowSettings(var12, var9.starGlowIntensity.getValue().floatValue());
        var12.process(var10, GraphicsResourceAllocator.UNPOOLED);
        var10.blitAndBlendToTexture(var11.gameRenderer.mainRenderTarget().getColorTextureView(), var11.gameRenderer.mainRenderTarget().getDepthTextureView());
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void night$forceSkybox(ClientLevel arg0, float arg1, Camera arg2, SkyRenderState arg3, CallbackInfo arg4) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var6 = null;
        Object var7 = null;
        if (var6 != null && var6.isToggled() && var6.modifySky.getValue()) {
            arg3.skybox = DimensionType.Skybox.OVERWORLD;
        }
    }

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void night$renderCustomSkyDisc(int arg0, CallbackInfo arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if (var3 != null && var3.isToggled() && var3.modifySky.getValue() && var3.skyMode.getValue().equalsIgnoreCase("Custom")) {
            SkyShader.renderSky();
            arg1.cancel();
        }
    }

    @ModifyVariable(method = "renderSkyDisc", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int night$modifySkyColor(int arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifySky.getValue() && var2.skyMode.getValue().equalsIgnoreCase("Vanilla")) return var2.skyColor.getColor().getRGB();
        return arg0;
    }

    @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void night$cancelDarkDisc(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifySky.getValue()) {
            arg0.cancel();
        }
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
    private void night$cancelSunriseAndSunset(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifySky.getValue()) {
            arg0.cancel();
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
    private void night$cancelSunMoonAndStars(PoseStack arg0, float arg1, float arg2, float arg3, MoonPhase arg4, float arg5, float arg6, CallbackInfo arg7) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var9 = null;
        Object var10 = null;
        if (((var9 == null) || (!(var9.isToggled()))) || (!(var9.modifySky.getValue()))) break label_5;
        Object var12 = Minecraft.getInstance();
        if (var12.level == null) break label_2;
        var tmp0 = (var12.level.dimensionType().skybox() == DimensionType.Skybox.NONE ? 1 : 0);
        int var11 = tmp0;
        int var13 = tmp0;
        if (!var11) {
            if ((!(var9.skyMode.getValue().equalsIgnoreCase("Custom"))) || (var9.starGlow.getValue())) break label_5;
        }
        arg7.cancel();
        label_5:
    }

    @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
    private void night$renderCustomEndSky(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifySky.getValue() && var2.skyMode.getValue().equalsIgnoreCase("Custom")) {
            SkyShader.renderSky();
            arg0.cancel();
        }
    }

}
