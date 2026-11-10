package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CErrorPacket extends PbPacket {
    public static final int ID = 7;
    private final String message;

    public S2CErrorPacket(String arg0) {
        this.message = arg0;
    }

    public S2CErrorPacket(FriendlyByteBuf arg0) {
        this.message = arg0.readUtf();
    }

    public int getPacketId() {
        return 7;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.message);
    }

    public String getMessage() {
        return this.message;
    }

}
