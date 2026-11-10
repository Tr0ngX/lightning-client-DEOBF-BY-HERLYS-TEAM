package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.TickEvent;
import lightning.managers.BoostManager;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.ISpeedModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.MovementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Speed", description = "Makes it so that you move faster than normal.", category = Module.Category.MOVEMENT)
public class SpeedModule extends Module implements ISpeedModule {
    public ModeSetting mode;
    public NumberSetting vanillaSpeed;
    public BooleanSetting vanillaOnGround;
    public BooleanSetting useTimer;
    public BooleanSetting timerBypass;
    public NumberSetting bypassThreshold;
    public NumberSetting timerMultiplier;
    public BooleanSetting speedInLiquid;
    public BooleanSetting boost;
    public NumberSetting boostMultiplier;
    public NumberSetting boostCap;
    public BooleanSetting autoJump;
    private double distance;
    private double speed;
    private double forward;
    private int stage;
    private int ticks;
    private boolean pressed;

    public SpeedModule() {
        var tmp0 = new String[4];
        tmp0[0] = "Vanilla";
        tmp0[1] = "Strafe";
        tmp0[2] = "StrafeStrict";
        tmp0[3] = "Grim";
        this.mode = new ModeSetting("Mode", "The method that will be used to increase your speed.", "Strafe", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Vanilla";
        this.vanillaSpeed = new NumberSetting("VanillaSpeed", "Speed", "The speed that will be applied to your movement.", new ModeSetting.Visibility(this.mode, tmp1), 10.0, 0.0, 20.0);
        var tmp2 = new String[1];
        tmp2[0] = "Vanilla";
        this.vanillaOnGround = new BooleanSetting("VanillaOnGround", "OnGround", "Only applies the speed when you are on ground.", new ModeSetting.Visibility(this.mode, tmp2), false);
        var tmp3 = new String[2];
        tmp3[0] = "Strafe";
        tmp3[1] = "StrafeStrict";
        this.useTimer = new BooleanSetting("UseTimer", "Adds a timer multiplier when strafing.", new ModeSetting.Visibility(this.mode, tmp3), false);
        this.timerBypass = new BooleanSetting("Bypass", "Allows you to use timer on certain servers.", new BooleanSetting.Visibility(this.useTimer, true), true);
        this.bypassThreshold = new NumberSetting("Threshold", "The threshold value for the timer bypass.", new BooleanSetting.Visibility(this.timerBypass, true), 25, 15, 30);
        this.timerMultiplier = new NumberSetting("TimerMultiplier", "Multiplier", "The timer multiplier that will be applied to the timer.", new BooleanSetting.Visibility(this.useTimer, true), 1.08f, 1.0f, 1.2f);
        var tmp4 = new String[2];
        tmp4[0] = "Strafe";
        tmp4[1] = "StrafeStrict";
        this.speedInLiquid = new BooleanSetting("SpeedInLiquid", "Increases your speed while in water or lava.", new ModeSetting.Visibility(this.mode, tmp4), false);
        var tmp5 = new String[2];
        tmp5[0] = "Strafe";
        tmp5[1] = "StrafeStrict";
        this.boost = new BooleanSetting("Boost", "Uses explosion knockback to boost your speed.", new ModeSetting.Visibility(this.mode, tmp5), false);
        this.boostMultiplier = new NumberSetting("BoostMultiplier", "Multiplier", "Scales the explosion knockback boost.", new BooleanSetting.Visibility(this.boost, true), 1.0, 0.01, 5.0);
        this.boostCap = new NumberSetting("BoostCap", "Cap", "Maximum boost value that can be applied.", new BooleanSetting.Visibility(this.boost, true), 5.0, 0.0, 10.0);
        var tmp6 = new String[1];
        tmp6[0] = "Grim";
        this.autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you when on ground.", new ModeSetting.Visibility(this.mode, tmp6), false);
        this.pressed = false;
    }

    public void onEnable() {
        this.stage = 1;
        this.ticks = 0;
        this.pressed = false;
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
        if (!(!this.pressed)) {
            mc.options.keyJump.setDown(false);
            this.pressed = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Strafe")) break label_2;
        if (this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            label_2:
            this.distance = Math.sqrt((Mth.square((mc.player.getX() - mc.player.xo)) + Mth.square((mc.player.getZ() - mc.player.zo))));
            if (isDrivingTimer()) {
            } else {
            }
            this.timerMultiplier.getValue().floatValue().setTimerMultiplier(1.0f);
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Grim"))) break label_22;
        if (!(mc.player.isVisuallyCrawling()) && mc.player.getPose() != Pose.SWIMMING && mc.player.getEyeHeight() >= 1.0f) {
            if (hasCeiling(mc.player.getX(), mc.player.getY(), mc.player.getZ())) {
            }
        } else {
        }
        int var2 = 0;
        int var3 = 1;
        if (!var2) break label_10;
        int var4 = hasCeiling(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int var5 = hasCeilingAhead();
        if (!(!var4)) {
            if (var5) break label_10;
        }
        var3 = 0;
        label_10:
        if (!(!var3) && this.autoJump.getValue()) {
            if (MovementUtils.isMoving()) break label_12;
        }
        if (!this.pressed) break label_12;
        mc.options.keyJump.setDown(false);
        this.pressed = false;
        label_12:
        if (this.autoJump.getValue() && MovementUtils.isMoving() && mc.player.onGround() && !(!var3)) {
            mc.options.keyJump.setDown(true);
            this.pressed = true;
            var5 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
            if (var5 == null) break label_13;
            var tmp0 = (var5.isGrimCompensating() ? 1 : 0);
            var4 = tmp0;
            int var6 = tmp0;
            if (mc.player.isSprinting() && !var4) {
                float var7 = (mc.player.getYRot() * 0.017453292f);
                double var8 = (-(Math.sin(((double) var7))) * 0.2);
                double var10 = (Math.cos(((double) var7)) * 0.2);
                Object var12 = MovementUtils.forward(0.2);
                mc.player.setDeltaMovement(((mc.player.getDeltaMovement().x - var8) + var12.x), mc.player.getDeltaMovement().y, ((mc.player.getDeltaMovement().z - var10) + var12.y));
            }
            break label_17;
        }
        if (!this.pressed) break label_17;
        mc.options.keyJump.setDown(false);
        this.pressed = false;
        label_17:
        var4 = 0;
        var5 = mc.level.entitiesForRendering().iterator();
        while (var5.hasNext()) {
            var6 = ((Entity) var5.next());
            if ((((var6 == null) || (var6 == mc.player)) || (!((var6 instanceof LivingEntity)))) || (EntityUtils.isGhost(var6))) break label_18;
            if (((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).isToggled()) {
                if (((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).getPlayer() == var6) break label_18;
            }
            if ((var6 instanceof ArmorStand)) break label_18;
            if (((double) Mth.sqrt(((float) mc.player.distanceToSqr(var6)))) > 1.5) {
                break label_18;
            }
            var4++;
        }
        if (var4 <= 0) break label_22;
        var5 = MovementUtils.forward((0.08 * ((double) var4)));
        mc.player.setDeltaMovement((mc.player.getDeltaMovement().x + var5.x), mc.player.getDeltaMovement().y, (mc.player.getDeltaMovement().z + var5.y));
        label_22:
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if (!(this.mode.getValue().equalsIgnoreCase("Strafe"))) {
            if (this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            }
            if (((HoleSnapModule) LightningClient.MODULE_MANAGER.getModule(HoleSnapModule.class)).isToggled() && ((HoleSnapModule) LightningClient.MODULE_MANAGER.getModule(HoleSnapModule.class)).hole != null) return;
            Object var2 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
            if (var2.isToggled() && var2.mode.getValue().equalsIgnoreCase("Control") && mc.player.isFallFlying()) return;
            if (mc.player.fallDistance < 5.0 && !(mc.player.isShiftKeyDown()) && !(mc.player.onClimbable()) && mc.level.getBlockState(mc.player.blockPosition()).getBlock() != Blocks.COBWEB && !(mc.player.getAbilities().flying)) {
                if (!(mc.player.isInWater())) {
                    if (!(mc.player.isInLava())) break label_5;
                }
                if (this.speedInLiquid.getValue()) break label_5;
            }
            return;
            label_5:
            if (mc.player.input.getMoveVector().y > 0.0f) break label_6;
            this.speed = (MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED) * (this.forward > 0.0 ? 0.66 : 1.0));
            if (this.stage == 1 && MovementUtils.isMoving() && !(!mc.player.verticalCollision)) {
                ((Vec3dAccessor) mc.player.getDeltaMovement()).setY(MovementUtils.getPotionJump(0.3999999463558197));
                arg0.setMovement(new Vec3(arg0.getMovement().x, mc.player.getDeltaMovement().y, arg0.getMovement().z));
                this.speed = (this.speed * 2.149);
                if (this.boost.getValue()) {
                    this.speed = (this.speed + getAppliedBoost());
                }
                this.stage = 2;
                break label_14;
            }
            if (this.stage == 2) {
                this.speed = (this.distance - (0.66 * (this.distance - MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED))));
                if (this.boost.getValue()) {
                    this.speed = (this.speed + getAppliedBoost());
                }
                this.stage = 3;
                break label_14;
            }
            if (mc.level.getEntityCollisions(mc.player, mc.player.getBoundingBox().move(0.0, mc.player.getDeltaMovement().y, 0.0)).isEmpty()) {
                if (!(!mc.player.verticalCollision)) {
                }
                this.stage = 1;
            }
            this.speed = (this.distance - (this.distance / 159.0));
            label_14:
            this.speed = Math.max(this.speed, MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
            if (((!(mc.player.input.keyPresses.forward())) || (mc.player.input.keyPresses.backward())) || (mc.player.input.keyPresses.left())) break label_15;
            int var3 = (!(mc.player.input.keyPresses.right()) ? 1 : 0);
            if (!(this.mode.getValue().equalsIgnoreCase("StrafeStrict"))) {
                if (!var3) {
                }
            } else {
            }
            double var4 = MovementUtils.getPotionSpeed(0.576);
            if (!(this.mode.getValue().equalsIgnoreCase("StrafeStrict"))) {
                if (!var3) {
                }
            } else {
            }
            double var6 = MovementUtils.getPotionSpeed(0.57);
            this.speed = Math.min(this.speed, (this.ticks > 25 ? var4 : var6));
            this.ticks = (this.ticks + 1);
            if (this.ticks > 50) {
                this.ticks = 0;
            }
            Object var8 = MovementUtils.forward(this.speed);
            arg0.setMovement(new Vec3(var8.x, arg0.getMovement().y, arg0.getMovement().z));
            arg0.setMovement(new Vec3(arg0.getMovement().x, arg0.getMovement().y, var8.y));
            this.forward = ((double) mc.player.input.getMoveVector().y);
            arg0.setCancelled(true);
        }
    }

    private double getAppliedBoost() {
        if (BoostManager.INSTANCE == null) return 0.0;
        double var1 = BoostManager.INSTANCE.getBoostSpeed(false);
        if (var1 == 0.0) return 0.0;
        double var3 = (var1 * this.boostMultiplier.getValue().doubleValue());
        return Math.min(var3, this.boostCap.getValue().doubleValue());
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    public String getModeValue() {
        return this.mode.getValue();
    }

    public float getVanillaSpeedValue() {
        return this.vanillaSpeed.getValue().floatValue();
    }

    public boolean isDrivingTimer() {
        if (mc.player == null) return false;
        if (!(this.mode.getValue().equalsIgnoreCase("Strafe")) && !(this.mode.getValue().equalsIgnoreCase("StrafeStrict"))) return false;
        if (!(this.useTimer.getValue())) return false;
        if (((!(MovementUtils.isMoving())) || (mc.player.isShiftKeyDown())) || (mc.player.isInWater())) break label_3;
        int var1 = (mc.player.fallDistance < 5.0 ? 1 : 0);
        if (!var1) break label_6;
        if (this.ticks <= this.bypassThreshold.getValue().intValue()) {
            if (this.timerBypass.getValue()) break label_6;
        }
        break label_7;
        label_6:
        label_7:
        return false;
    }

    private boolean hasCeiling(double arg0, double arg1, double arg2) {
        if (mc.level == null) return false;
        Object var7 = BlockPos.containing(arg0, (arg1 + 1.0), arg2);
        Object var8 = mc.level.getBlockState(var7);
        if (!(var8.isAir())) {
            if (((var8.isSolid()) || (var8.blocksMotion())) || (!(var8.canBeReplaced()))) {
                return true;
            }
        }
        Object var9 = BlockPos.containing(arg0, (arg1 + 1.2), arg2);
        if (!(var9.equals(var7))) {
            Object var10 = mc.level.getBlockState(var9);
            if (var10.isAir()) break label_4;
            if (!(var10.isSolid()) && !(var10.blocksMotion())) {
                if (var10.canBeReplaced()) break label_4;
            }
            break label_5;
            label_4:
            label_5:
            return false;
        }
        return false;
    }

    private boolean hasCeilingAhead() {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        double var1 = mc.player.getDeltaMovement().x;
        double var3 = mc.player.getDeltaMovement().z;
        double var5 = Math.hypot(var1, var3);
        if (var5 > 0.01) {
            double var7 = Math.min(var5, 0.35);
            double var9 = (mc.player.getX() + ((var1 / var5) * var7));
            double var11 = (mc.player.getZ() + ((var3 / var5) * var7));
            if (!(hasCeiling(var9, mc.player.getY(), var11))) return false;
        }
        if (MovementUtils.isMoving()) {
            var7 = MovementUtils.forward(0.3);
            double var8 = (mc.player.getX() + var7.x);
            double var10 = (mc.player.getZ() + var7.y);
            if (!(hasCeiling(var8, mc.player.getY(), var10))) return false;
        }
        return true;
    }

}
