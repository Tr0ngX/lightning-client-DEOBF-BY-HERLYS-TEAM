package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.LightningClient;
import lightning.events.impl.ChangePitchEvent;
import lightning.events.impl.ChangeYawEvent;
import lightning.events.impl.UpdateVelocityEvent;
import lightning.managers.RotationManager;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.modules.impl.player.RotationLockModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMinecraft {
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void night$cancelGhostTick(CallbackInfo arg0) {
        Object var3 = this;
        if (!((var3 instanceof RemotePlayer))) break label_5;
        Object var2 = ((RemotePlayer) var3);
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        var3 = null;
        if (var3 != null && var3.isGhost(var2)) {
            arg0.cancel();
            return;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var4 = null;
        if ((var4 == null) || (!(var4.isGhost(var2)))) break label_5;
        arg0.cancel();
        label_5:
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurn(double arg0, double arg1, CallbackInfo arg2) {
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) break label_2;
        Object var6 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        if (var6 != null && var6.isToggled() && !(var6.isRotate())) {
            var6.onMouseTurn(arg0, arg1);
            arg2.cancel();
            return;
        }
        Object var7 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var7 != null && var7.isFreeLookActive()) {
            var7.onMouseTurn(arg0, arg1);
            arg2.cancel();
            return;
        }
        Object var8 = ((AutoMaceModule) LightningClient.MODULE_MANAGER.getModule(AutoMaceModule.class));
        if ((var8 == null) || (!(var8.isFreeLookActive()))) break label_2;
        var8.onMouseTurn(arg0, arg1);
        arg2.cancel();
        label_2:
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity arg0, CallbackInfo arg1) {
        if (this == mc.player) {
            if (isPhantomEntity(arg0)) break label_2;
        }
        if (arg0 == mc.player) {
            if (isPhantomEntity(((Entity) this))) break label_2;
        }
        if (((this != mc.player) || (!(((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled()))) || (!(((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).antiPush.getValue()))) break label_3;
        arg1.cancel();
        break label_3;
        label_2:
        arg1.cancel();
        label_3:
    }

    private boolean isPhantomEntity(Entity arg0) {
        if (EntityUtils.isGhost(arg0)) return true;
        Object var2 = ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class));
        if (var2 == null) break label_1;
        return arg0 == var2.getPlayer();
    }

    @WrapOperation(method = "updateFluidInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V"))
    private void updateMovementInFluid(EntityFluidInteraction arg0, TagKey arg1, Entity arg2, double arg3, Operation arg4) {
        if (this == mc.player && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).antiLiquidPush.getValue()) return;
        var tmp0 = new Object[4];
        tmp0[0] = arg0;
        tmp0[1] = arg1;
        tmp0[2] = arg2;
        tmp0[3] = arg3;
        arg4.call(tmp0);
    }

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    private void updateVelocity(float arg0, Vec3 arg1, CallbackInfo arg2) {
        if (this != mc.player) return;
        Object var4 = new UpdateVelocityEvent(arg1, arg0);
        LightningClient.EVENT_HANDLER.post(var4);
        if (var4.isCancelled()) {
            arg2.cancel();
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(var4.getVelocity()));
        }
    }

    @Inject(method = "getYRot()F", at = @At("HEAD"), cancellable = true)
    private void getYaw(CallbackInfoReturnable arg0) {
        if (!(!RotationManager.swapActive) && this == mc.player) return;
        Object var2 = ((RotationLockModule) LightningClient.MODULE_MANAGER.getModule(RotationLockModule.class));
        if (!(var2.isToggled())) break label_2;
        if (!(var2.mode.getValue().equals("Yaw"))) {
            if (!(var2.mode.getValue().equals("Both"))) break label_2;
        }
        if (this != mc.player) break label_2;
        arg0.setReturnValue(var2.getYawValue(this.yRot));
        label_2:
    }

    @Inject(method = "getXRot()F", at = @At("HEAD"), cancellable = true)
    private void getPitchRaw(CallbackInfoReturnable arg0) {
        if (!(!RotationManager.swapActive) && this == mc.player) return;
        Object var2 = ((RotationLockModule) LightningClient.MODULE_MANAGER.getModule(RotationLockModule.class));
        if (!(var2.isToggled())) break label_2;
        if (!(var2.mode.getValue().equals("Pitch"))) {
            if (!(var2.mode.getValue().equals("Both"))) break label_2;
        }
        if (this != mc.player) break label_2;
        arg0.setReturnValue(var2.getPitchValue(this.xRot));
        label_2:
    }

    @Inject(method = "setYRot", at = @At("HEAD"), cancellable = true)
    private void setYaw(float arg0, CallbackInfo arg1) {
        if (this != mc.player) return;
        LightningClient.EVENT_HANDLER.post(new ChangeYawEvent(arg0));
    }

    @Inject(method = "setXRot", at = @At("HEAD"), cancellable = true)
    private void setPitch(float arg0, CallbackInfo arg1) {
        if (this != mc.player) return;
        LightningClient.EVENT_HANDLER.post(new ChangePitchEvent(arg0));
    }

    @ModifyExpressionValue(method = "getBlockSpeedFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private Block getVelocityMultiplier(Block arg0) {
        if (!(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled())) break label_2;
        if (arg0 == Blocks.SOUL_SAND) {
            if (!(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).soulSand.getValue())) {
            }
            if ((arg0 != Blocks.HONEY_BLOCK) || (!(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).honeyBlocks.getValue()))) break label_2;
        }
        return Blocks.STONE;
        label_2:
        return arg0;
    }

    @ModifyReturnValue(method = "isSprinting()Z", at = @At("RETURN"))
    private boolean injectIsSprinting(boolean arg0) {
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) {
            return arg0;
        }
        Object var2 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var2 != null && var2.isBounceActive() && var2.isGliding()) return true;
        return arg0;
    }

    @ModifyReturnValue(method = "getPose", at = @At("RETURN"))
    private Pose injectGetPose(Pose arg0) {
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) {
            return arg0;
        }
        Object var2 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var2 != null && var2.isBounceActive() && var2.isGliding()) return Pose.FALL_FLYING;
        return arg0;
    }

}
