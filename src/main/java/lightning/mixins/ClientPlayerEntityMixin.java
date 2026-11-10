package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import lightning.LightningClient;
import lightning.events.impl.ChangeHandEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.SendMovementEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.movement.AirStuckModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.modules.impl.movement.VelocityModule;
import lightning.modules.impl.player.NoEntityTraceModule;
import lightning.modules.impl.player.SwingModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.BlockEntityInstanceTracker;
import lightning.utils.EarlyTickHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalPlayer.class, priority = 500)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    private float xRotLast;
    @Shadow
    public float xBob;
    @Shadow
    public ClientInput input;

    public ClientPlayerEntityMixin(ClientLevel arg0, GameProfile arg1) {
        super(arg0, arg1);
    }

    @Shadow
    protected abstract void updateAutoJump(float arg0, float arg1);

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void night$clearTrackedBlockEntities(CallbackInfo arg0) {
        BlockEntityInstanceTracker.localPlayerConstructed();
        AirStuckModule.localPlayerConstructed();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void earlyTick$dispatch(CallbackInfo arg0) {
        if (Minecraft.getInstance().player != this) return;
        EarlyTickHooks.dispatch();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.BEFORE))
    private void tick$BEFORE(CallbackInfo arg0) {
        LightningClient.EVENT_HANDLER.post(new PlayerUpdateEvent());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER))
    private void tick$AFTER(CallbackInfo arg0) {
        LightningClient.EVENT_HANDLER.post(new UpdateMovementEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick$TAIL(CallbackInfo arg0) {
        LightningClient.EVENT_HANDLER.post(new UpdateMovementEvent.Post());
    }

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float modifySendPositionPitch(float arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
            var tmp0 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
            Object var2 = tmp0;
            if (tmp0 != null && var2.isBounceAutoPitch()) return var2.getBouncePitch();
        }
        return arg0;
    }

    @WrapOperation(method = "sendPosition", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;yRotLast:F", opcode = 180, ordinal = 0))
    private float sendPosition$yRotLast(LocalPlayer arg0, Operation arg1) {
        if (LightningClient.ROTATION_MANAGER.getRotation() != null) return LightningClient.ROTATION_MANAGER.getServerYaw();
        var tmp0 = new Object[1];
        tmp0[0] = arg0;
        return ((Float) arg1.call(tmp0)).floatValue();
    }

    @WrapOperation(method = "sendPosition", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;xRotLast:F", opcode = 180, ordinal = 0))
    private float sendPosition$xRotLast(LocalPlayer arg0, Operation arg1) {
        if (LightningClient.ROTATION_MANAGER.getRotation() != null) return LightningClient.ROTATION_MANAGER.getServerPitch();
        var tmp0 = new Object[1];
        tmp0[0] = arg0;
        return ((Float) arg1.call(tmp0)).floatValue();
    }

    @ModifyExpressionValue(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean tickMovement$isUsingItem(boolean arg0) {
        if (((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).items.getValue() && !(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).shouldSlow())) return false;
        return arg0;
    }

    @ModifyExpressionValue(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z"))
    private boolean tickMovement$isMovingSlowly(boolean arg0) {
        if (!arg0) return arg0;
        Object var2 = ((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class));
        if ((var2 == null) || (!(var2.isToggled()))) {
            return arg0;
        }
        if (isVisuallyCrawling() && var2.crawl.getValue()) return false;
        if (isCrouching() && var2.sneak.getValue()) return false;
        return arg0;
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void move(MoverType arg0, Vec3 arg1, CallbackInfo arg2) {
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
            arg2.cancel();
            return;
        }
        Object var4 = new PlayerMoveEvent(arg0, arg1);
        LightningClient.EVENT_HANDLER.post(var4);
        if (var4.isCancelled()) {
            arg2.cancel();
            double var5 = getX();
            double var7 = getZ();
            super.move(arg0, var4.getMovement());
            updateAutoJump(((float) (getX() - var5)), ((float) (getZ() - var7)));
        }
    }

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo arg0) {
        Object var2 = new SendMovementEvent();
        LightningClient.EVENT_HANDLER.post(var2);
        if (var2.isCancelled()) {
            arg0.cancel();
        }
    }

    @ModifyExpressionValue(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    @Nullable
    private static EntityHitResult pick$getEntityHitResult(EntityHitResult arg0) {
        Object var1 = ((NoEntityTraceModule) LightningClient.MODULE_MANAGER.getModule(NoEntityTraceModule.class));
        if (var1.isToggled()) {
            if (arg0 != null) {
            } else {
            }
            if (arg0.getEntity().shouldIgnore(null)) return null;
        }
        return arg0;
    }

    @Inject(method = "itemUseSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void onItemUseSpeedMultiplier(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.items.getValue() && !(var2.shouldSlow())) {
            arg0.setReturnValue(1.0f);
        }
    }

    @Inject(method = "shouldStopRunSprinting", at = @At("HEAD"), cancellable = true)
    private void shouldStopRunSprinting$HEAD(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.items.getValue() && !(var2.shouldSlow()) && isUsingItem() && !(isFallFlying()) && !(isMobilityRestricted()) && !(isPassenger())) {
            if (!(!this.horizontalCollision) && !this.minorHorizontalCollision) {
                arg0.setReturnValue(true);
                return;
            }
            if (((float) getFoodData().getFoodLevel()) <= 6.0f) {
                if (getAbilities().mayfly) {
                }
            } else {
            }
            int var6 = 0;
            int var7 = 0;
            if (!var6) {
                arg0.setReturnValue(true);
                return;
            }
            if (LightningClient.MODULE_MANAGER == null) {
            } else {
            }
            var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
            Object var5 = tmp0;
            Object var8 = tmp0;
            if (var5 != null && var5.isToggled()) {
                break label_11;
            }
            if (this.input == null) break label_10;
            int var4 = (this.input.hasForwardImpulse() ? 1 : 0);
            if (!var4) {
                arg0.setReturnValue(true);
                return;
            }
            if (isInWater() && !(isUnderWater())) {
                arg0.setReturnValue(true);
                return;
            }
            arg0.setReturnValue(false);
        }
    }

    @Inject(method = "shouldStopRunSprinting", at = @At("RETURN"), cancellable = true)
    private void shouldStopRunSprinting$RETURN(CallbackInfoReturnable arg0) {
        if (!(((Boolean) arg0.getReturnValue()).booleanValue())) break label_12;
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var2 = null;
        Object var3 = null;
        if ((((((((var2 == null) || (!(var2.isToggled()))) || (!(var2.items.getValue()))) || (var2.shouldSlow())) || (!(isUsingItem()))) || (isFallFlying())) || (isMobilityRestricted())) || (isPassenger())) break label_12;
        if (!(!this.horizontalCollision)) {
            if (!this.minorHorizontalCollision) break label_12;
        }
        if (((float) getFoodData().getFoodLevel()) <= 6.0f) {
            if (getAbilities().mayfly) {
            }
        } else {
        }
        int var6 = 0;
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var5 = tmp0;
        Object var7 = tmp0;
        if (var5 != null && var5.isToggled()) {
            break label_10;
        }
        if (this.input == null) break label_9;
        int var4 = (this.input.hasForwardImpulse() ? 1 : 0);
        if ((!var6) || (!var4)) break label_12;
        if (isInWater()) {
            if (!(isUnderWater())) break label_12;
        }
        arg0.setReturnValue(false);
        label_12:
    }

    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void canStartSprinting$HEAD(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var2 = null;
        Object var3 = null;
        if (((((((((var2 == null) || (!(var2.isToggled()))) || (!(var2.items.getValue()))) || (var2.shouldSlow())) || (!(isUsingItem()))) || (isSprinting())) || (isFallFlying())) || (isMobilityRestricted())) || (isPassenger())) break label_11;
        if (((float) getFoodData().getFoodLevel()) <= 6.0f) {
            if (getAbilities().mayfly) {
            }
        } else {
        }
        int var4 = 0;
        int var5 = 0;
        if (!var4) break label_11;
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var7 = tmp0;
        Object var8 = tmp0;
        if (var7 != null && var7.isToggled()) {
            break label_9;
        }
        if (this.input == null) break label_8;
        int var6 = (this.input.hasForwardImpulse() ? 1 : 0);
        if (!var6) break label_11;
        if (isInWater()) {
            if (!(isUnderWater())) break label_11;
        }
        arg0.setReturnValue(true);
        label_11:
    }

    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z"))
    private boolean canStartSprinting$isUsingItem(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.items.getValue() && !(var2.shouldSlow())) return false;
        return arg0;
    }

    @Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
    private void canStartSprinting$RETURN(CallbackInfoReturnable arg0) {
        if (((Boolean) arg0.getReturnValue()).booleanValue()) break label_11;
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var2 = null;
        Object var3 = null;
        if (((((((((var2 == null) || (!(var2.isToggled()))) || (!(var2.items.getValue()))) || (var2.shouldSlow())) || (!(isUsingItem()))) || (isSprinting())) || (isFallFlying())) || (isMobilityRestricted())) || (isPassenger())) break label_11;
        if (((float) getFoodData().getFoodLevel()) <= 6.0f) {
            if (getAbilities().mayfly) {
            }
        } else {
        }
        int var4 = 0;
        int var5 = 0;
        if (!var4) break label_11;
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var7 = tmp0;
        Object var8 = tmp0;
        if (var7 != null && var7.isToggled()) {
            break label_9;
        }
        if (this.input == null) break label_8;
        int var6 = (this.input.hasForwardImpulse() ? 1 : 0);
        if (!var6) break label_11;
        if (isInWater()) {
            if (!(isUnderWater())) break label_11;
        }
        arg0.setReturnValue(true);
        label_11:
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void pushOutOfBlocks(double arg0, double arg1, CallbackInfo arg2) {
        if (((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).isToggled() && ((VelocityModule) LightningClient.MODULE_MANAGER.getModule(VelocityModule.class)).antiBlockPush.getValue()) {
            arg2.cancel();
        }
    }

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void swingHand(InteractionHand arg0, CallbackInfo arg1) {
        if (((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).isToggled()) {
            if (((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue().equals("None")) break label_11;
            Object var3 = ((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue();
            int var4 = -1;
            switch (var3.hashCode()) {
                case -1085510111 -> { break label_0; }
                case 2076577 -> { break label_3; }
                case 56796616 -> { break label_1; }
                case 115914302 -> { break label_2; }
                default -> { break label_4; }
            }
            label_0:
            if (!(var3.equals("Default"))) break label_4;
            var4 = 0;
            break label_4;
            label_1:
            if (!(var3.equals("Mainhand"))) break label_4;
            var4 = 1;
            break label_4;
            label_2:
            if (!(var3.equals("Offhand"))) break label_4;
            var4 = 2;
            break label_4;
            label_3:
            if (!(var3.equals("Both"))) break label_4;
            var4 = 3;
            label_4:
            switch (var4) {
                case 0 -> { break label_5; }
                case 1 -> { break label_6; }
                case 2 -> { break label_7; }
                case 3 -> { break label_8; }
                default -> { break label_9; }
            }
            label_5:
            super.swing(arg0);
            break label_9;
            label_6:
            super.swing(InteractionHand.MAIN_HAND);
            break label_9;
            label_7:
            super.swing(InteractionHand.OFF_HAND);
            break label_9;
            label_8:
            super.swing(InteractionHand.MAIN_HAND);
            super.swing(InteractionHand.OFF_HAND);
            label_9:
            if (!(((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).hand.getValue().equalsIgnoreCase("Packet"))) {
                if (((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class)).noPacket.getValue()) break label_11;
            }
            this.connection.send(new ServerboundSwingPacket(arg0));
            label_11:
            arg1.cancel();
        }
    }

    @Inject(method = "handlePortalTransitionEffect", at = @At("HEAD"), cancellable = true)
    private void tickNausea(boolean arg0, CallbackInfo arg1) {
        if (((InventoryControlModule) LightningClient.MODULE_MANAGER.getModule(InventoryControlModule.class)).isToggled() && ((InventoryControlModule) LightningClient.MODULE_MANAGER.getModule(InventoryControlModule.class)).portals.getValue()) {
            arg1.cancel();
        }
    }

    @Inject(method = "startUsingItem", at = @At("HEAD"))
    private void setCurrentHand(InteractionHand arg0, CallbackInfo arg1) {
        LightningClient.EVENT_HANDLER.post(new ChangeHandEvent());
    }

}
