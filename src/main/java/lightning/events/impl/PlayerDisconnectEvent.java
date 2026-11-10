package lightning.events.impl;

import java.util.UUID;
import lightning.events.Event;
import lombok.Generated;

public class PlayerDisconnectEvent extends Event {
    private final UUID id;

    @Generated
    public PlayerDisconnectEvent(UUID arg0) {
        this.id = arg0;
    }

    @Generated
    public UUID getId() {
        return this.id;
    }

}
