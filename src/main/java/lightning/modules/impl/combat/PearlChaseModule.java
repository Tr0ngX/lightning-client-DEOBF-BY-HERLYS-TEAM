package lightning.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "PearlChase", description = "Automatically throws an Ender Pearl to chase enemy pearl throws with trajectory prediction and obstacle avoidance.", category = Module.Category.COMBAT)
public class PearlChaseModule extends Module {
    public ModeSetting target;
    public BooleanSetting ignoreNaked;
    public ModeSetting rotate;
    public ModeSetting switchMode;
    public NumberSetting offset;
    public NumberSetting distance;
    public ModeSetting arcMode;
    public BooleanSetting avoidObstacles;
    public NumberSetting enemyRange;
    public NumberSetting delay;
    public BooleanSetting whileEating;
    public BooleanSetting itemDisable;
    public BooleanSetting render;
    public NumberSetting renderDuration;
    private final Set processedPearls;
    private PearlChaseModule.PendingThrow pendingThrow;
    private final Timer renderTimer;
    private Vec3 renderLandingPos;
    private List renderPath;

    public PearlChaseModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Lowest";
        tmp0[1] = "Furthest";
        tmp0[2] = "Nearest";
        this.target = new ModeSetting("Target", "Priority used to select which enemy to chase.", "Nearest", tmp0);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore naked players, even if wearing only elytra.", false);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Normal";
        tmp1[2] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Rotation mode used before throwing the pearl.", "Silent", tmp1);
        this.switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to ender pearls.", "Silent", InventoryUtils.SWITCH_MODES);
        this.offset = new NumberSetting("Offset", "Distance offset relative to target landing position along flight path (positive = ahead, negative = behind).", 0.0, -10.0, 10.0);
        this.distance = new NumberSetting("Distance", "Minimum horizontal travel distance that enemy will move for pearl to be chased.", 10.0, 0.0, 50.0);
        var tmp2 = new String[3];
        tmp2[0] = "Smart";
        tmp2[1] = "Low";
        tmp2[2] = "High";
        this.arcMode = new ModeSetting("ArcMode", "Trajectory selection mode: Smart (low if clear, high if blocked), Low (fast direct), High (maximum lob).", "Smart", tmp2);
        this.avoidObstacles = new BooleanSetting("AvoidObstacles", "Calculates alternate high arcs or yaw offsets if direct path is blocked by terrain.", true);
        this.enemyRange = new NumberSetting("EnemyRange", "Maximum range to enemy player throwing pearl.", 64.0, 8.0, 128.0);
        this.delay = new NumberSetting("Delay", "Delay in ticks before throwing the pearl.", 0, 0, 20);
        this.whileEating = new BooleanSetting("WhileEating", "Allows throwing pearl while eating.", true);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off module if no ender pearls are found in inventory.", false);
        this.render = new BooleanSetting("Render", "Renders predicted landing position and pearl trajectory line.", true);
        this.renderDuration = new NumberSetting("RenderTime", "Duration in seconds to render trajectory after throwing.", 3.0, 0.5, 10.0);
        this.processedPearls = new HashSet();
        this.pendingThrow = null;
        this.renderTimer = new Timer();
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    public void onEnable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    public void onDisable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var6 = arg0.getEntity();
        if (!((var6 instanceof ThrownEnderpearl))) return;
        Object var7 = ((ThrownEnderpearl) var6);
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var8 = null;
        Object var9 = var7.getOwner();
        if ((var9 instanceof Player)) {
            var tmp0 = ((Player) var9);
            Object var10 = tmp0;
            var8 = tmp0;
            break label_7;
        }
        var10 = 16.0;
        Object var12 = mc.level.players().iterator();
        while (var12.hasNext()) {
            Object var13 = ((Player) var12.next());
            if (!(validTarget(var13))) break label_5;
            double var14 = var13.distanceToSqr(var7.position());
            if (var13.distanceToSqr(var7.position()) >= var10) {
                break label_5;
            }
            var10 = var14;
            var8 = var13;
        }
        label_7:
        if ((var8 == null) || (!(validTarget(var8)))) {
            return;
        }
        var10 = getBestTarget();
        if (var10 != null && var8 != var10) return;
        if (!(this.processedPearls.add(var7.getId()))) return;
        if (this.processedPearls.size() > 100) {
            this.processedPearls.clear();
        }
        var tmp1 = predictEnemyLanding(var7, var8);
        Object var5 = tmp1;
        if (tmp1 == null) return;
        double var11 = (var5.x - var7.getX());
        double var3 = (var5.z - var7.getZ());
        var13 = Math.sqrt(((var11 * var11) + ((var5.z - var7.getZ()) * var3)));
        if (var13 < this.distance.getValue().doubleValue()) return;
        Object var15 = new Vec3(var11, 0.0, var3);
        if (var13 > 1.0E-4) {
        } else {
        }
        var15 = Vec3.ZERO;
        Object var16 = var5.add(var15.scale(this.offset.getValue().doubleValue()));
        Object var17 = mc.level.clip(new ClipContext(var16.add(0.0, 2.0, 0.0), var16.subtract(0.0, 10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var17.getType() != HitResult.Type.MISS) {
            var16 = var17.getLocation();
        }
        var tmp2 = solveTrajectory(var16);
        Object var2 = tmp2;
        if (tmp2 == null) return;
        int var18 = this.delay.getValue().intValue();
        if (var18 <= 0) {
            executeThrow(var2.yaw(), var2.pitch(), var16, var2.path());
        } else {
            this.pendingThrow = new PearlChaseModule.PendingThrow(var16, var2.yaw(), var2.pitch(), var18, var2.path());
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.pendingThrow == null) break label_6;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        int var2 = (this.pendingThrow.delayTicks() - 1);
        if (var2 <= 0) {
            Object var3 = solveTrajectory(this.pendingThrow.targetPos());
            if (var3 != null) {
                executeThrow(var3.yaw(), var3.pitch(), this.pendingThrow.targetPos(), var3.path());
            } else {
                executeThrow(this.pendingThrow.yaw(), this.pendingThrow.pitch(), this.pendingThrow.targetPos(), this.pendingThrow.path());
            }
            this.pendingThrow = null;
            break label_6;
        }
        this.pendingThrow = new PearlChaseModule.PendingThrow(this.pendingThrow.targetPos(), this.pendingThrow.yaw(), this.pendingThrow.pitch(), var2, this.pendingThrow.path());
        label_6:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((((!(this.render.getValue())) || (this.renderLandingPos == null)) || (mc.player == null)) || (mc.level == null)) {
            return;
        }
        long var4 = this.renderTimer.timeElapsed();
        long var2 = ((long) (this.renderDuration.getValue().doubleValue() * 1000.0));
        if (var4 > ((long) (this.renderDuration.getValue().doubleValue() * 1000.0))) {
            this.renderLandingPos = null;
            this.renderPath = null;
            return;
        }
        float var6 = (1.0f - (((float) var4) / ((float) var2)));
        int var7 = ((int) (60.0f * var6));
        int var8 = ((int) (220.0f * var6));
        if (var7 <= 0 && var8 <= 0) return;
        Object var9 = new Color(0, 230, 255, var7);
        Object var10 = new Color(0, 230, 255, var8);
        Object var11 = new Color(80, 255, 180, var8);
        Object var12 = new AABB((this.renderLandingPos.x - 0.35), this.renderLandingPos.y, (this.renderLandingPos.z - 0.35), (this.renderLandingPos.x + 0.35), (this.renderLandingPos.y + 0.7), (this.renderLandingPos.z + 0.35));
        Renderer3D.renderBox(arg0.getMatrices(), var12, var9);
        Renderer3D.renderBoxOutline(arg0.getMatrices(), var12, var10);
        if ((this.renderPath == null) || (this.renderPath.size() <= 1)) break label_5;
        int var13 = 0;
        while (var13 < (this.renderPath.size() - 1)) {
            Renderer3D.renderLine(arg0.getMatrices(), ((Vec3) this.renderPath.get(var13)), ((Vec3) this.renderPath.get((var13 + 1))), var11);
            var13++;
        }
        label_5:
    }

    private void executeThrow(float arg0, float arg1, Vec3 arg2, List arg3) {
        if (this.switchMode.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (this.itemDisable.getValue()) {
                LightningClient.CHAT_MANAGER.tagged("You are currently not holding an Ender Pearl.", getName());
                setToggled(false);
            }
            return;
        }
        if (!(this.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var5 = InventoryUtils.find(0, 35, 8);
        int var6 = mc.player.getInventory().getSelectedSlot();
        if (var5 == -1) {
            if (this.itemDisable.getValue()) {
                LightningClient.CHAT_MANAGER.tagged("No Ender Pearls could be found in your inventory.", getName());
                setToggled(false);
            }
            return;
        }
        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            LightningClient.ROTATION_MANAGER.packetRotate(arg0, arg1, true);
            break label_8;
        }
        if (!(this.rotate.getValue().equalsIgnoreCase("Normal"))) break label_8;
        var tmp0 = new float[2];
        tmp0[0] = arg0;
        tmp0[1] = arg1;
        LightningClient.ROTATION_MANAGER.legacyRotate(tmp0, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
        mc.player.setYRot(arg0);
        mc.player.setXRot(arg1);
        label_8:
        InventoryUtils.switchSlot(this.switchMode.getValue(), var5, var6);
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg2, arg0, arg1));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.switchMode.getValue(), var5, var6);
        this.renderLandingPos = arg2;
        this.renderPath = arg3;
        this.renderTimer.reset();
    }

    private Vec3 predictEnemyLanding(ThrownEnderpearl arg0, Player arg1) {
        Object var3 = arg0.position();
        Object var4 = arg0.getDeltaMovement();
        if (var4.lengthSqr() < 1.0E-4 && arg1 != null) {
            var4 = getPearlInitialVelocity(arg1.getYRot(), arg1.getXRot(), arg1.getDeltaMovement(), arg1.onGround());
        }
        int var5 = 0;
        while (var5 < 300) {
            Object var6 = var3.add(var4);
            Object var7 = mc.level.clip(new ClipContext(var3, var6, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arg0));
            if (var7.getType() != HitResult.Type.MISS) return var7.getLocation();
            var3 = var6;
            var4 = var4.scale(0.99).subtract(0.0, 0.03, 0.0);
            var5++;
        }
        return var3;
    }

    private PearlChaseModule.TrajectorySolution solveTrajectory(Vec3 arg0) {
        Object var5 = mc.player.getEyePosition();
        Object var6 = arg0.subtract(var5);
        var tmp0 = ((float) Math.toDegrees(Math.atan2(-(var6.x), var6.z)));
        float var4 = tmp0;
        float var7 = tmp0;
        float var8 = 0.0f;
        double var9 = 1.7976931348623157E308;
        if (this.avoidObstacles.getValue()) {
            Object var11 = new float[7];
            var11[0] = 0.0f;
            var11[1] = -1.0f;
            var11[2] = 1.0f;
            var11[3] = -2.5f;
            var11[4] = 2.5f;
            var11[5] = -4.0f;
            Object var3 = var11;
            var11[6] = 4.0f;
            break label_1;
        }
        var11 = new float[1];
        var3 = var11;
        var11[0] = 0.0f;
        label_1:
        Object var2 = var3;
        var11 = var3;
        int var12 = var11.length;
        int var13 = 0;
        while (var13 < var12) {
            float var14 = var11[var13];
            float var15 = (var4 + var14);
            float var16 = -85.0f;
            while (var16 <= 85.0f) {
                Object var17 = simulatePearl(var5, var15, var16, arg0, false);
                double var18 = var17.distToTarget();
                if (var17.hitEarlyObstacle()) {
                    var18 = (var18 + 500.0);
                }
                if (this.arcMode.getValue().equalsIgnoreCase("Low")) {
                    var18 = (var18 + (((double) Math.max(0.0f, -(var16))) * 0.1));
                    break label_7;
                }
                if (this.arcMode.getValue().equalsIgnoreCase("High")) {
                    var18 = (var18 + (Math.max(0.0, (((double) var16) + 50.0)) * 0.2));
                    break label_7;
                }
                if ((!(this.arcMode.getValue().equalsIgnoreCase("Smart"))) || (var17.hitEarlyObstacle())) break label_7;
                var18 = (var18 + (((double) Math.max(0.0f, -(var16))) * 0.02));
                label_7:
                if (var18 >= var9) {
                } else {
                    var9 = var18;
                    var8 = var16;
                    var7 = var15;
                }
                var16 = (var16 + 1.0f);
            }
            if (var9 < 1.0) {
                break label_12;
            }
            var13++;
        }
        label_12:
        var11 = var8;
        var12 = (var8 - 1.5f);
        while (var12 <= (var8 + 1.5f)) {
            var13 = simulatePearl(var5, var7, var12, arg0, false);
            var14 = var13.distToTarget();
            if (var13.hitEarlyObstacle()) {
                var14 = (var14 + 500.0);
            }
            if (var14 >= var9) {
            } else {
                var9 = var14;
                var11 = var12;
            }
            var12 = (var12 + 0.1f);
        }
        var12 = simulatePearl(var5, var7, var11, arg0, true);
        return new PearlChaseModule.TrajectorySolution(var7, var11, var12.path());
    }

    private PearlChaseModule.SimulationResult simulatePearl(Vec3 arg0, float arg1, float arg2, Vec3 arg3, boolean arg4) {
        if (mc.player != null) {
            break label_1;
        }
        label_1:
        Object var7 = Vec3.ZERO;
        if (mc.player != null) {
            if (mc.player.onGround()) {
            }
        } else {
        }
        int var8 = 0;
        Object var9 = getPearlInitialVelocity(arg1, arg2, var7, var8);
        Object var10 = arg0;
        if (!(!arg4)) {
        } else {
        }
        Object var6 = null;
        Object var11 = null;
        if (!(!arg4)) {
            var6.add(var10);
        }
        Object var12 = null;
        int var13 = 0;
        double var14 = arg3.distanceTo(arg0);
        int var16 = 0;
        while (var16 < 250) {
            Object var17 = var10.add(var9);
            Object var18 = mc.level.clip(new ClipContext(var10, var17, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
            if (var18.getType() != HitResult.Type.MISS) {
                var12 = var18.getLocation();
                if (!(!arg4)) {
                    var6.add(var12);
                }
                double var19 = var12.distanceTo(arg0);
                double var21 = var12.distanceTo(arg3);
                if (var19 >= (var14 * 0.7)) break label_13;
                if (var21 <= 3.0) {
                    break label_13;
                }
                var13 = 1;
                break label_13;
            }
            var10 = var17;
            if (!(!arg4)) {
                var6.add(var10);
            }
            var9 = var9.scale(0.99).subtract(0.0, 0.03, 0.0);
            var16++;
        }
        label_13:
        if (var12 == null) {
            var12 = var10;
        }
        var16 = var12.distanceTo(arg3);
        if (var6 != null) {
        } else {
        }
        return new PearlChaseModule.SimulationResult;
    }

    private static Vec3 getLookVector(float arg0, float arg1) {
        float var2 = (-(Mth.sin(((double) (arg0 * 0.017453292f)))) * Mth.cos(((double) (arg1 * 0.017453292f))));
        float var3 = -(Mth.sin(((double) (arg1 * 0.017453292f))));
        float var4 = (Mth.cos(((double) (arg0 * 0.017453292f))) * Mth.cos(((double) (arg1 * 0.017453292f))));
        return new Vec3(((double) var2), ((double) var3), ((double) var4)).normalize();
    }

    private static Vec3 getPearlInitialVelocity(float arg0, float arg1, Vec3 arg2, boolean arg3) {
        Object var4 = getLookVector(arg0, arg1);
        Object var5 = new Vec3(arg2.x, (!(!arg3) ? 0.0 : arg2.y), arg2.z);
        return var4.scale(1.5).add(var5);
    }

    private boolean validTarget(Player arg0) {
        if ((arg0 == null) || (arg0 == mc.player)) {
            return false;
        }
        if (arg0.isDeadOrDying()) return false;
        if (mc.player.distanceToSqr(arg0) > Mth.square(this.enemyRange.getValue().doubleValue())) return false;
        if (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString())) return false;
        if (this.ignoreNaked.getValue()) {
            if (!(EntityUtils.isNaked(arg0))) {
            }
        } else {
        }
        return false;
    }

    private Player getBestTarget() {
        if ((mc.player == null) || (mc.level == null)) {
            return null;
        }
        Object var1 = null;
        double var2 = 0.0;
        Object var4 = mc.level.players().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Player) var4.next());
            if (!(validTarget(var5))) {
                break label_2;
            }
            Object var8 = this.target.getValue();
            int var9 = -1;
            switch (var8.hashCode()) {
                case -2012995118 -> { break label_4; }
                case 1575438095 -> { break label_5; }
                default -> { break label_6; }
            }
            label_4:
            if (!(var8.equals("Lowest"))) break label_6;
            var9 = 0;
            break label_6;
            label_5:
            if (!(var8.equals("Furthest"))) break label_6;
            var9 = 1;
            label_6:
            switch (var9) {
                case 0 -> { break label_7; }
                case 1 -> { break label_8; }
                default -> { break label_9; }
            }
            label_7:
            break label_10;
            label_8:
            break label_10;
            label_9:
            label_10:
            double var6 = mc.player.distanceToSqr(var5);
            if (var1 != null && var6 >= var2) {
                break label_2;
            }
            var1 = var5;
            var2 = var6;
        }
        return var1;
    }


    record TrajectorySolution(float yaw, float pitch, List path) {

        private TrajectorySolution(float arg0, float arg1, List arg2) {
            this.yaw = arg0;
            this.pitch = arg1;
            this.path = arg2;
        }

        public float yaw() {
            return this.yaw;
        }

        public float pitch() {
            return this.pitch;
        }

        public List path() {
            return this.path;
        }

    }

    record SimulationResult(Vec3 landingPos, double distToTarget, boolean hitEarlyObstacle, int ticks, List path) {

        private SimulationResult(Vec3 arg0, double arg1, boolean arg2, int arg3, List arg4) {
            this.landingPos = arg0;
            this.distToTarget = arg1;
            this.hitEarlyObstacle = arg2;
            this.ticks = arg3;
            this.path = arg4;
        }

        public Vec3 landingPos() {
            return this.landingPos;
        }

        public double distToTarget() {
            return this.distToTarget;
        }

        public boolean hitEarlyObstacle() {
            return this.hitEarlyObstacle;
        }

        public int ticks() {
            return this.ticks;
        }

        public List path() {
            return this.path;
        }

    }

    record PendingThrow(Vec3 targetPos, float yaw, float pitch, int delayTicks, List path) {

        private PendingThrow(Vec3 arg0, float arg1, float arg2, int arg3, List arg4) {
            this.targetPos = arg0;
            this.yaw = arg1;
            this.pitch = arg2;
            this.delayTicks = arg3;
            this.path = arg4;
        }

        public Vec3 targetPos() {
            return this.targetPos;
        }

        public float yaw() {
            return this.yaw;
        }

        public float pitch() {
            return this.pitch;
        }

        public int delayTicks() {
            return this.delayTicks;
        }

        public List path() {
            return this.path;
        }

    }
}
