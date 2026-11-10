package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.visuals.ChamsModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void night$forceChamsOverlayLayers(Avatar arg0, AvatarRenderState arg1, float arg2, CallbackInfo arg3) {
        if (!((arg0 instanceof Entity))) return;
        Object var5 = arg0;
        Object var6 = ((ChamsModule) LightningClient.MODULE_MANAGER.getModule(ChamsModule.class));
        if ((((var6 == null) || (!(var6.isToggled()))) || (!(var6.players.getValue()))) || (!(var6.isValidEntity(var5)))) {
            return;
        }
        arg1.showHat = true;
        arg1.showJacket = true;
        arg1.showLeftSleeve = true;
        arg1.showRightSleeve = true;
        arg1.showLeftPants = true;
        arg1.showRightPants = true;
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"), require = 0)
    private void night$handOutline(SubmitNodeCollector arg0, ModelPart arg1, PoseStack arg2, RenderType arg3, int arg4, int arg5, TextureAtlasSprite arg6) {
        arg0.submitModelPart(arg1, arg2, arg3, arg4, arg5, arg6, -1, null, night$outlineColor());
    }

    private static int night$outlineColor() {
        Object var0 = Minecraft.getInstance();
        if (var0.player == null) return 0;
        Object var1 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if (var1.isToggled() && var1.isValidEntity(var0.player)) return (var1.getColor(var0.player).getRGB() | -16777216);
        return 0;
    }

    @Inject(method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void night$applyPopChamYOffset(AvatarRenderState arg0, CallbackInfoReturnable arg1) {
        Object var3 = ((IChamsCapture) arg0);
        float var4 = var3.night$chamsYOffset();
        if (var4 != 0.0f) {
            Object var5 = ((Vec3) arg1.getReturnValue());
            if (var5 == null) {
            } else {
            }
            new Vec3(0.0, ((double) var4), 0.0).setReturnValue(var5.add(0.0, ((double) var4), 0.0));
        }
    }

}
