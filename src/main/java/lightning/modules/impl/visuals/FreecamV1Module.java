package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.system.MathUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;

@RegisterModule(name = "Freeze", description = "ThunderHack-style freecam: fly the camera around, optionally freeze your player or track an entity.", category = Module.Category.VISUALS)
public class FreecamV1Module extends Module {
    public NumberSetting hSpeed;
    public NumberSetting vSpeed;
    public BooleanSetting freeze;
    public BooleanSetting track;
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private LivingEntity trackEntity;

    public FreecamV1Module() {
        this.hSpeed = new NumberSetting("HSpeed", "Horizontal speed of the camera (scroll wheel changes it).", 1.0f, 0.1f, 3.0f);
        this.vSpeed = new NumberSetting("VSpeed", "Vertical speed of the camera (jump / sneak).", 0.42f, 0.1f, 3.0f);
        this.freeze = new BooleanSetting("Freeze", "Freezes your real player: cancels movement and movement packets.", false);
        this.track = new BooleanSetting("Track", "Attacking an entity makes the camera follow it. Press Shift or Escape to stop.", false);
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
            return;
        }
        mc.smartCull = false;
        this.trackEntity = null;
        this.prevFakePitch = mc.player.getXRot();
        this.fakePitch = mc.player.getXRot();
        this.prevFakeYaw = mc.player.getYRot();
        this.fakeYaw = mc.player.getYRot();
        this.prevFakeX = mc.player.getX();
        null.fakeX = this;
        this.fakeY = (mc.player.getY() + ((double) mc.player.getEyeHeight(mc.player.getPose())));
        this.prevFakeY = mc.player.getY();
        this.prevFakeZ = mc.player.getZ();
        null.fakeZ = this;
    }

    public void onDisable() {
        this.trackEntity = null;
        mc.smartCull = true;
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent arg0) {
        if (!(this.track.getValue())) break label_0;
        Object var3 = arg0.getTarget();
        if (!((var3 instanceof LivingEntity))) break label_0;
        Object var2 = ((LivingEntity) var3);
        this.trackEntity = var2;
        label_0:
    }

    @SubscribeEvent
    public void onKey(KeyInputEvent arg0) {
        int var2 = arg0.getKey();
        if (var2 != 256 && var2 != 340) {
            if (var2 == 344) {
            }
            this.trackEntity = null;
        }
    }

    @SubscribeEvent(priority = 100)
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
        if (this.trackEntity != null && this.trackEntity.isRemoved()) {
            this.trackEntity = null;
        }
        if (this.trackEntity != null) {
            this.fakeYaw = this.trackEntity.getYRot();
            this.fakePitch = this.trackEntity.getXRot();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            this.fakeX = this.trackEntity.getX();
            this.fakeY = (this.trackEntity.getY() + ((double) this.trackEntity.getEyeHeight(this.trackEntity.getPose())));
            this.fakeZ = this.trackEntity.getZ();
        } else {
            this.fakeYaw = mc.player.getYRot();
            this.fakePitch = mc.player.getXRot();
        }
    }

    public void onInput(float arg0, float arg1) {
        if (mc.player == null) return;
        if (this.trackEntity != null) return;
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double var3 = this.hSpeed.getValue().doubleValue();
        if (arg0 == 0.0f) {
            if (arg1 != 0.0f) {
            }
            double var5 = Math.sqrt(((double) ((arg0 * arg0) + (arg1 * arg1))));
            double var7 = (((double) arg0) / var5);
            double var9 = (((double) arg1) / var5);
            double var11 = Math.toRadians(((double) mc.player.getYRot()));
            double var13 = Math.sin(var11);
            double var15 = Math.cos(var11);
            this.fakeX = (this.fakeX + (((-(var13) * var7) + (var15 * var9)) * var3));
            this.fakeZ = (this.fakeZ + (((var15 * var7) + (var13 * var9)) * var3));
        }
        if (mc.options.keyJump.isDown()) {
            this.fakeY = (this.fakeY + this.vSpeed.getValue().doubleValue());
        }
        if (mc.options.keyShift.isDown()) {
            this.fakeY = (this.fakeY - this.vSpeed.getValue().doubleValue());
        }
    }

    @SubscribeEvent(priority = -100)
    public void onMove(PlayerMoveEvent arg0) {
        if (this.freeze.getValue()) {
            arg0.setX(0.0);
            arg0.setY(0.0);
            arg0.setZ(0.0);
            arg0.cancel();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (this.freeze.getValue() && (arg0.getPacket() instanceof ServerboundMovePlayerPacket)) {
            arg0.cancel();
        }
    }

    public void onScroll(double arg0) {
        float var3 = (arg0 > 0.0 ? 0.05f : -0.05f);
        this.hSpeed.setValue((this.hSpeed.getValue().floatValue() + var3));
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if ((arg0.getPlayer() == null) || (mc.player == null)) {
            return;
        }
        setToggled(false);
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent arg0) {
        setToggled(false);
    }

    public boolean isTracking() {
        return this.trackEntity != null;
    }

    public float getFakeYaw() {
        return ((float) MathUtils.interpolate(((double) this.prevFakeYaw), ((double) this.fakeYaw), ((double) partial())));
    }

    public float getFakePitch() {
        return ((float) MathUtils.interpolate(((double) this.prevFakePitch), ((double) this.fakePitch), ((double) partial())));
    }

    public double getFakeX() {
        return MathUtils.interpolate(this.prevFakeX, this.fakeX, ((double) partial()));
    }

    public double getFakeY() {
        return MathUtils.interpolate(this.prevFakeY, this.fakeY, ((double) partial()));
    }

    public double getFakeZ() {
        return MathUtils.interpolate(this.prevFakeZ, this.fakeZ, ((double) partial()));
    }

    private static float partial() {
        return mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

}
