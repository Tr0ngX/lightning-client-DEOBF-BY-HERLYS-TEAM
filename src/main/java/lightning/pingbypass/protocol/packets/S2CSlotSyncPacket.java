package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CSlotSyncPacket extends PbPacket {
    public static final int ID = 13;
    private final int slot;

    public S2CSlotSyncPacket(int arg0) {
        this.slot = arg0;
    }

    public S2CSlotSyncPacket(FriendlyByteBuf arg0) {
        this.slot = arg0.readVarInt();
    }

    public int getPacketId() {
        return 13;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeVarInt(this.slot);
    }

    public int getSlot() {
        return this.slot;
    }

}
