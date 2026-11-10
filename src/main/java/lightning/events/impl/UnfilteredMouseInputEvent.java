package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class UnfilteredMouseInputEvent extends Event {
    private final int button;
    private final int action;
    private final int mods;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getMods() {
        return this.mods;
    }

    @Generated
    public UnfilteredMouseInputEvent(int arg0, int arg1, int arg2) {
        this.button = arg0;
        this.action = arg1;
        this.mods = arg2;
    }

}
