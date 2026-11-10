package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class UpdateVelocityEvent extends Event {
    private final Vec3 movementInput;
    private final float speed;
    private Vec3 velocity;

    @Generated
    public Vec3 getMovementInput() {
        return this.movementInput;
    }

    @Generated
    public float getSpeed() {
        return this.speed;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public void setVelocity(Vec3 arg0) {
        this.velocity = arg0;
    }

    @Generated
    public UpdateVelocityEvent(Vec3 arg0, float arg1) {
        this.movementInput = arg0;
        this.speed = arg1;
    }

}
