package lightning.utils.rotations;

import lightning.modules.Module;
import lombok.Generated;

public class LegacyRotation {
    private float yaw;
    private float pitch;
    private final Module module;
    private final int priority;
    private long time;

    public LegacyRotation(float arg0, float arg1, int arg2) {
        this.yaw = arg0;
        this.pitch = arg1;
        this.module = null;
        this.priority = arg2;
        this.time = System.currentTimeMillis();
    }

    public LegacyRotation(float arg0, float arg1, Module arg2, int arg3) {
        this.yaw = arg0;
        this.pitch = arg1;
        this.module = arg2;
        this.priority = arg3;
        this.time = System.currentTimeMillis();
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    @Generated
    public long getTime() {
        return this.time;
    }

    @Generated
    public void setYaw(float arg0) {
        this.yaw = arg0;
    }

    @Generated
    public void setPitch(float arg0) {
        this.pitch = arg0;
    }

    @Generated
    public void setTime(long arg0) {
        this.time = arg0;
    }

}
