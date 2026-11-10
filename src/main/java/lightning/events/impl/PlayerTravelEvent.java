package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class PlayerTravelEvent extends Event {
    private final Vec3 movementInput;

    @Generated
    public Vec3 getMovementInput() {
        return this.movementInput;
    }

    @Generated
    public PlayerTravelEvent(Vec3 arg0) {
        this.movementInput = arg0;
    }

}
