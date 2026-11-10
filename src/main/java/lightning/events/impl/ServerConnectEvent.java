package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class ServerConnectEvent extends Event {
    private final ServerAddress address;
    private final ServerData info;

    @Generated
    public ServerConnectEvent(ServerAddress arg0, ServerData arg1) {
        this.address = arg0;
        this.info = arg1;
    }

    @Generated
    public ServerAddress getAddress() {
        return this.address;
    }

    @Generated
    public ServerData getInfo() {
        return this.info;
    }

}
