package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.api.IFOVModifierModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.IMotionCameraModule;
import lightning.modules.api.IViewClipModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.player.RotationLockModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin implements IMinecraft {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void move(float arg0, float arg1, float arg2);

    @Shadow
    protected abstract float getMaxZoom(float arg0);

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void update(Args arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if (var3 != null && var3.isToggled() && var3.isSmoothPerspective()) {
            arg0.set(0, ((float) var3.getDistance()));
            return;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var5 = null;
        if (var2 != null && var2.isToggled() && var2.isExtend()) {
            arg0.set(0, var2.getDistanceValue());
        }
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void clipToSpace(float arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if (var3 != null && var3.isToggled()) {
            arg1.setReturnValue(arg0);
        }
    }

    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void getSubmersionType(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).liquidOverlay.getValue()) {
            arg0.setReturnValue(FogType.NONE);
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void update$TAIL(DeltaTracker arg0, CallbackInfo arg1) {
        if (LightningClient.MODULE_MANAGER == null) return;
        if (((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam")).isToggled()) {
            this.detached = true;
            return;
        }
        Object var3 = ((IMotionCameraModule) LightningClient.MODULE_MANAGER.getModule("MotionCamera"));
        if ((((var3 == null) || (!(var3.isToggled()))) || (!(var3.isSmoothPerspective()))) || (!(var3.shouldBeDetached()))) break label_2;
        this.detached = true;
        if ((mc.options == null) || (!(mc.options.getCameraType().isFirstPerson()))) break label_2;
        var tmp0 = ((float) var3.getDistance());
        float var4 = tmp0;
        if (tmp0 <= 0.001f) break label_2;
        move(-(getMaxZoom(var4)), 0.0f, 0.0f);
        label_2:
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void update$setRotation(Args arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_10;
        Object var2 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        if (var2 != null && var2.isToggled()) {
            var tmp0 = new Object[2];
            tmp0[0] = var2.getFreeYaw();
            tmp0[1] = var2.getFreePitch();
            arg0.setAll(tmp0);
            return;
        }
        Object var3 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var3 != null && var3.isFreeLookActive()) {
            if (mc.options == null) break label_1;
            int var4 = (mc.options.getCameraType().isMirrored() ? 1 : 0);
            float var5 = var3.getFreeYaw();
            float var6 = var3.getFreePitch();
            if (!(!var4)) {
                var5 = (var5 + 180.0f);
                var6 = -(var6);
            }
            var tmp1 = new Object[2];
            tmp1[0] = var5;
            tmp1[1] = var6;
            arg0.setAll(tmp1);
            return;
        }
        var4 = ((AutoMaceModule) LightningClient.MODULE_MANAGER.getModule(AutoMaceModule.class));
        if (var4 != null && var4.isFreeLookActive()) {
            if (mc.options == null) break label_5;
            var5 = (mc.options.getCameraType().isMirrored() ? 1 : 0);
            var6 = var4.getFreeYaw();
            float var7 = var4.getFreePitch();
            if (!(!var5)) {
                var6 = (var6 + 180.0f);
                var7 = -(var7);
            }
            var tmp2 = new Object[2];
            tmp2[0] = var6;
            tmp2[1] = var7;
            arg0.setAll(tmp2);
            return;
        }
        var5 = ((RotationLockModule) LightningClient.MODULE_MANAGER.getModule(RotationLockModule.class));
        if (((var5 == null) || (!(var5.isToggled()))) || (!(var5.custom.getValue()))) break label_10;
        var6 = var5.getShakeYawOffset();
        var7 = var5.getShakePitchOffset();
        if (var6 == 0.0f) {
            if (var7 == 0.0f) break label_10;
        }
        var tmp3 = new Object[2];
        tmp3[0] = (((Float) arg0.get(0)).floatValue() + var6);
        tmp3[1] = (((Float) arg0.get(1)).floatValue() + var7);
        arg0.setAll(tmp3);
        label_10:
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void update$setPos(Args arg0) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var2 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        if (var2.isToggled()) {
            var tmp0 = new Object[3];
            tmp0[0] = var2.getFreeX();
            tmp0[1] = var2.getFreeY();
            tmp0[2] = var2.getFreeZ();
            arg0.setAll(tmp0);
            break label_2;
        }
        Object var3 = ((IMotionCameraModule) LightningClient.MODULE_MANAGER.getModule("MotionCamera"));
        if (((var3 == null) || (!(var3.isToggled()))) || (!(var3.on()))) break label_2;
        var tmp1 = new Object[3];
        tmp1[0] = var3.getFakeX();
        tmp1[1] = var3.getFakeY();
        tmp1[2] = var3.getFakeZ();
        arg0.setAll(tmp1);
        label_2:
    }

    @Inject(method = "calculateFov", at = @At("TAIL"), cancellable = true)
    private void getFOV(float arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var3 = ((IFOVModifierModule) LightningClient.MODULE_MANAGER.getModule("FOVModifier"));
        if (var3.isToggled()) {
            arg1.setReturnValue(var3.getFovValue());
        }
    }

}
