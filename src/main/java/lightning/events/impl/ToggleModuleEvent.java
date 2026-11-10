package lightning.events.impl;

import lightning.events.Event;
import lightning.modules.Module;
import lombok.Generated;

public class ToggleModuleEvent extends Event {
    private final Module module;
    private final boolean state;

    @Generated
    public ToggleModuleEvent(Module arg0, boolean arg1) {
        this.module = arg0;
        this.state = arg1;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public boolean isState() {
        return this.state;
    }

}
