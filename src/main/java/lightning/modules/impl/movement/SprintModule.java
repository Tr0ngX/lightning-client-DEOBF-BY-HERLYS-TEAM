package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.ISprintModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Sprint", description = "Makes it so that you are always sprinting when possible.", category = Module.Category.MOVEMENT)
public class SprintModule extends Module implements ISprintModule {
    public ModeSetting mode;
    public NumberSetting instantSpeed;
    public BooleanSetting instantWater;
    public BooleanSetting instantLava;
    public BooleanSetting pauseOnSetback;
    private float cachedYaw;
    private Vec2 cachedMove;
    private boolean grimQueued;
    private Float pendingYaw;
    private float pendingPitch;
    private int grimStrafe;

    public SprintModule() {
        var tmp0 = new String[4];
        tmp0[0] = "Legit";
        tmp0[1] = "Rage";
        tmp0[2] = "Instant";
        tmp0[3] = "Grim";
        this.mode = new ModeSetting("Mode", "The limits to when you can be sprinting.", "Rage", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Instant";
        this.instantSpeed = new NumberSetting("InstantSpeed", "Per-tick horizontal speed to move at (blocks/tick) when Mode is Instant.", new ModeSetting.Visibility(this.mode, tmp1), ((float) MovementUtils.DEFAULT_SPEED), 0.05f, 0.6f);
        var tmp2 = new String[1];
        tmp2[0] = "Instant";
        this.instantWater = new BooleanSetting("Water", "Keeps applying the Instant speed override while in water.", new ModeSetting.Visibility(this.mode, tmp2), true);
        var tmp3 = new String[1];
        tmp3[0] = "Instant";
        this.instantLava = new BooleanSetting("Lava", "Keeps applying the Instant speed override while in lava.", new ModeSetting.Visibility(this.mode, tmp3), false);
        this.pauseOnSetback = new BooleanSetting("PauseOnSetback", "Briefly pauses sprinting when receiving a setback from the server to allow GrimAC buffer to decay.", true);
    }

    public int getGrimStrafe() {
        return this.grimStrafe;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (mc.player == null) return;
        this.cachedYaw = mc.player.getYRot();
        this.cachedMove = mc.player.input.getMoveVector();
        if (isGrim()) {
            grimUpdate();
        } else {
            this.pendingYaw = null;
        }
        int var2 = shouldSprint();
        if (!(!var2)) {
            mc.player.setSprinting(true);
            break label_4;
        }
        if (!(mc.player.isSprinting())) break label_4;
        mc.player.setSprinting(false);
        label_4:
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if (mc.player == null) return;
        if (!(this.mode.getValue().equalsIgnoreCase("Instant"))) return;
        Object var3 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var3.isToggled() && var3.mode.getValue().equalsIgnoreCase("Control") && mc.player.isFallFlying()) return;
        Object var4 = ((HoleSnapModule) LightningClient.MODULE_MANAGER.getModule(HoleSnapModule.class));
        if (var4.isToggled() && var4.hole != null) return;
        if (mc.player.getFoodData().getFoodLevel() <= 6) break label_7;
        if (!(this.instantLava.getValue())) {
            if (mc.player.isInLava()) break label_7;
        }
        if (!(this.instantWater.getValue())) {
            if (mc.player.isInWater()) break label_7;
        }
        if (this.pauseOnSetback.getValue() && LightningClient.SERVER_MANAGER != null) {
            if (!(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L))) break label_7;
        }
        break label_8;
        label_7:
        label_8:
        int var2 = 0;
        int var5 = 0;
        if (!var2) return;
        Object var6 = this.cachedMove;
        if (var6.x == 0.0f) {
            if (var6.y != 0.0f) {
            }
            double var7 = Math.sqrt(((double) ((var6.x * var6.x) + (var6.y * var6.y))));
            double var9 = (((double) var6.x) / var7);
            double var11 = (((double) var6.y) / var7);
            double var13 = Math.toRadians(((double) this.cachedYaw));
            double var15 = Math.sin(var13);
            double var17 = Math.cos(var13);
            double var19 = ((var9 * var17) - (var11 * var15));
            double var21 = ((var9 * var15) + (var11 * var17));
            double var23 = this.instantSpeed.getValue().doubleValue();
            arg0.setMovement(new Vec3((var19 * var23), arg0.getMovement().y, (var21 * var23)));
        } else {
            arg0.setMovement(new Vec3(0.0, arg0.getMovement().y, 0.0));
        }
        arg0.setCancelled(true);
    }

    @SubscribeEvent
    public void onClientRotation(ClientRotationEvent arg0) {
        if ((this.pendingYaw == null) || (arg0.isCancelled())) {
            return;
        }
        arg0.setYaw(this.pendingYaw.floatValue());
        arg0.setPitch(this.pendingPitch);
        arg0.setOwner(this);
    }

    private boolean isGrim() {
        return this.mode.getValue().equalsIgnoreCase("Grim");
    }

    private void grimUpdate() {
        this.grimQueued = false;
        this.pendingYaw = null;
        if ((mc.player.isFallFlying()) || (mc.player.isPassenger())) {
            return;
        }
        if (this.pauseOnSetback.getValue() && LightningClient.SERVER_MANAGER != null && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L))) return;
        if (((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam")).isToggled()) return;
        int var2 = ((mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0));
        int var3 = ((mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0));
        if (!var2 && !var3) return;
        float var4 = ((float) Math.toDegrees(Math.atan2(((double) var2), ((double) var3))));
        int var5 = 0;
        if (!(!var2) && !(!var3)) {
            var5 = (var2 < 0 ? 1 : -1);
            float var1 = Mth.wrapDegrees(((mc.player.getYRot() + var4) + (var5 > 0 ? 45.0f : -45.0f)));
        } else {
            var1 = Mth.wrapDegrees((mc.player.getYRot() + var4));
        }
        this.pendingYaw = var1;
        this.pendingPitch = mc.player.getXRot();
        this.grimStrafe = var5;
        this.grimQueued = true;
    }

    public boolean isGrimCompensating() {
        if ((((mc.player == null) || (!(isToggled()))) || (!(isGrim()))) || (!this.grimQueued)) {
            return false;
        }
        if (this.pauseOnSetback.getValue() && LightningClient.SERVER_MANAGER != null && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L))) return false;
        if (LightningClient.ROTATION_MANAGER.getRotation() == null) break label_3;
        return LightningClient.ROTATION_MANAGER.getRotationOwner() == this;
    }

    public float getGrimYaw() {
        return LightningClient.ROTATION_MANAGER.getRotation().getYaw();
    }

    public boolean isInstantMode() {
        return this.mode.getValue().equalsIgnoreCase("Instant");
    }

    public void onEnable() {
        if (mc.player == null) return;
        mc.player.setSprinting(shouldSprint());
    }

    public void onDisable() {
        this.grimQueued = false;
        if (mc.player == null) return;
        mc.player.setSprinting(false);
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    public boolean shouldSprint() {
        if (this.pauseOnSetback.getValue() && LightningClient.SERVER_MANAGER != null && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L))) return false;
        if (!(((ClientPlayerEntityAccessor) mc.player).invokeCanSprint(true))) return false;
        if (mc.player.isInWater() && !(mc.player.isUnderWater())) return false;
        if (mc.player.isSwimming() && !(mc.player.onGround()) && !(mc.player.input.keyPresses.shift()) && !(mc.player.isInWater())) return false;
        if (mc.player.isInLava()) return false;
        if (mc.player.onClimbable()) return false;
        if (mc.player.isFallFlying()) {
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var1 = null;
            Object var2 = null;
            if (((var1 == null) || (!(var1.isToggled()))) || (!(var1.mode.getValue().equalsIgnoreCase("Bounce")))) {
                return false;
            }
        }
        if (mc.player.getFoodData().getFoodLevel() <= 6) return false;
        if (mc.player.isUsingItem()) {
            if ((!(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled())) || (!(((NoSlowModule) LightningClient.MODULE_MANAGER.getModule(NoSlowModule.class)).items.getValue()))) {
                return false;
            }
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Rage")) && !(this.mode.getValue().equalsIgnoreCase("Instant"))) {
            if (isGrim()) {
            }
            if (!(!mc.player.horizontalCollision) && !mc.player.minorHorizontalCollision) return false;
            if (isGrim() && !(isGrimCompensating())) return mc.player.input.keyPresses.forward();
            if (!(mc.player.input.keyPresses.forward()) && !(mc.player.input.keyPresses.backward()) && !(mc.player.input.keyPresses.left())) {
                if (mc.player.input.keyPresses.right()) {
                }
            } else {
            }
            return false;
        }
        if (!(((ClientPlayerEntityAccessor) mc.player).invokeIsWalking())) return false;
        if (!(!mc.player.horizontalCollision) && !mc.player.minorHorizontalCollision) return false;
        return mc.player.input.hasForwardImpulse();
    }

    @Generated
    public Float getPendingYaw() {
        return this.pendingYaw;
    }

}
