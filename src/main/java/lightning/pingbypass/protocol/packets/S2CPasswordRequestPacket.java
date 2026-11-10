package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CPasswordRequestPacket extends PbPacket {
    public static final int ID = 1;

    public S2CPasswordRequestPacket(FriendlyByteBuf arg0) {
    }

    public int getPacketId() {
        return 1;
    }

    public void write(FriendlyByteBuf arg0) {
    }

}
