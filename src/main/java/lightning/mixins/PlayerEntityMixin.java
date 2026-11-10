package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lightning.LightningClient;
import lightning.events.impl.PlayerTravelEvent;
import lightning.modules.api.IPatchModule;
import lightning.modules.api.IReachModule;
import lightning.modules.api.ISpeedModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IMinecraft {
    protected PlayerEntityMixin(EntityType arg0, Level arg1) {
        super(arg0, arg1);
    }

    @ModifyReturnValue(method = "isPushedByFluid", at = @At("RETURN"))
    private boolean isPushedByFluids(boolean arg0) {
        if (this == mc.player && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).antiLiquidPush.getValue()) return false;
        return arg0;
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void grimAttackVelocity$setDeltaMovement(Player arg0, Vec3 arg1) {
        if (this == mc.player && ((IPatchModule) LightningClient.MODULE_MANAGER.getModule("Patch")).isGrimAttackVelocity()) return;
        arg0.setDeltaMovement(arg1);
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
    private void grimAttackVelocity$setSprinting(Player arg0, boolean arg1) {
        if (this == mc.player && ((IPatchModule) LightningClient.MODULE_MANAGER.getModule("Patch")).isGrimAttackVelocity()) return;
        arg0.setSprinting(arg1);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travel(Vec3 arg0, CallbackInfo arg1) {
        Object var3 = new PlayerTravelEvent(arg0);
        LightningClient.EVENT_HANDLER.post(var3);
        if (var3.isCancelled()) {
            move(MoverType.SELF, getDeltaMovement());
            arg1.cancel();
        }
    }

    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getBlockInteractionRange(CallbackInfoReturnable arg0) {
        Object var2 = ((IReachModule) LightningClient.MODULE_MANAGER.getModule("Reach"));
        if (var2.isToggled()) {
            arg0.setReturnValue(var2.getAmountValue());
        }
    }

    @Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getEntityInteractionRange(CallbackInfoReturnable arg0) {
        Object var2 = ((IReachModule) LightningClient.MODULE_MANAGER.getModule("Reach"));
        if (var2.isToggled()) {
            arg0.setReturnValue(var2.getAmountValue());
        }
    }

    @ModifyReturnValue(method = "getDesiredPose", at = @At("RETURN"))
    private Pose night$controlRocketStandingHitbox(Pose arg0) {
        if (((arg0 != Pose.FALL_FLYING) || (this != mc.player)) || (LightningClient.MODULE_MANAGER == null)) {
            return arg0;
        }
        Object var2 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var2 != null && var2.shouldPinFallFlying()) return Pose.STANDING;
        Object var3 = ((AutoMaceModule) LightningClient.MODULE_MANAGER.getModule(AutoMaceModule.class));
        if (var3 == null) break label_3;
        return (var3.shouldPinFallFlying() ? Pose.STANDING : arg0);
    }

    @Inject(method = "getSpeed", at = @At("HEAD"), cancellable = true)
    private void getMovementSpeed(CallbackInfoReturnable arg0) {
        Object var2 = ((ISpeedModule) LightningClient.MODULE_MANAGER.getModule("Speed"));
        if (var2.isToggled() && var2.getModeValue().equalsIgnoreCase("Vanilla")) {
            arg0.setReturnValue(var2.getVanillaSpeedValue());
        }
    }

}
