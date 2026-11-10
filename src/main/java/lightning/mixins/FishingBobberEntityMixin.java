package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.LightningClient;
import lightning.modules.impl.movement.VelocityModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public class FishingBobberEntityMixin implements IMinecraft {
    @WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V"))
    private void pushOutOfBlocks(FishingHook arg0, Entity arg1, Operation arg2) {
        if (arg1 == mc.player && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).antiFishingRod.getValue()) return;
        var tmp0 = new Object[2];
        tmp0[0] = arg0;
        tmp0[1] = arg1;
        arg2.call(tmp0);
    }

}
