package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class PlayerMoveEvent extends Event {
    private final MoverType movementType;
    private Vec3 movement;

    public double getX() {
        return this.movement.x();
    }

    public void setX(double arg0) {
        this.movement = new Vec3(arg0, this.movement.y(), this.movement.z());
    }

    public double getY() {
        return this.movement.y();
    }

    public void setY(double arg0) {
        this.movement = new Vec3(this.movement.x(), arg0, this.movement.z());
    }

    public double getZ() {
        return this.movement.z();
    }

    public void setZ(double arg0) {
        this.movement = new Vec3(this.movement.x(), this.movement.y(), arg0);
    }

    @Generated
    public PlayerMoveEvent(MoverType arg0, Vec3 arg1) {
        this.movementType = arg0;
        this.movement = arg1;
    }

    @Generated
    public MoverType getMovementType() {
        return this.movementType;
    }

    @Generated
    public Vec3 getMovement() {
        return this.movement;
    }

    @Generated
    public void setMovement(Vec3 arg0) {
        this.movement = arg0;
    }

}
