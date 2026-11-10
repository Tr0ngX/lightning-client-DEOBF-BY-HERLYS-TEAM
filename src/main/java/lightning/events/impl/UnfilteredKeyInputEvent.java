package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class UnfilteredKeyInputEvent extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getScancode() {
        return this.scancode;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Generated
    public UnfilteredKeyInputEvent(int arg0, int arg1, int arg2, int arg3) {
        this.key = arg0;
        this.scancode = arg1;
        this.action = arg2;
        this.modifiers = arg3;
    }

}
