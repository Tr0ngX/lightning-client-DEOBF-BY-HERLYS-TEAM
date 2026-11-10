package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import lightning.utils.mixins.ISelfState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer implements IMinecraft {
    private static final double SAME_SPOT_RANGE_SQ = 1.0;

    public LivingEntityRendererMixin(EntityRendererProvider.Context arg0) {
        super(arg0);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void submit(LivingEntityRenderState arg0, PoseStack arg1, SubmitNodeCollector arg2, CameraRenderState arg3, CallbackInfo arg4) {
        Object var10 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if (!(var10.isToggled())) return;
        if (var10.corpses.getValue() && arg0.deathTime > 0.0f) {
            arg4.cancel();
            return;
        }
        if ((((!(var10.player.getValue())) || (!((arg0 instanceof AvatarRenderState)))) || (((ISelfState) arg0).night$isSelf())) || (mc.player == null)) break label_2;
        double var8 = (arg0.x - mc.player.getX());
        double var6 = (arg0.z - mc.player.getZ());
        if ((((arg0.x - mc.player.getX()) * var8) + ((arg0.z - mc.player.getZ()) * var6)) > 1.0) break label_2;
        arg4.cancel();
        label_2:
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void night$extractRenderState(LivingEntity arg0, LivingEntityRenderState arg1, float arg2, CallbackInfo arg3) {
        ((ISelfState) arg1).night$setSelf(arg0 == mc.player);
        if (arg0 == mc.player && LightningClient.ROTATION_MANAGER.inRenderTime()) {
            arg1.bodyRot = LightningClient.ROTATION_MANAGER.getRenderRotations()[0];
            arg1.yRot = Mth.wrapDegrees((LightningClient.ROTATION_MANAGER.getRenderRotations()[0] - arg1.bodyRot));
            arg1.xRot = LightningClient.ROTATION_MANAGER.getRenderRotations()[1];
            return;
        }
        if ((arg0 != mc.player) || (LightningClient.MODULE_MANAGER == null)) break label_3;
        var tmp0 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        Object var5 = tmp0;
        if ((((tmp0 == null) || (!(var5.isToggled()))) || (!(var5.mode.getValue().equalsIgnoreCase("Bounce")))) || (!(var5.bounceAutoPitch.getValue()))) break label_3;
        arg1.xRot = var5.bouncePitch.getValue().floatValue();
        label_3:
    }

}
