package lightning.utils.animations;

import lombok.Generated;
import net.minecraft.util.Mth;

public class Animation {
    private static final int MIN_DURATION_MS = 40;
    private float current;
    private float prev;
    private long startTime;
    private Easing.Method easing;
    private final int duration;
    private int activeDuration;

    public Animation(int arg0, Easing.Method arg1) {
        this(0.0f, 0.0f, arg0, arg1);
    }

    public Animation(float arg0, float arg1, int arg2, Easing.Method arg3) {
        this.prev = arg0;
        this.current = arg1;
        this.duration = arg2;
        this.activeDuration = arg2;
        this.easing = arg3;
        this.startTime = System.currentTimeMillis();
    }

    public float value() {
        if (this.activeDuration <= 0) return this.current;
        return Mth.lerp(Easing.ease(Easing.toDelta(this.startTime, this.activeDuration), this.easing), this.prev, this.current);
    }

    public boolean isFinished() {
        if (this.activeDuration > 0) {
            if (Easing.toDelta(this.startTime, this.activeDuration) >= 1.0f) {
            }
        } else {
        }
        return false;
    }

    public float get() {
        return value();
    }

    public float get(float arg0) {
        float var2 = value();
        if (this.current != arg0) {
            float var3 = Math.abs((arg0 - var2));
            float var4 = Math.abs((this.current - this.prev));
            float var5 = Math.max(var4, var3);
            if (var5 <= 1.0E-4f) {
            } else {
            }
            int var6 = Math.round((((float) this.duration) * Mth.clamp((var3 / var5), 0.0f, 1.0f)));
            this.prev = var2;
            this.current = arg0;
            this.activeDuration = Math.max(40, var6);
            this.startTime = System.currentTimeMillis();
        }
        return var2;
    }

    public void setEasing(Easing.Method arg0) {
        if (this.easing == arg0) return;
        float var2 = value();
        int var3 = ((int) Math.min(Easing.toDelta(this.startTime), 2147483647L));
        int var4 = Math.max(0, (this.activeDuration - var3));
        this.prev = var2;
        this.activeDuration = var4;
        this.startTime = System.currentTimeMillis();
        this.easing = arg0;
    }

    @Generated
    public float getCurrent() {
        return this.current;
    }

    @Generated
    public float getPrev() {
        return this.prev;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }

    @Generated
    public Easing.Method getEasing() {
        return this.easing;
    }

    @Generated
    public int getDuration() {
        return this.duration;
    }

    @Generated
    public int getActiveDuration() {
        return this.activeDuration;
    }

    @Generated
    public void setCurrent(float arg0) {
        this.current = arg0;
    }

    @Generated
    public void setPrev(float arg0) {
        this.prev = arg0;
    }

    @Generated
    public void setStartTime(long arg0) {
        this.startTime = arg0;
    }

    @Generated
    public void setActiveDuration(int arg0) {
        this.activeDuration = arg0;
    }

}
