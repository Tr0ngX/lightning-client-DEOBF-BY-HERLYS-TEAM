package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChangeYawEvent extends Event {
    private final float yaw;

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public ChangeYawEvent(float arg0) {
        this.yaw = arg0;
    }

}
