package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lightning.LightningClient;
import lightning.events.impl.ConsumeItemEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.managers.RotationManager;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.api.IFastClimbModule;
import lightning.modules.api.IHoleSnapModule;
import lightning.modules.api.INoJumpDelayModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.StepModule;
import lightning.modules.impl.player.SwingModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IMinecraft {
    @Shadow
    @Final
    private static AttributeModifier SPEED_MODIFIER_SPRINTING;
    @Shadow
    private int noJumpDelay;
    @Shadow
    protected ItemStack useItem;
    @Unique
    private float night$grimYaw0;
    @Unique
    private boolean night$grimSwapped;

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Holder arg0);

    public LivingEntityMixin(EntityType arg0, Level arg1) {
        super(arg0, arg1);
    }

    @WrapMethod(method = "maxUpStep")
    private float getStepHeight(Operation arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) break label_4;
        if (((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).isToggled()) {
            if (!(mc.player.onGround())) {
            }
            if (((var2 == null) || (!(var2.isToggled()))) || (!(var2.isStepEnabled()))) break label_4;
        }
        return ((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).height.getValue().floatValue();
        label_4:
        return ((Float) arg0.call(new Object[0])).floatValue();
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 2, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && this.noJumpDelay == 10) {
            this.noJumpDelay = var2.getTicksValue();
        }
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void night$bounceNoJumpDelay(CallbackInfo arg0) {
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) {
            return;
        }
        Object var2 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var2 != null && var2.isBounceActive()) {
            this.noJumpDelay = 0;
        }
    }

    @WrapOperation(method = "getCurrentSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/SwingAnimation;duration()I"))
    private int getHandSwingDuration(SwingAnimation arg0, Operation arg1) {
        var tmp0 = new Object[1];
        tmp0[0] = arg0;
        int var3 = ((Integer) arg1.call(tmp0)).intValue();
        if (this != mc.player) return var3;
        if (((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).isToggled() && ((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).modifySpeed.getValue() && mc.options.getCameraType().isFirstPerson()) {
        } else {
        }
        return var3;
    }

    @Inject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER))
    private void consumeItem(CallbackInfo arg0) {
        if (this == mc.player) {
            LightningClient.EVENT_HANDLER.post(new ConsumeItemEvent(this.useItem));
        }
    }

    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSprinting(Z)V", shift = At.Shift.AFTER), cancellable = true)
    private void setSprinting$setSprinting(boolean arg0, CallbackInfo arg1) {
        if (this != mc.player) break label_7;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var4 = null;
        Object var5 = null;
        if (var4 != null && var4.isToggled() && var4.mode.getValue().equalsIgnoreCase("Bounce")) return;
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var3 = tmp0;
        Object var6 = tmp0;
        if ((var3 == null) || (!(var3.isToggled()))) break label_7;
        Object var7 = getAttribute(Attributes.MOVEMENT_SPEED);
        if (var7 == null) break label_6;
        var7.removeModifier(SPEED_MODIFIER_SPRINTING.id());
        if (var3.shouldSprint()) {
            setSharedFlag(3, true);
            var7.addTransientModifier(SPEED_MODIFIER_SPRINTING);
            break label_6;
        }
        setSharedFlag(3, false);
        label_6:
        arg1.cancel();
        label_7:
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void jump$HEAD(CallbackInfo arg0) {
        if (this != mc.player) return;
        LightningClient.EVENT_HANDLER.post(new PlayerJumpEvent());
    }

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void jump$RETURN(CallbackInfo arg0) {
        if (this != mc.player) return;
        LightningClient.EVENT_HANDLER.post(new PlayerJumpEvent.Post());
    }

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void night$fastClimbIgnore(CallbackInfoReturnable arg0) {
        if (this != mc.player) return;
        Object var2 = ((IFastClimbModule) LightningClient.MODULE_MANAGER.getModule("FastClimb"));
        if (var2.isToggled() && var2.getSpeedValue() == 0.0f) {
            arg0.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "handleOnClimbable", constant = @Constant(floatValue = 0.15), require = 0)
    private float night$fastClimbScale(float arg0) {
        return night$fastClimbFactor(arg0);
    }

    @Unique
    private float night$fastClimbFactor(float arg0) {
        if (this != mc.player) return arg0;
        Object var2 = ((IFastClimbModule) LightningClient.MODULE_MANAGER.getModule("FastClimb"));
        if (!(var2.isToggled())) return arg0;
        float var3 = var2.getSpeedValue();
        if ((var3 <= 0.0f) || (var3 >= 1.0f)) {
            return arg0;
        }
        return (arg0 * var3);
    }

    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void night$hideElytraSwap(EquipmentSlot arg0, CallbackInfoReturnable arg1) {
        if (((arg0 != EquipmentSlot.CHEST) || (this != mc.player)) || (LightningClient.MODULE_MANAGER == null)) {
            return;
        }
        Object var3 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class)).getGrimHiddenElytra();
        if (var3 != null) {
            arg1.setReturnValue(var3);
        }
    }

    @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
    private void night$pinBounceAndControlRocketGlide(CallbackInfoReturnable arg0) {
        if ((this != mc.player) || (LightningClient.MODULE_MANAGER == null)) {
            return;
        }
        Object var3 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if ((var3 == null) || (!(var3.isToggled()))) break label_3;
        if (var3.isBounceActive() && var3.isGliding()) {
            arg0.setReturnValue(true);
            return;
        }
        if (!(var3.shouldPinFallFlying())) break label_3;
        arg0.setReturnValue(true);
        return;
        label_3:
        var tmp0 = ((AutoMaceModule) LightningClient.MODULE_MANAGER.getModule(AutoMaceModule.class));
        Object var2 = tmp0;
        if (tmp0 != null && var2.shouldPinFallFlying()) {
            arg0.setReturnValue(true);
        }
    }

    @WrapOperation(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"))
    private float wrapFallFlyingPitch(LivingEntity arg0, Operation arg1) {
        if (this == mc.player && LightningClient.MODULE_MANAGER != null) {
            var tmp0 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
            Object var3 = tmp0;
            if (tmp0 != null && var3.isBounceAutoPitch()) return var3.getBouncePitch();
        }
        var tmp1 = new Object[1];
        tmp1[0] = arg0;
        return ((Float) arg1.call(tmp1)).floatValue();
    }

    @WrapOperation(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 wrapFallFlyingLookAngle(LivingEntity arg0, Operation arg1) {
        if (this == mc.player && LightningClient.MODULE_MANAGER != null) {
            var tmp0 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
            Object var3 = tmp0;
            if (tmp0 != null && var3.isBounceAutoPitch()) return Vec3.directionFromRotation(var3.getBouncePitch(), arg0.getYRot());
        }
        var tmp1 = new Object[1];
        tmp1[0] = arg0;
        return ((Vec3) arg1.call(tmp1));
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travel$grimHead(Vec3 arg0, CallbackInfo arg1) {
        this.night$grimSwapped = false;
        if (this == mc.player && night$shouldSpoofRotation()) {
            this.night$grimYaw0 = ((EntityAccessor) this).getRawYRot();
            ((EntityAccessor) this).setRawYRot(night$spoofYaw());
            RotationManager.swapActive = true;
            this.night$grimSwapped = true;
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void travel$grimReturn(Vec3 arg0, CallbackInfo arg1) {
        if (!this.night$grimSwapped) return;
        this.night$grimSwapped = false;
        RotationManager.swapActive = false;
        ((EntityAccessor) this).setRawYRot(this.night$grimYaw0);
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float jumpFromGround$grimYaw(float arg0) {
        if (this == mc.player && night$shouldSpoofRotation()) {
        } else {
        }
        return arg0;
    }

    @Unique
    private boolean night$grimCompensating() {
        if (LightningClient.MODULE_MANAGER == null) return false;
        Object var1 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        if (var1 == null) break label_1;
        return var1.isGrimCompensating();
    }

    @Unique
    private boolean night$shouldSpoofRotation() {
        return night$grimCompensating();
    }

    @Unique
    private float night$spoofYaw() {
        return ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint")).getGrimYaw();
    }

}
