package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class CommandInputEvent extends Event {
    private final String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public CommandInputEvent(String arg0) {
        this.message = arg0;
    }

}
