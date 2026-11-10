package lightning.modules.impl.visuals;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IMotionCameraModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.AnimateUtil;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

@RegisterModule(name = "MotionCamera", description = "Smooths camera movement and adds smooth perspective transitions.", category = Module.Category.VISUALS)
public class MotionCameraModule extends Module implements IMotionCameraModule {
    public BooleanSetting noFirstPerson;
    public BooleanSetting pearlException;
    public BooleanSetting rubberbandException;
    public NumberSetting exceptionDuration;
    public NumberSetting firstPersonSpeed;
    public NumberSetting speed;
    public BooleanSetting smoothPerspective;
    public NumberSetting perspectiveSpeed;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private double distance;
    private double prevDistance;
    private final Timer pearlTimer;
    private final Timer rubberbandTimer;

    public MotionCameraModule() {
        this.noFirstPerson = new BooleanSetting("NoFirstPerson", "Disables motion camera in first person.", true);
        this.pearlException = new BooleanSetting("PearlException", "Still smooths in 1st person for a moment after throwing an ender pearl (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
        this.rubberbandException = new BooleanSetting("RubberbandException", "Still smooths in 1st person for a moment after a rubberband/teleport correction (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
        this.exceptionDuration = new NumberSetting("ExceptionDuration", "How long the pearl/rubberband exception window lasts, in ms.", new BooleanSetting.Visibility(this.noFirstPerson, true), 500, 100, 2000);
        this.firstPersonSpeed = new NumberSetting("FirstPersonSpeed", "Movement smoothness speed in first person.", 0.6, 0.01, 1.0);
        this.speed = new NumberSetting("Speed", "Movement smoothness speed in third person.", 0.3, 0.01, 1.0);
        this.smoothPerspective = new BooleanSetting("SmoothPerspective", "Smoothly transitions distance when switching between 1st and 3rd person.", true);
        this.perspectiveSpeed = new NumberSetting("PerspectiveSpeed", "Speed of perspective switch transition.", 0.5, 0.05, 1.0);
        this.distance = 0.0;
        this.prevDistance = 0.0;
        this.pearlTimer = new Timer();
        this.rubberbandTimer = new Timer();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if ((!(this.pearlException.getValue())) || (mc.player == null)) {
            return;
        }
        Object var4 = arg0.getEntity();
        if (!((var4 instanceof ThrownEnderpearl))) break label_2;
        var tmp0 = ((ThrownEnderpearl) var4);
        Object var3 = tmp0;
        var tmp1 = tmp0.getOwner();
        var4 = tmp1;
        if (!((tmp1 instanceof Player))) break label_2;
        var tmp2 = ((Player) var4);
        Object var2 = tmp2;
        if (tmp2 != mc.player) break label_2;
        this.pearlTimer.reset();
        label_2:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (!(this.rubberbandException.getValue())) return;
        if ((arg0.getPacket() instanceof ClientboundPlayerPositionPacket)) {
            this.rubberbandTimer.reset();
        }
    }

    private boolean inExceptionWindow() {
        long var1 = this.exceptionDuration.getValue().longValue();
        if (this.pearlException.getValue()) {
            if (this.pearlTimer.hasTimeElapsed(var1)) {
            }
            if (this.rubberbandException.getValue() && !(this.rubberbandTimer.hasTimeElapsed(var1))) {
            }
        } else {
        }
        return false;
    }

    private boolean isRidingOrFlying() {
        if (mc.player == null) break label_1;
        if (!(mc.player.isPassenger())) {
            if (!(mc.player.isFallFlying())) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        return false;
    }

    public boolean isSmoothPerspective() {
        return this.smoothPerspective.getValue();
    }

    public boolean on() {
        if (mc.options == null) return false;
        if ((!(isToggled())) || (isRidingOrFlying())) break label_2;
        if (this.noFirstPerson.getValue() && mc.options.getCameraType().isFirstPerson()) {
            if (!(inExceptionWindow())) break label_2;
        }
        break label_3;
        label_2:
        label_3:
        return false;
    }

    public boolean shouldBeDetached() {
        if (mc.options == null) return false;
        if ((!(isToggled())) || (!(this.smoothPerspective.getValue()))) break label_2;
        if (this.distance <= 0.01) {
            if (mc.options.getCameraType().isFirstPerson()) break label_2;
        }
        break label_3;
        label_2:
        label_3:
        return false;
    }

    public void onEnable() {
        if (mc.player != null) {
            this.fakeX = mc.player.getX();
            this.fakeY = (mc.player.getY() + ((double) mc.player.getEyeHeight(mc.player.getPose())));
            this.fakeZ = mc.player.getZ();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            if (mc.options.getCameraType().isFirstPerson()) {
            } else {
            }
            double var1 = getTargetPerspectiveDistance();
            0.0.distance = getTargetPerspectiveDistance();
            this.prevDistance = var1;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.options == null)) {
            return;
        }
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double var2 = mc.player.getX();
        double var4 = (mc.player.getY() + ((double) mc.player.getEyeHeight(mc.player.getPose())));
        double var6 = mc.player.getZ();
        if (isRidingOrFlying()) {
            this.fakeX = var2;
            this.fakeY = var4;
            this.fakeZ = var6;
        } else {
            if (mc.options.getCameraType().isFirstPerson()) {
            } else {
            }
            double var8 = this.speed.getValue().doubleValue();
            this.fakeX = AnimateUtil.animate(this.fakeX, var2, var8);
            this.fakeY = AnimateUtil.animate(this.fakeY, var4, var8);
            this.fakeZ = AnimateUtil.animate(this.fakeZ, var6, var8);
        }
        this.prevDistance = this.distance;
        var8 = mc.options.getCameraType().isFirstPerson();
        if (!(!var8)) {
        } else {
        }
        double var9 = getTargetPerspectiveDistance();
        if (this.smoothPerspective.getValue() && !var8) {
        } else {
        }
        AnimateUtil.animate(this.distance, var9, this.perspectiveSpeed.getValue().doubleValue()).distance = var9;
        if (!(!var8)) {
            this.prevDistance = 0.0;
        }
    }

    private double getTargetPerspectiveDistance() {
        Object var1 = ((ViewClipModule) LightningClient.MODULE_MANAGER.getModule(ViewClipModule.class));
        if (var1 != null && var1.isToggled() && var1.extend.getValue()) return var1.distance.getValue().doubleValue();
        return 4.0;
    }

    public double getFakeX() {
        if (mc.getDeltaTracker() != null) {
        } else {
        }
        float var1 = 1.0f;
        return MathUtils.interpolate(this.prevFakeX, this.fakeX, ((double) var1));
    }

    public double getFakeY() {
        if (mc.getDeltaTracker() != null) {
        } else {
        }
        float var1 = 1.0f;
        return MathUtils.interpolate(this.prevFakeY, this.fakeY, ((double) var1));
    }

    public double getFakeZ() {
        if (mc.getDeltaTracker() != null) {
        } else {
        }
        float var1 = 1.0f;
        return MathUtils.interpolate(this.prevFakeZ, this.fakeZ, ((double) var1));
    }

    public double getDistance() {
        if (mc.getDeltaTracker() != null) {
        } else {
        }
        float var1 = 1.0f;
        return MathUtils.interpolate(this.prevDistance, this.distance, ((double) var1));
    }

}
