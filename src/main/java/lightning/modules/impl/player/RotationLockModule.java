package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.util.Mth;

@RegisterModule(name = "RotationLock", description = "Locks your rotation to a certain yaw and pitch.", category = Module.Category.PLAYER)
public class RotationLockModule extends Module {
    public ModeSetting mode;
    public BooleanSetting custom;
    public NumberSetting pitch;
    private boolean lockInitialized;
    private float lockedYaw;
    private float lockedPitch;
    private static final long SHAKE_DURATION_MS = 180L;
    private static final float SHAKE_AMPLITUDE_DEG = 3.0f;
    private long yawSnapAtMs;
    private long pitchSnapAtMs;

    public RotationLockModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Yaw";
        tmp0[1] = "Pitch";
        tmp0[2] = "Both";
        this.mode = new ModeSetting("Mode", "Whether the pitch or yaw will be locked.", "Both", tmp0);
        var tmp1 = new String[3];
        tmp1[0] = "Yaw";
        tmp1[1] = "Pitch";
        tmp1[2] = "Both";
        this.custom = new BooleanSetting("Custom", "Holds your locked angle steady (camera free to turn) and only re-locks, snapping straight to the next 45deg, once you've turned far enough past it -- instead of an always-fixed value.", new ModeSetting.Visibility(this.mode, tmp1), false);
        var tmp2 = new String[2];
        tmp2[0] = "Pitch";
        tmp2[1] = "Both";
        this.pitch = new NumberSetting("Pitch", "Pitch", "The fixed degrees pitch locks to when Custom is off.", new ModeSetting.Visibility(this.mode, tmp2), 0.0f, -90.0f, 90.0f, 1);
        this.lockInitialized = false;
        this.lockedYaw = 0.0f;
        this.lockedPitch = 0.0f;
        this.yawSnapAtMs = -1L;
        this.pitchSnapAtMs = -1L;
    }

    public void onEnable() {
        this.lockInitialized = false;
    }

    private float shakeOffset(long arg0) {
        if (arg0 < 0L) return 0.0f;
        long var3 = (System.currentTimeMillis() - arg0);
        if (var3 >= 180L) return 0.0f;
        float var5 = (((float) var3) / 180.0f);
        return ((float) ((Math.sin(((((double) var5) * 3.141592653589793) * 5.0)) * 3.0) * ((double) (1.0f - var5))));
    }

    public float getShakeYawOffset() {
        if (this.custom.getValue()) {
        } else {
        }
        return 0.0f;
    }

    public float getShakePitchOffset() {
        if (this.custom.getValue()) {
        } else {
        }
        return 0.0f;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (mc.player != null) {
            if (!(this.custom.getValue())) {
            }
            this.lockInitialized = false;
            return;
        }
        float var2 = ((EntityAccessor) mc.player).getRawYRot();
        float var3 = ((EntityAccessor) mc.player).getRawXRot();
        if (!this.lockInitialized) {
            this.lockedYaw = nearest45(var2);
            this.lockedPitch = nearest45(var3);
            this.lockInitialized = true;
            return;
        }
        if (Math.abs(Mth.wrapDegrees((var2 - this.lockedYaw))) >= 45.0f) {
            float var4 = nearest45(var2);
            if (var4 != this.lockedYaw) {
                this.yawSnapAtMs = System.currentTimeMillis();
            }
            this.lockedYaw = var4;
        }
        if (Math.abs(Mth.wrapDegrees((var3 - this.lockedPitch))) >= 45.0f) {
            var4 = nearest45(var3);
            if (var4 != this.lockedPitch) {
                this.pitchSnapAtMs = System.currentTimeMillis();
            }
            this.lockedPitch = var4;
        }
    }

    private static float nearest45(float arg0) {
        return Mth.wrapDegrees((((float) Math.round((arg0 / 45.0f))) * 45.0f));
    }

    public float getYawValue(float arg0) {
        if (this.custom.getValue()) return this.lockedYaw;
        return nearest45(arg0);
    }

    public float getPitchValue(float arg0) {
        if (this.custom.getValue()) return this.lockedPitch;
        return this.pitch.getValue().floatValue();
    }

    public String getMetaData() {
        if (this.custom.getValue()) {
        } else {
        }
        Object var1 = "Auto";
        if (this.custom.getValue()) {
        } else {
        }
        Object var2 = String.valueOf(this.pitch.getValue().floatValue());
        return (var1 + ", " + var2);
    }

}
