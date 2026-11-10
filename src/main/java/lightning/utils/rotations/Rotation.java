package lightning.utils.rotations;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class Rotation {
    private float yaw;
    private float pitch;

    public Rotation(float arg0, float arg1) {
        this.yaw = arg0;
        this.pitch = arg1;
    }

    public Rotation(Entity arg0) {
        this(arg0.getYRot(), arg0.getXRot());
    }

    public void apply(Entity arg0) {
        arg0.setYRot(this.yaw);
        arg0.setXRot(this.pitch);
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
    public void setYaw(float arg0) {
        this.yaw = arg0;
    }

    @Generated
    public void setPitch(float arg0) {
        this.pitch = arg0;
    }

}
