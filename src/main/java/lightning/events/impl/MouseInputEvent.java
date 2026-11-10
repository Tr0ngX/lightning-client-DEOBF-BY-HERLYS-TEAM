package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class MouseInputEvent extends Event {
    private final int button;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public MouseInputEvent(int arg0) {
        this.button = arg0;
    }

}
