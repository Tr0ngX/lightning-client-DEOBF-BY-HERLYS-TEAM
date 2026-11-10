package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChangePitchEvent extends Event {
    private final float pitch;

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public ChangePitchEvent(float arg0) {
        this.pitch = arg0;
    }

}
