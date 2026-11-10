package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WalkAnimationState.class)
public class LimbAnimatorMixin {
    @ModifyReturnValue(method = "position()F", at = @At("RETURN"))
    private float getPos(float arg0) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) return 0.0f;
        return arg0;
    }

    @ModifyReturnValue(method = "position(F)F", at = @At("RETURN"))
    private float getPos2(float arg0) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) return 0.0f;
        return arg0;
    }

    @ModifyReturnValue(method = "speed()F", at = @At("RETURN"))
    private float getSpeed(float arg0) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) return 0.0f;
        return arg0;
    }

    @ModifyReturnValue(method = "speed(F)F", at = @At("RETURN"))
    private float getSpeed2(float arg0) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) return 0.0f;
        return arg0;
    }

}
