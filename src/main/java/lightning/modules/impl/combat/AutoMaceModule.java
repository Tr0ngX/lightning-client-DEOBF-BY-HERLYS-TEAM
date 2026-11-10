package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AutoMace", description = "Elytra-boosts above the target, free-falls in, and mace-smashes on repeat.", category = Module.Category.COMBAT)
public class AutoMaceModule extends Module {
    public NumberSetting range;
    public NumberSetting minHeight;
    public NumberSetting attackRange;
    public NumberSetting angle;
    public BooleanSetting smartTerrain;
    public NumberSetting terrainLookAhead;
    public NumberSetting terrainClimbRate;
    public NumberSetting terrainMaxClimb;
    public BooleanSetting fakeFly;
    public BooleanSetting swing;
    public ModeSetting swap;
    public BooleanSetting render;
    public ModeSetting renderMode;
    public CategorySetting entitiesCategory;
    public BooleanSetting players;
    public BooleanSetting friends;
    public BooleanSetting hostiles;
    public BooleanSetting neutrals;
    public BooleanSetting animals;
    public ColorSetting boxColor;
    private static final long FADE_TIME_MS = 300L;
    private static final float SMASH_FALL_DISTANCE = 1.5f;
    private static final int MIN_FREEFALL_TICKS = 3;
    private static final float STEEP_PITCH = 85.0f;
    private static final double LEAD_TICKS = 4.0;
    private static final int STUCK_SWEEP_TICKS = 20;
    private static final double SCAN_LOOKAHEAD = 8.0;
    private static final double SCAN_CLEAR_DIST = 6.0;
    private static final double SCAN_MIN_DIST = 2.5;
    private static final float[] SCAN_YAW_OFFSETS;
    public Entity target;
    private Entity lastTarget;
    private long lastTargetTime;
    private int originalSlot;
    private boolean ascending;
    private boolean elytraUnequipped;
    private int elytraParkedSlot;
    private int freefallTicks;
    private double freefallLastDistance;
    private double freefallLastTargetDist;
    private boolean launchKicked;
    private int armorParkedSlot;
    private int escapeStuckTicks;
    private double terrainLift;
    private int escapeSteepTicks;
    private int fakeFlyTicks;
    private static final int ESCAPE_STEEP_TICKS = 10;
    private static final int FAKE_FLY_DELAY_TICKS = 2;
    private boolean freeLookInitialized;
    private float freeYaw;
    private float freePitch;

    public AutoMaceModule() {
        this.range = new NumberSetting("Range", "Search radius to find a target.", 20.0, 5.0, 50.0);
        this.minHeight = new NumberSetting("MinHeight", "Vertical height above the target to reach before diving.", 15.0, 3.0, 30.0);
        this.attackRange = new NumberSetting("AttackRange", "Distance to the target that triggers the mace attack.", 4.0, 1.0, 6.0);
        this.angle = new NumberSetting("Angle", "Angle", "Flight angle relative to the target: 0 = horizontal, 90 = straight vertical.", new Setting.Visibility(), 60.0, 0.0, 90.0, 1);
        this.smartTerrain = new BooleanSetting("SmartTerrain", "Predicts terrain ahead during ascend/dive and steepens the boost to fly over/around it instead of Angle's fixed value.", true);
        this.terrainLookAhead = new NumberSetting("TerrainLookAhead", "SmartTerrain: how many steps ahead along the boost line are checked for obstacles.", 6.0, 2.0, 15.0);
        this.terrainClimbRate = new NumberSetting("TerrainClimbRate", "SmartTerrain: pitch (degrees) added per tick while the line ahead is blocked.", 4.0, 1.0, 15.0);
        this.terrainMaxClimb = new NumberSetting("TerrainMaxClimb", "SmartTerrain: max pitch lift before falling back to the escape sweep.", 40.0, 10.0, 85.0);
        this.fakeFly = new BooleanSetting("FakeFly", "Keeps real armor on and cycles the elytra in/out (GrimV3 trick) instead of wearing it the whole combo. Opt-in, reliability unverified.", false);
        this.swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        this.swap = new ModeSetting("Swap", "Weapon switch mode.", "Silent", tmp0);
        this.render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
        var tmp1 = new String[3];
        tmp1[0] = "Box";
        tmp1[1] = "Circle";
        tmp1[2] = "Both";
        this.renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", tmp1);
        this.entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
        this.players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
        this.friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.hostiles = new BooleanSetting("Hostiles", "Target hostile mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.neutrals = new BooleanSetting("Neutrals", "Target neutral mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
        this.target = null;
        this.lastTarget = null;
        this.lastTargetTime = 0L;
        this.originalSlot = -1;
        this.ascending = true;
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
        this.freefallTicks = 0;
        this.freefallLastDistance = 0.0;
        this.freefallLastTargetDist = -1.0;
        this.launchKicked = false;
        this.armorParkedSlot = -1;
        this.escapeStuckTicks = 0;
        this.terrainLift = 0.0;
        this.escapeSteepTicks = 0;
        this.fakeFlyTicks = 0;
        this.freeLookInitialized = false;
    }

    public Entity getTarget() {
        return this.target;
    }

    public boolean isFreeLookActive() {
        if (!(isToggled())) break label_0;
        return this.target != null;
    }

    public float getFreeYaw() {
        return this.freeYaw;
    }

    public float getFreePitch() {
        return this.freePitch;
    }

    public void onMouseTurn(double arg0, double arg1) {
        if (!this.freeLookInitialized && mc.player != null) {
            this.freeYaw = mc.player.getYRot();
            this.freePitch = mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        this.freeYaw = (this.freeYaw + ((float) (arg0 * 0.15)));
        this.freePitch = Mth.clamp((this.freePitch + ((float) (arg1 * 0.15))), -90.0f, 90.0f);
    }

    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.ascending = true;
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
        this.freefallTicks = 0;
        this.freefallLastDistance = 0.0;
        this.freefallLastTargetDist = -1.0;
        this.originalSlot = -1;
        this.freeLookInitialized = false;
        this.launchKicked = false;
        this.armorParkedSlot = -1;
        this.escapeStuckTicks = 0;
        this.escapeSteepTicks = 0;
        this.terrainLift = 0.0;
        this.fakeFlyTicks = 0;
        if (mc.player != null && InventoryUtils.find(Items.MACE) == -1) {
            setToggled(false);
        }
    }

    public void onDisable() {
        if (mc.player == null) return;
        reequipElytra();
        restoreArmorIfParked();
        if (this.originalSlot != -1) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.player.isDeadOrDying())) {
            return;
        }
        if (this.target != null) {
            if (!(this.target.isAlive())) {
            }
            this.target = findTarget();
        }
        if (this.target == null) {
            reequipElytra();
            restoreArmorIfParked();
            this.ascending = true;
            this.freeLookInitialized = false;
            return;
        }
        if (!this.freeLookInitialized) {
            this.freeYaw = mc.player.getYRot();
            this.freePitch = mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        var tmp0 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        Object var2 = tmp0;
        if (tmp0 == null) return;
        if (!(!this.ascending)) {
            tickAscend(var2);
        } else {
            tickDive(var2);
        }
    }

    private void tickAscend(ElytraFlyModule arg0) {
        reequipElytra();
        double var4 = (mc.player.getY() - this.target.getY());
        if (var4 >= this.minHeight.getValue().doubleValue()) {
            double var6 = Math.hypot((mc.player.getX() - this.target.getX()), (mc.player.getZ() - this.target.getZ()));
            Object var8 = mc.player.getDeltaMovement();
            var tmp0 = new Object[5];
            tmp0[0] = var4;
            tmp0[1] = var6;
            tmp0[2] = var8.x;
            tmp0[3] = var8.y;
            tmp0[4] = var8.z;
            LightningClient.LOGGER.info("[MaceDbg] ASCEND->DIVE height={} horizDist={} vel=({},{},{})", tmp0);
            this.ascending = false;
            this.freefallTicks = 0;
            this.escapeStuckTicks = 0;
            return;
        }
        if (!(ensureFlying(arg0))) return;
        var6 = this.target.getBoundingBox().getCenter();
        float var7 = ((float) RotationUtils.getYRotToVec(mc.player, var6));
        var8 = (var7 + 180.0f);
        float var9 = -(this.angle.getValue().floatValue());
        if (this.escapeSteepTicks > 0) {
            this.escapeSteepTicks = (this.escapeSteepTicks - 1);
            var9 = -85.0f;
        }
        int var10 = this.smartTerrain.getValue();
        int var11 = (mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025 ? 1 : 0);
        if (!(!var10) && !var11) {
        } else {
        }
        Object var3 = null;
        Object var12 = null;
        if (var3 != null) {
            this.escapeStuckTicks = 0;
            snapRotation(var3[0], var3[1]);
            arg0.tryUseGrimFirework(var3[0], var3[1]);
            return;
        }
        if (!var10) break label_8;
        int var13 = (ceilingBlocked() ? 1 : 0);
        if (!var10) break label_11;
        if (!(predictsCollision())) {
            if (!var11) break label_11;
        }
        break label_12;
        label_11:
        label_12:
        int var2 = 0;
        int var14 = 0;
        if (!var13) {
            if (!(!var2)) {
            }
            this.escapeStuckTicks = (this.escapeStuckTicks + 1);
        } else {
        }
        (this.escapeStuckTicks + 1).escapeStuckTicks = 0;
        if (!var13) {
            if (this.escapeStuckTicks > 20) {
            }
            var9 = -15.0f;
            var8 = (var8 + (90.0f * ((float) ((this.escapeStuckTicks / 20) % 4))));
            break label_18;
        }
        if (!var2) break label_18;
        var9 = -85.0f;
        label_18:
        snapRotation(var8, var9);
        arg0.tryUseGrimFirework(var8, var9);
    }

    private void tickDive(ElytraFlyModule arg0) {
        Object var2 = this.target.getBoundingBox().getCenter();
        Object var3 = var2.add(this.target.getDeltaMovement().scale(4.0));
        float var4 = ((float) RotationUtils.getYRotToVec(mc.player, var3));
        float var5 = ((float) RotationUtils.getXRotToVec(mc.player, var3));
        if (!this.elytraUnequipped) {
            double var6 = (mc.player.getY() - this.target.getY());
            double var8 = (this.attackRange.getValue().doubleValue() + 3.0);
            double var10 = Math.sqrt((Math.pow((mc.player.getX() - var2.x), 2.0) + Math.pow((mc.player.getZ() - var2.z), 2.0)));
            double var12 = (this.attackRange.getValue().doubleValue() * 1.5);
            Object var14 = mc.player.getDeltaMovement();
            var tmp0 = new Object[7];
            tmp0[0] = var6;
            tmp0[1] = var10;
            tmp0[2] = var8;
            tmp0[3] = var12;
            tmp0[4] = var14.x;
            tmp0[5] = var14.y;
            tmp0[6] = var14.z;
            LightningClient.LOGGER.info("[MaceDbg] DIVE-HOMING height={} horizDist={} cutHeight={} cutHorizDist={} vel=({},{},{})", tmp0);
            if (var6 < 1.0 && var10 > var12) {
                LightningClient.LOGGER.info("[MaceDbg] DIVE ABORT-TO-ASCEND (ran out of height, still far) height={} horizDist={}", var6, var10);
                this.ascending = true;
                return;
            }
            if (var6 > var8) break label_1;
            if (var10 > var12) {
                label_1:
                if (!(ensureFlying(arg0))) return;
                float var17 = var5;
                int var18 = (mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025 ? 1 : 0);
                if (this.smartTerrain.getValue() && !var18) {
                } else {
                }
                Object var16 = null;
                Object var19 = null;
                if (var16 != null) {
                    this.escapeStuckTicks = 0;
                    snapRotation(var16[0], var16[1]);
                    arg0.tryUseGrimFirework(var16[0], var16[1]);
                    return;
                }
                if (!(this.smartTerrain.getValue())) break label_9;
                if (!(predictsCollision())) {
                    if (!var18) break label_9;
                }
                break label_10;
                label_9:
                label_10:
                int var15 = 0;
                int var20 = 0;
                if (!(!var15)) {
                    this.escapeStuckTicks = (this.escapeStuckTicks + 1);
                    if (this.escapeStuckTicks > 20) {
                        this.ascending = true;
                        return;
                    }
                    var17 = (var5 < 0.0f ? -85.0f : 85.0f);
                } else {
                    this.escapeStuckTicks = 0;
                }
                snapRotation(var4, var17);
                arg0.tryUseGrimFirework(var4, var17);
                return;
            }
            var15 = mc.player.getDeltaMovement();
            var tmp1 = new Object[5];
            tmp1[0] = var6;
            tmp1[1] = var10;
            tmp1[2] = var15.x;
            tmp1[3] = var15.y;
            tmp1[4] = var15.z;
            LightningClient.LOGGER.info("[MaceDbg] CUT-ELYTRA (commit to freefall) height={} horizDist={} vel=({},{},{})", tmp1);
            unequipElytra();
            this.freefallTicks = 0;
            this.freefallLastDistance = mc.player.fallDistance;
            this.freefallLastTargetDist = -1.0;
            return;
        }
        var6 = mc.player.getEyePosition(1.0f);
        Object var7 = RotationUtils.getClampClosestPoint(var6, this.target.getBoundingBox());
        LightningClient.ROTATION_MANAGER.silentRotate(RotationUtils.getYRotToVec(var6, var7), RotationUtils.getXRotToVec(var6, var7));
        this.freefallTicks = (this.freefallTicks + 1);
        var8 = var6.distanceTo(var7);
        var tmp2 = new Object[6];
        tmp2[0] = this.freefallTicks;
        tmp2[1] = var8;
        tmp2[2] = mc.player.fallDistance;
        tmp2[3] = mc.player.getY();
        tmp2[4] = this.target.getY();
        tmp2[5] = mc.player.onGround();
        LightningClient.LOGGER.info("[MaceDbg] FREEFALL tick={} dist={} fallDistance={} y={} targetY={} onGround={}", tmp2);
        if (this.freefallTicks >= 3 && var8 <= this.attackRange.getValue().doubleValue() && mc.player.fallDistance >= 1.5) {
            var tmp3 = new Object[4];
            tmp3[0] = var8;
            tmp3[1] = this.attackRange.getValue().doubleValue();
            tmp3[2] = mc.player.fallDistance;
            tmp3[3] = mc.player.onGround();
            LightningClient.LOGGER.info("[MaceDbg] ATTACK dist={} attackRange={} fallDistance={} onGround={}", tmp3);
            attack(this.target);
            arg0.resetFireworkCooldown();
            this.escapeSteepTicks = 10;
            reequipElytra();
            arg0.sendGlideStart();
            this.ascending = true;
            this.launchKicked = false;
            return;
        }
        if (mc.player.onGround()) {
            var tmp4 = new Object[3];
            tmp4[0] = var8;
            tmp4[1] = this.attackRange.getValue().doubleValue();
            tmp4[2] = this.freefallTicks;
            LightningClient.LOGGER.info("[MaceDbg] MISS-ONGROUND dist={} attackRange={} freefallTicks={}", tmp4);
            arg0.resetFireworkCooldown();
            reequipElytra();
            arg0.sendGlideStart();
            this.ascending = true;
            return;
        }
        if ((mc.player.fallDistance + 0.01) < this.freefallLastDistance) {
            reequipElytra();
            this.ascending = true;
            return;
        }
        this.freefallLastDistance = mc.player.fallDistance;
        if (this.freefallLastTargetDist >= 0.0 && var8 > (this.freefallLastTargetDist + 0.05)) {
            LightningClient.LOGGER.info("[MaceDbg] MISS-OVERSHOOT-DETECTOR dist={} prevDist={}", var8, this.freefallLastTargetDist);
            reequipElytra();
            this.freefallLastTargetDist = -1.0;
            return;
        }
        this.freefallLastTargetDist = var8;
    }

    private void snapRotation(float arg0, float arg1) {
        mc.player.setYRot(arg0);
        mc.player.setXRot(arg1);
        mc.player.setYBodyRot(arg0);
        mc.player.setYHeadRot(arg0);
    }

    private boolean predictsCollision() {
        if (mc.level == null) return false;
        Object var1 = mc.player.getDeltaMovement();
        if (var1.lengthSqr() < 1.0E-4) return false;
        Object var2 = mc.player.position().add(0.0, (((double) mc.player.getBbHeight()) * 0.5), 0.0);
        double var3 = Math.max(3.0, (var1.length() * 6.0));
        Object var5 = var2.add(var1.normalize().scale(var3));
        Object var6 = mc.level.clip(new ClipContext(var2, var5, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return var6.getType() == HitResult.Type.BLOCK;
    }

    private boolean ceilingBlocked() {
        if (mc.level == null) return false;
        Object var2 = mc.player.position().add(0.0, ((double) mc.player.getBbHeight()), 0.0);
        var tmp0 = var2.add(0.0, 5.0, 0.0);
        Object var1 = tmp0;
        Object var3 = mc.level.clip(new ClipContext(var2, tmp0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return var3.getType() == HitResult.Type.BLOCK;
    }

    private double rayClearDistance(float arg0, float arg1) {
        return rayClearDistance(arg0, arg1, 8.0);
    }

    private double rayClearDistance(float arg0, float arg1, double arg2) {
        if (mc.level == null) return 0.0;
        double var5 = Math.toRadians(((double) arg0));
        double var7 = Math.toRadians(((double) arg1));
        double var9 = Math.cos(var7);
        Object var11 = new Vec3((-(Math.sin(var5)) * var9), -(Math.sin(var7)), (Math.cos(var5) * var9));
        Object var12 = mc.player.position().add(0.0, (((double) mc.player.getBbHeight()) * 0.5), 0.0);
        Object var13 = mc.level.clip(new ClipContext(var12, var12.add(var11.scale(arg2)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var13.getType() == HitResult.Type.BLOCK) {
        } else {
        }
        return arg2;
    }

    private float[] steerClear(float arg0, float arg1) {
        if (mc.level == null) return null;
        int var3 = this.terrainLookAhead.getValue().intValue();
        double var4 = (8.0 / ((double) var3));
        int var6 = 0;
        int var7 = 1;
        while (var7 <= var3) {
            float var8 = (arg1 - ((float) ((this.terrainLift * ((double) var7)) / ((double) var3))));
            double var9 = (var4 * ((double) var7));
            if (rayClearDistance(arg0, var8, var9) >= (var9 - 0.001)) {
            } else {
                var6 = 1;
                break label_4;
            }
            var7++;
        }
        label_4:
        var7 = this.terrainClimbRate.getValue().doubleValue();
        var9 = this.terrainMaxClimb.getValue().doubleValue();
        if (!(!var6)) {
        } else {
        }
        Math.min((this.terrainLift + var7), var9).terrainLift = Math.max(0.0, (this.terrainLift - (var7 * 0.5)));
        if (!(!var6)) {
            if (this.terrainLift < var9) {
            }
            var tmp0 = new float[2];
            tmp0[0] = arg0;
            tmp0[1] = (arg1 - ((float) this.terrainLift));
            return tmp0;
        }
        return null;
    }

    private boolean ensureFlying(ElytraFlyModule arg0) {
        if (mc.player.onGround()) {
            Object var2 = mc.player.getDeltaMovement();
            if (var2.y < 0.42) {
                mc.player.setDeltaMovement(var2.x, 0.45, var2.z);
            }
            this.launchKicked = true;
            return false;
        }
        if (!(!this.launchKicked)) {
            this.launchKicked = false;
            ensureElytraWorn();
            this.fakeFlyTicks = 0;
            arg0.sendGlideStart();
            return false;
        }
        fakeFlyCycle(arg0);
        if (mc.player.isFallFlying()) return true;
        ensureElytraWorn();
        arg0.sendGlideStart();
        return false;
    }

    private void fakeFlyCycle(ElytraFlyModule arg0) {
        if (((!(this.fakeFly.getValue())) || (this.elytraUnequipped)) || (mc.player.onGround())) {
            return;
        }
        this.fakeFlyTicks = (this.fakeFlyTicks + 1);
        if (this.fakeFlyTicks < 2) return;
        this.fakeFlyTicks = 0;
        arg0.sendGlideStart();
    }

    public boolean shouldPinFallFlying() {
        if (((((!(isToggled())) || (!(this.fakeFly.getValue()))) || (this.target == null)) || (this.elytraUnequipped)) || (mc.player == null)) break label_0;
        return !(mc.player.onGround());
    }

    private void ensureElytraWorn() {
        if (this.fakeFly.getValue()) return;
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) return;
        int var2 = InventoryUtils.find(Items.ELYTRA);
        if (var2 == -1) return;
        if ((this.armorParkedSlot != -1) || (mc.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())) break label_3;
        var tmp0 = findEmptySlot();
        int var1 = tmp0;
        if (tmp0 == -1) break label_3;
        InventoryUtils.swapEquipment(var1, 6);
        this.armorParkedSlot = var1;
        label_3:
        InventoryUtils.swapEquipment(var2, 6);
    }

    private void restoreArmorIfParked() {
        if (this.armorParkedSlot == -1) return;
        InventoryUtils.swapEquipment(this.armorParkedSlot, 6);
        this.armorParkedSlot = -1;
    }

    private void unequipElytra() {
        if (!(!this.elytraUnequipped)) return;
        if (this.fakeFly.getValue()) {
            this.elytraUnequipped = true;
            return;
        }
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;
        int var1 = findEmptySlot();
        if (var1 == -1) return;
        InventoryUtils.swapEquipment(var1, 6);
        this.elytraParkedSlot = var1;
        this.elytraUnequipped = true;
    }

    private void reequipElytra() {
        if (!this.elytraUnequipped) return;
        if (this.elytraParkedSlot != -1) {
            InventoryUtils.swapEquipment(this.elytraParkedSlot, 6);
        }
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
    }

    private int findEmptySlot() {
        int var1 = 0;
        while (var1 <= 35) {
            if (!(mc.player.getInventory().getItem(var1).isEmpty())) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    private void attack(Entity arg0) {
        int var2 = mc.player.getInventory().getSelectedSlot();
        int var3 = InventoryUtils.find(Items.MACE);
        int var4 = 0;
        if ((var3 == -1) || (var3 == var2)) break label_2;
        if (this.swap.getValue().equalsIgnoreCase("Normal")) {
            if (this.originalSlot == -1) {
                this.originalSlot = var2;
            }
            InventoryUtils.switchSlot("Normal", var3, var2);
            break label_2;
        }
        if (!(this.swap.getValue().equalsIgnoreCase("Silent"))) break label_2;
        InventoryUtils.switchSlot("Silent", var3, var2);
        var4 = 1;
        label_2:
        mc.getConnection().send(new ServerboundAttackPacket(arg0.getId()));
        mc.player.resetAttackStrengthTicker();
        if (this.swing.getValue()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        if (!(!var4)) {
            InventoryUtils.switchBack("Silent", var3, var2);
        }
    }

    private Entity findTarget() {
        if ((mc.player == null) || (mc.level == null)) {
            return null;
        }
        Object var1 = new ArrayList();
        Object var2 = mc.player.getEyePosition(1.0f);
        double var3 = this.range.getValue().doubleValue();
        Object var5 = mc.level.entitiesForRendering().iterator();
        while (var5.hasNext()) {
            Object var6 = ((Entity) var5.next());
            if (((var6 == mc.player) || (!(var6.isAlive()))) || (EntityUtils.isGhost(var6))) break label_2;
            double var7 = var2.distanceTo(RotationUtils.getClampClosestPoint(var2, var6.getBoundingBox()));
            if (var2.distanceTo(RotationUtils.getClampClosestPoint(var2, var6.getBoundingBox())) > var3) {
                break label_2;
            }
            if ((var6 instanceof Player)) {
                Object var9 = ((Player) var6);
                if (!(this.players.getValue())) break label_2;
                if (!(this.friends.getValue()) && LightningClient.FRIEND_MANAGER.contains(var9.getName().getString())) {
                    break label_2;
                }
                var1.add(var6);
                break label_2;
            }
            if (EntityUtils.isNeutral(var6)) {
                if (!(this.neutrals.getValue()) && !(EntityUtils.isAngry(var6))) {
                    break label_2;
                }
                var1.add(var6);
                break label_2;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(var6)) {
                var1.add(var6);
                break label_2;
            }
            if (!(this.animals.getValue())) break label_2;
            if (!(EntityUtils.isAnimal(var6))) {
                break label_2;
            }
            var1.add(var6);
        }
        return ((Entity) var1.stream().min(Comparator.comparingDouble((arg1) -> arg0.distanceTo(RotationUtils.getClampClosestPoint(arg0, arg1.getBoundingBox())))).orElse(null));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((!(this.render.getValue())) || (mc.player == null)) || (mc.player.isDeadOrDying())) {
            return;
        }
        Object var2 = this.target;
        float var3 = 1.0f;
        if (var2 != null) {
            this.lastTarget = var2;
            this.lastTargetTime = System.currentTimeMillis();
            break label_4;
        }
        if (this.lastTarget == null) break label_4;
        long var4 = (System.currentTimeMillis() - this.lastTargetTime);
        if (var4 < 300L) {
            var2 = this.lastTarget;
            var3 = (1.0f - (((float) var4) / 300.0f));
            break label_4;
        }
        this.lastTarget = null;
        label_4:
        if (var2 == null) return;
        var4 = ColorUtils.getColor(this.boxColor.getColor(), ((int) (((float) this.boxColor.getColor().getAlpha()) * var3)));
        if (!(this.renderMode.getValue().equalsIgnoreCase("Box"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
            Object var5 = EntityUtils.getRenderPos(var2, arg0.getTickDelta());
            Object var6 = var2.getBoundingBox().move((var5.x - var2.getX()), (var5.y - var2.getY()), (var5.z - var2.getZ()));
            Renderer3D.renderBox(arg0.getMatrices(), var6, var4);
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var6, var4);
        }
        if (!(this.renderMode.getValue().equalsIgnoreCase("Circle"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderTargetCircle(arg0.getMatrices(), var2, arg0.getTickDelta(), var4);
        }
    }

    static {
        var tmp0 = new float[7];
        tmp0[0] = 0.0f;
        tmp0[1] = -25.0f;
        tmp0[2] = 25.0f;
        tmp0[3] = -50.0f;
        tmp0[4] = 50.0f;
        tmp0[5] = -75.0f;
        tmp0[6] = 75.0f;
        SCAN_YAW_OFFSETS = tmp0;
    }

}
