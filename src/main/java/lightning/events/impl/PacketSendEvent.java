package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;

public class PacketSendEvent extends Event {
    private final Packet packet;

    @Generated
    public Packet getPacket() {
        return this.packet;
    }

    @Generated
    public PacketSendEvent(Packet arg0) {
        this.packet = arg0;
    }


    public class Post extends Event {
        private final Packet packet;

        @Generated
        public Packet getPacket() {
            return this.packet;
        }

        @Generated
        public Post(Packet arg0) {
            this.packet = arg0;
        }

    }
}
