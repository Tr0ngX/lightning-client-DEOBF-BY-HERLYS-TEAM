package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class KeyboardTickEvent extends Event {
    private float movementForward;
    private float movementSideways;

    @Generated
    public KeyboardTickEvent(float arg0, float arg1) {
        this.movementForward = arg0;
        this.movementSideways = arg1;
    }

    @Generated
    public float getMovementForward() {
        return this.movementForward;
    }

    @Generated
    public float getMovementSideways() {
        return this.movementSideways;
    }

    @Generated
    public void setMovementForward(float arg0) {
        this.movementForward = arg0;
    }

    @Generated
    public void setMovementSideways(float arg0) {
        this.movementSideways = arg0;
    }

}
