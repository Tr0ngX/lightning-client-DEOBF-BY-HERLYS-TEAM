package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public class PacketReceiveEvent extends Event {
    private final Packet packet;
    private final Connection connection;

    public PacketReceiveEvent(Packet arg0) {
        this(arg0, null);
    }

    @Generated
    public Packet getPacket() {
        return this.packet;
    }

    @Generated
    public Connection getConnection() {
        return this.connection;
    }

    @Generated
    public PacketReceiveEvent(Packet arg0, Connection arg1) {
        this.packet = arg0;
        this.connection = arg1;
    }

}
