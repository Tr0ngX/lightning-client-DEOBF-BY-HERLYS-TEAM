package lightning.managers;

import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.KeyboardTickEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.events.impl.UpdateVelocityEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.Module;
import lightning.modules.impl.core.RotationsModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Easing;
import lightning.utils.rotations.LegacyRotation;
import lightning.utils.rotations.Rotation;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public class RotationManager implements IMinecraft {
    private Rotation rotation;
    private Module rotationOwner;
    private float prevYaw;
    private float prevPitch;
    private float serverYaw;
    private float serverPitch;
    private float lastPacketTargetYaw;
    private float lastPacketTargetPitch;
    private float lastSilentTargetYaw;
    private float lastSilentTargetPitch;
    private double lastSilentX;
    private double lastSilentY;
    private double lastSilentZ;
    private float prevRenderYaw;
    private float prevRenderPitch;
    private long lastRenderTime;
    private boolean batchingRotations;
    private boolean batchHasPending;
    private float batchYaw;
    private float batchPitch;
    public static boolean swapActive;
    private final PriorityBlockingQueue legacyQueue;
    private static final HashMap LEGACY_PRIORITIES;
    private float prevFixYaw;
    private long lastPacketRotateTime;
    private boolean silentSyncRequired;

    public RotationManager() {
        this.rotation = null;
        this.rotationOwner = null;
        this.lastPacketTargetYaw = Float.NaN;
        this.lastPacketTargetPitch = Float.NaN;
        this.lastSilentTargetYaw = Float.NaN;
        this.lastSilentTargetPitch = Float.NaN;
        this.lastSilentX = Double.NaN;
        this.lastSilentY = Double.NaN;
        this.lastSilentZ = Double.NaN;
        this.lastRenderTime = 0L;
        this.batchingRotations = false;
        this.batchHasPending = false;
        this.legacyQueue = new PriorityBlockingQueue(11, this::compareLegacyRotations);
        this.lastPacketRotateTime = 0L;
        this.silentSyncRequired = false;
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent(priority = -2147483648)
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (mc.player == null) return;
        this.legacyQueue.removeIf((arg0) -> (System.currentTimeMillis() - arg0.getTime()) > 100L);
        Object var3 = ((LegacyRotation) this.legacyQueue.peek());
        Object var4 = new Rotation(mc.player);
        Object var5 = new ClientRotationEvent(var4);
        LightningClient.EVENT_HANDLER.post(var5);
        if (var5.isCancelled() && var5.getOwner() != null) {
            break label_3;
        }
        int var2 = (var5.isCancelled() ? 0 : -1);
        if (var3 != null && var3.getPriority() > var2) {
            this.rotation = new Rotation(var3.getYaw(), var3.getPitch());
            this.rotationOwner = var3.getModule();
            this.lastRenderTime = System.currentTimeMillis();
            break label_6;
        }
        if (var5.isCancelled()) {
            this.rotation = var4;
            this.rotationOwner = var5.getOwner();
            this.lastRenderTime = System.currentTimeMillis();
            break label_6;
        }
        this.rotation = null;
        this.rotationOwner = null;
        label_6:
    }

    @SubscribeEvent(priority = 2147483647)
    public void onUpdateMovement(UpdateMovementEvent arg0) {
        if (this.rotation == null) return;
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return;
        Object var2 = ((EntityAccessor) mc.player);
        this.prevYaw = var2.getRawYRot();
        this.prevPitch = var2.getRawXRot();
        var2.setRawYRot(this.rotation.getYaw());
        var2.setRawXRot(this.rotation.getPitch());
        swapActive = true;
    }

    @SubscribeEvent(priority = -2147483648)
    public void onUpdateMovement$POST(UpdateMovementEvent.Post arg0) {
        if (this.rotation == null) return;
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return;
        swapActive = false;
        Object var2 = ((EntityAccessor) mc.player);
        var2.setRawYRot(this.prevYaw);
        var2.setRawXRot(this.prevPitch);
    }

    public int getLegacyModulePriority(Module arg0) {
        return ((Integer) LEGACY_PRIORITIES.getOrDefault(arg0.getName(), 0)).intValue();
    }

    private int compareLegacyRotations(LegacyRotation arg0, LegacyRotation arg1) {
        if (arg0.getPriority() == arg1.getPriority()) return -(Long.compare(arg0.getTime(), arg1.getTime()));
        return -(Integer.compare(arg0.getPriority(), arg1.getPriority()));
    }

    public void legacyRotate(float[] arg0, int arg1) {
        legacyRotate(arg0[0], arg0[1], arg1);
    }

    public void legacyRotate(float arg0, float arg1, int arg2) {
        this.legacyQueue.removeIf((arg1) -> {
    if (arg1.getModule() != null) break label_0;
    return arg1.getPriority() == arg0;
});
        this.legacyQueue.add(new LegacyRotation(arg0, arg1, arg2));
    }

    public void legacyRotate(float[] arg0, Module arg1, int arg2) {
        legacyRotate(arg0[0], arg0[1], arg1, arg2);
    }

    public void legacyRotate(float arg0, float arg1, Module arg2, int arg3) {
        this.legacyQueue.removeIf((arg1) -> arg1.getModule() == arg0);
        this.legacyQueue.add(new LegacyRotation(arg0, arg1, arg2, arg3));
    }

    @SubscribeEvent
    public void onUpdateVelocity(UpdateVelocityEvent arg0) {
        if (mc.player == null) return;
        if (!(((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).movementFix.getValue())) return;
        if (this.rotation == null) return;
        arg0.setVelocity(EntityAccessor.invokeMovementInputToVelocity(arg0.getMovementInput(), arg0.getSpeed(), this.rotation.getYaw()));
        arg0.setCancelled(true);
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.player.isPassenger())) {
            return;
        }
        if (!(((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).movementFix.getValue())) return;
        if (this.rotation == null) return;
        float var2 = arg0.getMovementForward();
        float var3 = arg0.getMovementSideways();
        float var4 = ((mc.player.getYRot() - this.rotation.getYaw()) * 0.017453292f);
        float var5 = Mth.cos(((double) var4));
        float var6 = Mth.sin(((double) var4));
        arg0.setMovementForward(((float) Math.round(((var2 * var5) + (var3 * var6)))));
        arg0.setMovementSideways(((float) Math.round(((var3 * var5) - (var2 * var6)))));
        arg0.setCancelled(true);
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.player.isPassenger())) {
            return;
        }
        if (!(((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).movementFix.getValue())) return;
        if (this.rotation == null) return;
        this.prevFixYaw = mc.player.getYRot();
        mc.player.setYRot(this.rotation.getYaw());
    }

    @SubscribeEvent
    public void onPlayerJump$POST(PlayerJumpEvent.Post arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.player.isPassenger())) {
            return;
        }
        if (!(((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).movementFix.getValue())) return;
        if (this.rotation == null) return;
        mc.player.setYRot(this.prevFixYaw);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (mc.player == null) return;
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ServerboundMovePlayerPacket)) {
            Object var3 = ((ServerboundMovePlayerPacket) var2);
            if (!(var3.hasRotation())) return;
            this.serverYaw = var3.getYRot(mc.player.getYRot());
            this.serverPitch = var3.getXRot(mc.player.getXRot());
        }
    }

    public void packetRotate(float[] arg0) {
        packetRotate(arg0[0], arg0[1]);
    }

    private float[] applyJitter(float arg0, float arg1) {
        if (arg1 > -89.5f) {
            if (arg1 >= 89.5f) {
            }
            var tmp0 = new float[2];
            tmp0[0] = arg0;
            tmp0[1] = arg1;
            return tmp0;
        }
        Object var3 = ((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).jitter.getValue();
        if (!(!"Grim".equalsIgnoreCase(var3))) {
            float var4 = ((float) (((Math.random() * 2.0) - 1.0) * 0.0010000000474974513));
            arg1 = Mth.clamp((arg1 + var4), -90.0f, 90.0f);
            break label_7;
        }
        if (!"Normal".equalsIgnoreCase(var3)) break label_7;
        var4 = 0.15f;
        float var5 = 0.35f;
        float var6 = (var4 + ((float) (Math.random() * ((double) (var5 - var4)))));
        float var7 = (var4 + ((float) (Math.random() * ((double) (var5 - var4)))));
        float var8 = (Math.random() < 0.5 ? -1.0f : 1.0f);
        var tmp1 = (var6 * (Math.random() < 0.5 ? -1.0f : 1.0f));
        var6 = tmp1;
        arg0 = (arg0 + tmp1);
        var tmp2 = (var7 * var8);
        var7 = tmp2;
        arg1 = Mth.clamp((arg1 + tmp2), -90.0f, 90.0f);
        label_7:
        var tmp3 = new float[2];
        tmp3[0] = arg0;
        tmp3[1] = arg1;
        return tmp3;
    }

    public boolean isPacketRotateActive() {
        return (System.currentTimeMillis() - this.lastPacketRotateTime) < 500L;
    }

    public void packetRotate(float arg0, float arg1) {
        packetRotate(arg0, arg1, false);
    }

    public void packetRotate(float arg0, float arg1, boolean arg2) {
        if (!arg2 && this.lastPacketTargetYaw == arg0 && this.lastPacketTargetPitch == arg1) return;
        this.lastPacketTargetYaw = arg0;
        this.lastPacketTargetPitch = arg1;
        Object var4 = applyJitter(arg0, arg1);
        float var5 = var4[0];
        float var6 = var4[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = var5;
        this.serverPitch = var6;
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
        } else {
        }
        double var7 = mc.player.getX();
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
        } else {
        }
        double var9 = mc.player.getY();
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
        } else {
        }
        double var11 = mc.player.getZ();
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
        } else {
        }
        int var13 = mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(arg0, arg1, arg2, mc.player.horizontalCollision)));
    }

    public void silentRotate(float[] arg0) {
        silentRotate(arg0[0], arg0[1]);
    }

    public void beginBatchRotation() {
        this.batchingRotations = true;
        this.batchHasPending = false;
    }

    public void batchRotate(float arg0, float arg1) {
        this.batchYaw = arg0;
        this.batchPitch = arg1;
        this.batchHasPending = true;
        Object var3 = applyJitter(arg0, arg1);
        this.serverYaw = var3[0];
        this.serverPitch = var3[1];
        this.silentSyncRequired = true;
    }

    public void endBatchRotation() {
        this.batchingRotations = false;
        if (!this.batchHasPending) return;
        this.batchHasPending = false;
        silentRotate(this.batchYaw, this.batchPitch);
    }

    public void silentRotate(float arg0, float arg1) {
        if (!(!this.batchingRotations)) {
            batchRotate(arg0, arg1);
            return;
        }
        if ((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) break label_1;
        int var4 = (LightningClient.PINGBYPASS_CONFIG.isServer() ? 1 : 0);
        if (!(!var4)) {
        } else {
        }
        double var5 = mc.player.getX();
        if (!(!var4)) {
        } else {
        }
        double var7 = mc.player.getY();
        if (!(!var4)) {
        } else {
        }
        double var9 = mc.player.getZ();
        if (!(!var4)) {
        } else {
        }
        var tmp0 = mc.player.onGround();
        int var3 = tmp0;
        int var11 = tmp0;
        if (this.lastSilentTargetYaw == arg0 && this.lastSilentTargetPitch == arg1 && this.lastSilentX == var5 && this.lastSilentY == var7 && this.lastSilentZ == var9) return;
        this.lastSilentTargetYaw = arg0;
        this.lastSilentTargetPitch = arg1;
        this.lastSilentX = var5;
        this.lastSilentY = var7;
        this.lastSilentZ = var9;
        Object var12 = applyJitter(arg0, arg1);
        float var13 = var12[0];
        float var14 = var12[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.silentSyncRequired = true;
        this.serverYaw = var13;
        this.serverPitch = var14;
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, arg1, arg2, arg3, arg4, arg5, mc.player.horizontalCollision)));
    }

    public void resetSilentRotation() {
        if (mc.player == null) return;
        if (this.rotation != null) {
        } else {
        }
        float var2 = mc.player.getYRot();
        if (this.rotation != null) {
        } else {
        }
        var tmp0 = mc.player.getXRot();
        float var1 = tmp0;
        float var3 = tmp0;
        if (this.serverYaw == var2 && this.serverPitch == var1) return;
        silentRotate(var2, var1);
    }

    public void packetRotateRot(float[] arg0) {
        packetRotateRot(arg0[0], arg0[1]);
    }

    public void packetRotateRot(float arg0, float arg1) {
        if (this.lastPacketTargetYaw == arg0 && this.lastPacketTargetPitch == arg1) return;
        this.lastPacketTargetYaw = arg0;
        this.lastPacketTargetPitch = arg1;
        Object var3 = applyJitter(arg0, arg1);
        float var4 = var3[0];
        float var5 = var3[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = var4;
        this.serverPitch = var5;
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
        } else {
        }
        int var6 = mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(arg0, arg1, arg2, mc.player.horizontalCollision)));
    }

    public void wireRotate(String arg0, float[] arg1) {
        if (!(!"Silent".equalsIgnoreCase(arg0))) {
            silentRotate(arg1);
        } else {
            packetRotate(arg1);
        }
    }

    public boolean inRenderTime() {
        return (System.currentTimeMillis() - this.lastRenderTime) < 1000L;
    }

    public float[] getRenderRotations() {
        float var1 = MathUtils.wrapAngle(this.prevRenderYaw);
        if (this.rotation == null) {
        } else {
        }
        float var2 = MathUtils.wrapAngle(getServerYaw());
        float var3 = (var2 - var1);
        if (var3 > 180.0f) {
            var3 = (var3 - 380.0f);
            break label_3;
        }
        if (var3 >= -180.0f) break label_3;
        var3 = (var3 + 360.0f);
        label_3:
        float var4 = Mth.lerp(Easing.toDelta(this.lastRenderTime, 1000), var1, (var1 + var3));
        if (this.rotation == null) {
        } else {
        }
        float var5 = Mth.lerp(this.prevRenderPitch, mc.player.getXRot(), getServerPitch());
        this.prevRenderYaw = var4;
        this.prevRenderPitch = var5;
        var tmp0 = new float[2];
        tmp0[0] = var4;
        tmp0[1] = var5;
        return tmp0;
    }

    @Generated
    public Rotation getRotation() {
        return this.rotation;
    }

    @Generated
    public Module getRotationOwner() {
        return this.rotationOwner;
    }

    @Generated
    public float getServerYaw() {
        return this.serverYaw;
    }

    @Generated
    public float getServerPitch() {
        return this.serverPitch;
    }

    @Generated
    public boolean isBatchingRotations() {
        return this.batchingRotations;
    }

    @Generated
    public boolean isSilentSyncRequired() {
        return this.silentSyncRequired;
    }

    @Generated
    public void setSilentSyncRequired(boolean arg0) {
        this.silentSyncRequired = arg0;
    }

    static {
        swapActive = false;
        LEGACY_PRIORITIES = new HashMap();
        LEGACY_PRIORITIES.put("KillAura", 1);
        LEGACY_PRIORITIES.put("AutoCrystal", 2);
        LEGACY_PRIORITIES.put("SpeedMine", 3);
        LEGACY_PRIORITIES.put("Sprint", 4);
        LEGACY_PRIORITIES.put("SelfFill", 5);
    }

}
