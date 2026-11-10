package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.ElytraFlyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin {
    @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
    private static void night$hideControlRocketGlide(LivingEntity arg0, HumanoidRenderState arg1, float arg2, ItemModelResolver arg3, CallbackInfo arg4) {
        if (arg0 != Minecraft.getInstance().player) return;
        Object var5 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var5.isToggled() && var5.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            arg1.isFallFlying = false;
            arg1.speedValue = 1.0f;
        }
    }

}
