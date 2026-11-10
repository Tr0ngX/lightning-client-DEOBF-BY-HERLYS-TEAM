package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChatInputEvent extends Event {
    private String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public void setMessage(String arg0) {
        this.message = arg0;
    }

    @Generated
    public ChatInputEvent(String arg0) {
        this.message = arg0;
    }

}
