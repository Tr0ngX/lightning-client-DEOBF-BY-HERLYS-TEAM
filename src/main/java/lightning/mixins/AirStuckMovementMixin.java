package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.modules.impl.movement.AirStuckModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LocalPlayer.class, priority = 1000)
public abstract class AirStuckMovementMixin {
    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"), require = 1, expect = 1, allow = 1)
    private void night$freezeVanillaMovement(LocalPlayer arg0, MoverType arg1, Vec3 arg2, Operation arg3) {
        var tmp0 = new Object[3];
        tmp0[0] = arg0;
        tmp0[1] = arg1;
        tmp0[2] = AirStuckModule.movementDisplacement(arg2);
        arg3.call(tmp0);
    }

}
